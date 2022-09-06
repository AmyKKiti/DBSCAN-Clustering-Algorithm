// Project CSI2120/CSI2520
// Winter 2022
// Robert Laganiere, uottawa.ca
// version 1.2

package main

import (
	"encoding/csv"
	"fmt"
	"io"
	"math"
	"os"
	"runtime"
	"strconv"
	"sync"
	"time"
)

type GPScoord struct {
	lat  float64
	long float64
}

//For Label :
// 0 represents undefined
//-1 represents noise
//initially all coords are undefined = 0
type LabelledGPScoord struct {
	GPScoord
	ID    int // point ID (number of points)
	Label int // cluster ID
}

//type struc JobInstance : contient l'instance de Job à envoyer au channel
// à consommer par dbscan
type JobInstance struct {
	coord  []LabelledGPScoord //slice de gps coords
	offset int                //offset (ajouter a cluster id)
}

const N int = 4
const MinPts int = 5
const eps float64 = 0.0003
const filename string = "yellow_tripdata_2009-01-15_9h_21h_clean.csv"

func main() {

	start := time.Now()

	gps, minPt, maxPt := readCSVFile(filename)
	fmt.Printf("Number of points: %d\n", len(gps))

	minPt = GPScoord{40.7, -74.}
	maxPt = GPScoord{40.8, -73.93}

	// geographical limits
	fmt.Printf("SW:(%f , %f)\n", minPt.lat, minPt.long)
	fmt.Printf("NE:(%f , %f) \n\n", maxPt.lat, maxPt.long)

	// Parallel DBSCAN STEP 1.
	incx := (maxPt.long - minPt.long) / float64(N)
	incy := (maxPt.lat - minPt.lat) / float64(N)

	var grid [N][N][]LabelledGPScoord // a grid of GPScoord slices

	// Create the partition
	// triple loop! not very efficient, but easier to understand

	partitionSize := 0
	for j := 0; j < N; j++ {
		for i := 0; i < N; i++ {

			for _, pt := range gps {

				// is it inside the expanded grid cell
				if (pt.long >= minPt.long+float64(i)*incx-eps) && (pt.long < minPt.long+float64(i+1)*incx+eps) && (pt.lat >= minPt.lat+float64(j)*incy-eps) && (pt.lat < minPt.lat+float64(j+1)*incy+eps) {

					grid[i][j] = append(grid[i][j], pt) // add the point to this slide
					partitionSize++
				}
			}
		}
	}

	// ***

	// A producer thread that produces jobs (partition to be clustered)
	// And by consumer threads that clusters partitions

	//channel for les instances de jobs a consommer
	jobs := make(chan JobInstance, N*N)

	//synchronicsation
	var mutex sync.WaitGroup

	//nombre de threads
	threadnum := 16
	for z := 0; z < threadnum; z++ {

		mutex.Add(1) //ajouter la synchronisation
		//appeler go consomme, ceci appelle un thread concurrent
		go consomme(jobs, &mutex)

	}

	//produit N*N jobs ici
	for j := 0; j < N; j++ {
		for i := 0; i < N; i++ {

			//créer une instance de job
			oneJob := JobInstance{grid[i][j], i*10000000 + j*1000000}

			//envoyer dans le channel jobs
			jobs <- oneJob

		}
	}

	close(jobs)  //fermer le channel une fois tous les jobs envoyé, il n'y a plus de jobs a consommer
	mutex.Wait() //synchronisation, attendre que tous les consommateurs terminent leur travail

	// Parallel DBSCAN STEP 2.
	// Apply DBSCAN on each partition
	// ...

	// Parallel DBSCAN step 3.
	// merge clusters
	// *DO NOT PROGRAM THIS STEP

	end := time.Now()
	fmt.Printf("\nExecution time: %s of %d points\n", end.Sub(start), partitionSize)
	fmt.Printf("Number of CPUs: %d", runtime.NumCPU())
}

// Applique l'algorithme DBSCAN sur les points LabelledGPScoord
// coords : slice de LabelledGPScoord points sur laquelle on applique le clustering
// MinPts, eps: paramètres de l'algorithme DBSCAN
// offset: label de la premiere partition (également utilisé pour identifier le cluster, nclusters = nclusters + offset)
// La fonction renvoie le nombre de clusters trouvés
// La fonction imprime le nombre de clusters dans chaque partition et leur nombre de points.
func DBscan(coords []LabelledGPScoord, MinPts int, eps float64, offset int) (nclusters int) {

	nclusters = 0 //initialization

	for i := 0; i < len(coords); i++ { //pour chaque point P dans la base de données coords

		P := &coords[i] //P est un pointeur a un point dans coords

		if P.Label != 0 { // Précédemment traité dans la boucle interne
			continue
		}

		N := rangeQuery(coords, P) //Trouver des voisins

		if len(N) < MinPts { //Verifier la densité
			P.Label = -1 //Label comme Noise
			continue
		}

		nclusters++                  //nombre du cluster suivant
		P.Label = nclusters + offset //Label du point

		seedSet := make([]*LabelledGPScoord, 0, 5000) //slice de pointeurs de type LabelledGPScoords

		//Ajouter des voisins au seedset
		for n := 0; n < len(N); n++ {
			seedSet = append(seedSet, N[n])
		}

		for k := 0; k < len(seedSet); k++ { //Traiter chaque seed point Q
			Q := seedSet[k] //Q est un pointeur au points dans le seedset

			if Q.Label == -1 {
				Q.Label = nclusters + offset //Changer le bruit en point border
			}

			if Q.Label != 0 { //Deja traité
				continue
			}

			Q.Label = nclusters + offset //Étiquette du voisin

			newN := rangeQuery(coords, Q) //trouver des voisins

			if len(newN) >= MinPts { //Contrôle de densité (si Q est un point central)
				// Ajouter de nouveaux voisins à seedset

				for index := 0; index < len(newN); index++ {

					d := newN[index]
					seedSet = append(seedSet, d)
				}
			}

		}

	}

	// End of DBscan function
	// Printing the result (do not remove)
	fmt.Printf("Partition %10d : [%4d,%6d]\n", offset, nclusters, len(coords))

	return nclusters
}

//calcule les points voisins de Q utilisant la distance eps
//coords : slice de tous les points dans la base de donnée
//Q : pointeur a un LabelledGPScoord, on veut calculer la distance entre Q et chaque point dans coords
//retourne un slice de pointeurs de type LabelledGPScoord
func rangeQuery(coords []LabelledGPScoord, Q *LabelledGPScoord) []*LabelledGPScoord {

	neighbours := make([]*LabelledGPScoord, 0, 5000)

	//pour chaque point p dans la base de données des coordonnées
	for i := 0; i < len(coords); i++ {
		P := &coords[i]
		//calculer la distance
		if (P.ID != Q.ID) && (Distance(Q.GPScoord, P.GPScoord) <= eps) {
			//si distance est moins que eps, ajouter à la slice de voisins (N)
			neighbours = append(neighbours, P) // N := N ∪ {P}
		}

	}
	return neighbours
}

//Function qui sert a calculer la distance entre 2 points GPScoord p1 et p2
//retourne la valeur de la distance type float64
func Distance(p1 GPScoord, p2 GPScoord) (distance float64) {

	distance = math.Sqrt((p2.lat-p1.lat)*(p2.lat-p1.lat) +
		(p2.long-p1.long)*(p2.long-p1.long))

	return
}

//fonction consomme
//appelle la fonction DBscan et consomme les travaux envoyé au channel jobs
func consomme(jobs chan JobInstance, done *sync.WaitGroup) {

	for {
		j, more := <-jobs

		if more {
			// appeler la fonction dbscan
			DBscan(j.coord, MinPts, eps, j.offset)

		} else { //fini de consommer les jobs, le channel a été fermé (more = false)
			done.Done() //synchronisation
			return
		}
	}
}

// reads a csv file of trip records and returns a slice of the LabelledGPScoord of the pickup locations
// and the minimum and maximum GPS coordinates
func readCSVFile(filename string) (coords []LabelledGPScoord, minPt GPScoord, maxPt GPScoord) {

	coords = make([]LabelledGPScoord, 0, 5000)

	// open csv file
	src, err := os.Open(filename)
	defer src.Close()
	if err != nil {
		panic("File not found...")
	}

	// read and skip first line
	r := csv.NewReader(src)
	record, err := r.Read()
	if err != nil {
		panic("Empty file...")
	}

	minPt.long = 1000000.
	minPt.lat = 1000000.
	maxPt.long = -1000000.
	maxPt.lat = -1000000.

	var n int = 0

	for {
		// read line
		record, err = r.Read()

		// end of file?
		if err == io.EOF {
			break
		}

		if err != nil {
			panic("Invalid file format...")
		}

		// get lattitude
		lat, err := strconv.ParseFloat(record[9], 64)
		if err != nil {
			panic("Data format error (lat)...")
		}

		// is corner point?
		if lat > maxPt.lat {
			maxPt.lat = lat
		}
		if lat < minPt.lat {
			minPt.lat = lat
		}

		// get longitude
		long, err := strconv.ParseFloat(record[8], 64)
		if err != nil {
			panic("Data format error (long)...")
		}

		// is corner point?
		if long > maxPt.long {
			maxPt.long = long
		}

		if long < minPt.long {
			minPt.long = long
		}

		// add point to the slice
		n++
		pt := GPScoord{lat, long}
		coords = append(coords, LabelledGPScoord{pt, n, 0})
	}

	return coords, minPt, maxPt
}

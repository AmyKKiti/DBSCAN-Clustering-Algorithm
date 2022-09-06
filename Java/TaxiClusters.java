//Nom : Kanjanokphat Kitisuwanakul
//Numero d'etudiant : 300170040
//Cours : CSI 2520 

import java.util.LinkedList;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Collections;

/**
 * Cette classe contient le principal algorithme de lecture CSV et le DBSCAN.
 */
public class TaxiClusters {

    private List<TripRecord> tripList; //contient la liste des trips de ce dossier
    private Double eps;
    private int minPts;

    //constructeur
    public TaxiClusters(Double eps, int minPts){
        tripList = new LinkedList<TripRecord>();
        this.eps = eps;
        this.minPts = minPts;
    }

    //setters
    public void setTripList(List<TripRecord> tripList){
        this.tripList = tripList;
    }

    public void setEps(Double eps){
        this.eps = eps;
    }

    public void setminPts(int minPts){
        this.minPts = minPts;
    }

    //getters
    public List<TripRecord> getTripList(){
        return this.tripList;
    }

    public Double getEps(){
        return this.eps;
    }

    public int getminPts(){
        return this.minPts;
    }


    //méthode pour ajouter un voyage à tripList
    public void addTrip(TripRecord atrip){
        this.tripList.add(atrip);
    }

     //main method
     public static void main(String[] args) throws Exception{
        
        //variables + declarations
        String infileName = "";
        String outfileName = "";
        Double e = 0.0;
        int min = 0;
        Scanner scanner = new Scanner(System.in);

        //obtenir l'entrée de l'utilisateur
        System.out.println("Please enter file name to read from:");
        infileName = scanner.nextLine();
        System.out.println("Please enter file name to write to:");
        outfileName = scanner.nextLine();
        System.out.println("Please enter value of epsilon :");
        e = scanner.nextDouble();
        System.out.println("Please enter value of minimum points :");
        min = scanner.nextInt();
     
    
        //lire CSV file
        TaxiClusters myCluster = new TaxiClusters(e,min);
        List<String> listofLines = myCluster.readCSVFile(infileName); 
        
        //creer pts list and trip records
        List<GPScoord> listOfPts= new ArrayList<GPScoord>();
        listOfPts = myCluster.makePtsList(listofLines);

        //creer cluster list
        List<Cluster> clusterList = new LinkedList<Cluster>();
        clusterList= myCluster.DBSCAN(listOfPts);

        //sortie des données dans un fichier CSV
        myCluster.writeCSVFile("outputdata.csv", clusterList);
        Collections.sort(clusterList);
        myCluster.writeCSVFile(outfileName, clusterList);
        
       
    }

    /**
     * Cette méthode écrit le résultat de DBSCAN dans un fichier CSV.
     * @param fileName,clusterList
    */
    public void writeCSVFile(String fileName, List<Cluster> clusterList) {
        String header;

        header  = "Cluster ID,Longitude,Latitude,Number of points\n";

        try {
            File f = new File(fileName);
            FileWriter writer = new FileWriter(fileName);

            writer.write(header);
            
            for (Cluster c : clusterList){
                writer.write(c.display() + "\n");
            }

            writer.close();

          } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Cette méthode lit chaque ligne du fichier CSV dans une liste de chaînes de caractères.
     * @param fileName 
     * @return listOfLines, une liste de toutes les lignes lues à partir du fichier CSV
    */
    public List<String> readCSVFile(String fileName) throws Exception{

        List<String> listOfLines;
        listOfLines = new LinkedList<String>();

        Scanner scanner = new Scanner(new File(fileName));

		while (scanner.hasNext()) {
			String str = scanner.nextLine();
            listOfLines.add(str);	
		}

		scanner.close();
        return listOfLines;
    }

    public List<GPScoord> makePtsList(List<String> listOfLines){

        //stocker trip list
        List<GPScoord> ptsList;
        ptsList = new ArrayList<GPScoord>();

        //loop à travers toutes les lignes
        for (int i = 1; i < listOfLines.size(); i++){
            String str = listOfLines.get(i);
            List<String> data = Arrays.asList(str.split(","));

            //GPScoord data
            GPScoord pickupLoc = new GPScoord(0,0);
            pickupLoc.setxCoord(Double.valueOf(data.get(9)));
            pickupLoc.setyCoord(Double.valueOf(data.get(8)));
            ptsList.add(pickupLoc);

            ///tripdata
            TripRecord oneTrip;
            String pickupDate; //4
            GPScoord droppLoc = new GPScoord(0,0); //12,13 y,x
            Double tripdist; //7

            pickupDate = data.get(4);
            droppLoc.setxCoord(Double.valueOf(data.get(13)));
            droppLoc.setyCoord(Double.valueOf(data.get(12)));
            tripdist = Double.valueOf(data.get(7));

            //creer nouveau trip
            oneTrip = new TripRecord(pickupDate, pickupLoc, droppLoc, tripdist);
            //ajouter a tripList
            addTrip(oneTrip);

        }

        return ptsList;
    }


    /**
     * Cette méthode implemente l'algorithme DBSCAN pour le clustering.
     * @param  DB,eps,minPts 
     * @return list of clusters
    */
    public List<Cluster> DBSCAN(List<GPScoord> DB){
        
        List<Cluster> clusterList = new LinkedList<Cluster>();
        int clusterCounter = 0;

        for (int i = 0; i < DB.size() ; i++){

            GPScoord thisPoint = DB.get(i);
            if (!thisPoint.getLabel().equals("undefined")){
                continue; //Previously processed in inner loop */
            }
        
            List<GPScoord> neighbours = rangeQuery(DB, thisPoint); // Find neighbors

            if (neighbours.size() < minPts){ //density check
                thisPoint.setLabel("noise"); //label as noise
                continue;
            }

            clusterCounter++; //next cluster label
            thisPoint.setLabel(String.valueOf(clusterCounter)); //label(P) := C

            Cluster thisCluster = new Cluster(clusterCounter); //make new  cluster
            thisCluster.setPtsList(neighbours); //set point lists of Cluster


            List<GPScoord> seedSet = thisCluster.getPtsList(); 
            
            for (int j = 0; j < seedSet.size(); j++){  //for each point Q in S {
                GPScoord Q = seedSet.get(j);

                if (Q.getLabel().equals("noise")){
                    Q.setLabel(String.valueOf(clusterCounter));
                }
                if (!Q.getLabel().equals("undefined")){
                    continue;
                }
                Q.setLabel(String.valueOf(clusterCounter));
                neighbours = rangeQuery(DB, Q); // Find neighbors

                if (neighbours.size() >= minPts){

                    // do not add duplicates
                    for (GPScoord n : neighbours){
                        if (!seedSet.contains(n)){
                            seedSet.add(n);
                        }
                    }
                }
                

            }
           //System.out.println(thisCluster.display());
            clusterList.add(thisCluster);

        }

        return clusterList;
    }
/**
     * Cette méthode parcourt tous les points de la base de données et vérifie 
     * la distance epsilon pour obtenir les voisins d'un point donné Q
     * @param  DB,Q Liste des coordonnées GPS, le point Q 
     * @return liste de coordonnées voisins
    */
    public List<GPScoord> rangeQuery(List<GPScoord> DB, GPScoord Q){

        List<GPScoord> neighbours = new LinkedList<GPScoord>(); //Neighbors N := empty list

        for (int i = 0; i < DB.size(); i++){ //for each point P in database DB
            GPScoord thisRec = DB.get(i);
            double dist = distanceFunction(Q, thisRec); 
       
            if (dist <= eps){ //if distFunc(Q, P) ≤ eps then {
                neighbours.add(thisRec); // N := N ∪ {P}
            }
            
        }

        return neighbours;
    }

     /**
     * Cette méthode calcule la distance euclidienne entre deux coordonnées GPS.
     * @param  point1,point2 GPSCoords pour calculer la distance
     * @return distance entre 2 points
    */
    public double distanceFunction(GPScoord point1, GPScoord point2){
        
        double x;
        double y;

        x = point2.getxCoord() - point1.getxCoord();
        y = point2.getyCoord() - point1.getyCoord();

        double distance;

        distance = Math.sqrt((x*x) + (y*y));
     
        return distance;
    }


}
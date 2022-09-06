//Nom : Kanjanokphat Kitisuwanakul
//Numero d'etudiant : 300170040
//Cours : CSI 2520 

import java.util.LinkedList;
import java.util.List;

/**
 * Cette classe contient des instances de clusters calculés avec l'algorithme DBSCAN.
 */
public class Cluster implements Comparable<Cluster> {

    private List<GPScoord> ptsList; //liste des coordonnées gps qui composent ce cluster
    private int clusterID; //ID de ce cluster


    //constructor
    public Cluster(int id){
        this.clusterID = id;
        ptsList = new LinkedList<GPScoord>();
    }

    //getter
    public int getClusterID() {
        return clusterID;
    }

    public List<GPScoord> getPtsList() {
        return ptsList;
    }

    //setter
    public void setClusterID(int clusterID) {
        this.clusterID = clusterID;
    }


    public void setPtsList(List<GPScoord> ptsList) {
        this.ptsList = ptsList;
    }


   
    //méthode pour ajouter un point à ce cluster
    public void addPointToCluster(GPScoord thatPt){
        ptsList.add(thatPt);
    }


     //méthode pour afficher les données specifiques d'un cluster
    public String display(){

       //calculer moyenne de x
       double sumx = 0.0;
       double sumy = 0.0;
       for (int i = 0; i < ptsList.size(); i++){
            sumx = sumx + ptsList.get(i).getxCoord();
            sumy = sumy + ptsList.get(i).getyCoord();
       }

       sumx = sumx / ptsList.size();
       sumy = sumy / ptsList.size();

       String display;
       display = getClusterID() + "," + sumy + "," + sumx + "," + ptsList.size();

       return display;

    }

    @Override
    public int compareTo(Cluster o) {
        if (this.getPtsList().size() < o.getPtsList().size()){
            return 1;
        }
        else if (this.getPtsList().size() > o.getPtsList().size() ){
            return -1;
        }
        return 0;
    }


}

//Nom : Kanjanokphat Kitisuwanakul
//Numero d'etudiant : 300170040
//Cours : CSI 2520 

/**
 * Cette classe contient des instances de coordonnées au format cartésien
 * la coordonnée x correspond à la latitude et
 * la coordonnée y correspond à la longitude
 *  label indique si une coordonnée est visited, noise ou undefined
 * clusterNum est le cluster auquel la coordonnée a été assignée.
 */
public class GPScoord {
    
    private double xCoord;
    private double yCoord;
    private String label;
    private int clusterNum;

    public GPScoord(double x, double y){
        xCoord = x;
        yCoord = y;
        label = "undefined";
        clusterNum = -1;
    }

    public double getxCoord(){
        return xCoord;
    }

    public double getyCoord(){
        return yCoord;
    }

    public String getLabel() {
        return label;
    }

    public void setxCoord(double x){
        xCoord = x;
    }

    public void setyCoord(double y){
        yCoord = y;
    }


    public void setLabel(String label) {
        this.label = label;
    }

    public String toString(){
        return "ID : " + clusterNum + ", x : " + xCoord + ", y : " + yCoord + ", Label : " + label;
    }



}

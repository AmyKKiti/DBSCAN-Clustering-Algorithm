//Nom : Kanjanokphat Kitisuwanakul
//Numero d'etudiant : 300170040
//Cours : CSI 2520 

/**
 * Cette classe contient des instances d'enregistrements TripRecord extraits du fichier CSV.
 * Les attributs sont la date et l'heure de depart, le lieu de depart,
 * le lieu de d'arrivée et la distance du trajet.
 */
public class TripRecord {

    private String pickup_DateTime;
    private GPScoord pickup_Location;
    private GPScoord dropoff_Location;
    private Double trip_Distance;

    public TripRecord(String pd, GPScoord pl, GPScoord dl, Double td){
        pickup_DateTime = pd;
        pickup_Location = pl;
        dropoff_Location = dl;
        trip_Distance = td;
    }

    public void setDropoff_Location(GPScoord dropoff_Location) {
        this.dropoff_Location = dropoff_Location;
    }

    public void setPickup_DateTime(String pickup_DateTime) {
        this.pickup_DateTime = pickup_DateTime;
    }

    public void setPickup_Location(GPScoord pickup_Location) {
        this.pickup_Location = pickup_Location;
    }
    public void setTrip_Distance(Double trip_Distance) {
        this.trip_Distance = trip_Distance;
    }


    public GPScoord getDropoff_Location() {
        return dropoff_Location;
    }

    public String getPickup_DateTime() {
        return pickup_DateTime;
    }

    public GPScoord getPickup_Location() {
        return pickup_Location;
    }

    public Double getTrip_Distance() {
        return trip_Distance;
    }


    public String toString() {
        String str = "Pickup date and time : " + pickup_DateTime + "\nPickup location : " + pickup_Location.getxCoord() + 
        ", " + pickup_Location.getyCoord() + "\nDropoff location : " + dropoff_Location.getxCoord() + ", " +
        dropoff_Location.getyCoord() + "\nTrip distance : " + trip_Distance;

        return str;
    }


    
}

package info.androidhive.firebase;

/**
 * Created by Ganesh on 11/12/2017.
 *
 * A wrapper class to write data into the FirebaseDB
 */

public class LocationMark {
    String userID;
    //String online;
    double latitude;
    double longitude;

    public LocationMark() {
    }

    public LocationMark(String userID, double latitude, double longitude) {
        this.userID = userID;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}

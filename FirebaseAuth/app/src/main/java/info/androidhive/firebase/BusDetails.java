package info.androidhive.firebase;

/**
 * Created by shoaib on 20/11/17.
 */

public class BusDetails {
    private String busDriver;
    private String busNumber;
    private String route;
    private String permanentRoute;
    private String  capacity;
    private String femaleCapacity;
    public BusDetails() {
    }

    public  BusDetails(String busDriver, String busNumber, String route, String capacity, String femaleCapacity) {
        this.busDriver = busDriver;
        this.busNumber = busNumber;
        this.route = route;
        this.capacity = capacity;
        this.femaleCapacity = femaleCapacity;
        this.permanentRoute = route;
    }

    public String getBusDriver() {
        return busDriver;
    }

    public String getBusNumber() {
        return busNumber;
    }

    public String getRoute() {
        return route;
    }

    public String getCapacity() {
        return capacity;
    }

    public String getFemaleCapacity() {
        return femaleCapacity;
    }

    public void setBusDriver(String busDriver) {
        this.busDriver = busDriver;
    }

    public void setBusNumber(String busNumber) {
        this.busNumber = busNumber;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public void setCapacity(String capacity) {
        this.capacity = capacity;
    }

    public void setFemaleCapacity(String femaleCapacity) { this.femaleCapacity = femaleCapacity; }

    public String getPermanentRoute() { return permanentRoute; }

    public void setPermanentRoute(String permanentRoute) { this.permanentRoute = permanentRoute; }
}

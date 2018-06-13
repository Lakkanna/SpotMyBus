package info.androidhive.firebase;

/**
 * Created by Ganesh on 11/18/2017.
 */

class DriverUser {
    private String name;
    private String email;
    private String pass;
    private String routeNumber;
    private String vehicleNumber;
    private String phoneNumber;

    public DriverUser() {
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public DriverUser(String name, String email, String pass, String routeNumber, String vehicleNumber, String phoneNumber) {
        this.name = name;
        this.email = email;
        this.pass = pass;

        this.routeNumber = routeNumber;
        this.vehicleNumber = vehicleNumber;
        this.phoneNumber=phoneNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getRouteNumber() {
        return routeNumber;
    }

    public void setRouteNumber(String routeNumber) {
        this.routeNumber = routeNumber;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }
}

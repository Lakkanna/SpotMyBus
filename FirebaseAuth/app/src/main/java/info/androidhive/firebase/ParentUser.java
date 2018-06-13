package info.androidhive.firebase;

/**
 * Created by Ganesh on 11/14/2017.
 */

class ParentUser {
    private String email;
    //private String password;
    private String usn;

    public ParentUser() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsn() {
        return usn;
    }

    public void setUsn(String usn) {
        this.usn = usn;
    }

    public ParentUser(String email, String usn) {
        this.email = email;
        this.usn = usn;
    }
}

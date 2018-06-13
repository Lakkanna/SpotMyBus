package info.androidhive.firebase;
import android.os.AsyncTask;
import android.util.Log;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.ValueEventListener;

class DownloadFilesTask extends AsyncTask<String, Integer, Long> {
    private String userName, userUSN;
    String parentEmail;
    double latitude, longitude;
    FirebaseDatabase database;
    public DownloadFilesTask(String userName, String parentEmail, double latitude, double longitude){
        this.userName = userName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.parentEmail = parentEmail;
    }
    protected Long doInBackground(String... urls) {
        long x = 0;

        try {
            GMailSender sender = new GMailSender("shoaib.official.16940@gmail.com", "firebaseAuth");
            sender.sendMail("SOS",
                    "Your Ward " + userName + " has issued an SOS. They were Last seen at this location\n Latitude : " +
                            String.valueOf(latitude) + " Longitude : " + String.valueOf(longitude),
                    "shoaib.official.16940@gmail.com",
                    parentEmail);
        } catch (Exception e) {
            Log.e("SendMail", e.getMessage(), e);
        }

        return x;
    }

    protected void onProgressUpdate(Integer... progress) {

    }

    protected void onPostExecute(Long result) {

    }
}


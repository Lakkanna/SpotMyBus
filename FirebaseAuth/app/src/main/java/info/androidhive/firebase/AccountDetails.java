package info.androidhive.firebase;

import android.nfc.Tag;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.StringTokenizer;

public class AccountDetails extends AppCompatActivity {
    FirebaseAuth auth;
    DatabaseReference databaseReference;
    FirebaseDatabase firebaseDatabase;
    TextView uName,uEmail,uUsn,uAddr,uPhno,uGender,uBusStop,uBusNum;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_detials);
        final String loginFrom=getIntent().getStringExtra("caller");
        uName=(TextView)findViewById(R.id.uname);
        uEmail=(TextView)findViewById(R.id.uemail);
        uUsn=(TextView)findViewById(R.id.uusn);
        uAddr=(TextView)findViewById(R.id.uaddr);
        uGender=(TextView)findViewById(R.id.uusrgender);
        uPhno=(TextView)findViewById(R.id.uphone);
        uBusStop=(TextView)findViewById(R.id.userBusStop);
        uBusNum=(TextView)findViewById(R.id.userBusNum);




        auth=FirebaseAuth.getInstance();
        firebaseDatabase=FirebaseDatabase.getInstance();
        if(loginFrom.equals("StudentLogin")){
            databaseReference=firebaseDatabase.getReference("studentsUsers");
            //get Student Details
        databaseReference.orderByChild("studentEmail").equalTo(auth.getCurrentUser().getEmail()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot childDataSnapshot:dataSnapshot.getChildren()){
                    HashMap<String,String> studDetails=(HashMap)childDataSnapshot.getValue();
                    if(loginFrom.equals("StudentLogin")) {
                        uName.setText("HELLO " + studDetails.get("studentName"));
                        uAddr.setText("ADDRESS " + studDetails.get("studentAddr"));
                        uPhno.setText("PHONE " + studDetails.get("studentPhno"));
                        uUsn.setText("USN " + studDetails.get("studentUSN"));
                        uGender.setText("Gender " + studDetails.get("studentGender"));
                        uBusStop.setText("Bus Stop Location " +studDetails.get("busStop") );
                        uBusNum.setText("Allocated Bus Number " +studDetails.get("busNumber") );
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        }
        else if(loginFrom.equals("ParentLogin")){
            databaseReference=firebaseDatabase.getReference("parentUsers");
            databaseReference.orderByChild("email").equalTo(auth.getCurrentUser().getEmail()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot childDataSnapshot:dataSnapshot.getChildren()){
                        HashMap<String,String> studDetails=(HashMap)childDataSnapshot.getValue();
                        if(loginFrom.equals("ParentLogin")){
                            uName.setText("HELLO " + studDetails.get("email"));
                            uUsn.setText("Child USN " + studDetails.get("usn"));
                            uAddr.setVisibility(View.GONE);
                            uPhno.setVisibility(View.GONE);
                            uGender.setVisibility(View.GONE);
                            uBusStop.setVisibility(View.GONE);
                            uBusNum.setVisibility(View.GONE);

                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
        else if(loginFrom.equals("DriverLogin")){
            databaseReference=firebaseDatabase.getReference("driverUsers");
            databaseReference.orderByChild("email").equalTo(auth.getCurrentUser().getEmail()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot childDataSnapshot:dataSnapshot.getChildren()){
                        DriverUser driverUser=childDataSnapshot.getValue(DriverUser.class);
                        if(loginFrom.equals("DriverLogin")){
                            uName.setText("HELLO " + driverUser.getName());
                            uUsn.setText("Route Number " + driverUser.getRouteNumber());
                            uPhno.setText("PHONE " + driverUser.getPhoneNumber());
                            uAddr.setText("Vehicle Number " + driverUser.getVehicleNumber());
                            uGender.setVisibility(View.GONE);
                            uBusStop.setVisibility(View.GONE);
                            uBusNum.setVisibility(View.GONE);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        if(loginFrom.equals("StudentLogin")){

            String text1="Student User Details Loading...";
            uName.setText(text1);
        }
        else if(loginFrom.equals("ParentLogin")){

            String text1="Parent User Details Loading...";
            uName.setText(text1);
        }
        else if (loginFrom.equals("DriverLogin")){
            String text1="Driver User Details Loading...";
            uName.setText(text1);
        }


    }

}

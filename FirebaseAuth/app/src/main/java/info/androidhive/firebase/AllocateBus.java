package info.androidhive.firebase;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.AdapterView;

import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AllocateBus extends AppCompatActivity {

    private Spinner displayBus,busRoutes;
    private Button btnAllocate;
    private FirebaseAuth auth;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference ref;
    DatabaseReference busRef;
    String  uName, uUsn, uGender, busDriver;
    String busCapacity, femaleCapacity;
    int busCapacityy, femaleCapacityy;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allocate_bus);
        auth = FirebaseAuth.getInstance();

        ref = database.getReference("studentsUsers");

        //Toast.makeText(AllocateBus.this, auth.getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
        String userMail = auth.getCurrentUser().getEmail();
        ref.orderByChild("studentEmail").equalTo(userMail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot childDataSnapshot:dataSnapshot.getChildren()){
                    StudentUser temp = childDataSnapshot.getValue(StudentUser.class);
                    uName = temp.getStudentName();
                    uUsn = temp.getStudentUSN();
                    uGender = temp.getStudentGender();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        displayBus=(Spinner) findViewById(R.id.display_buses);
        busRoutes=(Spinner)findViewById(R.id.bus_routes);
        Toast.makeText(AllocateBus.this, "Please Select A Bus", Toast.LENGTH_SHORT).show();
        btnAllocate=(Button) findViewById(R.id.btn_allocate);
        final List<String> spinnerArray =  new ArrayList<String>();
        busRef = database.getReference().child("BusDetails");
        busRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Map<String,Object> buses = (Map<String,Object>) dataSnapshot.getValue();
                        for (Map.Entry<String, Object> entry : buses.entrySet()) {
                            Map singleBus = (Map)entry.getValue();
                            spinnerArray.add((String) singleBus.get("busNumber"));
                            busCapacity = (String)singleBus.get("capacity");
                            femaleCapacity = (String)singleBus.get("femaleCapacity");
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, spinnerArray);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        displayBus.setSelection(0);
                        displayBus.setAdapter(adapter);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

        displayBus.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int myPosition, long myID) {

                busRef = database.getReference("BusDetails");
                String item = parentView.getItemAtPosition(myPosition).toString();
                final List<String> spinnerArray2 =  new ArrayList<String>();
                busRef.orderByChild("busNumber").equalTo(item).addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Map<String,Object> buses = (Map<String,Object>) dataSnapshot.getValue();
                                for (Map.Entry<String, Object> entry : buses.entrySet()) {
                                    Map singleBus = (Map)entry.getValue();
                                    String route = (String) singleBus.get("route");
                                    busCapacity = (String) singleBus.get("capacity");
                                    femaleCapacity = (String) singleBus.get("femaleCapacity");
                                    String arrRoute[] = route.split(";");
                                    for(String entryRoute:arrRoute) {
                                        String tempStop[] = entryRoute.split(":");
                                        spinnerArray2.add((String)tempStop[0]);
                                    }

                                }
                                ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, spinnerArray2);
                                adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                busRoutes.setSelection(0);
                                busRoutes.setAdapter(adapter2);

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                //handle databaseError
                            }
                        });

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });








        btnAllocate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                        final String busNo = displayBus.getSelectedItem().toString();
                        final String busStop = busRoutes.getSelectedItem().toString();
                        busCapacityy = Integer.parseInt(busCapacity);
                        femaleCapacityy = Integer.parseInt(femaleCapacity);
                        boolean busAllocated = false;
                        if(uGender.equals("Female")) {
                            if(femaleCapacityy>0) {
                                femaleCapacityy -= 1;
                                busAllocated = true;
                            }
                            else if(busCapacityy>0){
                                busCapacityy -= 1;
                                busAllocated = true;
                            }
                        }
                        else {
                            if(busCapacityy>0) {
                                busCapacityy -= 1;
                                busAllocated = true;
                            }
                        }

                        if(busAllocated) {
                            busRef = database.getReference("BusDetails");
                            busRef.orderByChild("busNumber").equalTo(busNo).addListenerForSingleValueEvent(
                                    new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                                                BusDetails temp = childDataSnapshot.getValue(BusDetails.class);
                                                temp.setCapacity(Integer.toString(busCapacityy));
                                                temp.setFemaleCapacity(Integer.toString(femaleCapacityy));
                                                busRef.child(temp.getBusNumber()).setValue(temp);
                                            }
                                            ref = database.getReference("studentsUsers");
                                            ref.orderByChild("studentUSN").equalTo(uUsn).addListenerForSingleValueEvent(
                                                    new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                                                                StudentUser temp2 = childDataSnapshot.getValue(StudentUser.class);
                                                                temp2.setAllocationStatus(true);
                                                                temp2.setBusStop(busStop);
                                                                temp2.setBusNumber(busNo);
                                                                ref.child(temp2.getStudentUSN()).setValue(temp2);
                                                            }


                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {
                                                        }
                                                    });
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                        }
                                    });





                        }
                    //busAllocated = false;


                }
                catch (Exception e) {
                }
            }
        });
    }
}

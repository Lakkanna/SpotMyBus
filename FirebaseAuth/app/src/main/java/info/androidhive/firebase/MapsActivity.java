package info.androidhive.firebase;

/**
 * Created by Ganesh on 11/12/2017.
 */

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    String userID;
    String childUSN;
    String busNum;
    LocationListener locationListener;
    LocationMark locationMark;
    private GoogleMap mMap;
    ArrayList<LatLng> MarkerPoints;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    Marker mTrackStudent;
    LocationRequest mLocationRequest;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    //this part may require If wrapper for studentUsers or DriverUsers based on flow
    DatabaseReference myref;
    DatabaseReference myParentRef;
    DatabaseReference databaseLatLng;
    DatabaseReference databaseReference;
    DatabaseReference driverLatLng;
    DatabaseReference driverData;
    //DatabaseReference busPosition;

    LatLng latLng;
    String loginFrom;
    double latTrack;
    double lngTrack;
    BitmapDrawable bitmapdraw;
    Bitmap b;
    int height;
    int width;
    Bitmap smallMarker;
    boolean usnGot;
    boolean track;
    boolean busTrack;
    String busNumber, busRoute;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        auth = FirebaseAuth.getInstance();
        track=false;
        busTrack=false;
        databaseLatLng = FirebaseDatabase.getInstance().getReference("UserLatLngData");
        myref = database.getReference("studentsUsers");
        myParentRef=FirebaseDatabase.getInstance().getReference("UserLatLngData");
        driverLatLng=FirebaseDatabase.getInstance().getReference("BusPosition");
        driverData=FirebaseDatabase.getInstance().getReference("driverUsers");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String emailID = user.getEmail();
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        // Initializing
        usnGot=false;
        bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.mipmap.icon_car);
        b = bitmapdraw.getBitmap();
        height = 80;
        width = 45;
        smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

        loginFrom=getIntent().getStringExtra("caller");
        if(loginFrom.equals("ParentLogin")){
            databaseReference = database.getReference("parentUsers");
            databaseReference.orderByChild("email").equalTo(auth.getCurrentUser().getEmail()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                        HashMap<String, String> studDetails = (HashMap) childDataSnapshot.getValue();
                        userID = studDetails.get("usn");
                        usnGot = true;
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
        MarkerPoints = new ArrayList<>();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    public void plotRoute(){
        String routes[] = busRoute.split(";");
        for(String loc:routes) {
            String locTemp[] = loc.split(":");
            String coordinates[] = locTemp[1].split(",");
            LatLng tempLocation = new LatLng(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1]));
            MarkerPoints.add(tempLocation);
        }

        MarkerOptions options = new MarkerOptions();
        for(LatLng tempLoc : MarkerPoints) {
            options.position(tempLoc);
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
            mMap.addMarker(options);
        }

        if (MarkerPoints.size() >= 2) {
            LatLng origin = MarkerPoints.get(0);
            LatLng dest = MarkerPoints.get(1);

            // Getting URL to the Google Directions API
            String url = getUrl(origin, dest);
            Log.d("onMapClick", url.toString());
            FetchUrl FetchUrl = new FetchUrl();

            // Start downloading json data from Google Directions API
            FetchUrl.execute(url);

            for (int i = 2; i < MarkerPoints.size(); i++)//loop starts from 2 because 0 and 1 are already printed
            {
                origin = dest;
                dest = MarkerPoints.get(i);
                url = getUrl(origin, dest);
                Log.d("onMapClick", url.toString());
                FetchUrl = new FetchUrl();
                // Start downloading json data from Google Directions API
                FetchUrl.execute(url);

            }
            //move map camera
            mMap.moveCamera(CameraUpdateFactory.newLatLng(origin));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(40));
            //onLocationChanged(Location location);
            ParserTask parse = new ParserTask();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();

                mMap.setMyLocationEnabled(true);

            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);

        }

        if(loginFrom.equals("DriverLogin")) {
            DatabaseReference busRef = database.getReference("driverUsers");

            String userMail = auth.getCurrentUser().getEmail();
            //Toast.makeText(MapsActivity.this, userMail, Toast.LENGTH_SHORT).show();
            busRef.orderByChild("email").equalTo(userMail).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot childDataSnapshot:dataSnapshot.getChildren()){
                        DriverUser temp = childDataSnapshot.getValue(DriverUser.class);
                        busNumber = temp.getVehicleNumber();
                    }
                    //Toast.makeText(MapsActivity.this, busNumber, Toast.LENGTH_SHORT).show();


                    DatabaseReference busRef2 = database.getReference("BusDetails");
                    busRef2.orderByChild("busNumber").equalTo(busNumber).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for(DataSnapshot childDataSnapshot:dataSnapshot.getChildren()){
                                BusDetails temp = childDataSnapshot.getValue(BusDetails.class);
                                busRoute = temp.getRoute();
                            }
                            //Toast.makeText(MapsActivity.this, busRoute, Toast.LENGTH_SHORT).show();
                            plotRoute();
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

        // Setting onclick event listener for the map
        /*mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng point) {

                // Already two locations  (5 for testing)
                if (MarkerPoints.size() > 4) {
                    MarkerPoints.clear();
                    mMap.clear();
                }

                // Adding new item to the ArrayList
                MarkerPoints.add(point);

                // Creating MarkerOptions
                MarkerOptions options = new MarkerOptions();

                // Setting the position of the marker
                options.position(point);


                 * For the start location, the color of marker is GREEN and
                 * for the end location, the color of marker is RED.

                if (MarkerPoints.size() == 1) {
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                } else if (MarkerPoints.size() == 2) {
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                } else if (MarkerPoints.size() == 3) {
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                } else if (MarkerPoints.size() == 4) {
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                } else if (MarkerPoints.size() == 5) {
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));
                }


                // Add new marker to the Google Map Android API V2
                mMap.addMarker(options);

                // Checks, whether start and end locations are captured
                if (MarkerPoints.size() >= 2) {
                    LatLng origin = MarkerPoints.get(0);
                    LatLng dest = MarkerPoints.get(1);

                    // Getting URL to the Google Directions API
                    String url = getUrl(origin, dest);
                    Log.d("onMapClick", url.toString());
                    FetchUrl FetchUrl = new FetchUrl();

                    // Start downloading json data from Google Directions API
                    FetchUrl.execute(url);

                    for (int i = 2; i < MarkerPoints.size(); i++)//loop starts from 2 because 0 and 1 are already printed
                    {
                        origin = dest;
                        dest = MarkerPoints.get(i);
                        url = getUrl(origin, dest);
                        Log.d("onMapClick", url.toString());
                        FetchUrl = new FetchUrl();
                        // Start downloading json data from Google Directions API
                        FetchUrl.execute(url);

                    }
                    //move map camera
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(origin));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
                    //onLocationChanged(Location location);
                    ParserTask parse = new ParserTask();
                }

            }
        });*/

    }

    private String getUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;


        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;


        return url;
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            Log.d("downloadUrl", data.toString());
            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    // Fetches data from url passed
    private class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data.toString());
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask", jsonData[0].toString());
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                routes = parser.parse(jObject);
                Log.d("ParserTask", "Executing routes");
                Log.d("ParserTask", routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask", e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.RED);

                Log.d("onPostExecute", "onPostExecute lineoptions decoded");

            }

            // Drawing polyline in the Google Map for the i-th route
            if (lineOptions != null) {
                mMap.addPolyline(lineOptions);
            } else {
                Log.d("onPostExecute", "without Polylines drawn");
            }
        }
    }
    protected void getBusTrackingUpdates(DatabaseReference busDatRefObj,String busNum) {
        final DatabaseReference busPos=busDatRefObj;
        String lat;
        String lng;
        busPos.orderByChild("userID").equalTo(busNum).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot chiDataSnapshot1:dataSnapshot.getChildren()) {
                    LocationMark locationMark = chiDataSnapshot1.getValue(LocationMark.class);
                    locationMark.getLatitude();

                    //TODO get the lat and lng values and update the tracker Marker
                    //LocationMark locationMark=studDetails.get("01FB14ECS073");

                    latTrack = Double.parseDouble(String.valueOf(locationMark.getLatitude()));
                    lngTrack = Double.parseDouble(String.valueOf(locationMark.getLongitude()));
                }
                busTrack=true;
                busPos.onDisconnect();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    protected void getTrackingUpdates(DatabaseReference datRefObj) {
        final DatabaseReference dat=datRefObj;
        //databaseLatLng.child(userID).child("latitude");
        String lat;
        String lng;
               dat.orderByChild("userID").equalTo(userID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot chiDataSnapshot1:dataSnapshot.getChildren()){
                            LocationMark locationMark = chiDataSnapshot1.getValue(LocationMark.class);
                            locationMark.getLatitude();

                            //TODO get the lat and lng values and update the tracker Marker
                            //LocationMark locationMark=studDetails.get("01FB14ECS073");

                            latTrack= Double.parseDouble(String.valueOf(locationMark.getLatitude()));
                            lngTrack= Double.parseDouble(String.valueOf(locationMark.getLongitude()));
                            //extract latitude and longitude
                        }
                        track=true;
                        //restart the process in a loop to stop and start. Update
                        //buildGoogleApiClient();
                        dat.onDisconnect();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(100);
        mLocationRequest.setSmallestDisplacement(0f);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            //LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,locationListener,onLocationChanged());
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        //Toast.makeText(getApplicationContext(), "Location Updatedabc!", Toast.LENGTH_SHORT).show();
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            //Toast.makeText(getApplicationContext(), "Location Updated !", Toast.LENGTH_SHORT).show();
            mCurrLocationMarker.remove();
        }
        //Place current location marker
        //Toast.makeText(getApplicationContext(), "" + mLastLocation2.getLatitude() + ":" + mLastLocation2.getLongitude(), Toast.LENGTH_SHORT).show();
         latLng = new LatLng(location.getLatitude(), location.getLongitude());

        //Toast.makeText(getApplicationContext(), "Location Updated123 !", Toast.LENGTH_SHORT).show();

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        //markerOptions.icon(BitmapDescriptorFactory.fromBitmap((smallMarker)));


        //Get the user details i.e userID for tracking

        if(loginFrom.equals("StudentLogin")&&auth.getCurrentUser()!=null) {

            myref.orderByChild("studentEmail").equalTo(auth.getCurrentUser().getEmail()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(auth.getCurrentUser()!=null) {
                        for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                            HashMap<String, String> studDetails = (HashMap) childDataSnapshot.getValue();
                            userID = studDetails.get("studentUSN");
                            busNum=studDetails.get("busNumber");
                        }
                        locationMark = new LocationMark(userID, latLng.latitude, latLng.longitude);
                        databaseLatLng.child(userID).setValue(locationMark);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            //track bus location.
               getBusTrackingUpdates(FirebaseDatabase.getInstance().getReference("BusPosition"),busNum);
        }
        else if(loginFrom.equals("DriverLogin")&&auth.getCurrentUser()!=null) {

            driverData.orderByChild("email").equalTo(auth.getCurrentUser().getEmail()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(auth.getCurrentUser()!=null) {
                        for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                        DriverUser temp=childDataSnapshot.getValue(DriverUser.class);
                            userID=temp.getVehicleNumber();
                        }
                        locationMark = new LocationMark(userID, latLng.latitude, latLng.longitude);
                        driverLatLng.child(userID).setValue(locationMark);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        else if(loginFrom.equals("ParentLogin")&&auth.getCurrentUser()!=null) {
            getTrackingUpdates(FirebaseDatabase.getInstance().getReference("UserLatLngData"));
        }
        //String key=userID;
        if(!loginFrom.equals("ParentLogin")) {
            mCurrLocationMarker = mMap.addMarker(markerOptions);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
        }
        if(track==true) {
            if(mTrackStudent!=null){
                mTrackStudent.remove();
            }
            MarkerOptions markerTrack = new MarkerOptions();
            markerTrack.position(new LatLng(latTrack, lngTrack));
            markerTrack.title("Tracking Location");
            markerTrack.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latTrack, lngTrack)));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(19));
            mTrackStudent = mMap.addMarker(markerTrack);
            Location location1=new Location("");
            location1.setLatitude(latTrack);
            location1.setLongitude(lngTrack);
            animateMarker(location1,mTrackStudent);

        }
        if(busTrack==true) {
            if (mTrackStudent != null) {
                mTrackStudent.remove();
            }
            MarkerOptions markerTrack = new MarkerOptions();
            markerTrack.position(new LatLng(latTrack, lngTrack));
            markerTrack.title("Tracking Location");
            markerTrack.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latTrack, lngTrack)));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(19));
            mTrackStudent = mMap.addMarker(markerTrack);
            Location location1=new Location("");
            location1.setLatitude(latTrack);
            location1.setLongitude(lngTrack);
            animateMarker(location1,mTrackStudent);

        }
            //stop location updates

        if ((mGoogleApiClient != null&&auth.getCurrentUser()==null)) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            //getTrackingUpdates();
        }
            //continue to get updates
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other permissions this app might request.
            // You can add here other case statements according to your requirement.
        }
    }
    public static void animateMarker(final Location destination, final Marker marker) {
        if (marker != null) {
            final LatLng startPosition = marker.getPosition();
            final LatLng endPosition = new LatLng(destination.getLatitude(), destination.getLongitude());

            final float startRotation = marker.getRotation();

            final LatLngInterpolator latLngInterpolator = new LatLngInterpolator.LinearFixed();
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
            valueAnimator.setDuration(1000); // duration 1 second
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override public void onAnimationUpdate(ValueAnimator animation) {
                    try {
                        float v = animation.getAnimatedFraction();
                        LatLng newPosition = latLngInterpolator.interpolate(v, startPosition, endPosition);
                        marker.setPosition(newPosition);
                        marker.setRotation(computeRotation(v, startRotation, destination.getBearing()));
                    } catch (Exception ex) {
                        // I don't care atm..
                    }
                }
            });

            valueAnimator.start();
        }
    }
    private static float computeRotation(float fraction, float start, float end) {
        float normalizeEnd = end - start; // rotate start to 0
        float normalizedEndAbs = (normalizeEnd + 360) % 360;

        float direction = (normalizedEndAbs > 180) ? -1 : 1; // -1 = anticlockwise, 1 = clockwise
        float rotation;
        if (direction > 0) {
            rotation = normalizedEndAbs;
        } else {
            rotation = normalizedEndAbs - 360;
        }

        float result = fraction * rotation + start;
        return (result + 360) % 360;
    }
    private interface LatLngInterpolator {
        LatLng interpolate(float fraction, LatLng a, LatLng b);

        class LinearFixed implements LatLngInterpolator {
            @Override
            public LatLng interpolate(float fraction, LatLng a, LatLng b) {
                double lat = (b.latitude - a.latitude) * fraction + a.latitude;
                double lngDelta = b.longitude - a.longitude;
                // Take the shortest path across the 180th meridian.
                if (Math.abs(lngDelta) > 180) {
                    lngDelta -= Math.signum(lngDelta) * 360;
                }
                double lng = lngDelta * fraction + a.longitude;
                return new LatLng(lat, lng);
            }
        }
    }

}
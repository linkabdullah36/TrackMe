package com.waqasansari.trackme.activities;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.waqasansari.trackme.R;
import com.waqasansari.trackme.dialog.RequestAntiTheftPermission;
import com.waqasansari.trackme.dialog.RequestLocation;
import com.waqasansari.trackme.services.CaptureImage;
import com.waqasansari.trackme.services.HandleRequests;

import java.util.Calendar;

import fr.quentinklein.slt.LocationTracker;
import fr.quentinklein.slt.TrackerSettings;

public class Main extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationTracker locationTracker;

    //private List<LatLng> latLngList;
    //Polyline line;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //startService(new Intent(Main.this, CaptureImage.class));

        //latLngList = new ArrayList<>();


        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        Intent intent = new Intent(Main.this, HandleRequests.class);
        PendingIntent pendingIntent = PendingIntent.getService(Main.this, 0, intent, 0);
        AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 30*1000, pendingIntent);


        if(getIntent().getExtras() != null) {
            LatLng currentLocation = new LatLng(getIntent().getDoubleExtra("latitude", 0.0), getIntent().getDoubleExtra("longitude", 0.0));

            mMap.addMarker(new MarkerOptions().position(currentLocation).title(getIntent().getStringExtra("name")));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15.0f), 3000, null);
        } else setupLocationTracker();


        /*
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setApplicationId(Config.APP_ID)
                .setApiKey(Config.API_KEY)
                .setDatabaseUrl(Config.FIREBASE_URL)
                .build();
        FirebaseApp.initializeApp(Main.this, options);

        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i("DATA: ", dataSnapshot.getValue().toString());
                User user = null;
                for(DataSnapshot snapshot : dataSnapshot.getChildren())
                    user = snapshot.getValue(User.class);
                    //JSONObject object = new JSONObject(dataSnapshot.getValue().toString());
                    String[] latLngString = user.getLocation().trim().split(",");
                    Double lat = Double.valueOf(latLngString[0]);
                    Double lng = Double.valueOf(latLngString[1]);

                    LatLng latLng = new LatLng(lat, lng);
                    mMap.addMarker(new MarkerOptions().position(latLng).title("Location"));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f), 3000, null);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        */
    }


    private void setupLocationTracker() {
        TrackerSettings settings = new TrackerSettings()
                .setUseGPS(true)
                .setUseNetwork(true)
                .setUsePassive(true)
                .setTimeBetweenUpdates(1000 * 60)
                .setMetersBetweenUpdates(10);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationTracker = new LocationTracker(this, settings) {
            @Override
            public void onLocationFound(@NonNull Location location) {
                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                //if(latLngList.size() == 0) {
                    mMap.addMarker(new MarkerOptions().position(currentLocation).title("Current Location"));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15.0f), 3000, null);
                locationTracker.stopListening();
                //}

                //latLngList.add(currentLocation);
                //redrawLine();
            }

            @Override
            public void onTimeout() {
                Log.d("MESSAGE", "Time out");
            }
        };
    }


    /*
    private void redrawLine(){

        mMap.clear();  //clears all Markers and Polylines

        PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
        for (int i = 0; i < latLngList.size(); i++) {
            LatLng point = latLngList.get(i);
            options.add(point);
        }
        addMarker(); //add Marker in current position
        line = mMap.addPolyline(options); //add Polyline
    }

    private void addMarker(){
        mMap.addMarker(new MarkerOptions().position(latLngList.get(latLngList.size()-1)).title("Location"));
        //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngList.get(latLngList.size()-1), 15.0f), 3000, null);
    }
    */



    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
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




    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationTracker.startListening();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_request) {
            final CharSequence[] items = {"Location Request", "Special Request", "Cancel     "};
            final AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);
            builder.setTitle("Select option");
            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if(items[i].equals("Location Request")) {
                        new RequestLocation(Main.this).show();
                    } else if(items[i].equals("Special Request")) {
                        new RequestAntiTheftPermission(Main.this).show();
                    } else if(items[i].equals("Cancel"))
                        dialogInterface.dismiss();
                }
            });
            builder.show();
        }
        return super.onOptionsItemSelected(item);
    }
}

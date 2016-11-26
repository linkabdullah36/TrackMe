package com.waqasansari.trackme.services;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.waqasansari.trackme.R;
import com.waqasansari.trackme.activities.Dummy;
import com.waqasansari.trackme.model.Requests;
import com.waqasansari.trackme.model.User;
import com.waqasansari.trackme.utils.Config;
import com.waqasansari.trackme.utils.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import fr.quentinklein.slt.LocationTracker;
import fr.quentinklein.slt.TrackerSettings;

public class HandleRequests extends Service {
    private LocationTracker locationTracker;

    public HandleRequests() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Config.DATABASE_REFERENCE.child("user-data").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i("DATA", dataSnapshot.getValue().toString());
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot.getKey().equals(Config.USERNAME)) {
                        Requests requests = snapshot.getValue(Requests.class);

                        if (Utility.isRequestStored(HandleRequests.this)) {
                            Requests storedRequest = Utility.restoreFromDevice(HandleRequests.this);
                            if(requests.getLocation_request() != null || requests.getAnti_theft_permission() != null) {

                                boolean nullCheck = true;
                                //place some working check here!
                                if (!requests.getLocation_request().equals(storedRequest.getLocation_request())) {
                                    //location request
                                    String name;
                                    if(storedRequest.getLocation_request() == null && !requests.getLocation_request().contains("|"))
                                        name = requests.getLocation_request();
                                    else name = requests.getLocation_request().replace(storedRequest.getLocation_request(), "").replace("|", "");

                                    if(storedRequest.getAccepted_location_request() != null) {
                                        if (storedRequest.getAccepted_location_request().contains(name)) {
                                            //update location and stop service
                                            updateLocation();
                                            requests.setLocation_request(requests.getLocation_request().replace(name, ""));
                                            Map<String, Object> map = new HashMap<>();
                                            map.put(Utility.LOCATION_REQUEST, requests.getLocation_request());
                                            Config.DATABASE_REFERENCE.child("user-data").child(Config.USERNAME).updateChildren(map);
                                        } else {
                                            showNotification("New Location Request",
                                                    name.toUpperCase() + " sent you a request to access your location",
                                                    requests, name);
                                        }
                                    } else {
                                        showNotification("New Location Request",
                                                name.toUpperCase() + " sent you a request to access your location",
                                                requests, name);
                                    }


                                }
                                if (!requests.getAnti_theft_permission().equals(storedRequest.getAnti_theft_permission())) {
                                    //anti theft request
                                    String name = requests.getAnti_theft_permission().replace(storedRequest.getAnti_theft_permission(), "").trim().replaceAll("|", "");

                                    if(storedRequest.getAccepted_anti_theft_permission() != null) {
                                        if (storedRequest.getAccepted_anti_theft_permission().contains(name)) {
                                            //send pictures and stop service
                                            sendPicture(name);
                                            requests.setAnti_theft_permission(requests.getAnti_theft_permission().replace(name, ""));
                                            Map<String, Object> map = new HashMap<>();
                                            map.put(Utility.ANTI_THEFT_PERMISSION, requests.getAnti_theft_permission());
                                            Config.DATABASE_REFERENCE.child("user-data").child(Config.USERNAME).updateChildren(map);
                                            Utility.storeOnDevice(requests, HandleRequests.this);
                                        } else {
                                            showNotification("New Special Request",
                                                    name.toUpperCase() + " sent you a request to have Special Permission",
                                                    requests, name);
                                        }
                                    } else {
                                        showNotification("New Special Request",
                                                name.toUpperCase() + " sent you a request to have Special Permission",
                                                requests, name);
                                    }

                                }
                            }
                        } else Utility.storeOnDevice(requests, HandleRequests.this);

                    }
                }
                stopSelf();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return super.onStartCommand(intent, flags, startId);
    }


    private void showNotification(String title, String message, Requests requests, String name) {
        Intent resultIntent = new Intent(this, Dummy.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        resultIntent.putExtra("title", title);
        resultIntent.putExtra("message", message);
        resultIntent.putExtra("request", requests);
        resultIntent.putExtra("name", name);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, resultIntent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setTicker("New Notification Alert!")
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }

    private void updateLocation() {
        setupLocationTracker();
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

    private void setupLocationTracker() {
        TrackerSettings settings = new TrackerSettings()
                .setUseGPS(true)
                .setUseNetwork(true)
                .setUsePassive(true);
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
                Map<String, Object> values = new HashMap<>();
                values.put("location", String.valueOf(location.getLatitude() + "_" + location.getLongitude()));
                Config.DATABASE_REFERENCE.child("user-data").child(Config.USERNAME).updateChildren(values);
                locationTracker.stopListening();
            }

            @Override
            public void onTimeout() {
                Log.d("MESSAGE", "Time out");
            }
        };
    }

    private void sendPicture(final String name) {
        Config.DATABASE_REFERENCE.child("user-data").child(name).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    JSONObject object = new JSONObject(dataSnapshot.getValue().toString());
                    if(object.has("email")) {
                        String email = object.getString("email");
                        startService(new Intent(HandleRequests.this, CaptureImage.class).putExtra("email", email));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}

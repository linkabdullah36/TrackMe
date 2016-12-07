package com.waqasansari.trackme.handlers;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.waqasansari.trackme.activities.Main;
import com.waqasansari.trackme.utils.Config;
import com.waqasansari.trackme.utils.Utility;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by WaqasAhmed on 11/16/2016.
 */
public class GetAndShowLocation {
    private String key;
    private String newLocation;
    private Context context;


    public GetAndShowLocation(String key, Context context) {
        this.key = key;
        this.context = context;
    }

    public void getLocationAndStartActivity() {
        Config.DATABASE_REFERENCE.child("user-data").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    JSONObject object = new JSONObject(dataSnapshot.getValue().toString());
                    if(object.has("location")) {
                        newLocation = object.getString("location");
                        startActivity(key, newLocation);
                    } else Toast.makeText(context, key.toUpperCase() + " might not have active internet connection. Please try again.", Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



    private void startActivity(String name, String location) {
        String[] location1 = location.trim().split("_");

        Intent intent = new Intent(context, Main.class);
        intent.putExtra("name", name);
        intent.putExtra("latitude", location1[0]);
        intent.putExtra("longitude", location1[1]);

        context.startActivity(intent);
    }
}
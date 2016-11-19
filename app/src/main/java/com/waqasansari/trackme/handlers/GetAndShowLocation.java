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
public class GetAndShowLocation extends AsyncTask<Void, Void, String> {
    //private ProgressDialog dialog;
    private String key;
    private String oldLocation, newLocation;
    private int count = 0;
    private Context context;

    public GetAndShowLocation(String key, Context context) {
        this.key = key;
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //dialog = new ProgressDialog(context);
        //dialog.setTitle("Please Wait...");
        //dialog.setMessage("Getting Location");
        //dialog.setCancelable(false);
        //dialog.show();
    }

    @Override
    protected String doInBackground(Void... voids) {
        long time = System.currentTimeMillis();
        if(Utility.hasActiveInternetConnection(context)) {
            do {
                Config.DATABASE_REFERENCE.child("user-data").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        try {
                            JSONObject object = new JSONObject(dataSnapshot.getValue().toString());
                            if(object.has("location")) {
                                if(count == 0) {
                                    oldLocation = object.getString("location");
                                    count++;
                                }
                                newLocation = object.getString("location");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            } while ((System.currentTimeMillis() - time) > 15000);

            if(newLocation.equals(oldLocation))
                return "no-change";
            else return "new-location";


        } else return "no-internet";

    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        //dialog.dismiss();
        switch (s) {
            case "no-internet":
                Toast.makeText(context, "Make sure you have active internet conection", Toast.LENGTH_SHORT).show();
                break;
            case "no-change":
                Toast.makeText(context, key.toUpperCase() + " might not have active internet connection.", Toast.LENGTH_SHORT).show();
                if(newLocation != null)
                    startActivity(key, newLocation);
                break;
            case "new-location":
                startActivity(key, newLocation);
                break;
        }

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
package com.waqasansari.trackme.activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.waqasansari.trackme.R;
import com.waqasansari.trackme.model.Requests;
import com.waqasansari.trackme.utils.Config;
import com.waqasansari.trackme.utils.Utility;

import java.util.HashMap;
import java.util.Map;

public class Dummy extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dummy);


        final String title = getIntent().getStringExtra("title");
        String message = getIntent().getStringExtra("message");
        final String name = getIntent().getStringExtra("name");
        final Requests requests = (Requests) getIntent().getSerializableExtra("request");

        final Dialog dialog = new Dialog(Dummy.this);
        dialog.setContentView(R.layout.request_dialog);


        TextView requestText = (TextView) dialog.findViewById(R.id.requestText);
        TextView txtRequestText = (TextView) dialog.findViewById(R.id.txtRequestText);
        Button btnAccept = (Button) dialog.findViewById(R.id.btnAccept);
        Button btnReject = (Button) dialog.findViewById(R.id.btnReject);

        requestText.setText(title);
        txtRequestText.setText(message);

        btnReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                finish();
            }
        });

        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new CheckInternetUpdateData(title, name, requests, dialog).execute();
            }
        });

        dialog.show();
    }


    private class CheckInternetUpdateData extends AsyncTask<Void, Void, Boolean> {
        ProgressDialog dialog;
        String title, name;
        Requests requests;
        Dialog shownDialog;

        CheckInternetUpdateData(String title, String name, Requests requests, Dialog shownDialog) {
            this.title = title;
            this.name = name;
            this.requests = requests;
            this.shownDialog = shownDialog;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(Dummy.this);
            dialog.setTitle("Please Wait...");
            dialog.setMessage("Updating your data");
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            return Utility.hasActiveInternetConnection(Dummy.this);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if(aBoolean) {

                if(title.equals("New Location Request")) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(requests.getAccepted_location_request());
                    int length = stringBuilder.length();
                    if(stringBuilder.charAt(length-1) != '|')
                        stringBuilder.append('|');

                    stringBuilder.append(name);

                    requests.setAccepted_location_request(stringBuilder.toString());
                    requests.setLocation_request(requests.getLocation_request().replace(name, ""));

                    Map<String, Object> map = new HashMap<>();
                    map.put(Utility.LOCATION_REQUEST, requests.getLocation_request());
                    map.put(Utility.ACCEPTED_LOCATION_REQUEST, requests.getAccepted_location_request());
                    Config.DATABASE_REFERENCE.child("user-data").child(Config.USERNAME).updateChildren(map);


                } else if(title.equals("New Special Request")) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(requests.getAccepted_anti_theft_permission());
                    int length = stringBuilder.length();
                    if(stringBuilder.charAt(length-1) != '|')
                        stringBuilder.append(',');

                    stringBuilder.append(name);

                    requests.setAccepted_anti_theft_permission(stringBuilder.toString());
                    requests.setAnti_theft_permission(requests.getLocation_request().replace(name, ""));

                    Map<String, Object> map = new HashMap<>();
                    map.put(Utility.ANTI_THEFT_PERMISSION, requests.getAnti_theft_permission());
                    map.put(Utility.ACCEPTED_ANTI_THEFT_PERMISSION, requests.getAccepted_anti_theft_permission());
                    Config.DATABASE_REFERENCE.child("user-data").child(Config.USERNAME).updateChildren(map);
                }

                Toast.makeText(Dummy.this, "Your data is successfully updated", Toast.LENGTH_SHORT).show();

            } else Toast.makeText(Dummy.this, "Make sure you have active internet connection", Toast.LENGTH_SHORT).show();

            dialog.dismiss();
            shownDialog.dismiss();
            finish();
        }
    }
}

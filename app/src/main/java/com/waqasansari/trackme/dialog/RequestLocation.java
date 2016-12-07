package com.waqasansari.trackme.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.waqasansari.trackme.R;
import com.waqasansari.trackme.activities.Main;
import com.waqasansari.trackme.handlers.GetAndShowLocation;
import com.waqasansari.trackme.utils.Config;
import com.waqasansari.trackme.model.User;
import com.waqasansari.trackme.utils.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by WaqasAhmed on 10/28/2016.
 */

public class RequestLocation extends Dialog {

    public RequestLocation(Context context) {
        super(context);
    }

    public RequestLocation(Context context, int themeResId) {
        super(context, themeResId);
    }

    protected RequestLocation(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_request_location);

        findViewById(R.id.btnRequest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRequestLocation();
            }
        });
        findViewById(R.id.btnCacnel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCancel();
            }
        });

    }

    private void onCancel() {
        this.dismiss();
    }

    private void onRequestLocation() {
        EditText edtEmail = (EditText) findViewById(R.id.edtEmail);

        final String email = edtEmail.getText().toString();

        if(edtEmail.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Please provide Email", Toast.LENGTH_SHORT).show();
            return;
        }

        if(email.equals(Config.EMAIL)) {
            Toast.makeText(getContext(), "You can't send request to yourself.", Toast.LENGTH_SHORT).show();
            return;
        }

        Config.DATABASE_REFERENCE.child("user-data").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String key = null;
                boolean check = false;
                User user = null, desiredUser = null;
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    user = snapshot.getValue(User.class);
                    if(email.equals(user.getEmail())) {
                        check = true;
                        desiredUser = user;
                        key= snapshot.getKey();
                    }
                }

                if(check) {
                    if(desiredUser.getLocation_request() == null) desiredUser.setLocation_request("");

                    if(desiredUser.getLocation_request().contains(key)) {
                        Toast.makeText(getContext(), "Your previous request is not accepted yet.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if(desiredUser.getAccepted_location_request() == null) desiredUser.setAccepted_location_request("");
                    if (desiredUser.getAccepted_location_request().contains(Config.USERNAME)) {
                        new GetAndShowLocation(key, getContext()).getLocationAndStartActivity();
                        Toast.makeText(getContext(), "You'll shortly be shown location on a Map", Toast.LENGTH_SHORT).show();
                    } else {
                        AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                        alertDialog.setTitle("Request Sent");
                        alertDialog.setMessage("Your request has been sent. You can get location when your request is accepted.");
                        alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "OK", new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        alertDialog.show();
                    }



                    if (desiredUser.getLocation_request() == null || desiredUser.getLocation_request().equals(""))
                        desiredUser.setLocation_request(Config.USERNAME);
                    else
                        desiredUser.setLocation_request(desiredUser.getLocation_request() + "|" + Config.USERNAME);

                    HashMap<String, Object> params = new HashMap<>();
                    params.put(Utility.LOCATION_REQUEST, desiredUser.getLocation_request());

                    Config.DATABASE_REFERENCE.child("user-data").child(key).updateChildren(params);

                } else Toast.makeText(getContext(), "Email you provided has not been registered yet.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        if(isShowing())
            dismiss();
    }



}

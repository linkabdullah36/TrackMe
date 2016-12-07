package com.waqasansari.trackme.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
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
import com.waqasansari.trackme.handlers.GetAndShowLocation;
import com.waqasansari.trackme.services.CaptureImage;
import com.waqasansari.trackme.services.HandleRequests;
import com.waqasansari.trackme.utils.Config;
import com.waqasansari.trackme.model.User;
import com.waqasansari.trackme.utils.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by WaqasAhmed on 10/28/2016.
 */

public class RequestAntiTheftPermission extends Dialog {

    public RequestAntiTheftPermission(Context context) {
        super(context);
    }

    public RequestAntiTheftPermission(Context context, int themeResId) {
        super(context, themeResId);
    }

    protected RequestAntiTheftPermission(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_request_anti_thief_permission);

        findViewById(R.id.btnRequest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onAntiTheftPermission();
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

    private void onAntiTheftPermission() {
        EditText edtEmail = (EditText) findViewById(R.id.edtEmail);
        EditText edtIMEI = (EditText) findViewById(R.id.edtIMEI);

        final String email = edtEmail.getText().toString();
        final String imei = edtIMEI.getText().toString();

        if(email.isEmpty() || imei.isEmpty()) {
            Toast.makeText(getContext(), "Please provide all necessary information", Toast.LENGTH_SHORT).show();
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
                    key = snapshot.getKey();
                    user = snapshot.getValue(User.class);
                    if(email.equals(user.getEmail())) {
                        check = true;
                        desiredUser = user;
                    }
                }

                if(check) {

                    if(! imei.equals(user.getIMEI())) {
                        Toast.makeText(getContext(), "Provided IMEI doesn't match with user's IMEI", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if(desiredUser.getAnti_theft_permission() == null) desiredUser.setAnti_theft_permission("");

                    if(desiredUser.getAnti_theft_permission().contains(key)) {
                        Toast.makeText(getContext(), "Your previous request is not accepted yet.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if(desiredUser.getAccepted_anti_theft_permission() == null) desiredUser.setAccepted_anti_theft_permission("");
                    if (desiredUser.getAccepted_anti_theft_permission().contains(Config.USERNAME)) {
                        //show location and send pictures
                        new ShowLocationAndSendPictures(key).execute();
                        Toast.makeText(getContext(), "You'll shortly receive email with pictures of a suspect", Toast.LENGTH_SHORT).show();
                    } else {
                        AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                        alertDialog.setTitle("Request Sent");
                        alertDialog.setMessage("Your request has been sent. You can get pictures when your request is accepted.");
                        alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "OK", new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        alertDialog.show();
                    }


                    if (desiredUser.getAnti_theft_permission() == null || desiredUser.getAnti_theft_permission().equals(""))
                        desiredUser.setAnti_theft_permission(Config.USERNAME);
                    else
                        desiredUser.setAnti_theft_permission(desiredUser.getAnti_theft_permission() + "|" + Config.USERNAME);

                    HashMap<String, Object> params = new HashMap<>();
                    params.put(Utility.ANTI_THEFT_PERMISSION, desiredUser.getAnti_theft_permission());

                    Config.DATABASE_REFERENCE.child("user-data").child(key).updateChildren(params);
                    /*
                    if(! imei.equals(user.getIMEI())) {
                        Toast.makeText(getContext(), "Provided IMEI doesn't match with user's IMEI", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if(desiredUser.getLocation_request() == null)
                        desiredUser.setLocation_request(Config.USERNAME);
                    else desiredUser.setLocation_request(desiredUser.getLocation_request() + "," + Config.USERNAME);

                    HashMap<String, Object> params = new HashMap<>();
                    params.put(Utility.ANTI_THEFT_PERMISSION, desiredUser.getLocation_request());

                    Config.DATABASE_REFERENCE.child("user-data").child(key).updateChildren(params, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            Toast.makeText(getContext(), "Request Sent!", Toast.LENGTH_SHORT).show();
                            dismiss();
                        }
                    });
                    */
                } else Toast.makeText(getContext(), "Email you provided has not been registered yet.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if(isShowing())
            dismiss();
    }

    private class ShowLocationAndSendPictures extends AsyncTask<Void, Void, String> {
        String key;

        ShowLocationAndSendPictures(String key) {
            this.key = key;
        }


        @Override
        protected String doInBackground(Void... voids) {
            Config.DATABASE_REFERENCE.child("user-data").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        JSONObject object = new JSONObject(dataSnapshot.getValue().toString());
                        if(object.has("email")) {
                            String email = object.getString("email");
                            getContext().startService(new Intent(getContext(), CaptureImage.class).putExtra("email", email));
                        }
                        if(object.has("location")) {
                            new GetAndShowLocation(key, getContext()).getLocationAndStartActivity();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            return null;
        }
    }
}

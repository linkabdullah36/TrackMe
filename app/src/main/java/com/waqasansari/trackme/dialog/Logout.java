package com.waqasansari.trackme.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.waqasansari.trackme.R;
import com.waqasansari.trackme.activities.Main;
import com.waqasansari.trackme.activities.SignIn;
import com.waqasansari.trackme.model.User;
import com.waqasansari.trackme.utils.Config;
import com.waqasansari.trackme.utils.Utility;

/**
 * Created by WaqasAhmed on 12/7/2016.
 */

public class Logout extends Dialog {
    private EditText edtUsername, edtPassword;
    Context context;

    public Logout(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_logout);

        findViewById(R.id.btnCacnel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCancel();
            }
        });

        findViewById(R.id.btnLogout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onLogout();
            }
        });

    }

    private void onCancel(){
        this.dismiss();
    }

    private void onLogout() {
        edtUsername = (EditText) findViewById(R.id.edtUsername);
        edtPassword = (EditText) findViewById(R.id.edtPassword);

        final String email = edtUsername.getText().toString();

        if(email.isEmpty()) {
            Toast.makeText(context, "Please provide Username", Toast.LENGTH_SHORT).show();
            return;
        }
        if(edtPassword.getText().toString().isEmpty()) {
            Toast.makeText(context, "Please provide password to logout.", Toast.LENGTH_SHORT).show();
            return;
        }

        new CheckInternetAndLogout().execute();
    }



    private class CheckInternetAndLogout extends AsyncTask<Void, Void, Boolean> {
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(context);
            dialog.setTitle("Please Wait...");
            dialog.setMessage("Logging out");
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            return Utility.hasActiveInternetConnection(context);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if(aBoolean) {
                Config.DATABASE_REFERENCE.child("user-data").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        boolean check = false;
                        User user = null;
                        String username = null;
                        String userText = edtUsername.getText().toString();
                        for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            if(snapshot.getKey().equals(userText)) {
                                check = true;
                                user = snapshot.getValue(User.class);
                                username = snapshot.getKey();
                            }
                        }

                        if(check) {
                            if(edtPassword.getText().toString().equals(user.getPassword())) {
                                Config.EMAIL = user.getEmail();
                                Config.PASSWORD = user.getPassword();
                                Config.USERNAME = username;

                                Utility.storeUser(Config.USERNAME, Config.EMAIL, Config.PASSWORD, context);
                                Utility.clearAllCache(context);
                                context.startActivity(new Intent(context, SignIn.class).putExtra("logout", true));
                                ((Activity) context).finish();
                            } else Toast.makeText(context, "Password is incorrect", Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(context, "User not found", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            } else Toast.makeText(context, "Make sure you have active internet connection", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        }
    }

}

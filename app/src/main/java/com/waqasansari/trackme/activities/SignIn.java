package com.waqasansari.trackme.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.waqasansari.trackme.R;
import com.waqasansari.trackme.utils.Config;
import com.waqasansari.trackme.model.User;
import com.waqasansari.trackme.utils.Utility;

import java.util.List;

public class SignIn extends AppCompatActivity {
    EditText edtUsername, edtPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);


        edtUsername = (EditText) findViewById(R.id.edtUsername);
        edtPassword = (EditText) findViewById(R.id.edtPassword);


        findViewById(R.id.btnSignIn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //startActivity(new Intent(SignIn.this, Main.class));
                new CheckInternetAndSignIn().execute();
            }
        });
        findViewById(R.id.txtSignUp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignIn.this, SignUp.class));
            }
        });

        if(getIntent().getExtras() != null) {
            if(getIntent().getBooleanExtra("logout", false))
                return;
        }

        if(Utility.isUserStored(SignIn.this)) {
            String[] detail = Utility.restoreUser(SignIn.this);
            Config.USERNAME = detail[0];
            Config.EMAIL = detail[1];
            Config.PASSWORD = detail[2];
            startActivity(new Intent(SignIn.this, Main.class));
        }

        boolean hasBeenInitialized=false;
        List<FirebaseApp> firebaseApps = FirebaseApp.getApps(this);
        for(FirebaseApp app : firebaseApps){
            if(app.getName().equals(FirebaseApp.DEFAULT_APP_NAME)){
                hasBeenInitialized=true;
            }
        }

        if(!hasBeenInitialized) {
            Config.initializeFirebase(this);
        }

    }


    private class CheckInternetAndSignIn extends AsyncTask<Void, Void, Boolean>{
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(SignIn.this);
            dialog.setTitle("Please Wait...");
            dialog.setMessage("Signing in");
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            return Utility.hasActiveInternetConnection(SignIn.this);
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

                                Utility.storeUser(Config.USERNAME, Config.EMAIL, Config.PASSWORD, SignIn.this);
                                startActivity(new Intent(SignIn.this, Main.class));
                            } else Toast.makeText(SignIn.this, "Password is incorrect", Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(SignIn.this, "User not found", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            } else Toast.makeText(SignIn.this, "Make sure you have active internet connection", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        }
    }

}

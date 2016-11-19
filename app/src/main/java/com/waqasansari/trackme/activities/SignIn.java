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

public class SignIn extends AppCompatActivity {
    EditText edtUsername, edtPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        if (ContextCompat.checkSelfPermission(SignIn.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(SignIn.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(SignIn.this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(SignIn.this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(SignIn.this,
                        Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(SignIn.this,
                        Manifest.permission.READ_PHONE_STATE)
                        != PackageManager.PERMISSION_GRANTED) {


            // No explanation needed, we can request the permission.

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ActivityCompat.requestPermissions(SignIn.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.CAMERA,
                                Manifest.permission.READ_PHONE_STATE},
                        1);
            }
        }



        Config.initializeFirebase(this);

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
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0) {
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_GRANTED){

                } else {
                    Toast.makeText(SignIn.this, "Some of app\'s functions do need permissions. We are sorry for not proceeding further.", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

            }

        } else {

            // permission denied, boo! Disable the
            // functionality that depends on this permission.
            Toast.makeText(SignIn.this, "Some of app\'s functions need permissions. We are sorry for not proceeding further.", Toast.LENGTH_SHORT).show();
            finish();
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

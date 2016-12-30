package com.waqasansari.trackme.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.waqasansari.trackme.R;
import com.waqasansari.trackme.utils.Config;
import com.waqasansari.trackme.utils.Utility;

import java.util.HashMap;

public class SignUp extends AppCompatActivity {
    EditText edtEmail, edtUsername, edtPwd, edtConfirmPwd, edtIMEI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        edtEmail = (EditText) findViewById(R.id.edtEmail);
        edtUsername = (EditText) findViewById(R.id.edtUsername);
        edtPwd = (EditText) findViewById(R.id.edtPassword);
        edtConfirmPwd = (EditText) findViewById(R.id.edtConfirmPassword);

        edtIMEI = (EditText) findViewById(R.id.edtIMEI);
        TelephonyManager manager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        edtIMEI.setText(manager.getDeviceId());



        findViewById(R.id.btnSignIn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(edtEmail.getText().toString().isEmpty() ||
                        edtUsername.getText().toString().isEmpty() ||
                        edtPwd.getText().toString().isEmpty() ||
                        edtConfirmPwd.getText().toString().isEmpty()) {
                    Toast.makeText(SignUp.this, "Don't leave empty fields.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(Utility.checkUsername(edtUsername.getText().toString())) {
                    Toast.makeText(SignUp.this, "You might have used forbidden special characters.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(! edtPwd.getText().toString().equals(edtConfirmPwd.getText().toString())) {
                    Toast.makeText(SignUp.this, "Passwords don't match", Toast.LENGTH_SHORT).show();
                    return;
                }

                new CheckInternetAndSignUp().execute();

            }
        });
    }

    private class CheckInternetAndSignUp extends AsyncTask<Void, Void, Boolean> {
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(SignUp.this);
            dialog.setTitle("Please wait...");
            dialog.setMessage("Updating your data.");
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            return Utility.hasActiveInternetConnection(SignUp.this);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            dialog.dismiss();
            if(aBoolean) {


                Config.DATABASE_REFERENCE.child("user-data").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        boolean check = false;
                        for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String username = snapshot.getKey();
                            if(username.equals(edtUsername.getText().toString()))
                                check = true;
                        }
                        if(check)
                             Toast.makeText(SignUp.this, "Email Address already exists", Toast.LENGTH_SHORT).show();
                        else {

                            final String username = edtUsername.getText().toString();

                            HashMap<String, String> params = new HashMap<>();
                            params.put("email", edtEmail.getText().toString());
                            params.put("password", edtPwd.getText().toString());
                            params.put("imei", edtIMEI.getText().toString());


                            Config.DATABASE_REFERENCE.child("user-data").child(username).setValue(params, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    Config.EMAIL = edtEmail.getText().toString();
                                    Config.PASSWORD = edtPwd.getText().toString();
                                    Config.USERNAME = username;
                                    Toast.makeText(SignUp.this, "Successfully Signed up.", Toast.LENGTH_SHORT).show();
                                    Utility.storeUser(Config.USERNAME, Config.EMAIL, Config.PASSWORD, SignUp.this);
                                    startActivity(new Intent(SignUp.this, Main.class));
                                }
                            });

                         }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            } else Toast.makeText(SignUp.this, "Make sure you have active internet connection", Toast.LENGTH_SHORT).show();
        }
    }

}

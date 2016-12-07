package com.waqasansari.trackme.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.waqasansari.trackme.R;

public class SplashScreen extends AppCompatActivity implements Animation.AnimationListener {
    ImageView imgLogo;
    Animation animZoomIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spash_screen);

        String[] permissionsToBeAskedFor = {Manifest.permission_group.STORAGE,
                                            Manifest.permission_group.LOCATION,
                                            Manifest.permission.CAMERA,
                                            Manifest.permission.READ_PHONE_STATE};

        if (ContextCompat.checkSelfPermission(SplashScreen.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(SplashScreen.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(SplashScreen.this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(SplashScreen.this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(SplashScreen.this,
                        Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(SplashScreen.this,
                        Manifest.permission.READ_PHONE_STATE)
                        != PackageManager.PERMISSION_GRANTED) {


            // No explanation needed, we can request the permission.

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ActivityCompat.requestPermissions(SplashScreen.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.CAMERA,
                                Manifest.permission.READ_PHONE_STATE},
                        1);
            }
        } else proceed();


    }

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        startActivity(new Intent(SplashScreen.this, SignIn.class));
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }

    private void proceed() {
        imgLogo = (ImageView) findViewById(R.id.imgMainLogo);
        animZoomIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_in);
        animZoomIn.setAnimationListener(SplashScreen.this);
        imgLogo.startAnimation(animZoomIn);
    }

    private void requestPermissionAndContinue(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)){
                //Log.e(TAG, "permission denied, show dialog");
                Toast.makeText(SplashScreen.this, "Some of app\'s functions need permissions. We are sorry for not proceeding further.", Toast.LENGTH_SHORT).show();
                finish();
            }else{
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, 1);
            }
        }else{
            proceed();
        }
    }


    private void checkAndAskPermission(String permission) {
        if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {

        } else {

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            proceed();
        } else {
            // permission denied, boo! Disable the
            // functionality that depends on this permission.
            Toast.makeText(SplashScreen.this, "Some of app\'s functions need permissions. We are sorry for not proceeding further.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

}

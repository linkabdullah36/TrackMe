package com.waqasansari.trackme.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.hanks.htextview.HTextView;
import com.waqasansari.trackme.R;

public class SplashScreen extends AppCompatActivity implements Animation.AnimationListener {
    ImageView imgLogo;
    Animation animZoomIn;

    HTextView txtAppName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spash_screen);

        txtAppName = (HTextView) findViewById(R.id.txtAppName);
        imgLogo = (ImageView) findViewById(R.id.imgMainLogo);
        animZoomIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_in);
        animZoomIn.setAnimationListener(SplashScreen.this);

        imgLogo.startAnimation(animZoomIn);
    }

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        txtAppName.setText("TrackMe");
        startActivity(new Intent(SplashScreen.this, SignIn.class));
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }
}

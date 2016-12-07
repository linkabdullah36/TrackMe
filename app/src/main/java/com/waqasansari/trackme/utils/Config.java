package com.waqasansari.trackme.utils;

import android.content.Context;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.waqasansari.trackme.activities.SignIn;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by WaqasAhmed on 10/19/2016.
 */
public class Config {
    public static String EMAIL;
    public static String PASSWORD;
    public static String USERNAME;

    public static String LOCATION = null;

    public static List<String> capturedBitmaps = new ArrayList<>();

    public static String APP_ID = "1:949655863020:android:87c8b50cae40f983";
    public static String API_KEY = "AIzaSyBjXdRElhslv_hCM7-w9VmG6mDia_xJEak";
    public static String FIREBASE_URL = "https://trackme-db977.firebaseio.com/";

    public static DatabaseReference DATABASE_REFERENCE;

    public static void initializeFirebase(Context context) {
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setApplicationId(Config.APP_ID)
                .setApiKey(Config.API_KEY)
                .setDatabaseUrl(Config.FIREBASE_URL)
                .build();

        FirebaseApp.initializeApp(context, options);
        Config.DATABASE_REFERENCE = FirebaseDatabase.getInstance()
                .getReferenceFromUrl(Config.FIREBASE_URL);

    }
}

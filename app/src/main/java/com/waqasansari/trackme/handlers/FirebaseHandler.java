package com.waqasansari.trackme.handlers;

import android.content.Context;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.waqasansari.trackme.utils.Config;

/**
 * Created by WaqasAhmed on 10/18/2016.
 */
public class FirebaseHandler {
    private String firebaseString = "https://trackme-db977.firebaseio.com/";
    private DatabaseReference databaseReference;

    boolean emailCheck = false, pwdCheck = false;

    private static FirebaseHandler handler;

    public static FirebaseHandler initializeHandler(Context context) {
        if(handler == null)
            handler = new FirebaseHandler(context);

        return handler;
    }

    private FirebaseHandler(Context context){
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setApplicationId(Config.APP_ID)
                .setApiKey(Config.API_KEY)
                .setDatabaseUrl(Config.FIREBASE_URL)
                .build();

        FirebaseApp.initializeApp(context, options);
        databaseReference = FirebaseDatabase.getInstance()
                .getReferenceFromUrl(firebaseString);
    }


    public String getLocationOfUser(final String userEmailId) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i("DATA_SNAPSHOT", dataSnapshot.toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return null;
    }
}

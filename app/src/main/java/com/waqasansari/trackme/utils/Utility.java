package com.waqasansari.trackme.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.Nullable;
import android.util.Log;

import com.waqasansari.trackme.model.Requests;
import com.waqasansari.trackme.model.User;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by WaqasAhmed on 10/27/2016.
 */
public class Utility {
    public static User user = new User();


    public static String ACCEPTED_LOCATION_REQUEST = "accepted_location_request";
    public static String ACCEPTED_ANTI_THEFT_PERMISSION = "accepted_anti_theft_permission";
    public static String ANTI_THEFT_PERMISSION = "anti_theft_permission";
    public static String LOCATION_REQUEST = "location_request";


    public static boolean checkUsername(String username) {
        String USERNAME_PATTERN = "[a-zA-Z0-9][^.#$]";

        Pattern pattern;
        Matcher matcher;
        pattern = Pattern.compile(USERNAME_PATTERN);
        matcher = pattern.matcher(username);
        return matcher.matches();
    }

    private static boolean isNetworkAvailable(Context context){
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        return info != null;
    }

    public static boolean hasActiveInternetConnection(Context context){
        if(isNetworkAvailable(context)) {
            try {
                HttpURLConnection connection = (HttpURLConnection) (new URL("http://clients3.google.com/generate_204")).openConnection();
                connection.setRequestProperty("User-Agent", "Test");
                connection.setRequestProperty("Connection", "close");
                connection.setReadTimeout(1500);
                connection.connect();
                return (connection.getResponseCode() == 204 && connection.getContentLength() == 0);
            } catch (IOException e){
                Log.e("ERROR", "Error checking internet connection");
            }
        } else Log.e("ERROR", "No network available");
        return false;
    }

    public static void storeOnDevice(Requests requests, Context context) {
        String USER_FILE = "requests";
        SharedPreferences.Editor editor = context.getSharedPreferences(USER_FILE, Context.MODE_PRIVATE).edit();
        editor
                .putBoolean("is_stored", true)

                .putString(Utility.LOCATION_REQUEST, requests.getLocation_request())
                .putString(Utility.ACCEPTED_LOCATION_REQUEST, requests.getAccepted_location_request())

                .putString(Utility.ANTI_THEFT_PERMISSION, requests.getAnti_theft_permission())
                .putString(Utility.ACCEPTED_ANTI_THEFT_PERMISSION, requests.getAccepted_anti_theft_permission());

        editor.apply();
    }

    public static Requests restoreFromDevice(Context context) {
        String USER_FILE = "requests";
        Requests requests = new Requests();

        SharedPreferences preferences = context.getSharedPreferences(USER_FILE, Context.MODE_PRIVATE);


        requests.setLocation_request(preferences.getString(Utility.LOCATION_REQUEST, null));
        requests.setAccepted_location_request(preferences.getString(Utility.ACCEPTED_LOCATION_REQUEST, null));

        requests.setAnti_theft_permission(preferences.getString(Utility.ANTI_THEFT_PERMISSION, null));
        requests.setAccepted_anti_theft_permission(preferences.getString(Utility.ACCEPTED_ANTI_THEFT_PERMISSION, null));

        return requests;
    }

    public static boolean isRequestStored(Context context) {
        String USER_FILE = "requests";
        SharedPreferences preferences = context.getSharedPreferences(USER_FILE, Context.MODE_PRIVATE);

        return preferences.getBoolean("is_stored", false);
    }



    public static void cacheUser(String username, String email, String password) {

    }
}

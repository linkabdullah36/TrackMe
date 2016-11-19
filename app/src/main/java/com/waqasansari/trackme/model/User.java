package com.waqasansari.trackme.model;

/**
 * Created by WaqasAhmed on 10/24/2016.
 */
public class User {
    private String email;
    private String location;
    private String password;
    private String IMEI;

    private String location_request;
    private String anti_theft_permission;

    private String accepted_location_request;
    private String accepted_anti_theft_permission;

    public User() {
    }


    //Setters
    public void setEmail(String email) {
        this.email = email;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setIMEI(String IMEI) {
        this.IMEI = IMEI;
    }


    public void setLocation_request(String location_request) {
        this.location_request = location_request;
    }

    public void setAnti_theft_permission(String anti_theft_permission) {
        this.anti_theft_permission = anti_theft_permission;
    }

    public void setAccepted_location_request(String accepted_location_request) {
        this.accepted_location_request = accepted_location_request;
    }

    public void setAccepted_anti_theft_permission(String accepted_anti_theft_permission) {
        this.accepted_anti_theft_permission = accepted_anti_theft_permission;
    }


    //Getters
    public String getEmail() {
        return email;
    }

    public String getLocation() {
        return location;
    }

    public String getPassword() {
        return password;
    }

    public String getIMEI() {
        return IMEI;
    }


    public String getLocation_request() {
        return location_request;
    }

    public String getAnti_theft_permission() {
        return anti_theft_permission;
    }

    public String getAccepted_location_request() {
        return accepted_location_request;
    }
    public String getAccepted_anti_theft_permission() {
        return accepted_anti_theft_permission;
    }
}

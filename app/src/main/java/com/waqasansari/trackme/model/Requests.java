package com.waqasansari.trackme.model;

import java.io.Serializable;

/**
 * Created by WaqasAhmed on 11/12/2016.
 */

public class Requests implements Serializable{

    private String location_request;
    private String anti_theft_permission;

    private String accepted_location_request;
    private String accepted_anti_theft_permission;

    public Requests() { }

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

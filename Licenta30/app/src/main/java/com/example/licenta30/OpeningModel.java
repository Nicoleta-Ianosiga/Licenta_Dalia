package com.example.licenta30;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class OpeningModel {
    String door;
    String time_stamp;
    String user;
    public OpeningModel(String door, String user) {
        this.door = door;
        setTime_stamp();
        this.user = user;
    }

    public String getDoor() {
        return door;
    }
    public String getTime_stamp() {
        return time_stamp;
    }

    public void setTime_stamp() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String formattedDate = sdf.format(calendar.getTime());
        this.time_stamp = formattedDate;
    }

    public String getUser() {
        return user;
    }

}

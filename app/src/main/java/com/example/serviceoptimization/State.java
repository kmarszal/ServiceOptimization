package com.example.serviceoptimization;

import android.net.NetworkInfo;

import java.util.Calendar;

public class State {
    private float batteryLevel;
    private boolean isCharging;
    private boolean isNetworkConnected;
    private int connectionType;
    private int connectionSubType;
    private int hour;
    private int day;
    private int month;
    private long startTimeMillis;

    public boolean isCharging() {
        return isCharging;
    }

    public State setCharging(boolean charging) {
        isCharging = charging;
        return this;
    }

    public float getBatteryLevel() {
        return batteryLevel;
    }

    public State setBatteryLevel(float batteryLevel) {
        this.batteryLevel = batteryLevel;
        return this;
    }

    public State setNetworkInfo(NetworkInfo networkInfo) {
        isNetworkConnected = networkInfo != null && networkInfo.isConnected();
        if(isNetworkConnected) {
            connectionType = networkInfo.getType();
            connectionSubType = networkInfo.getSubtype();
        }
        return this;
    }

    public State setTime(Calendar calendar) {
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        day = calendar.get(Calendar.DAY_OF_WEEK);
        month = calendar.get(Calendar.MONTH);
        startTimeMillis = calendar.getTimeInMillis();
        return this;
    }

    public boolean isNetworkConnected() {
        return isNetworkConnected;
    }

    public int getConnectionType() {
        return connectionType;
    }

    public int getConnectionSubType() {
        return connectionSubType;
    }
}

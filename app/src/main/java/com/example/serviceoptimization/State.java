package com.example.serviceoptimization;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.Calendar;

public class State {
    private float batteryLevel;
    private boolean isCharging;
    private boolean isNetworkConnected;
    private int taskNumber;
    private int connectionType;
    private int connectionSubType;
    private int hour;
    private int day;
    private int month;
    private long startTimeMillis;
    private boolean toOffload;

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

    public State setTaskNumber(int taskNumber) {
        this.taskNumber = taskNumber;
        return this;
    }

    public boolean isToOffload() {
        return toOffload;
    }

    public State setToOffload(boolean toOffload) {
        this.toOffload = toOffload;
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

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    public int getHour() {
        return hour;
    }

    public long getStartTimeMillis() {
        return startTimeMillis;
    }

    @Override
    public int hashCode() {
        //8 digits - task number/offloaded?/is charging/network connection/hour/day/month
        //task number        - 0-?
        //offloaded          - 0/1
        //ischarging         - 0/1
        //network connection - 0/1/2 - disconnected/wi-fi/mobile
        //hour               - 0/1/2/3 - 06.00-12.00/12.00-17.00/17.00-00.00/00.00-06.00
        //day                - 0/1/2/3/4/5/6 - starting from monday
        //month              - 00/01/02/03/04/05/06/07/08/09/10/11

        int result = 10000000 * taskNumber;

        if(isCharging)
            result += 100000;

        if(connectionType == ConnectivityManager.TYPE_WIFI)
            result += 10000;
        if(connectionType == ConnectivityManager.TYPE_MOBILE)
            result += 20000;

        if(12 <= hour && hour <= 17)
            result += 1000;
        else if(17 <= hour)
            result += 2000;
        else if(hour <= 6)
            result += 3000;

        result += 100 * day;

        result += month;

        return result;
    }
}

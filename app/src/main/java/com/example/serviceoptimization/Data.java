package com.example.serviceoptimization;

import android.net.ConnectivityManager;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;

public class Data {
    private float batteryLevel;
    private int taskNumber;
    private boolean offloaded;
    private float batteryConsumption;
    private boolean isCharging;
    private boolean isNetworkConnected;
    private int connectionType;
    private int connectionSubType;
    private int hour;
    private int day;
    private int month;
    private long duration;

    public Data(State before, State after) {
        this.taskNumber = before.getTaskNumber();
        this.batteryLevel = before.getBatteryLevel();
        this.isCharging = before.isCharging();
        this.isNetworkConnected = before.isNetworkConnected();
        this.connectionType = before.getConnectionType();
        this.connectionSubType = before.getConnectionSubType();
        this.hour = before.getHour();
        this.day = before.getDay();
        this.month = before.getMonth();
        this.duration = after.getStartTimeMillis() - before.getStartTimeMillis();
        this.offloaded = before.isToOffload();
    }

    public long getDuration() {
        return duration;
    }

    public float getBatteryLevel() {
        return batteryLevel;
    }

    public Data asOffloaded() {
        this.offloaded = true;
        return this;
    }

    public Data asNotOffloaded() {
        this.offloaded = false;
        return this;
    }

    public float getBatteryConsumption() {
        return batteryConsumption;
    }

    public Instance toWekaInstance() {
        return toWekaInstance(1.0);
    }

    public Instance toWekaInstance(double weight) {
        double[] vals;

        vals = new double[9];
        vals[0] = offloaded ? 1 : 0;
        vals[1] = isCharging ? 1 : 0;
        vals[2] = isNetworkConnected ? 1 : 0;
        vals[3] = connectionType;
        vals[4] = connectionSubType;
        vals[5] = hour;
        vals[6] = day;
        vals[7] = month;
        vals[8] = duration;

        return new Instance(weight, vals);
    }

    public static FastVector getAttributes() {
        FastVector atts = new FastVector(9);
        atts.addElement(new Attribute("offloaded"));
        atts.addElement(new Attribute("isCharging"));
        atts.addElement(new Attribute("isNetworkConnected"));
        atts.addElement(new Attribute("connectionType"));
        atts.addElement(new Attribute("connectionSubType"));
        atts.addElement(new Attribute("hour"));
        atts.addElement(new Attribute("day"));
        atts.addElement(new Attribute("month"));
        atts.addElement(new Attribute("duration"));
        return atts;
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

        if(offloaded)
            result += 1000000;

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

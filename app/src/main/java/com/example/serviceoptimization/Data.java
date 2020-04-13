package com.example.serviceoptimization;

import java.util.ArrayList;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;

public class Data {
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
        this.batteryConsumption = before.getBatteryLevel() - after.getBatteryLevel();
        this.isCharging = before.isCharging();
        this.isNetworkConnected = before.isNetworkConnected();
        this.connectionType = before.getConnectionType();
        this.connectionSubType = before.getConnectionSubType();
        this.hour = before.getHour();
        this.day = before.getDay();
        this.month = before.getMonth();
        this.duration = after.getStartTimeMillis() - before.getStartTimeMillis();
    }

    public Instance toWekaInstance() {
        return toWekaInstance(1.0);
    }

    public Instance toWekaInstance(double weight) {
        double[]        vals;

        vals = new double[9];
        vals[0] = batteryConsumption;
        vals[1] = isCharging ? 1 : 0;
        vals[2] = isNetworkConnected ? 1 : 0;
        vals[3] = connectionType;
        vals[4] = connectionSubType;
        vals[5] = hour;
        vals[6] = day;
        vals[7] = month;
        vals[8] = duration;

        return new DenseInstance(weight, vals);
    }

    public static ArrayList<Attribute> getAttributes() {
        ArrayList<Attribute> atts;
        atts = new ArrayList<>(9);
        atts.add(new Attribute("batteryConsumption"));
        atts.add(new Attribute("isCharging"));
        atts.add(new Attribute("isNetworkConnected"));
        atts.add(new Attribute("connectionType"));
        atts.add(new Attribute("connectionSubType"));
        atts.add(new Attribute("hour"));
        atts.add(new Attribute("day"));
        atts.add(new Attribute("month"));
        atts.add(new Attribute("duration"));
        return atts;
    }
}

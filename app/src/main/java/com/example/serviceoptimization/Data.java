package com.example.serviceoptimization;

import weka.core.Attribute;
import weka.core.FastVector;
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
        double[] vals;

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

        return new Instance(weight, vals);
    }

    public static FastVector getAttributes() {
        FastVector atts = new FastVector(9);
        atts.addElement(new Attribute("batteryConsumption"));
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
}

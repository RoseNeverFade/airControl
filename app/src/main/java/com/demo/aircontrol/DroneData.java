package com.demo.aircontrol;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class DroneData {
    private static DroneData instance = new DroneData();
    public Double lngAnchor = 0d;
    public Double latAnchor = 0d;
    private String dataDir;
    private int lineNum;
    private ArrayList<Date> gpsTime;
    private ArrayList<Double> gpsLng;
    private ArrayList<Double> gpsLat;
    private ArrayList<Double> gpsAlt;
    private ArrayList<Double> gpsYaw;   //偏航角
    private ArrayList<Double> gpsPitch; //俯仰角
    private ArrayList<Double> gpsRoll;  //横滚角
    private ArrayList<Double> deltaLng;
    private ArrayList<Double> deltaLat;
    private ArrayList<Double> deltaDis;
    private ArrayList<Double> deltaAlt;
    private ArrayList<Double> deltaYaw;   //偏航角


    //TODO: normalization gpsLng,gpsLat,gpsAlt FOR BETTER GRAPH
    private ArrayList<Double> deltaPitch; //俯仰角
    private ArrayList<Double> deltaRoll;  //横滚角
    private DateFormat format;

    private Context context;

    //单例模式，保证全局只有一个DroneData
    private DroneData() {
        initData();
    }

    public static DroneData getInstance() {
        return instance;
    }

    public void setContext(Context context) {
        this.context = context;
        loadGPSData();
    }

    private void initData() {
        gpsTime = new ArrayList<Date>();
        gpsLng = new ArrayList<Double>();
        gpsLat = new ArrayList<Double>();
        gpsAlt = new ArrayList<Double>();
        gpsYaw = new ArrayList<Double>();
        gpsPitch = new ArrayList<Double>();
        gpsRoll = new ArrayList<Double>();

        deltaLng = new ArrayList<Double>();
        deltaLat = new ArrayList<Double>();
        deltaDis = new ArrayList<Double>();
        deltaAlt = new ArrayList<Double>();
        deltaYaw = new ArrayList<Double>();
        deltaPitch = new ArrayList<Double>();
        deltaRoll = new ArrayList<Double>();
        lineNum = 0;
//        dataDir = ".\\testdata.txt";
        format = new SimpleDateFormat("yyyy_MM_dd-hh:mm:ss");
    }

    private void loadGPSData() {
        initData();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(R.raw.testdata)));
            String str = "";
            if ((str = in.readLine()) == null) {
                //TODO: 异常处理
                return;
            }
            int lines = Integer.parseInt(str);
            if (lines == lineNum) {
                return;
            }
            in.readLine();
            while ((str = in.readLine()) != null) {
                clipGPSData(str);
            }
            calculateDelta();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void calculateDelta() {
        int size = gpsAlt.size();
        for (int i = 0; i < size - 1; i++) {
            double deltaln = 0d, deltaLa = 0d;
            deltaln = gpsLng.get(i + 1) - gpsLng.get(i);
            deltaLa = gpsLat.get(i + 1) - gpsLat.get(i);
            deltaLng.add(deltaln);
            deltaLat.add(deltaLa);
            deltaDis.add(Math.sqrt(deltaln * deltaln + deltaLa * deltaLa));
            deltaAlt.add((gpsAlt.get(i + 1) - gpsAlt.get(i)));
            deltaYaw.add((gpsYaw.get(i + 1) - gpsYaw.get(i)));
            deltaPitch.add((gpsPitch.get(i + 1) - gpsPitch.get(i)));
            deltaRoll.add((gpsRoll.get(i + 1) - gpsRoll.get(i)));
        }
    }

    private void clipGPSData(String str) {
        String[] arr = str.split("\\s+", 7);
        if (arr.length != 7) {
            throw new RuntimeException("Data Error!Please check 'gps_log'file!");
        }
        Date date = null;
        try {
            date = format.parse(arr[0]);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        gpsTime.add(date);
        gpsLng.add(Double.parseDouble(arr[1]));
        gpsLat.add(Double.parseDouble(arr[2]));
        gpsAlt.add(Double.parseDouble(arr[3]));
        gpsYaw.add(Double.parseDouble(arr[4]));
        gpsPitch.add(Double.parseDouble(arr[5]));
        gpsRoll.add(Double.parseDouble(arr[6]));
    }

    public void refreshGPSData() {
        loadGPSData();
    }

    public void setDir(String dataDir) {
        this.dataDir = dataDir;
    }

    public ArrayList<Date> getGpsTime() {
        return gpsTime;
    }

    public ArrayList<Double> getGpsLng() {
        lngAnchor = gpsLng.get(0);
        return gpsLng;
    }

    public ArrayList<Double> getGpsLat() {
        latAnchor = gpsLat.get(0);
        return gpsLat;
    }

    public ArrayList<Double> getGpsAlt() {
        return gpsAlt;
    }

    public ArrayList<Double> getGpsYaw() {
        return gpsYaw;
    }

    public ArrayList<Double> getGpsPitch() {
        return gpsPitch;
    }

    public ArrayList<Double> getGpsRoll() {
        return gpsRoll;
    }

    public ArrayList<Double> getDeltaLng() {
        return deltaLng;
    }

    public ArrayList<Double> getDeltaLat() {
        return deltaLat;
    }

    public ArrayList<Double> getDeltaDis() {
        return deltaDis;
    }

    public ArrayList<Double> getDeltaAlt() {
        return deltaAlt;
    }

    public ArrayList<Double> getDeltaYaw() {
        return deltaYaw;
    }

    public ArrayList<Double> getDeltaPitch() {
        return deltaPitch;
    }

    public ArrayList<Double> getDeltaRoll() {
        return deltaRoll;
    }
}
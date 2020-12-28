package com.demo.aircontrol;

import java.util.ArrayList;

public class MissionParams {
    MWaypointMission mgotostartpoint;
    int missiontype;
    MWaypointMission mwaypointmission;
    MHotpointMission mhotpointmission;
    MWaypointMission mgohome;

    MissionParams(String missionparams){
        String[] missions = missionparams.split(";");
        mgotostartpoint = new MWaypointMission(missions[1]);
        if (missions[2].contains("way")) {
            missiontype = 1;   // 航点飞行
            mwaypointmission = new MWaypointMission(missions[2]);
        }
        else if (missions[2].contains("hot")) {
            missiontype = 2;  // 圆形绕飞
            mhotpointmission = new MHotpointMission(missions[2]);
        }
        mgohome = new MWaypointMission(missions[3]);
    }
}

class MWaypointMission{
    float vel;
    int size;
    ArrayList<MWaypoint> mwaypointlist;

    MWaypointMission(){}

    MWaypointMission(String waymission){
        String[] s = waymission.split(",");
        size = Integer.parseInt(s[1]);
        vel = Float.parseFloat(s[2]);

        mwaypointlist = new ArrayList<>();
        for (int i=0; i<size; i++){
            mwaypointlist.add(new MWaypoint(Double.parseDouble(s[i*4+4]), Double.parseDouble(s[i*4+3]), Float.parseFloat(s[i*4+5]), Integer.parseInt(s[i*4+6])));
        }
    }
}

class MWaypoint{
    double lat;
    double lng;
    float alt;
    int staytime;
    MWaypoint(double clat, double clng, float calt, int cstaytime){
        lat = clat;
        lng = clng;
        alt = calt;
        staytime = cstaytime;
    }

}

class MHotpointMission{
    double lng;
    double lat;
    double alt;
    double hotr;
    float hotw;
    float hotangle;
    String hotstart;
    MHotpointMission(String hotmission){
        String[] s = hotmission.split(",");
        lng = Double.parseDouble(s[1]);
        lat = Double.parseDouble(s[2]);
        alt = Float.parseFloat(s[3]);
        hotr = Double.parseDouble(s[4]);
        hotw = Float.parseFloat(s[5]);
        hotstart = s[6];
        hotangle = Float.parseFloat(s[7]);
    }
}

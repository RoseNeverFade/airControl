package com.demo.aircontrol;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.TextView;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

import java.util.ArrayList;
import java.util.Date;

@SuppressLint("ViewConstructor")
public class ChartsMarkerView extends MarkerView {

    private final TextView tvContent;
    private ArrayList<Double> trueData;
    private ArrayList<Date> trueTime;

    private int mode;

    public ChartsMarkerView(Context context, int layoutResource) {
        this(context, layoutResource, 3);
    }

    public ChartsMarkerView(Context context, int layoutResource, int mode) {
        super(context, layoutResource);

        tvContent = findViewById(R.id.tvContent);
        trueTime = DroneData.getInstance().getGpsTime();
        this.mode = mode;
        getTrueData(mode);
    }

    private void getTrueData(int mode) {
        switch (mode) {
            case 0:
                trueData = DroneData.getInstance().getGpsLng();
                break;
            case 1:
                trueData = DroneData.getInstance().getGpsLat();
                break;
            case 2:
                trueData = DroneData.getInstance().getGpsAlt();
                break;
            case 3:
                break;
            default://TODO:ADD modes
        }
    }

    // runs every time the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        int i = (int) e.getX();
        if (mode == 3) {
            String content = "Data: " + ((Float) e.getY()).toString() + "\nTime: " + trueTime.get(i).toLocaleString();
            tvContent.setText(content);
            super.refreshContent(e, highlight);
        } else if (mode == 4) {
            String content = "Yaw: " + DroneData.getInstance().getGpsYaw().get(i).toString()
                    + " Pitch: " + DroneData.getInstance().getGpsPitch().get(i).toString()
                    + " Roll: " + DroneData.getInstance().getGpsRoll().get(i).toString()
                    + "\nTime: " + trueTime.get(i).toLocaleString();
            tvContent.setText(content);
            super.refreshContent(e, highlight);
        } else {
            String content = "Data: " + trueData.get(i).toString() + "\nTime: " + trueTime.get(i).toLocaleString();
            tvContent.setText(content);
            super.refreshContent(e, highlight);
        }
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2), -getHeight());
    }
}

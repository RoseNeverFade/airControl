package com.demo.aircontrol.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.demo.aircontrol.ChartsMarkerView;
import com.demo.aircontrol.DroneData;
import com.demo.aircontrol.R;
import com.demo.aircontrol.ValueOffsetFormatter;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class LatFrag extends Fragment {

    private DroneData droneData;
    private Context context;

    @NonNull
    public static Fragment newInstance() {
        return new LatFrag();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_lat, container, false);

        droneData = DroneData.getInstance();
        LineChart chart = v.findViewById(R.id.chart_lat);

        List<Entry> entries = new ArrayList<Entry>();
        // turn your data into Entry objects
        ArrayList<Double> latData = droneData.getGpsLat();
        int i = 0;
        for (double d : latData) {
            entries.add(new Entry(i, (float) (d - droneData.latAnchor)));
            i++;
        }
        LineDataSet dataSet = new LineDataSet(entries, "纬度"); // add entries to dataset
        dataSet.setCircleColor(Color.BLACK);
        dataSet.setCircleRadius(1f);
        LineData lineData = new LineData(dataSet);

        //add MarkerView
        ChartsMarkerView mv = new ChartsMarkerView(context, R.layout.custom_marker_view, 1);
        mv.setChartView(chart);
        chart.setMarker(mv);

        //Change Yaxis
        chart.getAxisLeft().setValueFormatter(new ValueOffsetFormatter(droneData.latAnchor));
        chart.getAxisRight().setEnabled(false);

        chart.getDescription().setText("无人机纬度随时间的变化曲线");
//        dataSet.setColor(...);
//        dataSet.setValueTextColor(...);

        chart.setData(lineData);
        chart.invalidate();

        return v;
    }

}

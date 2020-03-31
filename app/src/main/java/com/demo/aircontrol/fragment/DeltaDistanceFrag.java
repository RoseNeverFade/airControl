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
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class DeltaDistanceFrag extends Fragment {

    private DroneData droneData;
    private Context context;

    @NonNull
    public static Fragment newInstance() {
        return new DeltaDistanceFrag();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_delta_dis, container, false);
        LineChart chart = v.findViewById(R.id.chart_delta_dis);

        droneData = DroneData.getInstance();
        List<Entry> entries = new ArrayList<Entry>();
        // turn your data into Entry objects
        ArrayList<Double> lngData = droneData.getDeltaDis();
        int i = 0;
        for (double d : lngData) {
            entries.add(new Entry(i, (float) (d)));
            i++;
        }
        LineDataSet dataSet = new LineDataSet(entries, "距离差"); // add entries to dataset
        dataSet.setCircleColor(Color.BLACK);
        dataSet.setCircleRadius(1f);
        LineData lineData = new LineData(dataSet);

        //add MarkerView
        ChartsMarkerView mv = new ChartsMarkerView(context, R.layout.custom_marker_view);
        mv.setChartView(chart);
        chart.setMarker(mv);

        //Change Yaxis
//        chart.getAxisLeft().setValueFormatter(new ValueOffsetFormatter(droneData.lngAnchor));
        chart.getAxisRight().setEnabled(false);

        chart.getDescription().setText("飞行距离曲线");
//        dataSet.setColor(...);
//        dataSet.setValueTextColor(...);

        chart.setData(lineData);
        chart.invalidate();

        return v;
    }

}

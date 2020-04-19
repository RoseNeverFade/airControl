package com.demo.aircontrol.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.demo.aircontrol.DroneData;
import com.demo.aircontrol.R;
import com.demo.aircontrol.ui.util.ChartsMarkerView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.List;

public class AttitudeFrag extends Fragment {

    private DroneData droneData;
    private Context context;

    @NonNull
    public static Fragment newInstance() {
        return new AttitudeFrag();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_attitude, container, false);
        LineChart chart = v.findViewById(R.id.chart_attitude);

        droneData = DroneData.getInstance();
        List<Entry> yawEntries = new ArrayList<Entry>();
        List<Entry> pitchEntries = new ArrayList<Entry>();
        List<Entry> rollEntries = new ArrayList<Entry>();
        // turn your data into Entry objects
        ArrayList<Double> yawData = droneData.getGpsYaw();
        ArrayList<Double> pitchData = droneData.getGpsPitch();
        ArrayList<Double> rollData = droneData.getGpsRoll();
        int size = yawData.size();
        for (int i = 0; i < size; i++) {
            yawEntries.add(new Entry(i, yawData.get(i).floatValue()));
            pitchEntries.add(new Entry(i, pitchData.get(i).floatValue()));
            rollEntries.add(new Entry(i, rollData.get(i).floatValue()));
        }
        LineDataSet dataSet1 = new LineDataSet(yawEntries, "偏航角"); // add entries to dataset
        LineDataSet dataSet2 = new LineDataSet(pitchEntries, "俯仰角"); // add entries to dataset
        LineDataSet dataSet3 = new LineDataSet(rollEntries, "横滚角"); // add entries to dataset

        dataSet1.setColor(Color.LTGRAY);
        dataSet2.setColor(Color.CYAN);
        dataSet3.setColor(Color.GRAY);

        dataSet1.setCircleColor(Color.GREEN);
        dataSet1.setCircleRadius(1f);
        dataSet2.setCircleColor(Color.BLUE);
        dataSet2.setCircleRadius(1f);
        dataSet3.setCircleColor(Color.RED);
        dataSet3.setCircleRadius(1f);

        List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(dataSet1);
        dataSets.add(dataSet2);
        dataSets.add(dataSet3);
        LineData lineData = new LineData(dataSets);

        //add MarkerView
        ChartsMarkerView mv = new ChartsMarkerView(context, R.layout.custom_marker_view, 4);
        mv.setChartView(chart);
        chart.setMarker(mv);

        //Change Yaxis
//        chart.getAxisLeft().setValueFormatter(new ValueOffsetFormatter(droneData.lngAnchor));
        chart.getAxisRight().setEnabled(false);

        chart.getDescription().setText("无人机姿态角变化曲线");
//        dataSet.setColor(...);
//        dataSet.setValueTextColor(...);

        chart.setData(lineData);
        chart.invalidate();

        return v;
    }

}

package com.demo.aircontrol.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Scatter;
import com.anychart.core.scatter.series.Line;
import com.anychart.core.scatter.series.Marker;
import com.anychart.enums.MarkerType;
import com.anychart.enums.TooltipDisplayMode;
import com.anychart.graphics.vector.SolidFill;
import com.anychart.graphics.vector.text.HAlign;
import com.demo.aircontrol.DroneData;
import com.demo.aircontrol.R;
import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.List;

public class RoadMapFrag extends Fragment {

    private DroneData droneData;
    private Context context;

    @NonNull
    public static Fragment newInstance() {
        return new RoadMapFrag();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_roadmap, container, false);
//
//        droneData = DroneData.getInstance();
//        ScatterChart chart = v.findViewById(R.id.chart_roadmap);
//
//        List<Entry> entries = new ArrayList<Entry>();
//        // turn your data into Entry objects
//        ArrayList<Double> lngData = droneData.getGpsLng();
//        ArrayList<Double> latData = droneData.getGpsLat();
//        int i;
//        for(i=0;i<lngData.size();i++){
//            entries.add(new Entry((float)(lngData.get(i)-droneData.lngAnchor), (float)(latData.get(i)-droneData.latAnchor)));
//        }
//        ScatterDataSet dataSet = new ScatterDataSet(entries, "无人机1"); // add entries to dataset
//        ScatterData scatterData = new ScatterData(dataSet);
//
//        //add MarkerView
//        ChartsMarkerView mv = new ChartsMarkerView(context, R.layout.custom_marker_view,3);
//        mv.setChartView(chart);
//        chart.setMarker(mv);
//
//        //Change Yaxis
//        chart.getAxisLeft().setValueFormatter(new ValueOffsetFormatter(droneData.latAnchor));
//        chart.getXAxis().setValueFormatter(new ValueOffsetFormatter(droneData.lngAnchor));
//
//
//        chart.getDescription().setText("无人机路径图");
////        dataSet.setColor(...);
////        dataSet.setValueTextColor(...);
//
//        chart.setData(scatterData);
//        chart.invalidate();
//
//        return v;

        AnyChartView anyChartView = v.findViewById(R.id.any_chart_view);
        anyChartView.setProgressBar(v.findViewById(R.id.progress_bar));

        Scatter scatter = AnyChart.scatter();

        scatter.animation(true);

        scatter.title("路径图");

//        scatter.xScale()
//                .minimum(1.5d)
//                .maximum(5.5d);
//
//        scatter.yScale()
//                .minimum(40d)
//                .maximum(100d);

        scatter.yAxis(0).title("纬度");
        scatter.xAxis(0)
                .title("经度")
                .drawFirstLabel(false)
                .drawLastLabel(false);

//        scatter.interactivity()
//                .hoverMode(HoverMode.BY_SPOT)
//                .spotRadius(30d);

        scatter.tooltip().displayMode(TooltipDisplayMode.UNION);


        List<DataEntry> markerData = getMarkerData();
        Marker marker = scatter.marker(markerData);
        marker.type(MarkerType.CIRCLE)
                .size(1d);
        marker.hovered()
                .size(1d)
                .fill(new SolidFill("gold", 1d))
                .stroke("anychart.color.darken(gold)");
        marker.tooltip()
                .title(false)
//                .titleFormat("时间: {%time}")
                .hAlign(HAlign.START)
                .format("经度: {%Value}\\n纬度: {%X}");


        Line line = scatter.line(markerData);


//        marker.color()

//        GradientKey gradientKey[] = new GradientKey[] {
//                new GradientKey("#abcabc", 0d, 1d),
//                new GradientKey("#cbacba", 40d, 1d)
//        };
//        LinearGradientStroke linearGradientStroke = new LinearGradientStroke(0d, null, gradientKey, null, null, true, 1d, 2d);
        //AnyChart.scatter().xScale();

        anyChartView.setChart(scatter);
        return v;
    }

    private List<DataEntry> getMarkerData() {
        //        RangeColors rangeColors =RangeColors.instantiate();
//        rangeColors.items("#ff0000","#00ff00");
//        rangeColors.count(10);

        List<DataEntry> data = new ArrayList<>();
        droneData = DroneData.getInstance();
        List<Entry> entries = new ArrayList<Entry>();
        // turn your data into Entry objects
        ArrayList<Double> lngData = droneData.getGpsLng();
        ArrayList<Double> latData = droneData.getGpsLat();
//        ArrayList<Date> timeData = droneData.getGpsTime();
        int i;
        int size = lngData.size();
        for (i = 0; i < size; i++) {
            data.add(new CustomDataEntry(lngData.get(i), latData.get(i)));
        }
        return data;
    }

//    private String getColor(int i,int size){
//        String [] colors = {"#CCCCFF",
//                "#BEBEF6",
//                "#B0B0EC",
//                "#A2A2E3",
//                "#9494DA",
//                "#8686D1",
//                "#7979C7",
//                "#6B6BBE",
//                "#5D5DB5",
//                "#4F4FAC",
//                "#4141A2",
//                "#333399"};
//        int num = colors.length;
//
//        return colors[i*num/size];
//    }

    private class CustomDataEntry extends ValueDataEntry {
        CustomDataEntry(double x, double y /*, String color*/) {
            super(x, y);
//            setValue("color", color);
        }
    }
}

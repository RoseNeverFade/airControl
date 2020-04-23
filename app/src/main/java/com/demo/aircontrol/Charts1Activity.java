package com.demo.aircontrol;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.androidplot.Plot;
import com.androidplot.util.PixelUtils;
import com.androidplot.xy.*;

import java.text.DecimalFormat;
import java.util.Observable;
import java.util.Observer;

public class Charts1Activity extends AppCompatActivity {


    SampleDynamicXYDatasource data;
    SampleDynamicSeries drone1Series;
    ScatterSeries drone1Scatters;
    ScatterTop scatterTop;
    private XYPlot dynamicPlot;
    private XYPlot scatterPlot;
    private MyPlotUpdater plotUpdater;
    private Thread myThread;
    private Button buttonPlay;
    private Button buttonPause;
    private Button buttonReplay;
    private DroneData droneData;
    private TextView text_alt;
    private TextView text_time;

    private TextView text_yaw;
    private TextView text_pitch;
    private TextView text_roll;
    private TextView text_x;
    private TextView text_y;

    private Handler textUpdateHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            updateText();
            return false;
        }
    });
    private boolean replay = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charts1);

        droneData = DroneData.getInstance();

        text_alt = findViewById(R.id.text_alt);
        text_time = findViewById(R.id.text_time);
        text_yaw = findViewById(R.id.tt1);
        text_pitch = findViewById(R.id.tt2);
        text_roll = findViewById(R.id.tt3);
        text_x = findViewById(R.id.tt4);
        text_y = findViewById(R.id.tt5);

        buttonPlay = findViewById(R.id.button_play);
        buttonPause = findViewById(R.id.button_pause);
        buttonReplay = findViewById(R.id.button_replay);

        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                play();
            }
        });

        buttonPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pause();
            }
        });

        buttonReplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                replay();
            }
        });

        // initialize our XYPlot reference:
        dynamicPlot = (XYPlot) findViewById(R.id.plot);
        scatterPlot = (XYPlot) findViewById(R.id.scatter);

        plotUpdater = new MyPlotUpdater(dynamicPlot, scatterPlot);

        dataInit();

//        LineAndPointFormatter formatter2 =
//                new LineAndPointFormatter(Color.rgb(0, 0, 200), null, null, null);
//        formatter2.getLinePaint().setStrokeWidth(10);
//        formatter2.getLinePaint().setStrokeJoin(Paint.Join.ROUND);

        //formatter2.getFillPaint().setAlpha(220);
//        dynamicPlot.addSeries(sine2Series, formatter2);

        int upperBoundary = DroneData.calcMax(droneData.getGpsAlt()).intValue() + 1;

        int xMax = DroneData.calcMax(droneData.getGpsLat()).intValue() + 1;
        int xMin = DroneData.calcMin(droneData.getGpsLat()).intValue() - 1;
        int yMax = DroneData.calcMax(droneData.getGpsLng()).intValue() + 1;
        int yMin = DroneData.calcMin(droneData.getGpsLng()).intValue() - 1;

        //int lowerBoundary =DroneData.calcMin(droneData.getGpsAlt()).intValue()- 1;

        // thin out domain tick labels so they dont overlap each other:
        dynamicPlot.setDomainStepMode(StepMode.INCREMENT_BY_VAL);
        dynamicPlot.setDomainStepValue(5);//TODO:

        dynamicPlot.setRangeStepMode(StepMode.INCREMENT_BY_FIT);
//        dynamicPlot.setRangeStepValue(calcRangeStep(upperBoundary,6));
        //dynamicPlot.setRangeStepValue(5);

        dynamicPlot.getGraph().getLineLabelStyle(
                XYGraphWidget.Edge.LEFT).setFormat(new DecimalFormat("###.#"));


        // uncomment this line to freeze the range boundaries:
        dynamicPlot.setRangeBoundaries(0, upperBoundary, BoundaryMode.FIXED);
        dynamicPlot.setDomainBoundaries(0, SampleDynamicXYDatasource.WINDOW_SIZE, BoundaryMode.FIXED);
        //dynamicPlot.setRangeBoundaries(0, 20, BoundaryMode.FIXED);


        scatterPlot.setDomainBoundaries(xMin, xMax, BoundaryMode.FIXED);
        scatterPlot.setRangeBoundaries(yMin, yMax, BoundaryMode.FIXED);


        // create a dash effect for domain and range grid lines:
        DashPathEffect dashFx = new DashPathEffect(
                new float[]{PixelUtils.dpToPix(3), PixelUtils.dpToPix(3)}, 0);
        dynamicPlot.getGraph().getDomainGridLinePaint().setPathEffect(dashFx);
        dynamicPlot.getGraph().getRangeGridLinePaint().setPathEffect(dashFx);
    }

    private void dataInit() {
        data = new SampleDynamicXYDatasource();
        data.addObserver(plotUpdater);

        //===============XY Plot===============
        dynamicPlot.clear();

        // only display whole numbers in domain labels
        dynamicPlot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).
                setFormat(new DecimalFormat("0"));

        // getInstance and position datasets:

        drone1Series = new SampleDynamicSeries(data, 0, "Drone 1");
//        SampleDynamicSeries sine2Series = new SampleDynamicSeries(data, 1, "Drone 2");

        LineAndPointFormatter formatter1 = new LineAndPointFormatter(
                Color.rgb(0, 200, 0), null, null, null);
        formatter1.getLinePaint().setStrokeJoin(Paint.Join.ROUND);
        formatter1.getLinePaint().setStrokeWidth(3);

        dynamicPlot.addSeries(drone1Series, formatter1);

        // hook up the plotUpdater to the data model:


        //===============Scatter Plot===============
        scatterPlot.clear();

        drone1Scatters = new ScatterSeries(data, 0, "Drone 1");
        LineAndPointFormatter formatter2 =
                new LineAndPointFormatter(this, R.xml.point_formatter);
        scatterPlot.addSeries(drone1Scatters, formatter2);

        //===============Scatter Top================
        scatterTop = new ScatterTop(data, 0, "Drone 1");
        LineAndPointFormatter formatter_top =
                new LineAndPointFormatter(this, R.xml.point_formatter_2);
        scatterPlot.addSeries(scatterTop, formatter_top);

    }

    @SuppressLint("SetTextI18n")
    void updateText() {
        text_alt.setText(droneData.getGpsAlt().get(data.counts - 1).toString());
        text_time.setText(droneData.df.format(droneData.getGpsTime().get(data.counts - 1)));
        text_yaw.setText(droneData.getGpsYaw().get(data.counts - 1).toString());
        text_pitch.setText(droneData.getGpsPitch().get(data.counts - 1).toString());
        text_roll.setText(droneData.getGpsRoll().get(data.counts - 1).toString());
        text_x.setText(droneData.getGpsLat().get(data.counts - 1).toString());
        text_y.setText(droneData.getGpsLng().get(data.counts - 1).toString());
    }

    public int calcRangeStep(int maxVal, int stepNum) {
        //eg: maxval/stepnum = 321 , returns 400
        if (stepNum == 0) {
            return 100;
        }
        maxVal /= stepNum;
        int i = 0;
        int temp = 1;
        while (maxVal > 0) {
            temp = maxVal;
            maxVal /= 10;
            i++;
        }
        temp += 1;
        i--;
        while (i > 0) {
            temp *= 10;
            i--;
        }
        return temp;
    }

    public void play() {
        if (replay) {
            replay = false;
            //refreshData();
            dataInit();
        }
        // kick off the data generating thread:
        myThread = new Thread(data);
        myThread.start();
    }

    public void pause() {
        data.stopThread();
    }

    public void replay() {
        data.stopThread();
        replay = true;
    }

    public void refreshData() {

    }

//    @Override
//    public void onResume() {
//        super.onResume();
//    }

    @Override
    public void onPause() {
        data.stopThread();
        super.onPause();
    }

    private class MyPlotUpdater implements Observer {
        Plot xyPlot;
        Plot scatterPlot;

        public MyPlotUpdater(Plot xyPlot, Plot scatterPlot) {
            this.xyPlot = xyPlot;
            this.scatterPlot = scatterPlot;
        }

        @Override
        public void update(Observable o, Object arg) {
            xyPlot.redraw();
            scatterPlot.redraw();
        }
    }

    class SampleDynamicXYDatasource implements Runnable {

        static final int DRONE1 = 0;
        static final int DRONE2 = 1;
        static final int DRONE3 = 2;
        private static final long SLEEPTIME = 80; // update data per SLEEPTIME ms
        private static final int WINDOW_SIZE = 40;
        public int counts = 0;
        private int xySize = 0;
        private int phase = 0;
        private int sinAmp = 1;
        private MyObservable notifier;
        private boolean keepRunning = false;
        private int maxSize = 0;

        {
            notifier = new MyObservable();
        }

        SampleDynamicXYDatasource() {
            maxSize = droneData.getGpsAlt().size();
        }

        void stopThread() {
            keepRunning = false;
        }

        //@Override
        public void run() {
            try {
                keepRunning = true;
                boolean isRising = true;
                while (keepRunning) {
                    Thread.sleep(SLEEPTIME); // decrease or remove to speed up the refresh rate.
                    counts++;
                    if (counts > maxSize) {
                        stopThread();
                        continue;
                    }

                    textUpdateHandler.sendMessage(new Message());

                    notifier.notifyObservers();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        int getHistoryItemCount(int series) {
            return (counts > WINDOW_SIZE) ? WINDOW_SIZE : counts;
        }

        int getXYItemCount(int series) {
            return counts;
        }

        Number getX(int series, int index) {
            if (index >= counts) {
                throw new IllegalArgumentException();
            }
            return droneData.getGpsLat().get(index); //TODO:
        }

        Number getY(int series, int index) {
            if (index >= counts) {
                throw new IllegalArgumentException();
            }
            return droneData.getGpsLng().get(index);//TODO:
        }

        Number getTopX(int series) {
            return droneData.getGpsLat().get(counts - 1); //TODO:
        }

        Number getTopY(int series) {
            return droneData.getGpsLng().get(counts - 1); //TODO:
        }


        Number getTime(int series, int index) {
            if (index >= WINDOW_SIZE) {
                throw new IllegalArgumentException();
            }
            return index;
        }

        Number getAlt(int series, int index) {
            if (index >= WINDOW_SIZE) {
                throw new IllegalArgumentException();
            }
            switch (series) {
                case DRONE1:
                    return droneData.getGpsAlt().get((counts > WINDOW_SIZE) ? (counts - WINDOW_SIZE + index) : (index));
                case DRONE2:
                    return 0;
                default:
                    throw new IllegalArgumentException();
            }
        }

        void addObserver(Observer observer) {
            notifier.addObserver(observer);
        }

        public void removeObserver(Observer observer) {
            notifier.deleteObserver(observer);
        }

        // encapsulates management of the observers watching this datasource for update events:
        class MyObservable extends Observable {
            @Override
            public void notifyObservers() {
                setChanged();
                super.notifyObservers();
            }
        }

    }

    class SampleDynamicSeries implements XYSeries {
        private SampleDynamicXYDatasource datasource;
        private int seriesIndex;
        private String title;

        SampleDynamicSeries(SampleDynamicXYDatasource datasource, int seriesIndex, String title) {
            this.datasource = datasource;
            this.seriesIndex = seriesIndex;
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public int size() {
            return datasource.getHistoryItemCount(seriesIndex);
        }

        @Override
        public Number getX(int index) {
            return datasource.getTime(seriesIndex, index);
        }

        @Override
        public Number getY(int index) {
            return datasource.getAlt(seriesIndex, index);
        }
    }

    class ScatterSeries implements XYSeries {
        private SampleDynamicXYDatasource datasource;
        private int seriesIndex;
        private String title;

        ScatterSeries(SampleDynamicXYDatasource datasource, int seriesIndex, String title) {
            this.datasource = datasource;
            this.seriesIndex = seriesIndex;
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public int size() {
            return datasource.getXYItemCount(seriesIndex);
        }

        @Override
        public Number getX(int index) {
            return datasource.getX(seriesIndex, index);
        }

        @Override
        public Number getY(int index) {
            return datasource.getY(seriesIndex, index);
        }
    }

    class ScatterTop implements XYSeries {
        private SampleDynamicXYDatasource datasource;
        private int seriesIndex;
        private String title;

        ScatterTop(SampleDynamicXYDatasource datasource, int seriesIndex, String title) {
            this.datasource = datasource;
            this.seriesIndex = seriesIndex;
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public Number getX(int index) {
            return datasource.getTopX(seriesIndex);
        }

        @Override
        public Number getY(int index) {
            return datasource.getTopY(seriesIndex);
        }
    }
}
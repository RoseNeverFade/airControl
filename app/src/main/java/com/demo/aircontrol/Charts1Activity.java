package com.demo.aircontrol;

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
    SampleDynamicSeries sine1Series;
    private XYPlot dynamicPlot;
    private MyPlotUpdater plotUpdater;
    private Thread myThread;
    private Button buttonPlay;
    private Button buttonPause;
    private Button buttonReplay;
    private DroneData droneData;
    private TextView text_alt;
    private TextView text_time;
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

        plotUpdater = new MyPlotUpdater(dynamicPlot);

        dataInit();

//        LineAndPointFormatter formatter2 =
//                new LineAndPointFormatter(Color.rgb(0, 0, 200), null, null, null);
//        formatter2.getLinePaint().setStrokeWidth(10);
//        formatter2.getLinePaint().setStrokeJoin(Paint.Join.ROUND);

        //formatter2.getFillPaint().setAlpha(220);
//        dynamicPlot.addSeries(sine2Series, formatter2);

        int upperBoundary = DroneData.calcMax(droneData.getGpsAlt()).intValue() + 1;
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

        // create a dash effect for domain and range grid lines:
        DashPathEffect dashFx = new DashPathEffect(
                new float[]{PixelUtils.dpToPix(3), PixelUtils.dpToPix(3)}, 0);
        dynamicPlot.getGraph().getDomainGridLinePaint().setPathEffect(dashFx);
        dynamicPlot.getGraph().getRangeGridLinePaint().setPathEffect(dashFx);
    }

    private void dataInit() {
        dynamicPlot.clear();

        // only display whole numbers in domain labels
        dynamicPlot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).
                setFormat(new DecimalFormat("0"));

        // getInstance and position datasets:
        data = new SampleDynamicXYDatasource();
        sine1Series = new SampleDynamicSeries(data, 0, "Drone 1");
//        SampleDynamicSeries sine2Series = new SampleDynamicSeries(data, 1, "Drone 2");

        LineAndPointFormatter formatter1 = new LineAndPointFormatter(
                Color.rgb(0, 200, 0), null, null, null);
        formatter1.getLinePaint().setStrokeJoin(Paint.Join.ROUND);
        formatter1.getLinePaint().setStrokeWidth(3);
        dynamicPlot.addSeries(sine1Series,
                formatter1);

        // hook up the plotUpdater to the data model:
        data.addObserver(plotUpdater);
    }

    void updateText() {
        text_alt.setText(droneData.getGpsAlt().get(data.counts - 1).toString());

        text_time.setText(droneData.df.format(droneData.getGpsTime().get(data.counts - 1)));
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
            refreshData();
            dataInit();
            replay = false;
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

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        data.stopThread();
        super.onPause();
    }

    private class MyPlotUpdater implements Observer {
        Plot plot;

        public MyPlotUpdater(Plot plot) {
            this.plot = plot;
        }

        @Override
        public void update(Observable o, Object arg) {
            plot.redraw();
        }
    }

    class SampleDynamicXYDatasource implements Runnable {

        static final int DRONE1 = 0;
        static final int DRONE2 = 1;
        static final int DRONE3 = 2;
        private static final double FREQUENCY = 50; // larger is lower frequency
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
                    Thread.sleep(20); // decrease or remove to speed up the refresh rate.
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

        int getXYItemCount(int series) {
            return (counts > WINDOW_SIZE) ? WINDOW_SIZE : counts;
        }

        Number getX(int series, int index) {
            if (index >= WINDOW_SIZE) {
                throw new IllegalArgumentException();
            }
            return index;
        }

        Number getY(int series, int index) {
            if (index >= WINDOW_SIZE) {
//                return 0;
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
}
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
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class Charts1Activity extends AppCompatActivity {


    MyXYDatasource data;
    DynamicSeries drone1Series;
    ScatterSeries drone1Scatters;
    DroneIcon droneIcon;
    private XYPlot dynamicPlot;
    private XYPlot scatterPlot;
    private MyPlotUpdater plotUpdater;
    private Thread myThread;
    private Button buttonPlay;
    private Button buttonPause;
    private Button buttonReplay;
    private DroneData droneData;
    private double squareLen = 0;

    private TextView text_alt;
    private TextView text_time;
    private TextView text_yaw;
    private TextView text_pitch;
    private TextView text_roll;
    private TextView text_x;
    private TextView text_y;

    private static double xyScale = 0.762; //yRange:xRange
    private double chartXRange;
    private double chartYRange;
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

        double yMax = DroneData.calcMax(droneData.getGpsLat()).doubleValue();
        double yMin = DroneData.calcMin(droneData.getGpsLat()).doubleValue();
        double xMax = DroneData.calcMax(droneData.getGpsLng()).doubleValue();
        double xMin = DroneData.calcMin(droneData.getGpsLng()).doubleValue();

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
        dynamicPlot.setDomainBoundaries(0, MyXYDatasource.WINDOW_SIZE, BoundaryMode.FIXED);
        //dynamicPlot.setRangeBoundaries(0, 20, BoundaryMode.FIXED);


//        scatterPlot.setDomainStepMode(StepMode.INCREMENT_BY_FIT);
////        scatterPlot.setDomainStepValue(40);//TODO:
//        scatterPlot.setRangeStepMode(StepMode.INCREMENT_BY_FIT);
////        scatterPlot.setRangeStepValue(40);
//
//
//        scatterPlot.setRangeStepMode(StepMode.INCREMENT_BY_VAL);
//        scatterPlot.setDomainStepValue(5);
        double deltaX = xMax - xMin;
        double deltaY = yMax - yMin;
        squareLen = Math.max(xMax - xMin, yMax - yMin);
        if (deltaX * xyScale > deltaY) {
            //domain = deltaX
            chartXRange = deltaX;
            chartYRange = chartXRange * xyScale;

        } else {
            //range = deltaY
            chartYRange = deltaY;
            chartXRange = chartYRange / xyScale;
        }
        scatterPlot.setDomainBoundaries(xMin, xMin + chartXRange, BoundaryMode.FIXED);
        scatterPlot.setRangeBoundaries(yMin, yMin + chartYRange, BoundaryMode.FIXED);
        droneIcon.lineLength = squareLen * 0.05;
//        scatterPlot.setDomainBoundaries(null, null, BoundaryMode.GROW);
//        scatterPlot.setRangeBoundaries(null, null, BoundaryMode.GROW);


        // create a dash effect for domain and range grid lines:
        DashPathEffect dashFx = new DashPathEffect(
                new float[]{PixelUtils.dpToPix(3), PixelUtils.dpToPix(3)}, 0);
        dynamicPlot.getGraph().getDomainGridLinePaint().setPathEffect(dashFx);
        dynamicPlot.getGraph().getRangeGridLinePaint().setPathEffect(dashFx);

        scatterPlot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.LEFT).setFormat(new DecimalFormat("##.#####"));
        scatterPlot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).setFormat(new DecimalFormat("###.#####"));
    }

    private void dataInit() {
        data = new MyXYDatasource();
        data.addObserver(plotUpdater);

        //===============XY Plot===============
        dynamicPlot.clear();

        // only display whole numbers in domain labels
        dynamicPlot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).
                setFormat(new DecimalFormat("0"));

        // get datasets:
        drone1Series = new DynamicSeries(data, 0, "Drone 1");

        LineAndPointFormatter formatter1 = new LineAndPointFormatter(
                Color.rgb(0, 200, 0), null, null, null);
        formatter1.getLinePaint().setStrokeJoin(Paint.Join.ROUND);
        formatter1.getLinePaint().setStrokeWidth(3);
        formatter1.setLegendIconEnabled(false);
        dynamicPlot.addSeries(drone1Series, formatter1);


        //===============Scatter Plot===============
        scatterPlot.clear();

        drone1Scatters = new ScatterSeries(data, 0, "Drone 1");
        LineAndPointFormatter formatter2 =
                new LineAndPointFormatter(this, R.xml.point_formatter);
        formatter2.setLegendIconEnabled(false);
        scatterPlot.addSeries(drone1Scatters, formatter2);

        //===============Drone Icon================
        droneIcon = new DroneIcon(data, 0, "Drone 1");
        LineAndPointFormatter iconFormatter =
                new LineAndPointFormatter(this, R.xml.point_formatter_2);
        iconFormatter.setLegendIconEnabled(false);
        droneIcon.lineLength = squareLen * 0.05;
        scatterPlot.addSeries(droneIcon, iconFormatter);

        //===============Drone Icon Head================
        LineAndPointFormatter headFormatter =
                new LineAndPointFormatter(this, R.xml.point_formatter_3);
        headFormatter.setLegendIconEnabled(false);
        scatterPlot.addSeries(droneIcon.droneIconHead, headFormatter);

    }

    @SuppressLint("SetTextI18n")
    void updateText() {
        text_alt.setText(droneData.getGpsAlt().get(data.counts - 1).toString());
        text_time.setText(droneData.df.format(droneData.getGpsTime().get(data.counts - 1)));
        text_yaw.setText(droneData.getGpsYaw().get(data.counts - 1).toString());
        text_pitch.setText(droneData.getGpsPitch().get(data.counts - 1).toString());
        text_roll.setText(droneData.getGpsRoll().get(data.counts - 1).toString());
        text_x.setText(droneData.getGpsLng().get(data.counts - 1).toString());
        text_y.setText(droneData.getGpsLat().get(data.counts - 1).toString());
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
            if (droneIcon != null) {
                droneIcon.updateIcon();
            }
        }
    }

    class MyXYDatasource implements Runnable {

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

        MyXYDatasource() {
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
            return droneData.getGpsLng().get(index); //TODO:
        }

        Number getY(int series, int index) {
            if (index >= counts) {
                throw new IllegalArgumentException();
            }
            return droneData.getGpsLat().get(index);//TODO:
        }

        Number getTopX(int series) {
            return droneData.getGpsLng().get(counts - 1); //TODO:
        }

        Number getTopY(int series) {
            return droneData.getGpsLat().get(counts - 1); //TODO:
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

    class DynamicSeries implements XYSeries {
        private MyXYDatasource datasource;
        private int seriesIndex;
        private String title;

        DynamicSeries(MyXYDatasource datasource, int seriesIndex, String title) {
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
        private MyXYDatasource datasource;
        private int seriesIndex;
        private String title;

        ScatterSeries(MyXYDatasource datasource, int seriesIndex, String title) {
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

    class DroneIcon implements XYSeries {
        private MyXYDatasource datasource;
        private int seriesIndex;
        public DroneIconHead droneIconHead;
        private ArrayList<Point> iconPoints;
        private String title;
        private Point pos;
        public double lineLength = 0.0001;
        private double yaw = 0;

        DroneIcon(MyXYDatasource datasource, int seriesIndex, String title) {
            this.datasource = datasource;
            this.seriesIndex = seriesIndex;
            this.title = title;
            iconPoints = new ArrayList<Point>();
            initIcon();
        }

        public void initIcon() {
            pos = new Point(droneData.getGpsLng().get(0), droneData.getGpsLat().get(0));
            droneIconHead = new DroneIconHead(pos);
            for (int i = 0; i < 5; i++) {
                iconPoints.add(pos);
            }
        }

        public void updateIcon() {
            iconPoints.clear();

            this.yaw = 90 - droneData.getGpsYaw().get(data.counts - 1);
            double radians = Math.toRadians(this.yaw);
            double sina = Math.sin(radians) * lineLength;
            double cosa = Math.cos(radians) * lineLength;

            pos = new Point(datasource.getTopX(seriesIndex), datasource.getTopY(seriesIndex));
            Point head = new Point(pos.x.doubleValue() + cosa, pos.y.doubleValue() + sina * xyScale);
            iconPoints.add(head);
            droneIconHead.updatePos(head);
            iconPoints.add(new Point(pos.x.doubleValue() - cosa, pos.y.doubleValue() - sina * xyScale));
            iconPoints.add(new Point(pos.x.doubleValue(), pos.y.doubleValue()));
            iconPoints.add(new Point(pos.x.doubleValue() + sina, pos.y.doubleValue() - cosa * xyScale));
            iconPoints.add(new Point(pos.x.doubleValue() - sina, pos.y.doubleValue() + cosa * xyScale));
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public int size() {
            return 5;
        }

        @Override
        public Number getX(int index) {
            return iconPoints.get(index).x;
        }

        @Override
        public Number getY(int index) {
            return iconPoints.get(index).y;
        }
    }

    class DroneIconHead implements XYSeries {
        private Point pos;
        private String title;

        DroneIconHead(Point pos) {
            this(pos, "");
        }

        DroneIconHead(Point pos, String title) {
            this.pos = pos;
            this.title = title;
        }

        public void updatePos(Point point) {
            this.pos = point;
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
            return pos.x;
        }

        @Override
        public Number getY(int index) {
            return pos.y;
        }
    }
}


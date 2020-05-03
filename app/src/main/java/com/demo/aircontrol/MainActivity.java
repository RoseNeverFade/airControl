package com.demo.aircontrol;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.androidplot.util.PixelUtils;
import com.androidplot.xy.*;
import com.demo.aircontrol.util.model.MyGLSurfaceView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, ClientConnector.ConnectLinstener {

    //可视化相关
    ScatterSeries routeScatters;
    SimpleXYSeries heightSeries;
    DroneIcon droneIcon;
    ArrayList<Point> points;
    ArrayList<Point> roundPoints;
    boolean testStart = false;
    private XYPlot heightPlot;
    private MyGLSurfaceView gLView;


    //显示信息
    private TextView tLng;
    private TextView tLat;
    private TextView tAlt;
    private TextView tPitch;
    private TextView tRoll;
    private TextView tYaw;
    private Button btwaypoint;
    private Button bthotpoint;
    private Button btteam;
    private Button btcserver;
    private Button btnDemRecord;//记录经纬数据
    private Button btnDemOutput;//输出经纬数据至文件

    private static final int FILE_SELECT_CODE = 0;
    private Button btnHistory; //Button ...
    private Button btnHistoryRoute; //Button A
    private Button btnHistoryAnalyze; //Button C
    private XYPlot routePlot;

    private TextView missioninfo;
    private Button btstop;

    private PopupWindow popupWindowTeam;
    private PopupWindow popupWindowWay;
    private PopupWindow popupWindowHot;
    private PopupWindow popupWindowCon;
    private PopupWindow popupWindowCserver;
    private PopupWindow popupWindowResult;
    private PopupWindow popupWindowJointeam;
    private PopupWindow popupWindowRotate;
    private PopupWindow popupWindowAutorotate;
    private PopupWindow popupWindowHistory;

    private Calendar now = Calendar.getInstance();

    private DroneData droneData;

    private int teamnum;
    private int teamleader;

    private double waylng;
    private double waylat;
    private double wayalt;
    private double wayvel;
    private double hotlng;
    private double hotlat;
    private double hotalt;
    private double hotw;
    private double hotr;
    private String hotstart;
    private double autorotatew;
    private String serverhost;
    private int serverport;

    private HandlerThread serverHandlerThread;
    private Handler serverHandler;
    private ClientConnector serverConnector;

    private ArrayList<String> timelist;
    private ArrayList<String> lnglist;
    private ArrayList<String> latlist;
    private ArrayList<String> altlist;
    private ArrayList<String> rolllist;
    private ArrayList<String> pitchlist;
    private ArrayList<String> yawlist;

//    class TimeThread extends Thread {
//        @Override
//        public void run() {
//            do {
//                try {
//                    Thread.sleep(1000);
//                    Message msg = new Message();
//                    msg.what = 1;  //消息(一个整型值)
//                    mHandler.sendMessage(msg);// 每隔1秒发送一个msg给mHandler
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            } while (true);
//        }
//    }

    //在主线程里面处理消息并更新UI界面
//    private Handler mHandler = new Handler(){
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            switch (msg.what) {
//                case 1:
//                    long sysTime = System.currentTimeMillis();//获取系统时间
//                    CharSequence sysTimeStr = DateFormat.format("hh:mm:ss", sysTime);//时间显示格式
//                    tLng.setText(sysTimeStr); //更新时间
//                    break;
//                default:
//                    break;
//
//            }
//        }
//    };


//    private String sendToServer(String msg){
//        try {
//            Socket socket = new Socket();
//            socket.connect(new InetSocketAddress("192.168.0.108",9053));
//
//            //获取输出流，向服务器端发送信息
//            OutputStream os=socket.getOutputStream();//字节输出流
//            PrintWriter pw=new PrintWriter(os);//将输出流包装为打印流
//            pw.write(msg);
//            pw.flush();
//            socket.shutdownOutput();//关闭输出流
//
//            InputStream is=socket.getInputStream();
//            BufferedReader in = new BufferedReader(new InputStreamReader(is));
//            String info=null;
//            while((info=in.readLine())!=null){
//                System.out.println(info);
//            }
//            is.close();
//            in.close();
//            socket.close();
//            return info;
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//            return "error";
//        } catch (IOException e) {
//            e.printStackTrace();
//            return "error";
//        }
//    }

    @Override
    public void onReceiveData(final String data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                System.out.println("*****************************:" + data);
                String str = "";
                if (data.contains("updatedata")) {
                    String[] updatedata = data.split(",");
                    tLng.setText(updatedata[1]);
                    lnglist.add(updatedata[1]);
                    tLat.setText(updatedata[2]);
                    latlist.add(updatedata[2]);
                    tAlt.setText(updatedata[3]);
                    altlist.add(updatedata[3]);
                    tYaw.setText(updatedata[4]);
                    yawlist.add(updatedata[4]);
                    tPitch.setText(updatedata[5]);
                    pitchlist.add(updatedata[5]);
                    tRoll.setText(updatedata[6]);
                    rolllist.add(updatedata[6]);
                    SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss:SSS");//设置日期格式
                    timelist.add(df.format(new Date()));
                } else if (data.contains("connectsuccess")) {
                    initPopupWindowResult("连接成功");
                    popupWindowResult.showAtLocation(findViewById(R.id.main_body), Gravity.CENTER, 0, 0);
                } else if (data.contains("connecterror")) {
                    initPopupWindowResult("连接失败");
                    popupWindowResult.showAtLocation(findViewById(R.id.main_body), Gravity.CENTER, 0, 0);
                } else if (data.contains("createsuccess")) {
                    teamnum = Integer.parseInt(data.split(",")[1]);
                    teamleader = 1;
                    initPopupWindowResult("创建成功！\n编队号：" + teamnum);
                    popupWindowResult.showAtLocation(findViewById(R.id.main_body), Gravity.CENTER, 0, 0);
                } else if (data.contains("joinsuccess")) {
                    teamnum = Integer.parseInt(data.split(",")[1]);
                    initPopupWindowResult("加入编队成功");
                    popupWindowResult.showAtLocation(findViewById(R.id.main_body), Gravity.CENTER, 0, 0);
                } else if (data.contains("teamnotfound")) {
                    initPopupWindowResult("加入编队失败");
                    popupWindowResult.showAtLocation(findViewById(R.id.main_body), Gravity.CENTER, 0, 0);
                } else if (data.contains("stopsuccess")) {
                    missioninfo.setText("");
                    btstop.setVisibility(View.GONE);
                } else if (data.contains("finish")) {
                    missioninfo.setText("");
                    btstop.setVisibility(View.GONE);
                    initPopupWindowResult("执行完毕");
                    popupWindowResult.showAtLocation(findViewById(R.id.main_body), Gravity.CENTER, 0, 0);
                } else if (data.contains("waysavesuccess") || data.contains("wayexecutsuccess")) {
                    if (data.contains("savesuccess")) str = "已保存航点飞行任务，等待编队执行命令。\n";
                    else if (data.contains("executsuccess")) str = "开始执行航点飞行任务！\n";
                    missioninfo.setText(str + "经度：" + waylng + " 纬度：" + waylat + " 高度：" + wayalt + " 速度：" + wayvel);
                    btstop.setVisibility(View.VISIBLE);
                } else if (data.contains("hotsavesuccess") || data.contains("hotexecutsuccess")) {
                    if (data.contains("savesuccess")) str = "已保存圆周飞行任务，等待编队执行命令。\n";
                    else if (data.contains("executsuccess")) str = "开始执行圆周飞行任务！\n";
                    missioninfo.setText(str + "圆心经度：" + hotlng + " 圆心纬度：" + hotlat + " 圆心高度：" + hotalt + " 绕飞半径：" + hotr + " 角速度：" + hotw + " 起始方向：" + hotstart);
                    btstop.setVisibility(View.VISIBLE);
                } else if (data.contains("rotateexecutsuccess")) {
                    str = "开始执行无人机转向任务！\n机头转向：" + data.split(",")[1];
                    missioninfo.setText(str);
                    btstop.setVisibility(View.VISIBLE);
                    popupWindowRotate.dismiss();
                } else if (data.contains("arexecutsuccess") || data.contains("arsavesuccess")) {
                    if (data.contains("savesuccess")) str = "已保存原地旋转任务，等待编队执行命令。\n";
                    else if (data.contains("executsuccess")) str = "开始执行原地旋转任务！\n";
                    missioninfo.setText(str + "旋转速度：" + autorotatew);
                    btstop.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private Button btnLoad; //Button A

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //3d model
        ConstraintLayout layout = findViewById(R.id.modelBlock);
        gLView = new MyGLSurfaceView(this);
        layout.addView(gLView);

        droneData = droneData.getInstance();
        droneData.setContext(this);
        initUI();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //-----------------------------------------
            case R.id.btn_record://保存
                timelist = new ArrayList<>();
                lnglist = new ArrayList<>();
                latlist = new ArrayList<>();
                altlist = new ArrayList<>();
                yawlist = new ArrayList<>();
                pitchlist = new ArrayList<>();
                rolllist = new ArrayList<>();
                break;

            case R.id.btn_output://输出到文件

                //loginAccount();

                String fileName = "a.txt";//文件名规则？
                outputGPStoFile(fileName);
                break;


            case R.id.btn_team:
                initPopupWindowTeam();
                /* 这里是位置显示方式,在按钮的左下角 */
                popupWindowTeam.showAtLocation(findViewById(R.id.main_body), Gravity.RIGHT, 0, 0);
                break;
            case R.id.btn_waypoint:
                initPopupWindowWay();
                /* 这里是位置显示方式,在按钮的左下角 */
                popupWindowWay.showAtLocation(findViewById(R.id.main_body), Gravity.RIGHT, 0, 0);

                break;
            case R.id.btn_hotpoint:
                initPopupWindowHot();
                /* 这里是位置显示方式,在按钮的左下角 */
                popupWindowHot.showAtLocation(findViewById(R.id.main_body), Gravity.RIGHT, 0, 0);

                break;

            case R.id.btn_cserver:
                initPopupWindowCserver();
                popupWindowCserver.showAtLocation(findViewById(R.id.main_body), Gravity.CENTER, 0, 0);
                break;


            case R.id.btn_rotate:
                initPopupWindowRotate();
                popupWindowRotate.showAtLocation(findViewById(R.id.main_body), Gravity.RIGHT, 0, 0);
                break;

            case R.id.btn_autorotate:
                initPopupWindowAutorotate();
                popupWindowAutorotate.showAtLocation(findViewById(R.id.main_body), Gravity.RIGHT, 0, 0);
                break;

            case R.id.btn_stop:
                initPopupWindowConfirm(1);
                popupWindowCon.showAtLocation(findViewById(R.id.main_body), Gravity.CENTER, 0, 0);
                break;

            case R.id.btn_history:
                initPopupWindowHistory();
                popupWindowHistory.showAtLocation(findViewById(R.id.main_body), Gravity.CENTER, 0, 0);
                break;
        }
    }

    private Button btnPic3; //Button A

    private void initUI() {

        //显示信息
        tLng = (TextView) findViewById(R.id.textLng);
        tLat = (TextView) findViewById(R.id.textLat);
        tAlt = (TextView) findViewById(R.id.textAlt);
        tPitch = (TextView) findViewById(R.id.textPitch);
        tRoll = (TextView) findViewById(R.id.textRoll);
        tYaw = (TextView) findViewById(R.id.textYaw);

        tYaw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testChart();
            }
        });

        // ---------------设置单击事件-------------------------
        //  private Button btnDemRecord;//记录经纬数据
        // private Button btnDemOutput;//输出经纬数据至文件
        btnDemRecord = (Button) findViewById(R.id.btn_record);
        btnDemRecord.setOnClickListener(this);//记录经纬数据

        btnDemOutput = (Button) findViewById(R.id.btn_output);
        btnDemOutput.setOnClickListener(this);//输出经纬数据至文件

        btnHistory = (Button) findViewById(R.id.btn_history);
        btnHistory.setOnClickListener(this);


        btteam = (Button) findViewById(R.id.btn_team);
        btwaypoint = (Button) findViewById(R.id.btn_waypoint);
        bthotpoint = (Button) findViewById(R.id.btn_hotpoint);
        btcserver = (Button) findViewById(R.id.btn_cserver);

        btteam.setOnClickListener(this);
        btwaypoint.setOnClickListener(this);
        bthotpoint.setOnClickListener(this);
        btcserver.setOnClickListener(this);

//        new TimeThread().start(); //启动新的线程

        missioninfo = (TextView) findViewById(R.id.missininfo);
        btstop = (Button) findViewById(R.id.btn_stop);

        teamnum = 0;
        teamleader = 0;

        timelist = new ArrayList<>();
        lnglist = new ArrayList<>();
        latlist = new ArrayList<>();
        altlist = new ArrayList<>();
        yawlist = new ArrayList<>();
        pitchlist = new ArrayList<>();
        rolllist = new ArrayList<>();

        //可视化设置
        heightPlot = (XYPlot) findViewById(R.id.height_plot);
        routePlot = (XYPlot) findViewById(R.id.route_plot);
        initChartData();

        if (MyBuildConfig.isDebug) {
            droneData.loadFakeGPSData();
        }
    }

    //初始化图表数据
    private void initChartData() {
        heightPlot.clear();
        routePlot.clear();
        points = new ArrayList<Point>();

        //===============routePlot================
        //Scatter
        routeScatters = new ScatterSeries(points, "Drone 1");
        LineAndPointFormatter scatterFormatter = new LineAndPointFormatter(this, R.xml.point_formatter);
        scatterFormatter.setLegendIconEnabled(false);
        routePlot.addSeries(routeScatters, scatterFormatter);
        routePlot.setRangeBoundaries(-100, 100, BoundaryMode.GROW);
        routePlot.setDomainBoundaries(-100, 100, BoundaryMode.GROW);

        //Drone Icon
        droneIcon = new DroneIcon("Drone 1");
        LineAndPointFormatter iconFormatter =
                new LineAndPointFormatter(this, R.xml.point_formatter_2);
        iconFormatter.setLegendIconEnabled(false);
        routePlot.addSeries(droneIcon, iconFormatter);
        //Drone Icon Head
        LineAndPointFormatter headFormatter =
                new LineAndPointFormatter(this, R.xml.point_formatter_3);
        headFormatter.setLegendIconEnabled(false);
        routePlot.addSeries(droneIcon.droneIconHead, headFormatter);

        //===============heightPlot================
        heightSeries = new SimpleXYSeries(SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "height");
        BarFormatter barFormatter = new BarFormatter(Color.rgb(0x66, 0xcc, 0xff), Color.rgb(0x66, 0xcc, 0xaa));
        barFormatter.setLegendIconEnabled(false);
        heightPlot.addSeries(heightSeries, barFormatter);
        heightPlot.setDomainBoundaries(-1, 1, BoundaryMode.FIXED);
        heightPlot.setDomainStepValue(3);
        heightPlot.setRangeBoundaries(0, 100, BoundaryMode.GROW);

        // get a ref to the BarRenderer so we can make some changes to it:
        BarRenderer barRenderer = heightPlot.getRenderer(BarRenderer.class);
        if (barRenderer != null) {
            // make our bars a little thicker than the default so they can be seen better:
            barRenderer.setBarGroupWidth(
                    BarRenderer.BarGroupWidthMode.FIXED_WIDTH, PixelUtils.dpToPix(18));
        }

    }

    /**
     * 插入并更新图表数据
     *
     * @param point 新的点 Point(Number x,Number y)
     * @param yaw   偏航角
     */
    private void addChartPoint(Point point, double yaw, double height) {
        points.add(point);
        droneIcon.updateIcon(point, yaw);
        routePlot.redraw();
//        heightSeries.clear();
        heightSeries.setModel(Arrays.asList(
                new Number[]{height}),
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);

        heightPlot.setRangeBoundaries(0, ((int) height / 5 + 1) * 5, BoundaryMode.FIXED);
        heightPlot.redraw();
    }

    /**
     * 设置目标圆形（正128边形）轨迹
     *
     * @param axis 圆心 Point(Number x,Number y)
     * @param r    半径
     */
    private void addCircle(Point axis, double r) {
        int pointNum = 128;
        SimpleXYSeries circleSeries = new SimpleXYSeries("circlePoint");
        SimpleXYSeries axisSeries = new SimpleXYSeries("axis");
        axisSeries.addFirst(axis.x, axis.y);

        double theta = 2 * Math.PI / pointNum;
        for (int i = 0; i < 128; i++) {
            Number x = axis.x.doubleValue() + r * Math.cos(theta * i);
            Number y = axis.y.doubleValue() + r * Math.sin(theta * i);
            circleSeries.addFirst(x, y);
        }
        LineAndPointFormatter axisFormatter =
                new LineAndPointFormatter(this, R.xml.circle_axis_formatter);
        LineAndPointFormatter circleFormatter = new LineAndPointFormatter(
                Color.rgb(0, 200, 0), null, null, null);
        circleFormatter.getLinePaint().setStrokeJoin(Paint.Join.ROUND);
        circleFormatter.getLinePaint().setStrokeWidth(2);

        axisFormatter.setLegendIconEnabled(false);
        circleFormatter.setLegendIconEnabled(false);
        routePlot.addSeries(axisSeries, axisFormatter);
        routePlot.addSeries(circleSeries, circleFormatter);
    }

    /**
     * 测试可视化图表，仅debug模式可用
     */
    private void testChart() {
        if (!testStart && MyBuildConfig.isDebug) {
            testStart = true;
            ChartTestThread myThread = new ChartTestThread(this);
            myThread.start();
        }
    }

    class ChartTestThread extends Thread {
        MainActivity m;

        ChartTestThread(MainActivity m) {
            this.m = m;
        }

        @Override
        public void run() {
            double r = 60d;
            int num = 720;
            double h = 30d;
            m.addCircle(new Point(0, 0), r);
            double theta = 2 * Math.PI / 360;
            for (int i = 0; i < 720; i++) {
                double t = theta * i;
                Number x = r * Math.cos(t);
                Number y = r * Math.sin(t);
                double yaw = Math.toDegrees(t) + 90;

                m.addChartPoint(new Point(x.doubleValue() + Math.random() * 0.1, y.doubleValue() + Math.random() * 0.1), yaw + Math.random(), h + Math.random() * 0.5);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    Log.d("TAG", "File Uri: " + uri.toString());
                    // Get the path
                    String path = FileUtils.getPath(this, uri);
                    Log.d("TAG", "File Path: " + path);
                    // Get the file instance
                    // File file = new File(path);
                    // Initiate the upload
                    droneData.loadGPSData(path);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void outputGPStoFile(String FileName)
    {
        String ftime = ""+now.get(Calendar.YEAR)+'_'+now.get(Calendar.MONTH)+'_'+now.get(Calendar.DAY_OF_MONTH)+'-'+now.get(Calendar.HOUR_OF_DAY)+':'+now.get(Calendar.MINUTE);

        String state;
        String path;
        //获取内部存储根目录
        File inpath = android.os.Environment.getDataDirectory();
        //2 确认sdcard的存在
        state = android.os.Environment.getExternalStorageState();
        if(state.equals(android.os.Environment.MEDIA_MOUNTED)) {
            //3 获取扩展存储设备的文件目录
            path = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
            String filefullname =  path+"/gpsdata_"+ftime+".txt";



            try{
                // FileWriter fileWriter = new FileWriter(path+"/gpsdata.txt",true);
                FileWriter fileWriter = new FileWriter(filefullname,true);


                BufferedWriter bw = new BufferedWriter(fileWriter);
                // 输出坐标数量
                bw.newLine();
                bw.write("Time    Lng    Lat    Alt    Yaw    Pitch    Roll");
                bw.newLine();
                //遍历集合
                int len = timelist.size();
                for (int i=0; i<len; i++) {
                    bw.write("" + timelist.get(i));
                    bw.write("    ");
                    bw.write("" + lnglist.get(i));
                    bw.write("    ");
                    bw.write("" + latlist.get(i));
                    bw.write("    ");
                    bw.write("" + altlist.get(i));
                    bw.write("    ");
                    bw.write("" + yawlist.get(i));
                    bw.write("    ");
                    bw.write("" + pitchlist.get(i));
                    bw.write("    ");
                    bw.write("" + rolllist.get(i));
                    bw.newLine();
                    bw.flush();
                }
                // //释放资源
                bw.close();
                fileWriter.close();
            }catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this,"file is not exist",Toast.LENGTH_SHORT).show();
            }
        }
        else ///没有sd卡 就用手机本身内存
        {
            //getFilesDir
            File sdFie = android.os.Environment.getDataDirectory();
            path = android.os.Environment.getDataDirectory().getAbsolutePath();//获取手机内存绝对路径

            try{
                FileWriter fileWriter = new FileWriter(path+"/gpsdata_"+ftime+".txt",true);
                BufferedWriter bw = new BufferedWriter(fileWriter);
                // 输出坐标数量
                bw.write("" );
                bw.newLine();
                bw.write("Time    Lng    Lat    Alt    Yaw    Pitch    Roll");
                bw.newLine();
                //遍历集合
                int len = timelist.size();
                for (int i=0; i<len; i++) {
                    bw.write("" + timelist.get(i));
                    bw.write("    ");
                    bw.write("" + lnglist.get(i));
                    bw.write("    ");
                    bw.write("" + latlist.get(i));
                    bw.write("    ");
                    bw.write("" + altlist.get(i));
                    bw.write("    ");
                    bw.write("" + yawlist.get(i));
                    bw.write("    ");
                    bw.write("" + pitchlist.get(i));
                    bw.write("    ");
                    bw.write("" + rolllist.get(i));
                    bw.newLine();
                    bw.flush();
                }
                // //释放资源
                bw.close();
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "file is not exist", Toast.LENGTH_SHORT).show();
            }
        }
    }

    protected void initPopupWindowResult(String str) {
        View v = getLayoutInflater().inflate(R.layout.pop_result, null, false);
        popupWindowResult = new PopupWindow(v, 600, 300, true);
        popupWindowResult.setFocusable(true);

        popupWindowResult.setOutsideTouchable(true);  //设置点击屏幕其它地方弹出框消失
        popupWindowResult.setBackgroundDrawable(new BitmapDrawable());

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.5f;//设置阴影透明度
        getWindow().setAttributes(lp);
        popupWindowResult.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1f;
                getWindow().setAttributes(lp);
            }
        });

        TextView sometext = (TextView) v.findViewById(R.id.sometext);
        sometext.setText(str);

    }

    public void connectServer(String serverhost, int serverport) {
        serverHandlerThread = new HandlerThread("MainActivity", android.os.Process.THREAD_PRIORITY_BACKGROUND);
        serverHandlerThread.start();
        serverHandler = new Handler(serverHandlerThread.getLooper());
        serverConnector = new ClientConnector(serverhost, serverport);
        serverConnector.setOnConnectLinstener(this);
        serverHandler.post(new ConnectRunnable());
    }

    protected void initPopupWindowCserver() {
        View v = getLayoutInflater().inflate(R.layout.pop_connectserver, null, false);
        popupWindowCserver = new PopupWindow(v, 1000, 500, true);
        popupWindowCserver.setFocusable(true);

        popupWindowCserver.setOutsideTouchable(true);  //设置点击屏幕其它地方弹出框消失
        popupWindowCserver.setBackgroundDrawable(new BitmapDrawable());

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.5f;//设置阴影透明度
        getWindow().setAttributes(lp);
        popupWindowCserver.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1f;
                getWindow().setAttributes(lp);
            }
        });

        /* pop.xml视图里面的控件 */
        Button btyes = (Button) v.findViewById(R.id.bt_yes);
        Button btno = (Button) v.findViewById(R.id.bt_no);
        final TextView txtaddr = (TextView) v.findViewById(R.id.etip);


        btyes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindowCserver.dismiss();
                String addr = txtaddr.getText().toString();
                if (addr.contains(":")) {
                    try {
                        serverhost = addr.split(":")[0];
                        serverport = Integer.parseInt(addr.split(":")[1]);
                        connectServer(serverhost, serverport);
                        sleep(1000);
                    } catch (Exception e) {
                        initPopupWindowResult("连接失败");
                        popupWindowResult.showAtLocation(findViewById(R.id.main_body), Gravity.CENTER, 0, 0);
                    }
                } else {
                    initPopupWindowResult("连接失败");
                    popupWindowResult.showAtLocation(findViewById(R.id.main_body), Gravity.CENTER, 0, 0);
                }
            }
        });

        btno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindowCserver.dismiss();
            }
        });
    }

    protected void initPopupWindowJointeam() {
        View v = getLayoutInflater().inflate(R.layout.pop_jointeam, null, false);
        popupWindowJointeam = new PopupWindow(v, 1000, 500, true);
        popupWindowJointeam.setFocusable(true);

        popupWindowJointeam.setOutsideTouchable(true);  //设置点击屏幕其它地方弹出框消失
        popupWindowJointeam.setBackgroundDrawable(new BitmapDrawable());

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.5f;//设置阴影透明度
        getWindow().setAttributes(lp);
        popupWindowJointeam.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1f;
                getWindow().setAttributes(lp);
            }
        });

        /* pop.xml视图里面的控件 */
        Button btyes = (Button) v.findViewById(R.id.bt_yes);
        Button btno = (Button) v.findViewById(R.id.bt_no);
        final TextView etnum = (TextView) v.findViewById(R.id.etnum);

        btyes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindowJointeam.dismiss();

                try {
                    serverConnector.send("jointeam," + etnum.getText().toString());
                } catch (IOException e) {
                    e.printStackTrace();
                    initPopupWindowResult("加入编队失败");
                    popupWindowResult.showAtLocation(findViewById(R.id.main_body), Gravity.CENTER, 0, 0);
                }

            }
        });

        btno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindowJointeam.dismiss();
            }
        });
    }

    protected void initPopupWindowConfirm(final int type) {
        View v = getLayoutInflater().inflate(R.layout.pop_confirm, null, false);
        popupWindowCon = new PopupWindow(v, 1000, 500, true);
        popupWindowCon.setFocusable(true);

        popupWindowCon.setOutsideTouchable(true);  //设置点击屏幕其它地方弹出框消失
        popupWindowCon.setBackgroundDrawable(new BitmapDrawable());

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.5f;//设置阴影透明度
        getWindow().setAttributes(lp);
        popupWindowCon.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1f;
                getWindow().setAttributes(lp);
            }
        });

        /* pop.xml视图里面的控件 */
        Button btyes = (Button) v.findViewById(R.id.bt_yes);
        Button btno = (Button) v.findViewById(R.id.bt_no);
        TextView txtcontitle = (TextView) v.findViewById(R.id.txt_contitle);

        if (type == 21 || type == 31 || type == 51) txtcontitle.setText("保存配置并等待编队命令");
        else if (type == 1) {
            txtcontitle.setText("中止执行任务");
        }


        btyes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str;
                String res;

                if (type == 1) {
                    try {
                        serverConnector.send("stop");
                    } catch (IOException e) {
                        e.printStackTrace();
                        initPopupWindowResult("失败");
                        popupWindowResult.showAtLocation(findViewById(R.id.main_body), Gravity.CENTER, 0, 0);
                    }
                } else if (type / 10 == 2) {
                    String choose = "";
                    if (type == 21) choose = "all";
                    else if (type == 22) choose = "alone";
                    try {
                        serverConnector.send("way," + choose + "," + teamnum + "," + waylng + "," + waylat + "," + wayalt + "," + wayvel);
                    } catch (IOException e) {
                        e.printStackTrace();
                        initPopupWindowResult("失败");
                        popupWindowResult.showAtLocation(findViewById(R.id.main_body), Gravity.CENTER, 0, 0);
                    }
                    popupWindowWay.dismiss();
                } else if (type / 10 == 3) {
                    popupWindowHot.dismiss();
                    String choose = "";
                    if (type == 31) choose = "all";
                    else if (type == 32) choose = "alone";
                    try {
                        serverConnector.send("hot," + choose + "," + teamnum + "," + hotlng + "," + hotlat + "," + hotalt + "," + hotr + "," + hotw + "," + hotstart);
                    } catch (IOException e) {
                        e.printStackTrace();
                        initPopupWindowResult("失败");
                        popupWindowResult.showAtLocation(findViewById(R.id.main_body), Gravity.CENTER, 0, 0);
                    }
                } else if (type / 10 == 4) {
                    popupWindowRotate.dismiss();
                    str = "";
                    if (type == 41) str += "east";
                    else if (type == 42) str += "south";
                    else if (type == 43) str += "west";
                    else if (type == 44) str += "north";
                    try {
                        serverConnector.send("rotate," + str);
                    } catch (IOException e) {
                        e.printStackTrace();
                        initPopupWindowResult("失败");
                        popupWindowResult.showAtLocation(findViewById(R.id.main_body), Gravity.CENTER, 0, 0);
                    }

                } else if (type / 10 == 5) {
                    String choose = "";
                    if (type == 51) choose = "all";
                    else if (type == 52) choose = "alone";
                    try {
                        serverConnector.send("ar," + choose + "," + teamnum + "," + autorotatew);
                    } catch (IOException e) {
                        e.printStackTrace();
                        initPopupWindowResult("失败");
                        popupWindowResult.showAtLocation(findViewById(R.id.main_body), Gravity.CENTER, 0, 0);
                    }
                    popupWindowAutorotate.dismiss();
                }
                popupWindowCon.dismiss();
            }
        } );

        btno.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                popupWindowCon.dismiss();
            }
        } );
    }

    protected void initPopupWindowTeam() {
        /* TODO Auto-generated method stub */

        /* 获取自定义布局文件pop.xml的视图 */
        View popupWindow_view = getLayoutInflater().inflate(R.layout.pop_team, null,
                false);
        /* 创建PopupWindow实例,200,150分别是宽度和高度 */
        popupWindowTeam = new PopupWindow(popupWindow_view, 600, 600, true);

        popupWindowTeam.setOutsideTouchable(true);  //设置点击屏幕其它地方弹出框消失
        popupWindowTeam.setBackgroundDrawable(new BitmapDrawable());

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.5f;//设置阴影透明度
        getWindow().setAttributes(lp);
        popupWindowTeam.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1f;
                getWindow().setAttributes(lp);
            }
        });

        /* pop.xml视图里面的控件 */
        Button btcteam = (Button) popupWindow_view.findViewById(R.id.create_team);
        Button btjteam = (Button) popupWindow_view.findViewById(R.id.join_team);
        Button btqteam = (Button) popupWindow_view.findViewById(R.id.quit_team);
        TextView txtteamstate = (TextView) popupWindow_view.findViewById(R.id.team_state);

        if (teamnum != 0) {
            txtteamstate.setText("编队号：" + teamnum);
            btcteam.setEnabled(false);
            btjteam.setEnabled(false);
        } else {
            btqteam.setEnabled(false);
        }

        btcteam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    serverConnector.send("createteam," + System.currentTimeMillis() % 1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                popupWindowTeam.dismiss();

            }
        });

        btjteam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindowTeam.dismiss();
                initPopupWindowJointeam();
                popupWindowJointeam.showAtLocation(findViewById(R.id.main_body), Gravity.CENTER, 0, 0);
            }
        });

        btqteam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                teamleader = 0;
                teamnum = 0;
                popupWindowTeam.dismiss();
            }
        });

    }

    protected void initPopupWindowWay() {
        View popupWindow_view = getLayoutInflater().inflate(R.layout.pop_way, null,
                false);
        /* 创建PopupWindow实例,200,150分别是宽度和高度 */
        popupWindowWay = new PopupWindow(popupWindow_view, 1000, 1200, true);

        popupWindowWay.setOutsideTouchable(true);  //设置点击屏幕其它地方弹出框消失
        popupWindowWay.setBackgroundDrawable(new BitmapDrawable());

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.5f;//设置阴影透明度
        getWindow().setAttributes(lp);
        popupWindowWay.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1f;
                getWindow().setAttributes(lp);
            }
        });

        /* pop.xml视图里面的控件 */
        Button bttwgo = (Button) popupWindow_view.findViewById(R.id.bt_twgo);
        Button btwgo = (Button) popupWindow_view.findViewById(R.id.bt_wgo);
        final EditText etlng = (EditText) popupWindow_view.findViewById(R.id.et_lng);
        final EditText etlat = (EditText) popupWindow_view.findViewById(R.id.et_lat);
        final EditText etalt = (EditText) popupWindow_view.findViewById(R.id.et_alt);
        final EditText etvel = (EditText) popupWindow_view.findViewById(R.id.et_vel);


        if (teamnum == 0) {
            bttwgo.setEnabled(false);
        } else {
            if (teamleader == 0) {
                bttwgo.setText("保存配置");
            }
        }

        bttwgo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    wayalt = Double.parseDouble(etalt.getText().toString());
                    waylat = Double.parseDouble(etlat.getText().toString());
                    waylng = Double.parseDouble(etlng.getText().toString());
                    wayvel = Double.parseDouble(etvel.getText().toString());
                } catch (Exception e) {
                    initPopupWindowResult("参数不合法");
                    popupWindowResult.showAtLocation(findViewById(R.id.main_body), Gravity.CENTER, 0, 0);
                }
                initPopupWindowConfirm(21);
                popupWindowCon.showAtLocation(findViewById(R.id.main_body), Gravity.CENTER, 0, 0);

            }
        } );

        btwgo.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v ) {
                try {
                    wayalt = Double.parseDouble(etalt.getText().toString());
                    waylat = Double.parseDouble(etlat.getText().toString());
                    waylng = Double.parseDouble(etlng.getText().toString());
                    wayvel = Double.parseDouble(etvel.getText().toString());
                } catch (Exception e) {
                    initPopupWindowResult("参数不合法");
                    popupWindowResult.showAtLocation(findViewById(R.id.main_body), Gravity.CENTER, 0, 0);
                }
                initPopupWindowConfirm(22);
                popupWindowCon.showAtLocation(findViewById(R.id.main_body), Gravity.CENTER, 0, 0);
            }
        } );

    }

    protected void initPopupWindowHot() {
        View popupWindow_view = getLayoutInflater().inflate(R.layout.pop_hot, null,
                false);
        /* 创建PopupWindow实例,200,150分别是宽度和高度 */
        popupWindowHot = new PopupWindow(popupWindow_view, 1000, 1400, true);

        popupWindowHot.setOutsideTouchable(true);  //设置点击屏幕其它地方弹出框消失
        popupWindowHot.setBackgroundDrawable(new BitmapDrawable());

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.5f;//设置阴影透明度
        getWindow().setAttributes(lp);
        popupWindowHot.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1f;
                getWindow().setAttributes(lp);
            }
        });

        /* pop.xml视图里面的控件 */
        Button btthgo = (Button) popupWindow_view.findViewById(R.id.bt_thgo);
        Button bthgo = (Button) popupWindow_view.findViewById(R.id.bt_hgo);
        final EditText etlng = (EditText) popupWindow_view.findViewById(R.id.et_lng);
        final EditText etlat = (EditText) popupWindow_view.findViewById(R.id.et_lat);
        final EditText etalt = (EditText) popupWindow_view.findViewById(R.id.et_alt);
        final EditText etw = (EditText) popupWindow_view.findViewById(R.id.et_w);
        final EditText etr = (EditText) popupWindow_view.findViewById(R.id.et_r);
        final Spinner spstart = (Spinner) popupWindow_view.findViewById(R.id.sp_start);

        if (teamnum == 0) {
            btthgo.setEnabled(false);
        } else {
            if (teamleader == 0) {
                btthgo.setText("保存配置");
            }
        }

        btthgo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    hotalt = Double.parseDouble(etalt.getText().toString());
                    hotlat = Double.parseDouble(etlat.getText().toString());
                    hotlng = Double.parseDouble(etlng.getText().toString());
                    hotw = Double.parseDouble(etw.getText().toString());
                    hotr = Double.parseDouble(etr.getText().toString());
                    String tmp = spstart.getSelectedItem().toString();
                    if (tmp.contains("东")) hotstart = "east";
                    else if (tmp.contains("南")) hotstart = "south";
                    else if (tmp.contains("西")) hotstart = "west";
                    else if (tmp.contains("北")) hotstart = "north";
                } catch (Exception e) {
                    initPopupWindowResult("参数不合法");
                    popupWindowResult.showAtLocation(findViewById(R.id.main_body), Gravity.CENTER, 0, 0);
                }

                initPopupWindowConfirm(31);
                popupWindowCon.showAtLocation(findViewById(R.id.main_body), Gravity.CENTER, 0, 0);

            }
        });

        bthgo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    hotalt = Double.parseDouble(etalt.getText().toString());
                    hotlat = Double.parseDouble(etlat.getText().toString());
                    hotlng = Double.parseDouble(etlng.getText().toString());
                    hotw = Double.parseDouble(etw.getText().toString());
                    hotr = Double.parseDouble(etr.getText().toString());
                    String tmp = spstart.getSelectedItem().toString();
                    if (tmp.contains("东")) hotstart = "east";
                    else if (tmp.contains("南")) hotstart = "south";
                    else if (tmp.contains("西")) hotstart = "west";
                    else if (tmp.contains("北")) hotstart = "north";
                } catch (Exception e) {
                    initPopupWindowResult("参数不合法");
                    popupWindowResult.showAtLocation(findViewById(R.id.main_body), Gravity.CENTER, 0, 0);
                }

                initPopupWindowConfirm(32);
                popupWindowCon.showAtLocation(findViewById(R.id.main_body), Gravity.CENTER, 0, 0);

            }
        });

    }

    protected void initPopupWindowRotate() {
        /* TODO Auto-generated method stub */

        /* 获取自定义布局文件pop.xml的视图 */
        View popupWindow_view = getLayoutInflater().inflate(R.layout.pop_rotate, null,
                false);
        /* 创建PopupWindow实例,200,150分别是宽度和高度 */
        popupWindowRotate = new PopupWindow(popupWindow_view, 500, 800, true);

        popupWindowRotate.setOutsideTouchable(true);  //设置点击屏幕其它地方弹出框消失
        popupWindowRotate.setBackgroundDrawable(new BitmapDrawable());

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.5f;//设置阴影透明度
        getWindow().setAttributes(lp);
        popupWindowRotate.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1f;
                getWindow().setAttributes(lp);
            }
        });

        /* pop.xml视图里面的控件 */
        Button bteast = (Button) popupWindow_view.findViewById(R.id.bt_east);
        Button btwest = (Button) popupWindow_view.findViewById(R.id.bt_west);
        Button btnorth = (Button) popupWindow_view.findViewById(R.id.bt_north);
        Button btsouth = (Button) popupWindow_view.findViewById(R.id.bt_south);


        bteast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initPopupWindowConfirm(41);
                popupWindowCon.showAtLocation(findViewById(R.id.main_body), Gravity.CENTER, 0, 0);
            }
        });

        btwest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initPopupWindowConfirm(43);
                popupWindowCon.showAtLocation(findViewById(R.id.main_body), Gravity.CENTER, 0, 0);

            }
        });

        btnorth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initPopupWindowConfirm(44);
                popupWindowCon.showAtLocation(findViewById(R.id.main_body), Gravity.CENTER, 0, 0);

            }
        });

        btsouth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initPopupWindowConfirm(42);
                popupWindowCon.showAtLocation(findViewById(R.id.main_body), Gravity.CENTER, 0, 0);

            }
        });

    }

    protected void initPopupWindowAutorotate() {
        /* TODO Auto-generated method stub */

        /* 获取自定义布局文件pop.xml的视图 */
        View popupWindow_view = getLayoutInflater().inflate(R.layout.pop_autorotate, null,
                false);
        /* 创建PopupWindow实例,200,150分别是宽度和高度 */
        popupWindowAutorotate = new PopupWindow(popupWindow_view, 1000, 600, true);

        popupWindowAutorotate.setOutsideTouchable(true);  //设置点击屏幕其它地方弹出框消失
        popupWindowAutorotate.setBackgroundDrawable(new BitmapDrawable());

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.5f;//设置阴影透明度
        getWindow().setAttributes(lp);
        popupWindowAutorotate.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1f;
                getWindow().setAttributes(lp);
            }
        });

        /* pop.xml视图里面的控件 */
        Button bttargo = (Button) popupWindow_view.findViewById(R.id.bt_targo);
        Button btargo = (Button) popupWindow_view.findViewById(R.id.bt_argo);
        final EditText etw = (EditText) popupWindow_view.findViewById(R.id.et_w);

        if (teamnum == 0) {
            bttargo.setEnabled(false);
        } else {
            if (teamleader == 0) {
                bttargo.setText("保存配置");
            }
        }

        bttargo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    autorotatew = Double.parseDouble(etw.getText().toString());
                } catch (Exception e) {
                    initPopupWindowResult("参数不合法");
                    popupWindowResult.showAtLocation(findViewById(R.id.main_body), Gravity.CENTER, 0, 0);
                }
                initPopupWindowConfirm(51);
                popupWindowCon.showAtLocation(findViewById(R.id.main_body), Gravity.CENTER, 0, 0);

            }
        });

        btargo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    autorotatew = Double.parseDouble(etw.getText().toString());
                } catch (Exception e) {
                    initPopupWindowResult("参数不合法");
                    popupWindowResult.showAtLocation(findViewById(R.id.main_body), Gravity.CENTER, 0, 0);
                }

                initPopupWindowConfirm(52);
                popupWindowCon.showAtLocation(findViewById(R.id.main_body), Gravity.CENTER, 0, 0);
            }
        });

    }

    protected void initPopupWindowHistory() {
        /* TODO Auto-generated method stub */

        /* 获取自定义布局文件pop.xml的视图 */
        View popupWindow_view = getLayoutInflater().inflate(R.layout.pop_history, null,
                false);
        /* 创建PopupWindow实例,200,150分别是宽度和高度 */
        popupWindowHistory = new PopupWindow(popupWindow_view, 500, 800, true);

        popupWindowHistory.setOutsideTouchable(true);  //设置点击屏幕其它地方弹出框消失
        popupWindowHistory.setBackgroundDrawable(new BitmapDrawable());

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.5f;//设置阴影透明度
        getWindow().setAttributes(lp);
        popupWindowHistory.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1f;
                getWindow().setAttributes(lp);
            }
        });

        /* pop.xml视图里面的控件 */
        Button btnLoad = (Button) popupWindow_view.findViewById(R.id.btn_load);
        Button btnPic1 = (Button) popupWindow_view.findViewById(R.id.btn_hisRoute);
        Button btnPic2 = (Button) popupWindow_view.findViewById(R.id.btn_hisAnalyze);


        btnLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileChooser();
            }
        });

        btnPic1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(MainActivity.this, Charts1Activity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        btnPic2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(MainActivity.this, Charts3Activity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private class ConnectRunnable implements Runnable {


        @Override
        public void run() {
            try {
                serverConnector.connect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class ScatterSeries implements XYSeries {
        private ArrayList<Point> points;
        private String title;

        ScatterSeries(ArrayList<Point> points, String title) {
            this.points = points;
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public int size() {
            return points.size();
        }

        @Override
        public Number getX(int index) {
            return points.get(index).x;
        }

        @Override
        public Number getY(int index) {
            return points.get(index).y;
        }
    }

    class DroneIcon implements XYSeries {
        public SimpleXYSeries droneIconHead;
        private ArrayList<Point> iconPoints;
        private String title;
        private Point pos;
        private double lineLength = 5;
        private double yaw = 0;

        DroneIcon(String title) {
            this.title = title;
            iconPoints = new ArrayList<Point>();
            initIcon();
        }

        public void initIcon() {
            pos = new Point();
            droneIconHead = new SimpleXYSeries("");
        }

        public void updateIcon(Point point, double yaw) {
            iconPoints.clear();

            this.yaw = yaw;
            double radians = Math.toRadians(this.yaw);
            double sina = Math.sin(radians) * lineLength;
            double cosa = Math.cos(radians) * lineLength;

            pos = point;
            Point head = new Point(pos.x.doubleValue() + cosa, pos.y.doubleValue() + sina);
            iconPoints.add(head);
            droneIconHead.clear();
            droneIconHead.addFirst(head.x, head.y);
            iconPoints.add(new Point(pos.x.doubleValue() - cosa, pos.y.doubleValue() - sina));
            iconPoints.add(new Point(pos.x.doubleValue(), pos.y.doubleValue()));
            iconPoints.add(new Point(pos.x.doubleValue() + sina, pos.y.doubleValue() - cosa));
            iconPoints.add(new Point(pos.x.doubleValue() - sina, pos.y.doubleValue() + cosa));
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public int size() {
            return iconPoints.size();
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



}

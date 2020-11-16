package com.demo.aircontrol;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.*;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.androidplot.util.PixelUtils;
import com.androidplot.xy.*;
import com.demo.aircontrol.util.model.ModelSurfaceView;
import com.demo.aircontrol.util.model.SceneLoader;
import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.flightcontroller.RTKState;
import dji.common.flightcontroller.virtualstick.FlightControlData;
import dji.common.flightcontroller.virtualstick.YawControlMode;
import dji.common.mission.hotpoint.HotpointHeading;
import dji.common.mission.hotpoint.HotpointMission;
import dji.common.mission.hotpoint.HotpointMissionEvent;
import dji.common.mission.hotpoint.HotpointMissionState;
import dji.common.mission.hotpoint.HotpointStartPoint;
import dji.common.mission.waypoint.*;
import dji.common.model.LocationCoordinate2D;
import dji.common.util.CommonCallbacks;
import dji.keysdk.FlightControllerKey;
import dji.keysdk.KeyManager;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.mission.MissionControl;
import dji.sdk.mission.hotpoint.HotpointMissionOperator;
import dji.sdk.mission.hotpoint.HotpointMissionOperatorListener;
import dji.sdk.mission.waypoint.WaypointMissionOperator;
import dji.sdk.mission.waypoint.WaypointMissionOperatorListener;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKInitEvent;
import dji.sdk.sdkmanager.DJISDKManager;
import org.andresoviedo.util.android.ContentUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static dji.keysdk.FlightControllerKey.HOME_LOCATION_LATITUDE;
import static dji.keysdk.FlightControllerKey.HOME_LOCATION_LONGITUDE;
import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    public static final String FLAG_CONNECTION_CHANGE = "dji_sdk_connection_change";
    private static final String TAG = MainActivity.class.getName();
    private static final String[] REQUIRED_PERMISSION_LIST = new String[]{
            Manifest.permission.VIBRATE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
    };
    private static final int REQUEST_PERMISSION_CODE = 12345;
    private static UAVState uavstate;
    private static BaseProduct mProduct;
    private Handler mHandler;
    private FlightController mFlightController = null;
    private WaypointMissionOperatorListener waypointListener;
    private HotpointMissionOperatorListener hotpointlistener;
    private WaypointMissionOperator waypointMissionOperator = null;
    private HotpointMissionOperator hotpointMissionOperator = null;
    //飞控数据
    private double droneLocationLat = 181, droneLocationLng = 181;
    private double droneRtkLat = 0, droneRtkLng = 0;
    private float droneRtkAlt = 0;
    private double droneLocationAlt = 0;
    private double droneAttitudeYaw = 0;
    private double droneAttitudePitch = 0;
    private double droneAttitudeRoll = 0;
    private double droneVelocity = 0;
    private boolean droneRtk = false;
    private boolean uavconnected = false;
    private boolean stopall = false;
    private Object stopallLock = new Object();
    private String droneFlightMode = "";
    private String droneTime;
    private ArrayList<String> timelist;
    private ArrayList<String> lnglist;
    private ArrayList<String> latlist;
    private ArrayList<String> altlist;
    private ArrayList<String> rolllist;
    private ArrayList<String> pitchlist;
    private ArrayList<String> yawlist;
    private PopupWindow popupWindowWay;
    private PopupWindow popupWindowHot;
    private PopupWindow popupWindowCon;
    private PopupWindow popupWindowCserver;
    private PopupWindow popupWindowClientid;
    private PopupWindow popupWindowReturnAlt;
    private PopupWindow popupWindowRotate;
    private PopupWindow popupWindowAutorotate;
    private String clientid = "0";
    private String missionparams;
    private HandlerThread serverHandlerThread;
    private Handler serverHandler;
    private SocketClient client;
    private int stopmission;
    private double waylng;
    private double waylat;
    private float wayalt;
    private float ralt;
    private float wayvel;
    private double hotlng;
    private double hotlat;
    private double hotalt;
    private float hotw;
    private double hotr;
    private String hotstart;
    private String hotcircles;
    private float autorotatew;
    private String arstart;
    private String arcircles;
    private String serveruri;
    //显示信息
    private TextView tLng;
    private TextView tLat;
    private TextView tAlt;
    private TextView tPitch;
    private TextView tRoll;
    private TextView tYaw;
    private TextView tState;
    private TextView uavconnectstate;
    private TextView serverconnectstate;
    private TextView textcid;
    private TextView textralt;
    private TextView tBattery;
    private TextView tRtks;
    private Button btwaypoint;
    private Button bthotpoint;
    private Button btcserver;
    private Button btclientid;
    private Button btereturnalt;
    private Button btnDemRecord;//记录经纬数据
    private Button btnDemOutput;//输出经纬数据至文件
    private TextView missioninfo;

    private int wexeccnt;
    private int hexeccnt;

    private int sendmsgmap[];
    private int revmsgmap[];



    SharedPreferences sp = null;

    //可视化相关
    ScatterSeries routeScatters;
    SimpleXYSeries heightSeries;
    DroneIcon droneIcon;
    ArrayList<Point> points;
    ArrayList<Point> roundPoints;
    boolean testStart = false;
    private XYPlot heightPlot;
    SceneLoader scene;
    private ModelSurfaceView gLView;
    private SimpleXYSeries circleSeries;
    private SimpleXYSeries axisSeries;
    private double minX;
    private double minY;
    private double maxX;
    private double maxY;
    private static double xyScale = 0.762; //yRange:xRange
    private double chartXRange;
    private double chartYRange;
    private double EPS = 1E-8;

    private static final int FILE_SELECT_CODE = 0;
    private Button btnHistory; //Button ...
    private Button btnHistoryRoute; //Button A
    private Button btnHistoryAnalyze; //Button C
    private XYPlot routePlot;

    private PopupWindow popupWindowHistory;


    private DroneData droneData;
    private Button btstop;
    private List<String> missingPermission = new ArrayList<>();
    private AtomicBoolean isRegistrationInProgress = new AtomicBoolean(false);
    private Runnable updateRunnable = new Runnable() {

        @Override
        public void run() {
            Intent intent = new Intent(FLAG_CONNECTION_CHANGE);
            sendBroadcast(intent);
        }
    };

    private Button btnLoad; //Button A

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // When the compile and target version is higher than 22, please request the following permission at runtime to ensure the SDK works well.
        if (!MyBuildConfig.isDebug) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                checkAndRequestPermissions();
            }
        }
        setContentView(R.layout.activity_main);
        if (!MyBuildConfig.isDebug) {
            //Initialize DJI SDK Manager
            mHandler = new Handler(Looper.getMainLooper());
        }
        //3d model
        ConstraintLayout layout = findViewById(R.id.modelBlock);
        try {
            gLView = new ModelSurfaceView(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        layout.addView(gLView);

        droneData = droneData.getInstance();
        droneData.setContext(this);
        //copyAssets();

        initUI();

//        new Thread(() -> {
//            do {
//                try {
//                    Thread.sleep(500);
//                    if (reconnectserver && client != null && client.connected == 0){
//                        connectServer(serveruri);
//                    }
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            } while (true);
//        }).start();
    }

    private void initUI() {

        //显示信息
        tLng = (TextView) findViewById(R.id.textLng);
        tLat = (TextView) findViewById(R.id.textLat);
        tAlt = (TextView) findViewById(R.id.textAlt);
        tPitch = (TextView) findViewById(R.id.textPitch);
        tRoll = (TextView) findViewById(R.id.textRoll);
        tYaw = (TextView) findViewById(R.id.textYaw);
        tBattery = (TextView) findViewById(R.id.textBattery);
        tRtks = (TextView) findViewById(R.id.textRtks);
        tState = (TextView) findViewById(R.id.textuavstate);
        uavconnectstate = (TextView) findViewById(R.id.uavconnectstate);
        serverconnectstate = (TextView) findViewById(R.id.serverconnectstate);
        textcid = (TextView) findViewById(R.id.textcid);
        textralt = (TextView) findViewById(R.id.returnalt);

        // ---------------设置单击事件-------------------------
        btnDemRecord = (Button) findViewById(R.id.btn_record);
        btnDemRecord.setOnClickListener(this);//记录经纬数据

        btnDemOutput = (Button) findViewById(R.id.btn_output);
        btnDemOutput.setOnClickListener(this);//输出经纬数据至文件

        btnHistory = (Button) findViewById(R.id.btn_history);
        btnHistory.setOnClickListener(this);
//        btnLoad = (Button) findViewById(R.id.btn_load);
//        btnLoad.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                showFileChooser();
//            }
//        });
//
//        btnPic1 = (Button) findViewById(R.id.btn_pic1);
//        btnPic1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(MainActivity.this, Charts1Activity.class);
//                startActivity(intent);
//            }
//        });
//
//
//        btnPic2 = (Button) findViewById(R.id.btn_pic2);
//        btnPic2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(MainActivity.this, Charts2Activity.class);
//                startActivity(intent);
//            }
//        });
//
//        btnData = (Button) findViewById(R.id.btn_data);
//        btnData.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(MainActivity.this, Charts3Activity.class);
//                startActivity(intent);
//            }
//        });


        btwaypoint = (Button) findViewById(R.id.btn_waypoint);
        bthotpoint = (Button) findViewById(R.id.btn_hotpoint);
        btcserver = (Button) findViewById(R.id.btn_cserver);
        btclientid = (Button) findViewById(R.id.btn_eid);
        btereturnalt = (Button) findViewById(R.id.btn_ereturnalt);

        btwaypoint.setOnClickListener(this);
        bthotpoint.setOnClickListener(this);
        btcserver.setOnClickListener(this);
        btclientid.setOnClickListener(this);
        btereturnalt.setOnClickListener(this);

        missioninfo = (TextView) findViewById(R.id.missioninfo);
        btstop = (Button) findViewById(R.id.btn_stop);


        timelist = new ArrayList<>();
        lnglist = new ArrayList<>();
        latlist = new ArrayList<>();
        altlist = new ArrayList<>();
        yawlist = new ArrayList<>();
        pitchlist = new ArrayList<>();
        rolllist = new ArrayList<>();

        sp = this.getSharedPreferences("params", Context.MODE_PRIVATE);

        uavstate = UAVState.NONE;
        stopmission = 0;

        clientid = sp.getString("clientid", "0");
        textcid.setText("id："+clientid);

        ralt = sp.getFloat("returnalt", 25);
        textralt.setText(Float.toString(ralt));

        sendmsgmap = new int[100000];
        revmsgmap = new int[100000];
        for(int i=0; i<100000; i++){
            sendmsgmap[i] = 0;
            revmsgmap[i] = 0;
        }


        //可视化设置
        heightPlot = (XYPlot) findViewById(R.id.height_plot);
        routePlot = (XYPlot) findViewById(R.id.route_plot);
        initChartData();

        loadModelFromAssets();
        scene = new SceneLoader(this);
        scene.init();
        if (MyBuildConfig.isDebug) {
            droneData.loadFakeGPSData();
            tRoll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    testChart();
                }
            });
        }

    }

    @Override
    public void onResume() {
        Log.e(TAG, "onResume");
        super.onResume();
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

//                if (mFlightController != null) {
//                    //                // 原地悬停旋转
//                    mFlightController.setVirtualStickModeEnabled(true, new CommonCallbacks.CompletionCallback() {
//                        @Override
//                        public void onResult(DJIError djiError) {
////                    System.out.println(djiError.getDescription());
//                        }
//                    });
//
//                    try {
//                        sleep(5000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    mFlightController.setYawControlMode(YawControlMode.ANGULAR_VELOCITY);
//
//                }
//
//                if (mFlightController != null && mFlightController.isVirtualStickControlModeAvailable()) {
//
//
//
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            missioninfo.setText("开始执行原地旋转任务!\n" + "旋转速度：" + autorotatew);
////                            Message msg = new Message();
////                            msg.what = 1;
////                            btstophandler.sendMessage(msg);
//                            uavstate = UAVState.AREXEC;
//                            while (true){
//                                if (stopmission == 1){
//                                    stopmission = 0;
//                                    missioninfo.setText("");
////                                    msg.what = 1;
////                                    btstophandler.sendMessage(msg);
//                                    uavstate = UAVState.NONE;
//                                    break;
//                                }
//                                try {
//                                    sleep(100);
//                                } catch (InterruptedException e) {
//                                    e.printStackTrace();
//                                }
//                                YawControlMode tmpyaw =  mFlightController.getYawControlMode();
//                                System.out.println(tmpyaw);
////
//                                mFlightController.sendVirtualStickFlightControlData(new FlightControlData((float)droneAttitudePitch, (float)droneAttitudeRoll, 5, (float)droneLocationAlt), new CommonCallbacks.CompletionCallback() {
//                                    @Override
//                                    public void onResult(DJIError djiError) {
////                                        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++"+djiError.getDescription());
//                                    }
//                                });
////
//                            }
////
////
////
//                        }
//                    }).start();
//
//
//                }
//                outputGPStoFile();
                outputGPStoFile();
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
            case R.id.btn_eid:
                initPopupWindowClientid();
                popupWindowClientid.showAtLocation(findViewById(R.id.main_body), Gravity.CENTER, 0, 0);
                break;

            case R.id.btn_ereturnalt:
                initPopupWindowReturnAlt();
                popupWindowReturnAlt.showAtLocation(findViewById(R.id.main_body), Gravity.CENTER, 0, 0);
                break;
        }
    }

    private void sendMessage(String msg){
        new Thread(() -> {
            long index = System.currentTimeMillis() % 100000;
            while (sendmsgmap[(int)index] != 0){
                index = System.currentTimeMillis() % 100000;
            }

            sendmsgmap[(int)index] = 1;
            int cnt = 0;
            try{
                client.send(msg + "-" + index);
                System.out.println("s:" + msg + "-" + index);
                while(sendmsgmap[(int)index] != 2 && cnt < 50){
                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    cnt += 1;
                    client.send(msg + "-" + index);
                    System.out.println("s:" + msg + "-" + index);
                }
            } catch (Exception e){
                showToast("服务器未连接");
                runOnUiThread(()->{
                    serverconnectstate.setText("服务器：未连接");
                });
            }
            if (cnt >= 50){
                showToast("服务器连接断开");
                runOnUiThread(()->{
                    serverconnectstate.setText("服务器：未连接");
                });
            }

        }).start();
    }



    private void outputGPStoFile() {
        Calendar now = Calendar.getInstance();

        String ftime = "" + now.get(Calendar.YEAR) + '_' + (now.get(Calendar.MONTH)+1) + '_' + now.get(Calendar.DAY_OF_MONTH) + '-' + now.get(Calendar.HOUR_OF_DAY) + ':' + now.get(Calendar.MINUTE);

        String state;
        String path;
        //获取内部存储根目录
        File inpath = android.os.Environment.getDataDirectory();
        //2 确认sdcard的存在
        state = android.os.Environment.getExternalStorageState();
        if (state.equals(android.os.Environment.MEDIA_MOUNTED)) {
            //3 获取扩展存储设备的文件目录
            path = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
            String filefullname = path + "/gpsdata_" + ftime + ".txt";


            try {
                // FileWriter fileWriter = new FileWriter(path+"/gpsdata.txt",true);
                FileWriter fileWriter = new FileWriter(filefullname, true);


                BufferedWriter bw = new BufferedWriter(fileWriter);
                // 输出坐标数量
                bw.newLine();
                bw.write("Time    Lng    Lat    Alt    Yaw    Pitch    Roll");
                bw.newLine();
                //遍历集合
                int len = timelist.size();
                for (int i = 0; i < len; i++) {
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
                showToast("File saved successfully!");
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "file is not exist", Toast.LENGTH_SHORT).show();
            }
        } else ///没有sd卡 就用手机本身内存
        {
            //getFilesDir
            File sdFie = android.os.Environment.getDataDirectory();
            path = android.os.Environment.getDataDirectory().getAbsolutePath();//获取手机内存绝对路径

            try {
                FileWriter fileWriter = new FileWriter(path + "/gpsdata_" + ftime + ".txt", true);
                BufferedWriter bw = new BufferedWriter(fileWriter);
                // 输出坐标数量
                bw.write("");
                bw.newLine();
                bw.write("Time    Lng    Lat    Alt    Yaw    Pitch    Roll");
                bw.newLine();
                //遍历集合
                int len = timelist.size();
                for (int i = 0; i < len; i++) {
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
                showToast("File saved successfully!");
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "file is not exist", Toast.LENGTH_SHORT).show();
            }
        }
    }


    public void connectServer(String struri) {
        serverHandlerThread = new HandlerThread("MainActivity", android.os.Process.THREAD_PRIORITY_BACKGROUND);
        serverHandlerThread.start();
        serverHandler = new Handler(serverHandlerThread.getLooper());
        URI uri = URI.create(struri);

        client = new SocketClient(uri) {
            @Override
            public void onMessage(String data) {

                runOnUiThread(() -> {
                    System.out.println("r:" + data);
                    String [] datasplit = data.split("-");

                    if (revmsgmap[Integer.parseInt(datasplit[1])] == 0){
                        revmsgmap[Integer.parseInt(datasplit[1])] = 1;

                        if (data.contains("connectsuccess")) {
                            showToast("连接成功");
                            serverconnectstate.setText("服务器：已连接");
                        } else if (data.contains("connecterror")) {
                            showToast("连接失败");
                        } else if (data.contains("missionparams")){
                            missionparams = datasplit[0];
                            showToast("gotmission");
                            sendMessage(clientid + ",gotmission");
                        } else if (data.contains("gotmessage")){
                            sendmsgmap[Integer.parseInt(datasplit[1])] = 2;
                        } else if (data.contains("gotostartpoint")){
                            new Thread(() -> {
                                try {
                                    missionmanage(1, 2);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }).start();
                        }
//                        else if (data.contains("takeoff")){
//                            new Thread(() -> {
//                                try {
//                                    missionmanage(0, 0);
//                                } catch (InterruptedException e) {
//                                    e.printStackTrace();
//                                }
//                            }).start();
//                        }
                        else if (data.contains("execmission")){
                            new Thread(() -> {
                                try {
                                    missionmanage(3, 3);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }).start();
                        } else if (data.contains("startlanding")){
                            mFlightController.startLanding(new CommonCallbacks.CompletionCallback() {
                                @Override
                                public void onResult(DJIError djiError) {
                                    showResultToast(djiError);
                                }
                            });
                        }else if (data.contains("signaltest")) {
                            new Thread(() -> {
                                signaltest(Integer.parseInt(datasplit[0].split(",")[1]));
                            }).start();
                        } else if (data.contains("stopall")){
                            stopallmission();
                        } else if (data.contains("toreturn")){
                            new Thread(() -> {
                                try {
                                    while (uavstate != UAVState.NONE) {
                                        sleep(2000);
                                    }
                                    missionmanage(4, 4);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }).start();
                        }

                        if (!data.contains("gotmessage") && !data.contains("startlanding")){

                            new Thread(() -> {
                                for (int i=0; i<30; i++){
                                    client.send(clientid + ",gotmessage-" + datasplit[1]);
                                    try {
                                        sleep(100);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).start();
                        }

                    }


                });
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                runOnUiThread(() -> {
                    serverconnectstate.setText("服务器：未连接");
                });
            }
        };
        serverHandler.post(new ConnectRunnable());
    }

    protected void initPopupWindowReturnAlt() {
        View v = getLayoutInflater().inflate(R.layout.pop_returnalt, null, false);
        popupWindowReturnAlt = new PopupWindow(v, 1000, 500, true);
        popupWindowReturnAlt.setFocusable(true);

        popupWindowReturnAlt.setOutsideTouchable(true);  //设置点击屏幕其它地方弹出框消失
        popupWindowReturnAlt.setBackgroundDrawable(new BitmapDrawable());

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.5f;//设置阴影透明度
        getWindow().setAttributes(lp);
        popupWindowReturnAlt.setOnDismissListener(new PopupWindow.OnDismissListener() {

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
        final TextView txtralt = (TextView) v.findViewById(R.id.etreturnalt);

        txtralt.setText(String.valueOf(sp.getFloat("returnalt", 25)));

        btyes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ralt = Float.parseFloat(txtralt.getText().toString());
                textralt.setText(Float.toString(ralt));
                SharedPreferences.Editor editor = sp.edit();
                editor.putFloat("returnalt", Float.parseFloat(txtralt.getText().toString()));
                editor.commit();
                popupWindowReturnAlt.dismiss();

            }
        });

        btno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindowReturnAlt.dismiss();
            }
        });
    }

    protected void initPopupWindowClientid() {
        View v = getLayoutInflater().inflate(R.layout.pop_clientid, null, false);
        popupWindowClientid = new PopupWindow(v, 1000, 500, true);
        popupWindowClientid.setFocusable(true);

        popupWindowClientid.setOutsideTouchable(true);  //设置点击屏幕其它地方弹出框消失
        popupWindowClientid.setBackgroundDrawable(new BitmapDrawable());

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.5f;//设置阴影透明度
        getWindow().setAttributes(lp);
        popupWindowClientid.setOnDismissListener(new PopupWindow.OnDismissListener() {

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
        final TextView txtid = (TextView) v.findViewById(R.id.etid);

        txtid.setText(sp.getString("clientid", ""));

        btyes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clientid = txtid.getText().toString();
                textcid.setText("id："+clientid);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("clientid", txtid.getText().toString());
                editor.commit();
                popupWindowClientid.dismiss();

            }
        });

        btno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindowClientid.dismiss();
            }
        });
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

        txtaddr.setText(sp.getString("addr", ""));

        btyes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("addr", txtaddr.getText().toString());
                editor.commit();
                popupWindowCserver.dismiss();
                serveruri = "ws://" + txtaddr.getText().toString() + "/link/";
                try {
                    connectServer(serveruri);
//                    connectServer("ws://192.168.43.205:8000/link/");
                    sleep(3000);
                    sendMessage("#" + clientid);

                } catch (Exception e) {
                    showToast("连接失败");
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

    protected  void signaltest(int num){
        String[] missions = missionparams.split(";");
        String[] s = missions[num].split(",");
        boolean res = false;
//        if (num == 0){
//            res = uploadwaypointmission(0, new String[1]);
//        }
//        else {
            if (missions[num].contains("way")){
                if (num == 4) res = uploadwaypointmission(3, s);      // 返航
                else if (num == 1) res = uploadwaypointmission(2, s);
                else if (num == 3) res = uploadwaypointmission(4, s);
            }
            else if (missions[num].contains("hot")){
                res = true;
            }
//        }

        uavstate = UAVState.NONE;

        if (res){
            sendMessage(clientid+",signalok");
        }
        else {
            sendMessage(clientid+",signalerror");
        }
    }

    protected synchronized void missionmanage(int start, int end) throws InterruptedException {
        String[] missions = missionparams.split(";");

        for (int i=start; i<=end; i++){
            while (uavstate != UAVState.NONE) {
                if (uavstate == UAVState.ERROR) uavstate = UAVState.NONE;

                sleep(2000);
            }
            uavstate = UAVState.READY;

//            if (i == 0){
//                execwaypointmission();      // 起飞
//            }
//            else {

                String[] s = missions[i].split(",");
                if (missions[i].contains("way")) {
//                waylng = Double.parseDouble(s[1]);
//                waylat = Double.parseDouble(s[2]);
//                wayalt = Float.parseFloat(s[3]);
//                wayvel = Float.parseFloat(s[4]);
//                    if (i == 4) execwaypointmission(3, s);      // 返航
//                    else if (i == 1) execwaypointmission(2, s);
//                    else if (i == 3) execwaypointmission(4, s);
                    execwaypointmission();
                } else if (missions[i].contains("hot")) {
                    hotlng = Double.parseDouble(s[1]);
                    hotlat = Double.parseDouble(s[2]);
                    hotalt = Float.parseFloat(s[3]);
                    hotr = Double.parseDouble(s[4]);
                    hotw = Float.parseFloat(s[5]);
                    hotstart = s[6];
                    hotcircles = s[7];
                    exechotpointmission();
                } else if (missions[i].contains("rotate")) {
                    // 有机头转向
//                int type = 0;
//                if (s[1].contains("西")) type = 41;
//                else if (s[1].contains("北")) type = 42;
//                else if (s[1].contains("东")) type = 43;
//                else if (s[1].contains("南")) type = 44;
//                final int t = type;
//                execrotatemission(t);


                    // 去掉机头转向
                    uavstate = UAVState.NONE;

                } else if (missions[i].contains("ar")) {
                    // 去掉悬停旋转
//                    autorotatew = Float.parseFloat(s[1]);
//                    arstart = s[2];
//                    arcircles = s[3];
//                    execarmission();
//                    try {
//                        sleep(3000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }


                    // 去掉悬停旋转
                    uavstate = UAVState.NONE;
                }

//            }


            while (uavstate != UAVState.NONE) {
                if (uavstate == UAVState.ERROR){
                    sendMessage(clientid + ",execerror");
                    uavstate = UAVState.NONE;
                    return ;
                }

                sleep(200);
            }

//            if (i == 0){
//                sendMessage(clientid + ",alreadytakeoff");
//            }
//            else
            if (i == 2){
                sendMessage(clientid + ",arrivestartpoint");
            } else if (i == 3) {
                sendMessage(clientid + ",finishmission");
            } else if (i == 4) {
                sendMessage(clientid + ",returned");
            }

            synchronized (stopallLock){
                if (stopall) {
                    stopall = false;
                    return ;
                }
            }
        }
    }

    private void stopallmission(){
        synchronized (stopallLock){
            stopall = true;
        }
        if (uavstate == UAVState.WAYEXEC) {
            waypointMissionOperator.stopMission(new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    showResultToast(djiError);
                }
            });
        } else if (uavstate == UAVState.HOTEXEC) {
            hotpointMissionOperator.stop(new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    showResultToast(djiError);
                }
            });
        } else if (uavstate == UAVState.ROTATEEXEC || uavstate == UAVState.AREXEC) {
            stopmission = 1;
        }
    }

    protected boolean uploadwaypointmission(int type, String[] s){
        WaypointMission waypointMission = null;
        WaypointMission.Builder builder = new WaypointMission.Builder();
        builder.maxFlightSpeed(10f);
        builder.setExitMissionOnRCSignalLostEnabled(false);
        builder.finishedAction(WaypointMissionFinishedAction.NO_ACTION);
        builder.flightPathMode(WaypointMissionFlightPathMode.NORMAL);
        builder.gotoFirstWaypointMode(WaypointMissionGotoWaypointMode.SAFELY);
        builder.headingMode(WaypointMissionHeadingMode.AUTO);
        builder.repeatTimes(1);
        List<Waypoint> waypointList = new ArrayList<>();
        if (type == 0){
            builder.autoFlightSpeed(3);
            waypointList.add(new Waypoint(droneLocationLat, droneLocationLng, 3));
            waypointList.add(new Waypoint(droneLocationLat, droneLocationLng, 6));
        }
        else if (type == 1){
            builder.autoFlightSpeed(wayvel);
            waypointList.add(new Waypoint((droneLocationLat+waylat)/2, (droneLocationLng+waylng)/2, ((float)droneLocationAlt-wayalt)/2+wayalt));
            waypointList.add(new Waypoint(waylat, waylng, wayalt));
        }
        else if (type == 2){    //  飞到起点
            int len = Integer.parseInt(s[1]);
            float vel = Float.parseFloat(s[2]);
            int staytime;
            builder.autoFlightSpeed(vel);
            waypointList.add(new Waypoint(droneLocationLat, droneLocationLng, 6));
            Waypoint eachWaypoint;
            for (int i=0; i<len; i++){
                eachWaypoint = new Waypoint(Double.parseDouble(s[i*4+4]), Double.parseDouble(s[i*4+3]), Float.parseFloat(s[i*4+5]));
                staytime = Integer.parseInt(s[i*4+6]);
                if (staytime > 0){
                    eachWaypoint.addAction(new WaypointAction(WaypointActionType.STAY, staytime));
                }
                waypointList.add(eachWaypoint);
            }
        }
        else if (type == 3){    // 返航
            int len = Integer.parseInt(s[1]);
            float vel = Float.parseFloat(s[2]);
            int staytime;
            builder.autoFlightSpeed(vel);
            waypointList.add(new Waypoint(droneLocationLat, droneLocationLng, (float)droneLocationAlt));
            waypointList.add(new Waypoint((droneLocationLat+Double.parseDouble(s[4]))/2, (droneLocationLng+Double.parseDouble(s[3]))/2, ((float)droneLocationAlt+ralt)/2));
            Waypoint eachWaypoint;
            for (int i=0; i<len; i++){
                eachWaypoint = new Waypoint(Double.parseDouble(s[i*4+4]), Double.parseDouble(s[i*4+3]), ralt);
                staytime = Integer.parseInt(s[i*4+6]);
                if (staytime > 0){
                    eachWaypoint.addAction(new WaypointAction(WaypointActionType.STAY, staytime));
                }
                waypointList.add(eachWaypoint);
            }
        }
        else if (type == 4){    // 航点飞行
            int len = Integer.parseInt(s[1]);
            float vel = Float.parseFloat(s[2]);
            int staytime;
            builder.autoFlightSpeed(vel);
            waypointList.add(new Waypoint(droneLocationLat, droneLocationLng, Float.parseFloat(s[5])));
            Waypoint eachWaypoint;
            for (int i=0; i<len; i++){
                eachWaypoint = new Waypoint(Double.parseDouble(s[i*4+4]), Double.parseDouble(s[i*4+3]), Float.parseFloat(s[i*4+5]));
                staytime = Integer.parseInt(s[i*4+6]);
                if (staytime > 0){
                    eachWaypoint.addAction(new WaypointAction(WaypointActionType.STAY, staytime));
                }
                waypointList.add(eachWaypoint);
            }
        }
        builder.waypointList(waypointList).waypointCount(waypointList.size());
        waypointMission = builder.build();
        DJIError djiError = waypointMissionOperator.loadMission(waypointMission);
        showResultToast(djiError);
        toError(djiError);
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int ucnt = 0;
        int ecnt = 0;

        while(waypointMissionOperator.getCurrentState() != WaypointMissionState.READY_TO_UPLOAD){
            if (ucnt >= 14) {
                uavstate = UAVState.ERROR;
            }
            try{
                if (uavstate == UAVState.ERROR) {
                    showToast("load mission error");
                    sleep(500);
                    return false ;
                } else {
                    sleep(500);
                    ucnt += 1;
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        waypointMissionOperator.uploadMission(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                showResultToast(djiError);
                toError(djiError);
            }
        });
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while(waypointMissionOperator.getCurrentState() != WaypointMissionState.READY_TO_EXECUTE){
            if (ecnt >= 14){
                uavstate = UAVState.ERROR;
            }
            try{
                if (uavstate == UAVState.ERROR) {
                    showToast("upload mission error");
                    sleep(500);
                    return false;
                }else {
                    sleep(500);
                    ecnt += 1;
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        return true;
    }

    protected void execwaypointmission() {
        waypointMissionOperator.startMission(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                showResultToast(djiError);
                toError(djiError);
            }
        });
    }

    protected void exechotpointmission() {
        // 圆形绕飞
        HotpointMission hotpointMission = new HotpointMission();
        hotpointMission.setHotpoint(new LocationCoordinate2D(hotlat, hotlng));
        hotpointMission.setAltitude(hotalt);
        hotpointMission.setRadius(hotr);
        hotpointMission.setAngularVelocity(hotw);
        HotpointStartPoint startPoint;
        if (hotstart.equals("东")) startPoint = HotpointStartPoint.EAST;
        else if (hotstart.equals("南")) startPoint = HotpointStartPoint.SOUTH;
        else if (hotstart.equals("西")) startPoint = HotpointStartPoint.WEST;
        else if (hotstart.equals("北")) startPoint = HotpointStartPoint.NORTH;
        else startPoint = HotpointStartPoint.NEAREST;
        hotpointMission.setStartPoint(startPoint);
        HotpointHeading heading = HotpointHeading.TOWARDS_HOT_POINT;
        hotpointMission.setHeading(heading);
        try {
            sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        hotpointMissionOperator.startMission(hotpointMission, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                showResultToast(djiError);
                toError(djiError);
            }
        });

        ArrayList<Double> stoppoint = new ArrayList<>();
        if (hotcircles.equals("1")){
            if (hotstart.equals("东")) stoppoint.add(-90.0);
            else if (hotstart.equals("南")) stoppoint.add(0.0);
            else if (hotstart.equals("西")) stoppoint.add(90.0);
            else if (hotstart.equals("北")) {
                stoppoint.add(180.0);
                stoppoint.add(-180.0);
            }
        } else if (hotcircles.equals("1/2")) {
            if (hotstart.equals("东")) stoppoint.add(90.0);
            else if (hotstart.equals("南")) {
                stoppoint.add(180.0);
                stoppoint.add(-180.0);
            } else if (hotstart.equals("西")) stoppoint.add(-90.0);
            else if (hotstart.equals("北")) stoppoint.add(0.0);
        } else if (hotcircles.equals("1/4")){
            if (hotstart.equals("东")) {
                stoppoint.add(180.0);
                stoppoint.add(-180.0);
                stoppoint.add(0.0);
            }
            else if (hotstart.equals("南")) {
                stoppoint.add(90.0);
                stoppoint.add(-90.0);
            } else if (hotstart.equals("西")) {
                stoppoint.add(180.0);
                stoppoint.add(-180.0);
                stoppoint.add(0.0);
            }
            else if (hotstart.equals("北")) {
                stoppoint.add(90.0);
                stoppoint.add(-90.0);
            }
        }


        try {
            sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        int len = stoppoint.size();
        while (true){
            if (uavstate == UAVState.ERROR || uavstate == UAVState.NONE) return;

            try {
                sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (int i=0; i<len; i++){
                if (Math.abs(stoppoint.get(i)-droneAttitudeYaw) < 3){
                    hotpointMissionOperator.stop(new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            showResultToast(djiError);
                        }
                    });
                    return ;
                }
            }
        }
    }

    protected void execarmission() {

        if (mFlightController != null) {
            //                // 原地悬停旋转
            mFlightController.setVirtualStickModeEnabled(true, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
//                    System.out.println(djiError.getDescription());
                }
            });

            while (!mFlightController.isVirtualStickAdvancedModeEnabled()) {
                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mFlightController.setVirtualStickAdvancedModeEnabled(true);
            }

            try {
                sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mFlightController.setYawControlMode(YawControlMode.ANGULAR_VELOCITY);

        }

        if (mFlightController != null && mFlightController.isVirtualStickControlModeAvailable()) {


            new Thread(() -> {
//                missioninfo.setText("开始执行原地旋转任务!\n" + "旋转速度：" + autorotatew);
                uavstate = UAVState.AREXEC;
                showToast("Execution started!");

                while (true) {
                    if (stopmission == 1) {
                        stopmission = 0;
                        uavstate = UAVState.NONE;
                        showToast("Execution finished!");

                        mFlightController.setVirtualStickModeEnabled(false, new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onResult(DJIError djiError) {
//                    System.out.println(djiError.getDescription());
                            }
                        });
                        break;
                    }
                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
//                                YawControlMode tmpyaw =  mFlightController.getYawControlMode();
//                                System.out.println(tmpyaw);
//
                    mFlightController.sendVirtualStickFlightControlData(new FlightControlData((float) droneAttitudePitch, (float) droneAttitudeRoll, -autorotatew, (float) droneLocationAlt), djiError -> {
                        showResultToast(djiError);
                        // toError(djiError);
                    });
//
                }
//
//
//
            }).start();

            ArrayList<Double> stoppoint = new ArrayList<>();
            if (arcircles.equals("1")){
                if (arstart.equals("东")) stoppoint.add(90.0);
                else if (arstart.equals("南")) {
                    stoppoint.add(180.0);
                    stoppoint.add(-180.0);
                } else if (arstart.equals("西")) stoppoint.add(-90.0);
                else if (arstart.equals("北")) stoppoint.add(0.0);
            } else if (arcircles.equals("1/2")) {
                if (arstart.equals("东")) stoppoint.add(-90.0);
                else if (arstart.equals("南")) stoppoint.add(0.0);
                else if (arstart.equals("西")) stoppoint.add(90.0);
                else if (arstart.equals("北")) {
                    stoppoint.add(180.0);
                    stoppoint.add(-180.0);
                }
            } else if (arcircles.equals("1/4")){
                if (arstart.equals("东")) {
                    stoppoint.add(180.0);
                    stoppoint.add(-180.0);
                    stoppoint.add(0.0);
                }
                else if (arstart.equals("南")) {
                    stoppoint.add(90.0);
                    stoppoint.add(-90.0);
                } else if (arstart.equals("西")) {
                    stoppoint.add(180.0);
                    stoppoint.add(-180.0);
                    stoppoint.add(0.0);
                }
                else if (arstart.equals("北")) {
                    stoppoint.add(90.0);
                    stoppoint.add(-90.0);
                }
            }

            if (arcircles.equals("1")){
                try {
                    sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            int len = stoppoint.size();
            while (true){
                try {
                    sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                for (int i=0; i<len; i++){
                    if (Math.abs(stoppoint.get(i)-droneAttitudeYaw) < 3){
                        stopmission = 1;
                        return ;
                    }
                }
            }
        }
    }

    protected void execrotatemission(int type){
        final float rotatedir = (type - 40) * 90 - 180;


        if (mFlightController != null) {
            //                // 原地悬停旋转
            mFlightController.setVirtualStickModeEnabled(true, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
//                    System.out.println(djiError.getDescription());
                }
            });

            while (!mFlightController.isVirtualStickAdvancedModeEnabled()) {
                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mFlightController.setVirtualStickAdvancedModeEnabled(true);
            }

            try {
                sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mFlightController.setYawControlMode(YawControlMode.ANGLE);

        }

        if (mFlightController != null && mFlightController.isVirtualStickControlModeAvailable()) {


            new Thread(() -> {
                String dir[] = {"西", "北", "东", "南"};
//                missioninfo.setText("开始执行无人机转向任务！\n机头转向：" + dir[type - 41]);
                uavstate = UAVState.ROTATEEXEC;
                showToast("Execution started!");

                int outtag = 0;
                while (outtag < 20) {
                    if (stopmission == 1) {
                        break;
                    }
                    if (type != 44 && Math.abs(rotatedir - droneAttitudeYaw) < 0.1 || type == 44 && (Math.abs(rotatedir - Math.abs(droneAttitudeYaw)) < 0.1)) outtag += 1;
                    else outtag = 0;
                    try {
                        sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
//                                YawControlMode tmpyaw =  mFlightController.getYawControlMode();
//                                System.out.println(tmpyaw);
//
                    mFlightController.sendVirtualStickFlightControlData(new FlightControlData((float) droneAttitudePitch, (float) droneAttitudeRoll, rotatedir, (float) droneLocationAlt), new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            showResultToast(djiError);
                            // toError(djiError);
                        }
                    });
//
                }
                stopmission = 0;
//                missioninfo.setText("未执行飞行任务");
                uavstate = UAVState.NONE;
                mFlightController.setVirtualStickModeEnabled(false, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
//                    System.out.println(djiError.getDescription());
                    }
                });
                showToast("Execution finished!");
//
//
//
            }).start();


        }
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

        if (type == 1) {
            txtcontitle.setText("中止执行任务");
        }
        if (type == 21){
            txtcontitle.setText("发送指令");
        }

        //可视化
        if (type == 30) {
            addCircle(new Point(hotlng, hotlat), hotr);
        }

        btyes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                String str;
//                String res;
                popupWindowCon.dismiss();

                try {
                    if (type == 1) {
                        stopallmission();
                    } else if (type / 10 == 2) {
                        if (type == 20){
                            new Thread(() -> {
                                execwaypointmission();
                            }).start();
                            popupWindowWay.dismiss();
                        }
                        else if (type == 21){
                            new Thread(() -> {
                                if (uploadwaypointmission(1, new String[1]))
                                {
                                    showToast("发送指令成功");

                                }else {
                                    showToast("发送指令失败");
//                                    View popupWindow_view = getLayoutInflater().inflate(R.layout.pop_way, null,
//                                            false);
//                                    CheckBox cbss = (CheckBox) popupWindow_view.findViewById(R.id.sendsuccess);
//                                    cbss.setChecked(false);

                                }
                                uavstate = UAVState.NONE;
                            }).start();
                        }

                    } else if (type / 10 == 3) {
                        //TODO:bugfix
                        double xr = hotr / 85360.64873;
                        addCircle(new Point(hotlng, hotlat), xr);
                        new Thread(() -> {
                            hotcircles = "1";
                            exechotpointmission();
                        }).start();

                        popupWindowHot.dismiss();

                    } else if (type / 10 == 4) {
                        popupWindowRotate.dismiss();

                        new Thread(() -> {
                            execrotatemission(type);
                        }).start();

//                        if (mFlightController != null && mFlightController.isVirtualStickControlModeAvailable()) {
//                            new Thread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    while (Math.abs(rotatedir - droneAttitudeYaw) > 1e-4){
//                                        if (stopmission == 1){
//                                            stopmission = 0;
//                                            missioninfo.setText("");
////                                            msg.what = 0;
////                                            btstophandler.sendMessage(msg);
//                                            uavstate = UAVState.NONE;
//                                            break;
//                                        }
//
//                                        try {
//                                            sleep(100);
//                                        } catch (InterruptedException e) {
//                                            e.printStackTrace();
//                                        }
//                                        new Thread(new Runnable() {
//                                            @Override
//                                            public void run() {
//                                                mFlightController.sendVirtualStickFlightControlData(new FlightControlData((float)droneAttitudePitch, (float)droneAttitudeRoll, rotatedir, (float)droneLocationAlt), new CommonCallbacks.CompletionCallback() {
//                                                    @Override
//                                                    public void onResult(DJIError djiError) {
//                                                        showResultToast(djiError);
//                                                    }
//                                                });
//                                            }
//                                        }).start();
//                                    }
//                                }
//                            }).start();
//                        }

                    } else if (type / 10 == 5) {
                        popupWindowAutorotate.dismiss();
                        new Thread(() -> {
                            arstart = "东";
                            arcircles = "1";
                            execarmission();
                        }).start();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showToast("失败");
                }
            }
        });

        btno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindowCon.dismiss();
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


        /* pop.xml视图里面的控件 */
        Button btwgo = (Button) popupWindow_view.findViewById(R.id.bt_wgo);
        final EditText etlng = (EditText) popupWindow_view.findViewById(R.id.et_lng);
        final EditText etlat = (EditText) popupWindow_view.findViewById(R.id.et_lat);
        final EditText etalt = (EditText) popupWindow_view.findViewById(R.id.et_alt);
        final EditText etvel = (EditText) popupWindow_view.findViewById(R.id.et_vel);
        Button btsendmsg = (Button) popupWindow_view.findViewById(R.id.sendmsg);
//        CheckBox cbss = (CheckBox) popupWindow_view.findViewById(R.id.sendsuccess);

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.5f;//设置阴影透明度
        getWindow().setAttributes(lp);
        popupWindowWay.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("waylng", etlng.getText().toString());
                editor.putString("waylat", etlat.getText().toString());
                editor.putString("wayalt", etalt.getText().toString());
                editor.putString("wayvel", etvel.getText().toString());
                editor.commit();

                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1f;
                getWindow().setAttributes(lp);
            }
        });


        etlng.setText(sp.getString("waylng", ""));
        etlat.setText(sp.getString("waylat", ""));
        etalt.setText(sp.getString("wayalt", ""));
        etvel.setText(sp.getString("wayvel", ""));

        btwgo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    if (waypointMissionOperator.getCurrentState() == WaypointMissionState.READY_TO_EXECUTE){
                        initPopupWindowConfirm(20);
                        popupWindowCon.showAtLocation(findViewById(R.id.main_body), Gravity.CENTER, 0, 0);
                    }else {
                        showToast("指令还没有发送成功");
                    }
                } catch (Exception e) {
                    showToast("参数不合法");
                }
            }
        });

        btsendmsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    wayalt = Float.parseFloat(etalt.getText().toString());
                    waylat = Double.parseDouble(etlat.getText().toString());
                    waylng = Double.parseDouble(etlng.getText().toString());
                    wayvel = Float.parseFloat(etvel.getText().toString());
                    initPopupWindowConfirm(21);
                    popupWindowCon.showAtLocation(findViewById(R.id.main_body), Gravity.CENTER, 0, 0);
                } catch (Exception e) {
                    showToast("参数不合法");
                }
            }
        });
    }

    protected void initPopupWindowHot() {
        View popupWindow_view = getLayoutInflater().inflate(R.layout.pop_hot, null,
                false);
        /* 创建PopupWindow实例,200,150分别是宽度和高度 */
        popupWindowHot = new PopupWindow(popupWindow_view, 1000, 1400, true);

        popupWindowHot.setOutsideTouchable(true);  //设置点击屏幕其它地方弹出框消失
        popupWindowHot.setBackgroundDrawable(new BitmapDrawable());

        /* pop.xml视图里面的控件 */
        Button bthgo = (Button) popupWindow_view.findViewById(R.id.bt_hgo);
        final EditText etlng = (EditText) popupWindow_view.findViewById(R.id.et_lng);
        final EditText etlat = (EditText) popupWindow_view.findViewById(R.id.et_lat);
        final EditText etalt = (EditText) popupWindow_view.findViewById(R.id.et_alt);
        final EditText etw = (EditText) popupWindow_view.findViewById(R.id.et_w);
        final EditText etr = (EditText) popupWindow_view.findViewById(R.id.et_r);
        final Spinner spstart = (Spinner) popupWindow_view.findViewById(R.id.sp_start);

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.5f;//设置阴影透明度
        getWindow().setAttributes(lp);
        popupWindowHot.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {

                SharedPreferences.Editor editor = sp.edit();
                editor.putString("hotlng", etlng.getText().toString());
                editor.putString("hotlat", etlat.getText().toString());
                editor.putString("hotalt", etalt.getText().toString());
                editor.putString("hotw", etw.getText().toString());
                editor.putString("hotr", etr.getText().toString());
                editor.putInt("hotstart", spstart.getSelectedItemPosition());
                editor.commit();

                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1f;
                getWindow().setAttributes(lp);
            }
        });



        etlng.setText(sp.getString("hotlng", ""));
        etlat.setText(sp.getString("hotlat", ""));
        etalt.setText(sp.getString("hotalt", ""));
        etw.setText(sp.getString("hotw", ""));
        etr.setText(sp.getString("hotr", ""));
        spstart.setSelection(sp.getInt("hotstart", 0));

        bthgo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                try {
                    hotalt = Double.parseDouble(etalt.getText().toString());
                    hotlat = Double.parseDouble(etlat.getText().toString());
                    hotlng = Double.parseDouble(etlng.getText().toString());
                    hotw = Float.parseFloat(etw.getText().toString());
                    hotr = Double.parseDouble(etr.getText().toString());
                    hotstart = spstart.getSelectedItem().toString();
                    initPopupWindowConfirm(30);
                    popupWindowCon.showAtLocation(findViewById(R.id.main_body), Gravity.CENTER, 0, 0);
                } catch (Exception e) {
                    showToast("参数不合法");
                }


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
                initPopupWindowConfirm(43);
                popupWindowCon.showAtLocation(findViewById(R.id.main_body), Gravity.CENTER, 0, 0);
            }
        });

        btwest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initPopupWindowConfirm(41);
                popupWindowCon.showAtLocation(findViewById(R.id.main_body), Gravity.CENTER, 0, 0);

            }
        });

        btnorth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initPopupWindowConfirm(42);
                popupWindowCon.showAtLocation(findViewById(R.id.main_body), Gravity.CENTER, 0, 0);

            }
        });

        btsouth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initPopupWindowConfirm(44);
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
        Button btargo = (Button) popupWindow_view.findViewById(R.id.bt_argo);
        final EditText etw = (EditText) popupWindow_view.findViewById(R.id.et_w);

        etw.setText(sp.getString("autorotatew", ""));

        btargo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences.Editor editor = sp.edit();
                editor.putString("autorotatew", etw.getText().toString());
                editor.commit();

                try {
                    autorotatew = Float.parseFloat(etw.getText().toString());
                    initPopupWindowConfirm(50);
                    popupWindowCon.showAtLocation(findViewById(R.id.main_body), Gravity.CENTER, 0, 0);
                } catch (Exception e) {
                    showToast("参数不合法");
                }

            }
        });

    }

    private void initFlightController() {
        final Calendar now = Calendar.getInstance();


        if (isFlightControllerSupported()) {
            mFlightController = ((Aircraft) DJISDKManager.getInstance().getProduct()).getFlightController();
            mFlightController.setStateCallback(new FlightControllerState.Callback() {
                @Override
                public void onUpdate(FlightControllerState
                                             djiFlightControllerCurrentState) {

                    droneLocationLat = djiFlightControllerCurrentState.getAircraftLocation().getLatitude();
                    droneLocationLng = djiFlightControllerCurrentState.getAircraftLocation().getLongitude();

                    //更新飞控数据
                    droneLocationAlt = djiFlightControllerCurrentState.getAircraftLocation().getAltitude();
                    droneAttitudePitch = djiFlightControllerCurrentState.getAttitude().pitch;
                    droneAttitudeRoll = djiFlightControllerCurrentState.getAttitude().roll;
                    droneAttitudeYaw = djiFlightControllerCurrentState.getAttitude().yaw;
                    SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss:SSS");//设置日期格式
                    droneTime = df.format(new Date());
                    droneFlightMode = mFlightController.getState().getFlightModeString();
                    double x = mFlightController.getState().getVelocityX();
                    double y = mFlightController.getState().getVelocityY();
                    double z = mFlightController.getState().getVelocityZ();
                    droneVelocity = Math.sqrt(x*x+y*y+z*z);

                    lnglist.add(String.valueOf(droneLocationLng));
                    latlist.add(String.valueOf(droneLocationLat));
                    altlist.add(String.valueOf(droneLocationAlt));
                    yawlist.add(String.valueOf(droneAttitudeYaw));
                    pitchlist.add(String.valueOf(droneAttitudePitch));
                    rolllist.add(String.valueOf(droneAttitudeRoll));
                    timelist.add(df.format(new Date()));

                    if (client != null && client.connected == 1){
                        try {
                            client.send(clientid + ",updatedata," + droneTime + "," + uavstate.statevalue + String.format(",%.7f,%.7f,%.7f,%.7f,%.7f,%.7f,%.7f,%b,", droneLocationLng, droneLocationLat, droneLocationAlt, droneAttitudePitch, droneAttitudeRoll, droneAttitudeYaw, droneVelocity, uavconnected) + droneFlightMode);
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }


                    if(waypointMissionOperator != null) {
                        if (waypointMissionOperator.getCurrentState() == WaypointMissionState.EXECUTING){
                            uavstate = UAVState.WAYEXEC;
                            wexeccnt = 0;
                        }
                        if (waypointMissionOperator.getCurrentState() != WaypointMissionState.EXECUTING && uavstate == UAVState.WAYEXEC) wexeccnt += 1;
                        if (wexeccnt >= 5){
                            uavstate = UAVState.NONE;
                            wexeccnt = 0;
                        }
                    }
                    if(hotpointMissionOperator != null) {
                        if (hotpointMissionOperator.getCurrentState() == HotpointMissionState.EXECUTING){
                            uavstate = UAVState.HOTEXEC;
                            hexeccnt = 0;
                        }
                        if (hotpointMissionOperator.getCurrentState() != HotpointMissionState.EXECUTING && uavstate  == UAVState.HOTEXEC) hexeccnt += 1;
                        if (hexeccnt >= 5){
                            uavstate = UAVState.NONE;
                            hexeccnt = 0;
                        }
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tLng.setText(String.format("%.7f", droneLocationLng));
                            tLat.setText(String.format("%.7f", droneLocationLat));
                            tAlt.setText(String.format("%.7f", droneLocationAlt));
                            tYaw.setText(String.valueOf(droneAttitudeYaw));
                            tPitch.setText(String.valueOf(droneAttitudePitch));
                            tRoll.setText(String.valueOf(droneAttitudeRoll));
                            tState.setText("State："+uavstate.toString());

                            if (client != null && client.connected == 1){
                                serverconnectstate.setText("服务器：已连接");
                            } else {
                                serverconnectstate.setText("服务器：未连接");
                            }
                        }
                    });


                    //可视化
                    addChartPoint(new Point(droneLocationLng, droneLocationLat), droneAttitudeYaw, droneLocationAlt);
                    rotateModel(droneAttitudeYaw, droneAttitudePitch, droneAttitudeRoll);
                }
            });

            if (mFlightController.getRTK().isConnected()){
                mFlightController.getRTK().setStateCallback(new RTKState.Callback() {
                    @Override
                    public void onUpdate(RTKState rtkState) {
                        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss:SSS");//设置日期格式
                        droneTime = df.format(new Date());

                        droneRtk = rtkState.isRTKBeingUsed();
                        droneRtkLat = rtkState.getMobileStationLocation().getLatitude();
                        droneRtkLng = rtkState.getMobileStationLocation().getLongitude();
                        droneRtkAlt = rtkState.getMobileStationAltitude();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (droneRtk) tRtks.setText("信号正常");
                                else tRtks.setText("无信号");
                            }
                        });

                        if (client != null && client.connected == 1){
                            try {
                                client.send(clientid + ",updatertk," + droneTime + String.format(",%b,%.7f,%.7f,%.7f", droneRtk, droneRtkLng, droneRtkLat, droneRtkAlt));
                            } catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }


            if (waypointMissionOperator == null) {
                waypointMissionOperator = MissionControl.getInstance().getWaypointMissionOperator();
                setUpWaypointListener();
            }

            if (hotpointMissionOperator == null) {
                hotpointMissionOperator = MissionControl.getInstance().getHotpointMissionOperator();
                setUpHotpointListener();
            }
        }
    }

    private boolean isFlightControllerSupported() {
        return DJISDKManager.getInstance().getProduct() != null &&
                DJISDKManager.getInstance().getProduct() instanceof Aircraft &&
                ((Aircraft) DJISDKManager.getInstance().getProduct()).getFlightController() != null;
    }

    /**
     * Checks if there is any missing permissions, and
     * requests runtime permission if needed.
     */
    private void checkAndRequestPermissions() {
        // Check for permissions
        for (String eachPermission : REQUIRED_PERMISSION_LIST) {
            if (ContextCompat.checkSelfPermission(this, eachPermission) != PackageManager.PERMISSION_GRANTED) {
                missingPermission.add(eachPermission);
            }
        }
        // Request for missing permissions
        if (missingPermission.isEmpty()) {
            startSDKRegistration();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            showToast("Need to grant the permissions!");
            ActivityCompat.requestPermissions(this,
                    missingPermission.toArray(new String[missingPermission.size()]),
                    REQUEST_PERMISSION_CODE);
        }

    }

    /**
     * Result of runtime permission request
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Check for granted permission and remove from missing list
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (int i = grantResults.length - 1; i >= 0; i--) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    missingPermission.remove(permissions[i]);
                }
            }
        }
        // If there is enough permission, we will start the registration
        if (missingPermission.isEmpty()) {
            startSDKRegistration();
        } else {
            showToast("Missing permissions!!!");
        }
    }

    private void startSDKRegistration() {
        if (isRegistrationInProgress.compareAndSet(false, true)) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    showToast("registering, pls wait...");
                    DJISDKManager.getInstance().registerApp(MainActivity.this.getApplicationContext(), new DJISDKManager.SDKManagerCallback() {
                        @Override
                        public void onRegister(DJIError djiError) {
                            if (djiError == DJISDKError.REGISTRATION_SUCCESS) {
                                showToast("Register Success");
                                DJISDKManager.getInstance().startConnectionToProduct();
                            } else {
                                showToast("Register sdk fails, please check the bundle id and network connection!");
                            }
                            Log.v(TAG, djiError.getDescription());
                        }

                        @Override
                        public void onProductDisconnect() {
                            tearDownWaypointListener();
                            tearDownHotpointListener();
                            Log.d(TAG, "onProductDisconnect");
                            showToast("Product Disconnected");
                            notifyStatusChange();
                            runOnUiThread(() -> {
                                uavconnectstate.setText("无人机：未连接");
                            });
                            uavconnected = false;
                        }

                        @Override
                        public void onProductConnect(BaseProduct baseProduct) {
                            Log.d(TAG, String.format("onProductConnect newProduct:%s", baseProduct));
                            showToast("Product Connected");
                            notifyStatusChange();
                            runOnUiThread(() -> {
                                uavconnectstate.setText("无人机：已连接");
                            });
                            uavconnected = true;
                            initFlightController();
                        }

                        @Override
                        public void onComponentChange(BaseProduct.ComponentKey componentKey, BaseComponent oldComponent,
                                                      BaseComponent newComponent) {

                            if (newComponent != null) {
                                newComponent.setComponentListener(new BaseComponent.ComponentListener() {

                                    @Override
                                    public void onConnectivityChange(boolean isConnected) {
                                        Log.d(TAG, "onComponentConnectivityChanged: " + isConnected);
                                        notifyStatusChange();
                                    }
                                });
                            }
                            Log.d(TAG,
                                    String.format("onComponentChange key:%s, oldComponent:%s, newComponent:%s",
                                            componentKey,
                                            oldComponent,
                                            newComponent));

                        }

                        @Override
                        public void onInitProcess(DJISDKInitEvent djisdkInitEvent, int i) {

                        }

                        @Override
                        public void onDatabaseDownloadProgress(long l, long l1) {

                        }

                        @Override
                        public void onProductChanged(BaseProduct baseProduct){

                        }

                    });
                }
            });
        }
    }

    private void notifyStatusChange() {
        mHandler.removeCallbacks(updateRunnable);
        mHandler.postDelayed(updateRunnable, 500);
    }

    private void showToast(final String toastMsg) {

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_LONG).show();
            }
        });

    }

    private void showResultToast(DJIError djiError) {
        if (djiError != null) showToast(djiError.getDescription());
    }

    private void toError(DJIError djiError){
        System.out.println(djiError != null);
        if (djiError != null) uavstate = UAVState.ERROR;
    }

    private void setUpHotpointListener() {
        hotpointlistener = new HotpointMissionOperatorListener() {
            @Override
            public void onExecutionUpdate(@NonNull HotpointMissionEvent hotpointMissionEvent) {
//                showToast("Execution update!");
            }

            @Override
            public void onExecutionStart() {
                showToast("Execution started!");
//                missioninfo.setText("开始执行圆形飞行任务！\n圆心经度：" + hotlng + " 圆心纬度：" + hotlat + " 圆心高度：" + hotalt + "\n绕飞半径：" + hotr + " 角速度：" + hotw + " 起始方向：" + hotstart);
//                missioninfo.setText("开始执行圆形飞行任务！\n");

                uavstate = UAVState.HOTEXEC;
            }

            @Override
            public void onExecutionFinish(@Nullable DJIError djiError) {
                showToast("Execution finished!");
//                missioninfo.setText("未执行飞行任务");
                uavstate = UAVState.NONE;
            }
        };

        if (hotpointMissionOperator != null && hotpointlistener != null) {
            hotpointMissionOperator.addListener(hotpointlistener);
        }
    }

    private void setUpWaypointListener() {
        // Example of Listener
        waypointListener = new WaypointMissionOperatorListener() {
            @Override
            public void onDownloadUpdate(@NonNull WaypointMissionDownloadEvent waypointMissionDownloadEvent) {
                // Example of Download Listener
                if (waypointMissionDownloadEvent.getProgress() != null
                        && waypointMissionDownloadEvent.getProgress().isSummaryDownloaded
                        && waypointMissionDownloadEvent.getProgress().downloadedWaypointIndex == 1) {
//                    showToast("Download successful!");
                }
            }

            @Override
            public void onUploadUpdate(@NonNull WaypointMissionUploadEvent waypointMissionUploadEvent) {
                // Example of Upload Listener
                if (waypointMissionUploadEvent.getProgress() != null
                        && waypointMissionUploadEvent.getProgress().isSummaryUploaded
                        && waypointMissionUploadEvent.getProgress().uploadedWaypointIndex == 1) {
//                    showToast("Upload successful!");
                }
            }

            @Override
            public void onExecutionUpdate(@NonNull WaypointMissionExecutionEvent waypointMissionExecutionEvent) {
                // Example of Execution Listener
                Log.d(TAG,
                        (waypointMissionExecutionEvent.getPreviousState() == null
                                ? ""
                                : waypointMissionExecutionEvent.getPreviousState().getName())
                                + ", "
                                + waypointMissionExecutionEvent.getCurrentState().getName()
                                + (waypointMissionExecutionEvent.getProgress() == null
                                ? ""
                                : waypointMissionExecutionEvent.getProgress().targetWaypointIndex));
            }

            @Override
            public void onExecutionStart() {
                showToast("Execution started!");
//                missioninfo.setText("开始执行航点飞行任务！\n" + "经度：" + waylng + " 纬度：" + waylat + "\n高度：" + wayalt + " 速度：" + wayvel);
//                missioninfo.setText("开始执行航点飞行任务！\n");
                uavstate = UAVState.WAYEXEC;
            }

            @Override
            public void onExecutionFinish(@Nullable DJIError djiError) {
                showToast("Execution finished!");
//                missioninfo.setText("未执行飞行任务");
                uavstate = UAVState.NONE;
            }
        };

        if (waypointMissionOperator != null && waypointListener != null) {
            // Example of adding listeners
            waypointMissionOperator.addListener(waypointListener);
        }
    }

    private void tearDownHotpointListener() {
        if (hotpointMissionOperator != null && hotpointlistener != null) {
            hotpointMissionOperator.removeListener(hotpointlistener);
        }
    }

//    private Handler btstophandler = new Handler(){
//        @Override
//        public void handleMessage(Message msg){
//            if (msg.what == 1){
//                btstop.setVisibility(View.VISIBLE);
//            }else if (msg.what == 0) {
//                btstop.setVisibility(View.GONE);
//            }
//        }
//    };

    private void tearDownWaypointListener() {
        if (waypointMissionOperator != null && waypointListener != null) {
            // Example of removing listeners
            waypointMissionOperator.removeListener(waypointListener);
        }
    }

    enum UAVState {
        NONE(0), WAYEXEC(1), HOTEXEC(2), ROTATEEXEC(3), AREXEC(4), READY(5), ERROR(6);
        private int statevalue;

        UAVState(int v) {
            this.statevalue = v;
        }
    }

    private class ConnectRunnable implements Runnable {


        @Override
        public void run() {
            try {
                client.connect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //初始化图表数据
    private void initChartData() {
        heightPlot.clear();
        routePlot.clear();
        points = new ArrayList<Point>();

        //===============routePlot================
        //Scatter
        minX = 1000;
        minY = 1000;
        maxX = 0;
        maxY = 0;
        chartXRange = 0;
        chartYRange = 0;
        routeScatters = new ScatterSeries(points, "Drone 1");
        LineAndPointFormatter scatterFormatter = new LineAndPointFormatter(this, R.xml.point_formatter);
        scatterFormatter.setLegendIconEnabled(false);
        routePlot.addSeries(routeScatters, scatterFormatter);
        routePlot.setRangeBoundaries(-1, 1, BoundaryMode.FIXED);
        routePlot.setDomainBoundaries(-1, 1, BoundaryMode.FIXED);

        routePlot.getGraph().getLineLabelStyle(
                XYGraphWidget.Edge.LEFT).setFormat(new DecimalFormat("##.#####"));
        routePlot.getGraph().getLineLabelStyle(
                XYGraphWidget.Edge.BOTTOM).setFormat(new DecimalFormat("###.#####"));
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

    //错误提示
    private void makeToastText(final String text, final int toastDuration) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), text, toastDuration).show();
            }
        });
    }

    /**
     * 插入并更新图表数据
     *
     * @param point 新的点 Point(Number x,Number y)
     * @param yaw   偏航角
     */
    private void addChartPoint(Point point, double yaw, double height) {

        minX = Math.min(point.x.doubleValue(), minX);
        minY = Math.min(point.y.doubleValue(), minY);
        maxX = Math.max(point.x.doubleValue(), maxX);
        maxY = Math.max(point.y.doubleValue(), maxY);
        double deltaX = maxX - minX;
        double deltaY = maxY - minY;
        if (deltaX < EPS && deltaY < EPS) {
            chartXRange = EPS;
            chartYRange = EPS * xyScale;
        } else {
            if (deltaX * xyScale > deltaY) {
                //domain = deltaX
                chartXRange = deltaX;
                chartYRange = chartXRange * xyScale;

            } else {
                //range = deltaY
                chartYRange = deltaY;
                chartXRange = chartYRange / xyScale;
            }
        }
        double iconLen = chartYRange * 0.05;
        droneIcon.lineLength = iconLen;

        routePlot.setRangeBoundaries(minY - iconLen, minY + chartYRange + iconLen, BoundaryMode.FIXED);
        routePlot.setDomainBoundaries(minX - iconLen * xyScale, minX + chartXRange + iconLen * xyScale, BoundaryMode.FIXED);
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
        if (circleSeries != null) {
            circleSeries.clear();
        }
        if (axisSeries != null) {
            axisSeries.clear();
        }
        int pointNum = 128;
        circleSeries = new SimpleXYSeries("circlePoint");
        axisSeries = new SimpleXYSeries("axis");
        axisSeries.addFirst(axis.x, axis.y);

        for (int i = 0; i < 128; i++) {
            Number x = axis.x.doubleValue() + r * Math.cos(2 * Math.PI * i / pointNum);
            Number y = axis.y.doubleValue() + r * Math.sin(2 * Math.PI * i / pointNum) * xyScale;
            circleSeries.addFirst(x, y);
        }
        circleSeries.addFirst(axis.x.doubleValue() + r, axis.y);

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
     * 旋转无人机模型
     */
    private void rotateModel(double yaw, double pitch, double roll) {
        gLView.rotateModel(180 - yaw, pitch, roll);
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

    public ModelSurfaceView getGLView() {
        return gLView;
    }

    public SceneLoader getScene() {
        return scene;
    }

    private void loadModelFromAssets() {
        ContentUtils.provideAssets(this);
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
                    try {
                        droneData.loadGPSData(path);
                    } catch (IOException e) {
                        e.printStackTrace();
                        makeToastText("文件读取失败，请检查文件格式或内容！", Toast.LENGTH_LONG);
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
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

    protected void initPopupWindowHistory() {
        /* TODO Auto-generated method stub */

        /* 获取自定义布局文件pop.xml的视图 */
        View popupWindow_view = getLayoutInflater().inflate(R.layout.pop_history, null,
                false);
        /* 创建PopupWindow实例,200,150分别是宽度和高度 */
        popupWindowHistory = new PopupWindow(popupWindow_view, 500, 400, true);

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
                if (!droneData.isLoaded()) {
                    makeToastText("请先读取试验记录！", Toast.LENGTH_SHORT);
                    return;
                }
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
                if (!droneData.isLoaded()) {
                    makeToastText("请先读取试验记录！", Toast.LENGTH_SHORT);
                    return;
                }
                try {
                    Intent intent = new Intent(MainActivity.this, Charts3Activity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    class ChartTestThread extends Thread {
        MainActivity m;

        ChartTestThread(MainActivity m) {
            this.m = m;
        }

        @Override
        public void run() {
            double r = 0.001d;
            int num = 720;
            double h = 30d;
            m.addCircle(new Point(0, 0), r * 111194.926);
            double theta = 2 * Math.PI / 360;
            for (int i = 0; i < 720; i++) {
                double t = theta * i;
                Number x = r * Math.cos(t);
                Number y = r * Math.sin(t);
                double yaw = -Math.toDegrees(t);

                m.addChartPoint(new Point(x.doubleValue(), y.doubleValue()), yaw + Math.random(), h);
                double pitch = -10;
                double roll = 0;
                m.rotateModel(-yaw, pitch, roll);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
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
        public double lineLength = 0.0001;
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

            this.yaw = 90 - yaw;
            double radians = Math.toRadians(this.yaw);
            double sina = Math.sin(radians) * lineLength;
            double cosa = Math.cos(radians) * lineLength;

            pos = point;
            Point head = new Point(pos.x.doubleValue() + cosa, pos.y.doubleValue() + sina * xyScale);
            iconPoints.add(head);
            droneIconHead.clear();
            droneIconHead.addFirst(head.x, head.y);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        client.close();
    }

}

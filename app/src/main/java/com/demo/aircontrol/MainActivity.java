package com.demo.aircontrol;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

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
    private Button btnDemRecord;//记录经纬数据
    private Button btnDemOutput;//输出经纬数据至文件

    private PopupWindow popupWindowTeam;
    private PopupWindow popupWindowWay;
    private PopupWindow popupWindowHot;
    private PopupWindow popupWindowCon;

    private Calendar now = Calendar.getInstance();


    class TimeThread extends Thread {
        @Override
        public void run() {
            do {
                try {
                    Thread.sleep(1000);
                    Message msg = new Message();
                    msg.what = 1;  //消息(一个整型值)
                    mHandler.sendMessage(msg);// 每隔1秒发送一个msg给mHandler
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (true);
        }
    }

    //在主线程里面处理消息并更新UI界面
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    long sysTime = System.currentTimeMillis();//获取系统时间
                    CharSequence sysTimeStr = DateFormat.format("hh:mm:ss", sysTime);//时间显示格式
                    tLng.setText(sysTimeStr); //更新时间
                    break;
                default:
                    break;

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();
    }

    private void initUI() {

        //显示信息
        tLng = (TextView) findViewById(R.id.textLng);
        tLat = (TextView) findViewById(R.id.textLat);
        tAlt = (TextView) findViewById(R.id.textAlt);
        tPitch = (TextView) findViewById(R.id.textPitch);
        tRoll = (TextView) findViewById(R.id.textRoll);
        tYaw = (TextView) findViewById(R.id.textYaw);

        // ---------------设置单击事件-------------------------
        //  private Button btnDemRecord;//记录经纬数据
        // private Button btnDemOutput;//输出经纬数据至文件
        btnDemRecord = (Button) findViewById(R.id.btn_record);
        btnDemRecord.setOnClickListener(this);//记录经纬数据

        btnDemOutput = (Button) findViewById(R.id.btn_output);
        btnDemOutput.setOnClickListener(this);//输出经纬数据至文件


        btteam = (Button) findViewById(R.id.btn_team);
        btwaypoint = (Button) findViewById(R.id.btn_waypoint);
        bthotpoint = (Button) findViewById(R.id.btn_hotpoint);

        btteam.setOnClickListener(this);
        btwaypoint.setOnClickListener(this);
        bthotpoint.setOnClickListener(this);

        new TimeThread().start(); //启动新的线程

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //-----------------------------------------
            case R.id.btn_record://保存


                break;

            case R.id.btn_output://输出到文件

                //loginAccount();

                String fileName ="a.txt";//文件名规则？
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

            case R.id.btn_test:

                break;

            //----------------------------------------

        }
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
                int len = 0;
                for (int i=0; i<len; i++) {
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
                int len = 0;
                for (int i=0; i<len; i++) {
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
    }

    protected  void initPopupWindowConfirm()
    {
        View v = getLayoutInflater().inflate(R.layout.pop_confirm, null, false);
        popupWindowCon = new PopupWindow(v, 300, 300, true);
        popupWindowCon.setFocusable(true);

        /* pop.xml视图里面的控件 */
        Button	btyes	= (Button) v.findViewById( R.id.bt_yes );
        Button	btno	= (Button) v.findViewById( R.id.bt_no);


        btyes.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                /*
                 * TODO Auto-generated method stub
                 * 这里可以执行相关操作
                 */
                System.out.println( "打开操作" );
            }
        } );

        btno.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                /*
                 * TODO Auto-generated method stub
                 * 这里可以执行相关操作
                 */
                System.out.println( "保存操作" );
            }
        } );
    }

    protected void initPopupWindowTeam()
    {
        /* TODO Auto-generated method stub */

        /* 获取自定义布局文件pop.xml的视图 */
        View popupWindow_view = getLayoutInflater().inflate( R.layout.pop_team, null,
                false );
        /* 创建PopupWindow实例,200,150分别是宽度和高度 */
        popupWindowTeam = new PopupWindow( popupWindow_view, 600, 600, true );


        /* 设置动画效果 */
        popupWindowTeam.setAnimationStyle( R.style.AnimationFade );

        /* pop.xml视图里面的控件 */
        Button	btcteam	= (Button) popupWindow_view.findViewById( R.id.create_team );
        Button	btjteam	= (Button) popupWindow_view.findViewById( R.id.join_team);
        TextView txtteamstate	= (TextView) popupWindow_view.findViewById( R.id.team_state);


        btcteam.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                /*
                 * TODO Auto-generated method stub
                 * 这里可以执行相关操作
                 */
                System.out.println( "打开操作" );
            }
        } );

        btjteam.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                /*
                 * TODO Auto-generated method stub
                 * 这里可以执行相关操作
                 */
                System.out.println( "保存操作" );
            }
        } );

    }

    protected void initPopupWindowWay()
    {
        /* TODO Auto-generated method stub */

        /* 获取自定义布局文件pop.xml的视图 */
        View popupWindow_view = getLayoutInflater().inflate( R.layout.pop_way, null,
                false );
        /* 创建PopupWindow实例,200,150分别是宽度和高度 */
        popupWindowWay = new PopupWindow( popupWindow_view, 1000, 1200, true );


        /* 设置动画效果 */
        popupWindowWay.setAnimationStyle( R.style.AnimationFade );

        /* pop.xml视图里面的控件 */
        Button	bttwgo	= (Button) popupWindow_view.findViewById( R.id.bt_twgo );
        Button	btwgo	= (Button) popupWindow_view.findViewById( R.id.bt_wgo);


        bttwgo.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                /*
                 * TODO Auto-generated method stub
                 * 这里可以执行相关操作
                 */
                System.out.println( "打开操作" );
                initPopupWindowConfirm();
                popupWindowCon.showAtLocation(findViewById(R.id.main_body), Gravity.CENTER, 0, 0);

            }
        } );

        btwgo.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                /*
                 * TODO Auto-generated method stub
                 * 这里可以执行相关操作
                 */
                System.out.println( "保存操作" );
                initPopupWindowConfirm();
                popupWindowCon.showAtLocation(findViewById(R.id.main_body), Gravity.CENTER, 0, 0);

            }
        } );

    }

    protected void initPopupWindowHot()
    {
        /* TODO Auto-generated method stub */

        /* 获取自定义布局文件pop.xml的视图 */
        View popupWindow_view = getLayoutInflater().inflate( R.layout.pop_hot, null,
                false );
        /* 创建PopupWindow实例,200,150分别是宽度和高度 */
        popupWindowHot = new PopupWindow( popupWindow_view, 1000, 1400, true );


        /* 设置动画效果 */
        popupWindowHot.setAnimationStyle( R.style.AnimationFade );

        /* pop.xml视图里面的控件 */
        Button	btthgo	= (Button) popupWindow_view.findViewById( R.id.bt_thgo );
        Button	bthgo	= (Button) popupWindow_view.findViewById( R.id.bt_hgo);


        btthgo.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                /*
                 * TODO Auto-generated method stub
                 * 这里可以执行相关操作
                 */
                System.out.println( "打开操作" );
                initPopupWindowConfirm();
                popupWindowCon.showAtLocation(findViewById(R.id.main_body), Gravity.CENTER, 0, 0);

            }
        } );

        bthgo.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                /*
                 * TODO Auto-generated method stub
                 * 这里可以执行相关操作
                 */
                System.out.println( "保存操作" );
                initPopupWindowConfirm();
                popupWindowCon.showAtLocation(findViewById(R.id.main_body), Gravity.CENTER, 0, 0);

            }
        } );

    }




}

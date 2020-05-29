package com.demo.aircontrol;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.demo.aircontrol.util.model.MyGLSurfaceView;

public class Charts2Activity extends AppCompatActivity {

    private MyGLSurfaceView gLView;
    private TextView t_roll;
    private TextView t_pitch;
    private TextView t_yaw;

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            long time = SystemClock.uptimeMillis() % 20L;
            double angle = 0.090d * ((int) time) + 45;
            rotate(angle, 0, angle);
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charts2);
        ConstraintLayout layout = findViewById(R.id.modelBlock);

        t_pitch = findViewById(R.id.text2);
        t_roll = findViewById(R.id.text4);
        t_yaw = findViewById(R.id.text6);

        gLView = new MyGLSurfaceView(this);
        layout.addView(gLView);

//        Charts2PagerAdapter charts2PagerAdapter = new Charts2PagerAdapter(this, getSupportFragmentManager());
//        ViewPager viewPager = findViewById(R.id.view_pager);
//        viewPager.setAdapter(charts2PagerAdapter);
//        TabLayout tabs = findViewById(R.id.tabs);
//        tabs.setTabMode(TabLayout.MODE_FIXED);
//        tabs.setupWithViewPager(viewPager);


        MyThread myThread = new MyThread();
        myThread.start();
    }

    void rotate(double roll, double pitch, double yaw) {
        t_roll.setText(Double.toString(roll));
        t_pitch.setText(Double.toString(pitch));
        t_yaw.setText(Double.toString(yaw));
//        gLView.rotate(roll,pitch,yaw);
        gLView.rotate(0, 0, 0);
    }

    class MyThread extends Thread {
        @Override
        public void run() {
            for (int i = 0; i < 200; i++) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Message msg = new Message();
                handler.sendMessage(msg);
            }
        }
    }
}
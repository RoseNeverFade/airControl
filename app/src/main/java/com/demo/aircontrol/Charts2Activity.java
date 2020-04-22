package com.demo.aircontrol;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class Charts2Activity extends AppCompatActivity {

    private GLSurfaceView gLView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charts2);
        ConstraintLayout layout = findViewById(R.id.constraintLayout1);

        gLView = new MyGLSurfaceView(this);
        layout.addView(gLView);

//        Charts2PagerAdapter charts2PagerAdapter = new Charts2PagerAdapter(this, getSupportFragmentManager());
//        ViewPager viewPager = findViewById(R.id.view_pager);
//        viewPager.setAdapter(charts2PagerAdapter);
//        TabLayout tabs = findViewById(R.id.tabs);
//        tabs.setTabMode(TabLayout.MODE_FIXED);
//        tabs.setupWithViewPager(viewPager);

    }
}
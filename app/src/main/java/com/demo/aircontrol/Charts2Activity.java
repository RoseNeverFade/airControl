package com.demo.aircontrol;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import com.demo.aircontrol.ui.main.Charts2PagerAdapter;
import com.google.android.material.tabs.TabLayout;

public class Charts2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charts2);
        Charts2PagerAdapter charts2PagerAdapter = new Charts2PagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager2);
        viewPager.setAdapter(charts2PagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs2);
        tabs.setTabMode(TabLayout.MODE_FIXED);
        tabs.setupWithViewPager(viewPager);

    }
}
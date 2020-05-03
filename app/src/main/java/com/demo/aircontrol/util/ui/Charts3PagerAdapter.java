package com.demo.aircontrol.util.ui;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import com.demo.aircontrol.R;
import com.demo.aircontrol.fragment.chart3Fragment.*;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class Charts3PagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.tab_text_6, R.string.tab_text_7, R.string.tab_text_8, R.string.tab_text_9, R.string.tab_text_10, R.string.tab_text_11, R.string.tab_text_12};
    private final Context mContext;

    public Charts3PagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        if (position == 0) {
            return DeltaAltFrag.newInstance();
        } else if (position == 1) {
            return DeltaDistanceFrag.newInstance();
        } else if (position == 2) {
            return DeltaLngFrag.newInstance();
        } else if (position == 3) {
            return DeltaLatFrag.newInstance();
        } else if (position == 4) {
            return DeltaYawFrag.newInstance();
        } else if (position == 5) {
            return DeltaPitchFrag.newInstance();
        } else {
            return DeltaRollFrag.newInstance();
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        return 7;
    }
}
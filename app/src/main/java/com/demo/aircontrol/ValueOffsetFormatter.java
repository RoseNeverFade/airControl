package com.demo.aircontrol;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;

public class ValueOffsetFormatter extends ValueFormatter {

    private float anchor = 0;
    private ArrayList<Double> trueData;

    public ValueOffsetFormatter() {
        this(0);
    }

    public ValueOffsetFormatter(double anchor) {
        this((float) anchor);
    }

    public ValueOffsetFormatter(float anchor) {
        this.anchor = anchor;
    }

    // override this for custom formatting of XAxis or YAxis labels
    @Override
    public String getAxisLabel(float value, AxisBase axis) {
        return super.getAxisLabel(anchor + value, axis);
    }
}

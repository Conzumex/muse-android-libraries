package com.conzumex.mfmeter.sleepgraph;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SleepEntry implements Comparable<SleepEntry> {
    public float xValue;
    public float yValue;
    public float xValueClose =-1;

    public SleepEntry(float xValue, float yValue) {
        this.xValue = xValue;
        this.yValue = yValue;
    }
    public SleepEntry(float xValue, float yValue,float xValueClose) {
        this.xValue = xValue;
        this.yValue = yValue;
        this.xValueClose = xValueClose;
    }

    @Override
    public int compareTo(SleepEntry o) {
        return (int) (this.xValue - o.xValue);
    }

    public static Set<Float> yValsUnique(final List<SleepEntry> entries) {
        Set<Float> uniqueYs = new HashSet<>();
        //to add dummy values when no entries is loaded
        if(entries==null){
            uniqueYs.add(0f);
            uniqueYs.add(1f);
            uniqueYs.add(2f);
            uniqueYs.add(3f);
            return uniqueYs;
        }
        for(final SleepEntry item: entries) {
            uniqueYs.add(item.yValue);
        }
        return uniqueYs;
    }
}

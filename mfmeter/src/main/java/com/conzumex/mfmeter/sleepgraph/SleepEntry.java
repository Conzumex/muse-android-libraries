package com.conzumex.mfmeter.sleepgraph;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SleepEntry implements Comparable<SleepEntry> {
    public float xValue;
    public float yValue;

    public SleepEntry(float xValue, float yValue) {
        this.xValue = xValue;
        this.yValue = yValue;
    }

    @Override
    public int compareTo(SleepEntry o) {
        return (int) (this.xValue - o.xValue);
    }

    public static Set<Float> yValsUnique(final List<SleepEntry> entries) {
        Set<Float> uniqueYs = new HashSet<>();
        for(final SleepEntry item: entries) {
            uniqueYs.add(item.yValue);
        }
        return uniqueYs;
    }
}

package com.conzumex.mfmeter.sleepgraph;

import androidx.annotation.NonNull;

import java.util.HashSet;
import java.util.Set;

public class NapEntry implements Comparable<NapEntry> {
    public float xValue;
    public int duration;

    public NapEntry(float xValue, int duration) {
        this.xValue = xValue;
        this.duration = duration;
    }
    public NapEntry(float xValue) {
        this.xValue = xValue;
        this.duration = 1;
    }

    @Override
    public int compareTo(NapEntry o) {
        return (int) (this.xValue - o.xValue);
    }


    @NonNull
    @Override
    public String toString() {
        return "x: "+xValue+", duration: "+duration;
    }
}

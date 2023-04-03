package com.conzumex.mfmeter;

import java.util.Calendar;
import java.util.Date;

public class FuelSession {
    public Date startTime,endTime;
    public int markerColor,sessionColor;
    public FuelSession(Date startTime, Date endTime, int markerColor, int sessionColor) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.markerColor = markerColor;
        this.sessionColor = sessionColor;
    }

    public int getStartMinutes(){
        Calendar tempCal = Calendar.getInstance();
        tempCal.setTime(startTime);
        return (tempCal.get(Calendar.HOUR_OF_DAY)*60)+tempCal.get(Calendar.MINUTE);
    }

    public int getEndMinutes(){
        Calendar tempCal = Calendar.getInstance();
        tempCal.setTime(endTime);
        return (tempCal.get(Calendar.HOUR_OF_DAY)*60)+tempCal.get(Calendar.MINUTE);
    }
}

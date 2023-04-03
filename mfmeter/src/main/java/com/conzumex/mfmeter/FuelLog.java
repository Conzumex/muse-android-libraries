package com.conzumex.mfmeter;

import java.util.Calendar;
import java.util.Date;

public class FuelLog {
    public Date startTime,endTime;
    public FuelLog(Date startTime, Date endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
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

    public String getDuration(){
        Calendar tempCal = Calendar.getInstance();
        tempCal.setTime(startTime);
        int startMinutes = (tempCal.get(Calendar.HOUR_OF_DAY)*60)+tempCal.get(Calendar.MINUTE);
        tempCal.setTime(endTime);
        int endMinutes = (tempCal.get(Calendar.HOUR_OF_DAY)*60)+tempCal.get(Calendar.MINUTE);
        return (endMinutes - startMinutes)+" min";
    }
}

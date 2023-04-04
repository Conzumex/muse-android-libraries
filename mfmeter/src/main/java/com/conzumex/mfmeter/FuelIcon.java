package com.conzumex.mfmeter;

import java.util.Calendar;
import java.util.Date;

public class FuelIcon {
    Date time;
    int icon;
    public FuelIcon(Date time, int icon){
        this.icon=icon;
        this.time=time;
    }

    public int getTimeMinutes(){
        Calendar tempCal = Calendar.getInstance();
        tempCal.setTime(time);
        return (tempCal.get(Calendar.HOUR_OF_DAY)*60)+tempCal.get(Calendar.MINUTE);
    }
}

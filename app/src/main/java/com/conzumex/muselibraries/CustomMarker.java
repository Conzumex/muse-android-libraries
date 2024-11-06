package com.conzumex.muselibraries;

import android.content.Context;
import android.widget.TextView;

import com.conzumex.mfmeter.sleepgraph.SleepEntry;
import com.conzumex.mfmeter.sleepgraph.SleepMarker;

public class CustomMarker extends SleepMarker {
    TextView tv1,tv2;
    public supplier mSupply;
    public CustomMarker(Context context, int layoutId) {
        super(context, layoutId);
        tv1 = findViewById(R.id.textView);
        tv2 = findViewById(R.id.textView2);
    }

    @Override
    public void refreshContent(SleepEntry selectedEntry, float selectedX) {
        super.refreshContent(selectedEntry, selectedX);
        if(mSupply!=null){
            CharSequence[] texts = mSupply.getVal(selectedEntry,selectedX);
            tv1.setText(texts[0]);
            tv2.setText(texts[1]);
            return;
        }
        tv1.setText(selectedX+"");
        String tempXSelected = "none";
        if(selectedEntry!=null)
            tempXSelected = selectedEntry.xValue+"";
        tv2.setText(tempXSelected);
    }

    public interface supplier{
        CharSequence[] getVal(SleepEntry entry,float x);
    }
}

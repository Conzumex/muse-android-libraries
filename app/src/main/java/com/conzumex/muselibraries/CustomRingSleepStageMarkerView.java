package com.conzumex.muselibraries;

import android.content.Context;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.conzumex.circleseekbar.marker.Marker;
import com.conzumex.mfmeter.sleepgraph.SleepEntry;
import com.conzumex.mfmeter.sleepgraph.SleepMarker;

public class CustomRingSleepStageMarkerView extends Marker {

    private TextView tvContent,tvContent2;
    View llRoot;
    supplier mSupplier;
    boolean isSupplied=false;

    public CustomRingSleepStageMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);
        // find your layout components
        tvContent = (TextView) findViewById(R.id.tvContent);
        tvContent2 = (TextView) findViewById(R.id.tvContent2);
        llRoot = findViewById(R.id.ll_root);
    }

    public void setMarkerBg(int color){
        llRoot.setBackgroundColor(color);
    }

    // callbacks everytime the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    @Override
    public void refreshContent(float e, float x) {

        String[] valueShow = new String[]{e + "", x + ""};
        if (isSupplied)
            valueShow = mSupplier.onValueSupply(e, x);
        tvContent.setText(Html.fromHtml(valueShow[0]));
        if (!valueShow[1].isEmpty())
            tvContent2.setText(Html.fromHtml(valueShow[1]));

        // this will perform necessary layouting
//        super.refreshContent(e, x);
    }

    public interface supplier {
        String[] onValueSupply(float e, float xVal);
    }

    public void setSupplier(supplier mSupplier) {
        this.mSupplier=mSupplier;
        isSupplied=true;
    }
}
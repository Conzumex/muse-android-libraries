package com.conzumex.muselibraries;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.conzumex.charts.charts.LineChart;
import com.conzumex.charts.components.XAxis;
import com.conzumex.charts.components.YAxis;
import com.conzumex.charts.data.Entry;
import com.conzumex.charts.data.LineData;
import com.conzumex.charts.data.LineDataSet;
import com.conzumex.charts.highlight.Highlight;
import com.conzumex.charts.listener.OnChartValueSelectedListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    List<Entry> listEntries;
    LineData data;
    EditText edtText;
    Button btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LineChart lineChart = findViewById(R.id.line_chart);
        edtText = findViewById(R.id.edt_text);
        btn = findViewById(R.id.button);


        listEntries = getEntries();
        data = getLineData(listEntries);
        loadChart(lineChart,data);

        btn.setOnClickListener(view->{
            int val = Integer.parseInt(edtText.getText().toString());
            listEntries = getEntries();

            Entry newEntry = getYValueForAverage(val,listEntries);
            newEntry.setIcon(getDrawable(R.drawable.ic_graph_marker));

            Entry temp2Entry = new Entry(val+20, newEntry.getY(),newEntry.getIcon());

//            listEntries.add(newEntry);
//
//            Collections.sort(listEntries);
            data = getLineData(listEntries);
            List<Entry> listIcons = new ArrayList<>();
            listIcons.add(newEntry);
//            listIcons.add(temp2Entry);
            LineDataSet iconSet = new LineDataSet(listIcons,"icons");
            data.addDataSet(iconSet);
            loadChart(lineChart,data);
        });

    }

    private List<Entry> getEntries(){
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(10,15));
        entries.add(new Entry(20,20));
        entries.add(new Entry(30,25));
        entries.add(new Entry(40,35));
        entries.add(new Entry(50,35));
        entries.add(new Entry(60,35));
        entries.add(new Entry(70,40));
        entries.add(new Entry(80,40));
        entries.add(new Entry(90,50));
        entries.add(new Entry(100,55));
        entries.add(new Entry(110,65));
        entries.add(new Entry(120,75));
        entries.add(new Entry(130,85));
        entries.add(new Entry(140,95));
        entries.add(new Entry(150,75));
        entries.add(new Entry(160,65));
        entries.add(new Entry(170,60));
        entries.add(new Entry(180,60));
        entries.add(new Entry(190,45));
        entries.add(new Entry(200,50));
        return entries;
    }


    private LineData getLineData(List<Entry> entries){
        LineData lineData = new LineData();
//        lineData.addDataSet(getSecondaryLineDataSet());
        LineDataSet lineDataSet = new LineDataSet(entries, "Intensity items");
        lineDataSet.setDrawCircles(false);
        lineDataSet.setDrawValues(false);
        lineDataSet.setGradientColors(getGradientColors(10,110));
        lineDataSet.setLineWidth(2.5f);
        lineDataSet.setDrawHorizontalHighlightIndicator(false);
        lineDataSet.setDrawVerticalHighlightIndicator(false);
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineData.addDataSet(lineDataSet);
        return lineData;
    }

    private LineDataSet getSecondaryLineDataSet(){
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(10,15));
        entries.add(new Entry(20,20));
        entries.add(new Entry(30,25));
        entries.add(new Entry(40,35));
        entries.add(new Entry(50,35));
        entries.add(new Entry(60,35));
        entries.add(new Entry(70,40));
        entries.add(new Entry(80,40));
        entries.add(new Entry(90,50));
        entries.add(new Entry(100,55));
        entries.add(new Entry(110,65));
        entries.add(new Entry(120,75));
        entries.add(new Entry(130,85));
        entries.add(new Entry(140,95));
        entries.add(new Entry(150,75));
        entries.add(new Entry(160,65));
        entries.add(new Entry(170,60));
        entries.add(new Entry(180,60));
        entries.add(new Entry(190,45));
        entries.add(new Entry(200,50));
        entries.add(new Entry(210,50));
        entries.add(new Entry(220,55));
        entries.add(new Entry(230,65));
        entries.add(new Entry(240,75));
        entries.add(new Entry(250,85));

        LineDataSet lineDataSet = new LineDataSet(entries, "Intensity items 2");
        lineDataSet.setDrawCircles(false);
        lineDataSet.setColor(Color.parseColor("#800035"));
        lineDataSet.setDrawValues(false);
        lineDataSet.setLineWidth(1.5f);
        lineDataSet.setGradientColors(getGradientColors(10,110));
        lineDataSet.setDrawHorizontalHighlightIndicator(false);
        lineDataSet.setDrawVerticalHighlightIndicator(false);
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        return lineDataSet;
    }


    private void loadChart(LineChart lineChart, LineData lineData) {
        lineChart.setData(lineData);
        lineChart.setDragEnabled(true);
        lineChart.setPinchZoom(true);
        lineChart.setScaleEnabled(false);
        lineChart.getDescription().setEnabled(false);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.setScaleXEnabled(true);
        lineChart.getLegend().setEnabled(false);
        lineChart.moveViewToX(0);
        lineChart.setDoubleTapToZoomEnabled(false);

        float offsetLeft = lineChart.getViewPortHandler().offsetLeft();
        float offsetRight = lineChart.getViewPortHandler().offsetRight();
        float offsetTop = lineChart.getViewPortHandler().offsetTop();
        float offsetBottom = lineChart.getViewPortHandler().offsetBottom();
        lineChart.setViewPortOffsets(offsetLeft, offsetTop, 0, 130);

        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setDrawLabels(true);
        yAxis.setDrawAxisLine(false);
        yAxis.setDrawGridLinesBehindData(true);
        yAxis.enableGridDashedLine(10f, 10f, 0f);


        yAxis.setGranularity(10.0f);


        yAxis.setAxisMinimum(10);
        yAxis.setAxisMaximum(110);
        yAxis.setGranularityEnabled(true);
        yAxis.setGridColor(Color.parseColor("#3c3c3c"));
        yAxis.setTextColor(Color.parseColor("#3b3b3b"));

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setEnabled(true);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setDrawGridLinesBehindData(true);
        xAxis.setLabelCount(6);
        xAxis.enableGridDashedLine(10f, 10f, 0f);
        xAxis.setGridColor(Color.parseColor("#3c3c3c"));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawLabels(true);
        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(210);
        xAxis.setDrawAxisLine(false);
        xAxis.setTextColor(Color.parseColor("#3c3c3c"));
        xAxis.setLabelRotationAngle(90);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

//        xAxis.setValueFormatter((value, axis) -> {
//            int hours = (int) (value / 60);
//            int minutes = (int) (value % 60);
//            cal.set(Calendar.HOUR_OF_DAY, hours);
//            cal.set(Calendar.MINUTE, minutes);
//            return sdfHour.format(cal.getTime()).toLowerCase(Locale.ROOT);
//        });

        lineChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                Drawable tempIcon = e.getIcon();
                int hours = (int) (e.getX() / 60);
                int minutes = (int) (e.getX() % 60);
                cal.set(Calendar.HOUR_OF_DAY, hours);
                cal.set(Calendar.MINUTE, minutes);
                int index = (int) e.getX();
//                if(tempIcon!=null){
//                    if(graphIcons.get(index).equals("FOOD")){
//                        Log.d("GraphClick","Food Clicked");
//                    }else if(graphIcons.get(index).equals("ACTIVITY")){
//                        Log.d("GraphClick","Activity Clicked");
//                    }else {
//                        logGraph(cal.getTimeInMillis());
//                    }
//                }
            }

            @Override
            public void onNothingSelected() {

            }
        });


        lineChart.setVisibleXRangeMaximum(210);
        lineChart.setVisibleXRangeMinimum(7);
        Calendar tempCal = Calendar.getInstance();
        int hourVal = tempCal.get(Calendar.HOUR_OF_DAY);
        float chartPosition = 0;
        if (hourVal > 11) {
            int minVal = tempCal.get(Calendar.MINUTE);
            int index = hourVal * 60;
            chartPosition = index - 600;    //to get the position of 10 hour back
        }
        lineChart.moveViewToX(0);
        lineChart.setVisibleXRangeMaximum(210);
//        lineChart.setRendererLeftYAxis(new GlucoseGraphYAxisRenderer(lineChart.getViewPortHandler(), yAxis, lineChart.getTransformer(YAxis.AxisDependency.LEFT)));
//        lineChart.post(() -> {
//            setupGradient(lineChart, 10, 110);
//        });
    }


    private void setupGradient(LineChart mChart, float minAxis, float maxAxis) {

        Paint paint = mChart.getRenderer().getPaintRender();

//        LinearGradient linGrad = getGradient(minAxis,maxAxis);
//        paint.setShader(linGrad);
//        mChart.invalidate();
    }

    private int[] getGradientColors(float minAxis, float maxAxis) {
        int difference = (int) (maxAxis - minAxis);
        int stepValue = 10;
        if (difference > 90) stepValue = 20;

        int length = difference / stepValue;
        if (difference % stepValue != 0) length++;

        int[] gradientColors = new int[length];

        for (int i = 0, j = (int) minAxis; i < length; i++, j = j + stepValue) {
            if (j > 60) {
                gradientColors[i] = Color.parseColor("#ff2617");
            } else if (j > 30) {
                gradientColors[i] = Color.parseColor("#fff717");
            } else {
                gradientColors[i] = Color.parseColor("#1bff17");
            }
        }

        return gradientColors;
    }

    private Entry getYValueForAverage(float x,List<Entry> listEntries){
        Entry result;
        //if list has no items returns null
        if(listEntries.isEmpty())
            return null;
        int lastPos = listEntries.size()-1;
        //if x is before the first item, return item same as first
        if(listEntries.get(0).getX()>=x){
            return listEntries.get(0);
        }else if(listEntries.get(lastPos).getX()<=x){    //if x is after the last x value, take as last entry
            return listEntries.get(lastPos);
        }
        //find the nearest index if it between the last and first item
        float distance = Math.abs(listEntries.get(0).getX() - x);
        int idx = 0;
        for(int c = 1; c < listEntries.size(); c++){
            float cdistance = Math.abs(listEntries.get(c).getX() - x);
            if(cdistance < distance){
                idx = c;
                distance = cdistance;
            }
        }

        Entry tempEntry1 = listEntries.get(idx);
        Entry tempEntry2;
        float startX = tempEntry1.getX();
        float endX;
        if(tempEntry1.getX()<x) {
            tempEntry2 = listEntries.get(idx+1);
            endX = tempEntry2.getX();
        }
        else if(tempEntry1.getX()==x)
            return tempEntry1;
        else {
            tempEntry2 = listEntries.get(idx-1);
            startX = tempEntry2.getX();
            endX = tempEntry1.getX();
        }

        float y1 = tempEntry1.getY();
        float y2 = tempEntry2.getY();

        float difference = endX - startX;
        float percentage = (x-startX)/difference;

        float resultY;

        if(tempEntry1.getY() == tempEntry2.getY()){
            resultY = tempEntry1.getY();
        }else if(tempEntry1.getY()<tempEntry2.getY() && tempEntry1.getX()<tempEntry2.getX()){
            resultY = tempEntry1.getY() + (percentage * (tempEntry2.getY()-tempEntry1.getY()));
            Log.d("TEST","1 resultY "+resultY+" e1Y = "+tempEntry1.getY()+" + (perce * (e2y "+tempEntry2.getY()+" - "+tempEntry1.getY());
        }else if(tempEntry1.getY()>tempEntry2.getY() && tempEntry1.getX()>tempEntry2.getX()){
            resultY = tempEntry2.getY() + (percentage * (tempEntry1.getY()-tempEntry2.getY()));
            Log.d("TEST","2 resultY "+resultY+" e2Y = "+tempEntry2.getY()+" + (perce * (e1y "+tempEntry1.getY()+" - "+tempEntry2.getY());
        }else if(tempEntry2.getY()>tempEntry1.getY()){
            resultY = tempEntry2.getY() - (percentage * (tempEntry2.getY()-tempEntry1.getY()));
            Log.d("TEST","4 resultY "+resultY+" e2Y = "+tempEntry2.getY()+" + (perce * (e2y "+tempEntry2.getY()+" - "+tempEntry1.getY());
        }else{
            resultY = tempEntry1.getY() - (percentage * (tempEntry1.getY()-tempEntry2.getY()));
            Log.d("TEST","3 resultY "+resultY+" e2Y = "+tempEntry2.getY()+" + (perce * (e1y "+tempEntry1.getY()+" - "+tempEntry2.getY());
        }

        return new Entry(x,resultY);
    }

}
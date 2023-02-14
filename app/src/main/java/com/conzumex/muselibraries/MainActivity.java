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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LineChart lineChart = findViewById(R.id.line_chart);

        loadChart(lineChart,getLineData());

    }

    private LineData getLineData(){
        LineData lineData = new LineData();
        List<List<Entry>> entriesList = new ArrayList<>();

        Calendar tempCal = Calendar.getInstance();

        List<Entry> entries = new ArrayList<>();

        int previousIndex = 0;

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

        if (!entries.isEmpty()) entriesList.add(new ArrayList<>(entries));

        lineData.addDataSet(getSecondaryLineDataSet());

        for (List<Entry> e : entriesList) {
            LineDataSet lineDataSet = new LineDataSet(e, "Intensity items");
            lineDataSet.setDrawCircles(false);
            lineDataSet.setDrawValues(false);
            lineDataSet.setLineWidth(2.5f);
            lineDataSet.setDrawHorizontalHighlightIndicator(false);
            lineDataSet.setDrawVerticalHighlightIndicator(false);
            lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            lineData.addDataSet(lineDataSet);
        }


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
        lineDataSet.setSecondary(true);
        lineDataSet.setColor(Color.parseColor("#800035"));
        lineDataSet.setDrawValues(false);
        lineDataSet.setLineWidth(1.5f);
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
        xAxis.setAxisMaximum(500);
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


        lineChart.setVisibleXRangeMaximum(700);
        lineChart.setVisibleXRangeMinimum(7);
        Calendar tempCal = Calendar.getInstance();
        int hourVal = tempCal.get(Calendar.HOUR_OF_DAY);
        float chartPosition = 0;
        if (hourVal > 11) {
            int minVal = tempCal.get(Calendar.MINUTE);
            int index = hourVal * 60;
            chartPosition = index - 600;    //to get the position of 10 hour back
        }
        lineChart.moveViewToX(chartPosition);
        lineChart.setVisibleXRangeMaximum(1440);
//        lineChart.setRendererLeftYAxis(new GlucoseGraphYAxisRenderer(lineChart.getViewPortHandler(), yAxis, lineChart.getTransformer(YAxis.AxisDependency.LEFT)));
        lineChart.post(() -> {
            setupGradient(lineChart, 10, 110);
        });
    }


    private void setupGradient(LineChart mChart, float minAxis, float maxAxis) {

        Paint paint = mChart.getRenderer().getPaintRender();
        int height = mChart.getHeight();
        int width = mChart.getWidth();
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

        LinearGradient linGrad = new LinearGradient(0, height, 0, 0, gradientColors, null, Shader.TileMode.REPEAT);
        paint.setShader(linGrad);
        mChart.invalidate();
    }

}
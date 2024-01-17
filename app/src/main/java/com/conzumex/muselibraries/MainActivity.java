package com.conzumex.muselibraries;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.res.ResourcesCompat;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.conzumex.charts.charts.LineChart;
import com.conzumex.charts.charts.RoundedBarChart;
import com.conzumex.charts.charts.RoundedCandleChart;
import com.conzumex.charts.charts.RoundedCombinedChart;
import com.conzumex.charts.components.AxisBase;
import com.conzumex.charts.components.LimitLine;
import com.conzumex.charts.components.XAxis;
import com.conzumex.charts.components.YAxis;
import com.conzumex.charts.data.BarData;
import com.conzumex.charts.data.BarDataSet;
import com.conzumex.charts.data.BarEntry;
import com.conzumex.charts.data.CandleData;
import com.conzumex.charts.data.CandleDataSet;
import com.conzumex.charts.data.CandleEntry;
import com.conzumex.charts.data.CombinedData;
import com.conzumex.charts.data.Entry;
import com.conzumex.charts.data.LineData;
import com.conzumex.charts.data.LineDataSet;
import com.conzumex.charts.formatter.IAxisValueFormatter;
import com.conzumex.charts.highlight.Highlight;
import com.conzumex.charts.listener.OnChartValueSelectedListener;
import com.conzumex.mfmeter.FuelIcon;
import com.conzumex.mfmeter.FuelLog;
import com.conzumex.mfmeter.FuelSession;
import com.conzumex.mfmeter.MFMeter;
import com.conzumex.mfmeter.sleepgraph.SleepEntry;
import com.conzumex.mfmeter.sleepgraph.SleepStageGraph;
import com.conzumex.progressbar.ProgressTextFormatter;
import com.conzumex.progressbar.RoundedProgressBar;
import com.conzumex.progressbar.RoundedProgressBarVertical;
import com.conzumex.progressbar.charts.ProgressBarGraphChart;
import com.conzumex.progressbar.charts.ProgressRoundGraphChart;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

//    List<Entry> listEntries;
//    LineData data;
//    EditText edtText;
//    Button btn;
//    RoundedProgressBar progressBar,progressBar2;
//    ProgressTextFormatter progressFormatter;
    MFMeter meter;
    Button btn;
    RoundedBarChart barChart;
//    RoundedProgressBarVertical rbVertical;
    ProgressBarGraphChart pbBarchart;
    ProgressRoundGraphChart pbRoundchart;

    RoundedCombinedChart roundCandle;
    SleepStageGraph sleepGraph;
    LineChart lineChart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        LineChart lineChart = findViewById(R.id.line_chart);
//        edtText = findViewById(R.id.edt_text);
//        btn = findViewById(R.id.button);
//        progressBar = findViewById(R.id.prog_test);
//        progressBar2 = findViewById(R.id.pb_120);
        meter = findViewById(R.id.meter);
        btn = findViewById(R.id.button);
        barChart = findViewById(R.id.barchart);
        pbBarchart = findViewById(R.id.pb_barchart);
        pbRoundchart = findViewById(R.id.pb_roundchart);
        lineChart = findViewById(R.id.line_chart);
//        rbVertical = findViewById(R.id.roundedProgressBarVertical);

//        roundCandle = findViewById(R.id.chart_progress_2);
        sleepGraph = findViewById(R.id.sleep_graph);

        loadChart(lineChart);


        long tempStartTime = 1680546600000L;
        List<FuelSession> list = new ArrayList<>();
        list.add(new FuelSession(new Date(tempStartTime+(1000*60*30)),new Date(tempStartTime+(1000*60*90)),Color.parseColor("#f71e1e"),Color.parseColor("#40CB2020")));
        list.add(new FuelSession(new Date(tempStartTime+(1000*60*150)),new Date(tempStartTime+(1000*60*200)),Color.parseColor("#f71e1e"),Color.parseColor("#40CB2020")));
        list.add(new FuelSession(new Date(tempStartTime+(1000*60*350)),new Date(tempStartTime+(1000*60*450)),Color.parseColor("#f71e1e"),Color.parseColor("#40CB2020")));
        list.add(new FuelSession(new Date(tempStartTime+(1000*60*500)),new Date(tempStartTime+(1000*60*550)),Color.parseColor("#f71e1e"),Color.parseColor("#40CB2020")));
        list.add(new FuelSession(new Date(tempStartTime+(1000*60*750)),new Date(tempStartTime+(1000*60*1000)),Color.parseColor("#f71e1e"),Color.parseColor("#40CB2020")));
        List<FuelLog> listLog = new ArrayList<>();
        listLog.add(new FuelLog(new Date(tempStartTime+(1000*60*50)),new Date(tempStartTime+(1000*60*70))));
        List<FuelIcon> listIcon = new ArrayList<>();
        listIcon.add(new FuelIcon(new Date(tempStartTime+(1000*60*70)),R.drawable.ic_graph_marker));
        meter.loadData(list,listLog,listIcon);

        meter.setSnapPos(2);
        meter.setSnapEnabled(false);

        btn.setOnClickListener(vew->{
            meter.scrollToSnapPos(3);
        });

//        rbVertical.setProgressTextFormatter(new ProgressTextFormatter() {
//            @NonNull
//            @Override
//            public String getMinWidthString() {
//                return "100";
//            }
//
//            @NonNull
//            @Override
//            public String getProgressText(float progressValue) {
//                return (int)(progressValue*100)+"";
//            }
//        });

//        progressFormatter = new ProgressTextFormatter() {
//            @NonNull
//            @Override
//            public String getProgressText(float v) {
//                return new DecimalFormat("0.#").format(Math.round(v * 1000.0) / 10.0)+"%";
//            }
//
//            @NonNull
//            @Override
//            public String getMinWidthString() {
//                return "%";
//            }
//        };
//
//        listEntries = getEntries();
//        data = getLineData(listEntries);
//        loadChart(lineChart,data);
//
//        progressBar2.setProgressTextFormatter(progressFormatter);
//
//        btn.setOnClickListener(view->{
//            float val = Float.parseFloat(edtText.getText().toString());
//            listEntries = getEntries();
//
//            Entry newEntry = getYValueForAverage(val,listEntries);
//            newEntry.setIcon(getDrawable(R.drawable.ic_graph_marker));
//
//            Entry temp2Entry = new Entry(val+20, newEntry.getY(),newEntry.getIcon());
//
////            listEntries.add(newEntry);
////
////            Collections.sort(listEntries);
//            data = getLineData(listEntries);
//            List<Entry> listIcons = new ArrayList<>();
//            listIcons.add(newEntry);
////            listIcons.add(temp2Entry);
//            LineDataSet iconSet = new LineDataSet(listIcons,"icons");
//            data.addDataSet(iconSet);
//            loadChart(lineChart,data);
//            progressBar.setProgressPercentage(val,true);
//            progressBar2.setProgressPercentage(val,true);
//        });

        loadChart();

        List<SleepEntry> sleepEntries = new ArrayList<>();
        sleepEntries.add(new SleepEntry(0,2));
        sleepEntries.add(new SleepEntry(1,3));
        sleepEntries.add(new SleepEntry(2,2));
        sleepEntries.add(new SleepEntry(3,1));
        sleepEntries.add(new SleepEntry(4,3));
        sleepEntries.add(new SleepEntry(6,2));
        sleepEntries.add(new SleepEntry(7,0));
        sleepEntries.add(new SleepEntry(8,2));

        sleepGraph.loadData(sleepEntries);
        sleepGraph.setLabelXFormatter(value -> ((int)value)+" am");
        sleepGraph.setLabelYFormatter(value -> {
            if(value==0)
                return "Deep";
            else if(value==1)
                return "Light";
            else if(value==2)
                return "REM";
            else
                return "Awake";
        });


//        loadCandleData();
        List<Integer> entries = new ArrayList<>();
        entries.add(54);
        entries.add(75);
        entries.add(60);
        entries.add(80);
        entries.add(70);
        entries.add(64);
        entries.add(36);
        pbBarchart.setProgressData(entries);
        pbBarchart.disablePercentage("9");
        pbRoundchart.setProgressData(entries);

    }

    void loadChart(LineChart lineChart) {
        List<Entry> lineData = getTestTemperatureData();

        if (lineData.isEmpty()) {
            lineChart.setData(null);
            lineChart.setNoDataText("No Data Available");
            lineChart.setNoDataTextColor(getColor(R.color.white));
            lineChart.invalidate();
            return;
        }

        int lastPos = lineData.size()-1;
        Entry tempLastEntry = lineData.get(lastPos);
//        tempLastEntry.setIcon(AppCompatResources.getDrawable(getApplicationContext(),R.drawable.ic_graph_marker));
        lineData.set(lastPos,tempLastEntry);

        LineDataSet set = new LineDataSet(lineData, "lineDataSet");
        set.setValueTextColor(Color.WHITE);
        set.setHighlightEnabled(false);
        set.setColors(getColor(R.color.purple_200));
        set.setDrawIcons(true);
        set.setDrawValues(false);
        set.setDrawCircles(false);
        set.setLineWidth(2);

        List<Entry> fillEntries = new ArrayList<>();
        fillEntries.add(new Entry(0.5f,99));
        fillEntries.add(new Entry(24.5f,99));
        LineDataSet fillset = new LineDataSet(fillEntries, "fillDataSet");
        fillset.setDrawFilled(true);
        fillset.setDrawCircles(false);
        fillset.setColor(getColor(R.color.white));
        fillset.setHighlightEnabled(false);
        int[] colors = new int[]{getColor(R.color.new_graph_intensity_red_light), getColor(R.color.new_graph_intensity_orange_light), getColor(R.color.new_graph_intensity_yellow_light), getColor(R.color.new_graph_intensity_green_light),};
        fillset.setFillDrawable(new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, colors));

        LineData data = new LineData(set);
//        data.addDataSet(fillset);

        lineChart.setData(data);
        lineChart.getLegend().setEnabled(false);
        lineChart.setScaleEnabled(false);
        lineChart.getDescription().setEnabled(false);
        lineChart.getAxisLeft().setEnabled(false);
        YAxis yAxis = lineChart.getAxisRight();
        yAxis.setDrawLabels(true);
        yAxis.setDrawGridLines(true);
        yAxis.setDrawAxisLine(false);
        lineChart.getAxisLeft().setAxisMinimum(97.5f);
        yAxis.setAxisMinimum(97.5f);
        yAxis.setAxisMaximum(100.5f);
        yAxis.setGranularity(1.0f);
        yAxis.setTextColor(getColor(R.color.white));

        LimitLine limitLine = new LimitLine(99);
        limitLine.enableDashedLine(10f,10f,0);
        limitLine.setLineColor(getColor(R.color.white));
        limitLine.setLineWidth(1f);
        limitLine.setGradientColors(new int[]{getColor(R.color.new_graph_intensity_red_light),getColor(R.color.transparent)});
        yAxis.addLimitLine(limitLine);
        yAxis.setDrawLimitLinesBehindData(true);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setDrawLabels(true);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);
        xAxis.setAxisMinimum(0.5f);
        xAxis.setAxisMaximum(24.5f);
        xAxis.setGranularity(1.0f);
        xAxis.setLabelCount(6);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(getColor(R.color.white));
//        xAxis.setValueFormatter(new IAxisValueFormatter() {
//            @Override
//            public String getFormattedValue(float value, AxisBase axis) {
//                try {
//                    int itemPos = (int) value;
//                    return TimeUtils.formatHourForGraph(itemPos);
//                } catch (Exception e) {
//                    return "";
//                }
//            }
//        });

        lineChart.invalidate();
//        lineChart.post(() -> {
//            CustomMarkerView mv = new CustomMarkerView(context, R.layout.custom_graph_marker);
//            int width = lineChart.getWidth();
//            mv.setChartWidth(width);
//            lineChart.setMarkerView(mv);
//            mv.setSupplier((e, highlight) -> {
//                int itemPos = (int) e.getX();
//                if (tempPrevEntry == null || tempPrevEntry != e) {
//                    mVibrator.vibrate(VibrationUtils.SHORT_VIBRATE);
//                    tempPrevEntry = e;
//                }
//                return "<font color=#ffffff>" + TimeUtils.getWeekDayName(itemPos) + "  : </font> <font color=#f2e14c>" + (int) e.getY() + " km</font>";
//            });
//        });
    }

    List<Entry> getTestTemperatureData(){
        List<Entry> entries = new ArrayList<>();

        entries.add(new Entry(0, 98));
        entries.add(new Entry(1, 98.2f));
        entries.add(new Entry(2, 98.3f));
        entries.add(new Entry(3, 98.5f));
        entries.add(new Entry(4, 98.4f));
        entries.add(new Entry(5, 98.3f));
        entries.add(new Entry(6, 98.7f));
        entries.add(new Entry(7, 99));
        entries.add(new Entry(8, 98.8f));
        entries.add(new Entry(9, 98));
        entries.add(new Entry(10, 98.4f));
        entries.add(new Entry(11, 99));
        entries.add(new Entry(12, 99.5f));
        entries.add(new Entry(13, 99));
        entries.add(new Entry(14, 98.3f));
        entries.add(new Entry(15, 98.5f));
        entries.add(new Entry(16, 98));
//        entries.add(new Entry(17, 100));
//        entries.add(new Entry(18, 100));
//        entries.add(new Entry(19, 100));
//        entries.add(new Entry(20, 100));
//        entries.add(new Entry(21, 98));
//        entries.add(new Entry(22, 98));
//        entries.add(new Entry(23, 98));

        return entries;
    }

    private void loadCandleData(){
        List<CandleEntry> canldes = new ArrayList<>();
        canldes.add(new CandleEntry(1,20,40,20,40));
        canldes.add(new CandleEntry(2,30,55,30,55));
        canldes.add(new CandleEntry(3,50,80,50,80));
        canldes.add(new CandleEntry(4,20,90,20,90));

        CandleDataSet dataSet = new CandleDataSet(canldes,"candles");
        dataSet.setDrawValues(true);
        dataSet.setColor(Color.rgb(80, 80, 80));
//        dataSet.setShadowColor(getColor(R.color.transparent));
        dataSet.setShadowWidth(0f);
        dataSet.setBarSpace(0.4f);
        dataSet.setIncreasingColor(Color.parseColor("#d13100"));
        dataSet.setIncreasingPaintStyle(Paint.Style.FILL);
        dataSet.setHighlightEnabled(true);
        dataSet.setDrawHorizontalHighlightIndicator(false);
        dataSet.enableDashedHighlightLine(10f, 5f, 0f);
        dataSet.setHighLightColor(Color.parseColor("#cecece"));
        dataSet.setHighlightLineWidth(1);
        dataSet.setValueTextColor(Color.parseColor("#9c9c9c"));
//        dataSet.setValueTypeface(ResourcesCompat.getFont(this, R.font.nunito_semi_bold));
        dataSet.setValueTextSize(6);
//        Date finalStartDateLabels = startDate;
//        dataSet.setValueFormatter((value,entry, dataSetIndex, viewPortHandler) -> {
//            try {
//                Calendar c = Calendar.getInstance();
//                if (finalStartDateLabels == null) return "";
//                c.setTime(finalStartDateLabels);
//                c.add(Calendar.SECOND, (int) value);
//                return TimeUtils.getCustomTimeFormat(c.getTime(), "hh:mm");
//            } catch (Exception e) {
//                return "";
//            }
//        });


        //for maximum hour value



        CandleData data = new CandleData(dataSet);
        CombinedData dataCombined = new CombinedData();
        dataCombined.setData(data);


//        binding.sleepTimeBarChartView.setDrawOrder(new CombinedChart.DrawOrder[]{CombinedChart.DrawOrder.CANDLE, CombinedChart.DrawOrder.LINE});
//        binding.sleepTimeBarChartView.setData(dataCombined);
//        binding.sleepTimeBarChartView.getLegend().setEnabled(false);
//        binding.sleepTimeBarChartView.setScaleEnabled(false);
//        binding.sleepTimeBarChartView.getDescription().setEnabled(false);
//        binding.sleepTimeBarChartView.setHighlightPerDragEnabled(true);
//        binding.sleepTimeBarChartView.getAxisLeft().setEnabled(false);


        CandleData cdData= new CandleData(dataSet);

        roundCandle.setData(dataCombined);
        roundCandle.getLegend().setEnabled(false);
        roundCandle.setScaleEnabled(false);
        roundCandle.getDescription().setEnabled(false);
        roundCandle.setHighlightPerDragEnabled(true);
        roundCandle.getAxisLeft().setEnabled(false);

        roundCandle.getAxisLeft().setAxisMaximum(100);
        roundCandle.getAxisLeft().setAxisMinimum(0);
        YAxis yAxis = roundCandle.getAxisRight();
        yAxis.setDrawLabels(true);
        yAxis.setDrawAxisLine(true);
        yAxis.setDrawGridLinesBehindData(true);
        yAxis.setAxisMaximum(100);
        yAxis.setAxisMinimum(0);
//        yAxis.setLabelCount(hourCount+1);
//        yAxis.setGranularityEnabled(true);
//        yAxis.setGranularity(3600);
        yAxis.enableGridDashedLine(10f, 10f, 0f);
        yAxis.setGridColor(Color.parseColor("#fefefe"));
        yAxis.setTextColor(getColor(R.color.white));
//        Date finalStartDate = new Date();
//        yAxis.setValueFormatter((value, axis) -> {
//            try {
//                Calendar c = Calendar.getInstance();
//                if (finalStartDate == null) return "";
//
//                c.setTime(finalStartDate);
//                c.add(Calendar.SECOND, (int) value);
//
//                return TimeUtils.getCustomTimeFormat(c.getTime(), "hh:mm aa");
//            } catch (Exception e) {
//                return "";
//            }
//        });


        XAxis xAxis = roundCandle.getXAxis();
        xAxis.setDrawLabels(true);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLinesBehindData(true);
        xAxis.setAxisMinimum(-1);
        xAxis.setAxisMaximum(8);
        xAxis.enableGridDashedLine(10f, 10f, 0f);
        xAxis.setGridColor(Color.parseColor("#fefefe"));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(getColor(R.color.white));

        roundCandle.invalidate();
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

    void loadChart() {
        List<BarEntry> barData = getTestStressData();

        if (barData.isEmpty()) {
            barChart.setData(null);
            barChart.setNoDataText("No Data Available");
            barChart.invalidate();
            return;
        }

        BarDataSet set = new BarDataSet(barData, "BarDataSet");
        set.setValueTextColor(Color.WHITE);
        set.setHighLightAlpha(0);
        set.setDrawValues(false);
        set.setHighlightEnabled(false);
        set.setColors(getColor(R.color.white));
        set.setDrawIcons(false);
        set.setFormLineWidth(5);
        set.setHighLightAlpha(255);
        set.setHighLightColor(getColor(R.color.white));
        set.setHighlightEnabled(true);

        BarData data = new BarData(set);
        data.setBarWidth(0.5f); // set custom bar width

        barChart.setData(data);
        barChart.getLegend().setEnabled(false);
        barChart.setScaleEnabled(false);
        barChart.setFitBars(true); // make the x-axis fit exactly all bars
        barChart.getDescription().setEnabled(false);
        barChart.getAxisLeft().setEnabled(false);
        YAxis yAxis = barChart.getAxisRight();
        yAxis.setDrawLabels(true);
        yAxis.setDrawGridLines(false);
        yAxis.setDrawAxisLine(false);
        barChart.getAxisLeft().setAxisMinimum(-10.0f);
        barChart.getAxisLeft().setAxisMaximum(105f);
        yAxis.setAxisMinimum(-10.0f);
        yAxis.setAxisMaximum(105f);
        yAxis.setTextColor(getColor(R.color.white));
        yAxis.setTypeface(ResourcesCompat.getFont(getApplicationContext(), R.font.rb_medium));

        XAxis xAxis = barChart.getXAxis();
        xAxis.setDrawLabels(true);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.TOP);
        xAxis.setTextColor(getColor(R.color.white));
        xAxis.setTypeface(ResourcesCompat.getFont(getApplicationContext(), R.font.rb_medium));

        barChart.invalidate();
    }

    public static List<BarEntry> getTestStressData(){
        List<BarEntry> entries = new ArrayList<>();

        entries.add(new BarEntry(0, 100));
        entries.add(new BarEntry(1, 75));
        entries.add(new BarEntry(2, 60));
        entries.add(new BarEntry(3, 80));
        entries.add(new BarEntry(4, 70));
        entries.add(new BarEntry(5, 64));
        entries.add(new BarEntry(6, 36));

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
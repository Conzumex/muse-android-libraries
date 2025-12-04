package com.conzumex.mfmeter.sleepgraph;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import com.conzumex.mfmeter.R;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class NapStageGraph extends View {
    boolean drawEditMode = false;
    int parentViewWidth,parentViewHeight;
    float chartWidth,chartHeight;
    float chartValueXArea,chartValueYArea;
    float axisHSpace=-1,axisVSpace=-1;
    int axisValuePaddingH = 50,axisValuePaddingV = 50;
    int gridValuePaddingH = 20,gridValuePaddingV = 20;
    int axisLabelPaddingH = 20,axisLabelPaddingV = 20;
    int bgColor = Color.BLACK;
    int labelColor = Color.WHITE;
    int axisColor = Color.WHITE;
    int gridXColor = Color.GRAY;
    int gridYColor = Color.DKGRAY;
    Paint bgPaint;
    Paint linePaint;
    Paint gridLinePaint;
    Paint valuePaint;
    Paint labelPaint;
    Path mPath;
    Paint markerPaint;
    int cornerRadius = 15;
    float textSize = 28f;
    LinearGradient linearGradient;

    float valueStartPosY;
    float valueStartPosX;
    float valueEndPosX;
    float valueEndPosY;
    float valuePointCount;
    float minuteXValue;
    float maxMinutes = 300;
    float awakeGridY;
    float touchX=-1, touchY=-1;
    float lastMarkerX=-1;
    SleepMarker markerTest;
    boolean drawMarkers = true;
    int markerLayout = R.layout.marker_default;

    String[] stageLabels = new String[]{"Awake","REM","Core","Deep"};
    int[] stageLabelColors = null;
    int[] gradientColors = new int[]{Color.parseColor("#bbb14a37"),Color.parseColor("#bb1b647f"),Color.parseColor("#bb00477e"),Color.parseColor("#bb1a1951")};
    int xAxisLabelCount = 4;
    List<NapEntry> napValues = new ArrayList<>();
    DecimalFormat dc2Point = new DecimalFormat("0.##",new DecimalFormatSymbols(Locale.US));
    labelFormatX labelXFormatter;
    Typeface fontFace = Typeface.DEFAULT;


    public NapStageGraph(Context context) {
        this(context, null, 0);
    }

    public NapStageGraph(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NapStageGraph(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        bgPaint = new Paint();

        linePaint = new Paint();
        linePaint.setColor(axisColor);

        gridLinePaint = new Paint();
        gridLinePaint.setColor(gridXColor);

        labelPaint = new Paint();

        valuePaint = new Paint();
        valuePaint.setColor(Color.YELLOW);
        valuePaint.setStrokeWidth(1);
        valuePaint.setPathEffect(new CornerPathEffect(cornerRadius));

        markerPaint = new Paint();

        mPath = new Path();
        labelXFormatter = value -> dc2Point.format(value)+"";

        if (attrs == null) return;
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.NapStageGraph);

        // Set background color via xml (If exists and isn't the default value)
        @ColorInt int newTextColor = attributes.getColor(R.styleable.NapStageGraph_napGraphBackgroundColor, bgColor);
        if (newTextColor != bgColor) bgColor = newTextColor;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        parentViewWidth = getWidth();
        parentViewHeight = getHeight();

        if(isInEditMode() && drawEditMode) {
            parentViewHeight = 900;
            touchX = 345;
            loadDummyData();
        }

        if(axisHSpace==-1)
            axisHSpace = maxLabelLength() + axisLabelPaddingH;

        if(axisVSpace==-1)
            axisVSpace = textSize + axisLabelPaddingV;

        chartWidth = parentViewWidth-axisHSpace;
        chartHeight = parentViewHeight-axisVSpace;

        chartValueXArea = chartWidth - (axisValuePaddingH*2);
        chartValueYArea = chartHeight - (axisValuePaddingV*2);

        valueStartPosY = chartHeight - axisValuePaddingV;
        valueStartPosX = axisHSpace + axisValuePaddingH;

        valueEndPosX = parentViewWidth - axisValuePaddingH;
        valueEndPosY = axisValuePaddingV;

        valuePointCount = valueEndPosX - valueStartPosX;

        if(linearGradient==null)
            linearGradient = new LinearGradient(
                    valueStartPosX, valueEndPosY, valueStartPosX, valueStartPosY, // Coordinates for the gradient line
                    gradientColors, // Array of colors for the gradient
                    null, // Optional: array of floats for color positions (0.0 to 1.0)
                    Shader.TileMode.CLAMP // How to handle areas outside the gradient bounds
            );
    }

    void loadDummyData(){
        napValues = new ArrayList<>();
        napValues.add(new NapEntry(45));
        napValues.add(new NapEntry(20));
        napValues.add(new NapEntry(134,20));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();

        drawBgAxis(canvas);

        minuteXValue = valuePointCount/maxMinutes;

        drawGridsLabels(canvas);

        valuePaint.setShader(linearGradient);
        drawValues(canvas);

        if (touchX != -1)
            drawMarker(canvas);
//        Log.d("NAP_GRAPH","drawn");

    }

    float maxLabelLength(){

        labelPaint.setColor(Color.WHITE);
        labelPaint.setTextSize(textSize);
        labelPaint.setTextAlign(Paint.Align.RIGHT);
        labelPaint.setTypeface(fontFace);

        float maxLength = 0;
        for(String stage:stageLabels){
            maxLength = Math.max(maxLength,labelPaint.measureText(stage));
        }
        return maxLength;
    }


    void drawValues(Canvas canvas){

        //start of the sleep
        mPath.moveTo(valueStartPosX,valueStartPosY);
        mPath.lineTo(valueStartPosX,awakeGridY + gridValuePaddingV);

        Collections.sort(napValues);

        for(NapEntry nap : napValues){
            mPath.lineTo((nap.xValue*minuteXValue)+valueStartPosX,awakeGridY + gridValuePaddingV);
            mPath.lineTo((nap.xValue*minuteXValue)+valueStartPosX, axisValuePaddingV);
            mPath.lineTo(((nap.xValue + nap.duration)*minuteXValue)+valueStartPosX, axisValuePaddingV);
            mPath.lineTo(((nap.xValue + nap.duration)*minuteXValue)+valueStartPosX, awakeGridY + gridValuePaddingV);
        }

        //ending sleep
        mPath.lineTo(valueEndPosX,awakeGridY + gridValuePaddingV);
        mPath.lineTo(valueEndPosX,valueStartPosY);
        mPath.close();
        canvas.drawPath(mPath,valuePaint);
    }

    void drawGridsLabels(Canvas canvas){
        //grid lines
        labelPaint.setColor(labelColor);
        labelPaint.setTextSize(textSize);
        labelPaint.setTypeface(fontFace);
        gridLinePaint.setColor(gridXColor);

        labelPaint.setTextAlign(Paint.Align.RIGHT);
        awakeGridY = (float) chartHeight / stageLabels.length;
        float gridPos, labelCenterY;
        for(int i=0;i<stageLabels.length;i++){
            if(stageLabelColors!=null && stageLabelColors.length>i){
                labelPaint.setColor(stageLabelColors[i]);
            }
            gridPos = awakeGridY * (i+1);
            canvas.drawLine(axisHSpace,gridPos,parentViewWidth,gridPos,gridLinePaint);
            labelCenterY = gridPos - (awakeGridY/2);
            canvas.drawText(stageLabels[i],axisHSpace - axisLabelPaddingH,labelCenterY,labelPaint);
        }

        //grid lines YAxis
        labelPaint.setColor(labelColor);
        gridLinePaint.setColor(gridYColor);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        float labelYPos = chartHeight +  (axisVSpace /2) +(textSize/2);
        canvas.drawLine(valueStartPosX,0,valueStartPosX,chartHeight,gridLinePaint);
        canvas.drawText(labelXFormatter.getLabel(getIndexFromX(valueStartPosX)),valueStartPosX,labelYPos,labelPaint);
        canvas.drawLine(valueEndPosX,0,valueEndPosX,chartHeight,gridLinePaint);
        float endLabelPosX = valueEndPosX;
        String endLabel = labelXFormatter.getLabel(getIndexFromX(valueEndPosX));
        float labelWidth = measureTextWidth(endLabel,labelPaint);
        endLabelPosX = (endLabelPosX+(labelWidth/2)) > parentViewWidth ? parentViewWidth - labelWidth/2:endLabelPosX;
        canvas.drawText(endLabel,endLabelPosX,labelYPos,labelPaint);
        float gridYDiff = chartValueXArea / (xAxisLabelCount-1);
        float gridYPos;
        for(int i=1;i<xAxisLabelCount-1;i++){
            gridYPos = (gridYDiff * i) + valueStartPosX;
            canvas.drawLine(gridYPos,0,gridYPos,chartHeight,gridLinePaint);
            canvas.drawText(labelXFormatter.getLabel(getIndexFromX(gridYPos)),gridYPos,labelYPos,labelPaint);
        }
    }

    /** set axis label padding Vertically*/
    public void setAxisLabelPaddingV(int value){
        this.axisLabelPaddingV = value;
    }

    /** set axis label padding Horizontally*/
    public void setAxisLabelPaddingH(int value){
        this.axisLabelPaddingH = value;
    }

    /** set grid line x color*/
    public void setXAxisSpaceLabel(int value){
        this.axisVSpace = value;
    }

    /** set grid line x color*/
    public void setGridYColor(int color){
        this.gridYColor = color;
    }

    /** set grid line x color*/
    public void setGridXColor(int color){
        this.gridXColor = color;
    }

    /** set axis line color*/
    public void setAxisColor(int color){
        this.axisColor = color;
    }

    /** set label Color*/
    public void setLabelColor(int labelColor){
        this.labelColor = labelColor;
    }

    /** set stageLabels Colors*/
    public void setGradientColors(int[] gradientColors){
        this.gradientColors = gradientColors;
    }

    /** set stageLabels Colors*/
    public void setStageLabelColors(int[] stageLabels){
        this.stageLabelColors = stageLabels;
    }

    /** set stageLabels*/
    public void setStageLabels(String[] stageLabels){
        this.stageLabels = stageLabels;
    }

    /** set markerView*/
    public void setMarkerView(SleepMarker mMarker){
        this.markerTest = mMarker;
    }

    /** set the nap awake entries*/
    public void setNapValues(List<NapEntry> entries){
        this.napValues = entries;
    }

    /** set font size for the texts*/
    public void setBgColor(int color){
        this.bgColor = color;
    }

    /** set font size for the texts*/
    public void setTextSize(float textSize){
        this.textSize = textSize;
    }

    /** set font family for the texts*/
    public void setFontFace(Typeface fontFace){
        this.fontFace = fontFace;
    }

    /** set the graph duration*/
    public void setMaxMinutes(float maxMinutes){
        this.maxMinutes = maxMinutes;
    }

    /** format the labels for xAxis*/
    public void setLabelXFormatter(labelFormatX labelXFormatter) {
        this.labelXFormatter = labelXFormatter;
    }

    public interface labelFormatX{
        String getLabel(float value);
    }

    float measureTextWidth(String text,Paint lablePaint){
        return lablePaint.measureText(text);
    }

    void drawBgAxis(Canvas canvas){
        bgPaint.setColor(bgColor);
        canvas.drawPaint(bgPaint);
        linePaint.setColor(axisColor);
        canvas.drawLine(axisHSpace,chartHeight,parentViewWidth,chartHeight,linePaint);
        canvas.drawLine(axisHSpace,0,axisHSpace,chartHeight,linePaint);
        if(isInEditMode() && drawEditMode)
            canvas.drawLine(0,parentViewHeight,parentViewWidth,parentViewHeight,linePaint);
    }

    void drawMarker(Canvas canvas){
//        if(setMarkerValueOnly && (touchX<chartValueMinx || touchX>chartValueMaxX)) {
//            if(!showLastMarker)
//                return;
//            else{
//                touchX = lastMarkerX;
//            }
//        }
        markerPaint.setStrokeWidth(2);
        markerPaint.setColor(Color.WHITE);

        //valueTouchX for finding the entries with including linewidth
        float selectedX = getIndexFromX(touchX);
//        float selectedX = 3;
        DecimalFormat df = new DecimalFormat("#.#",new DecimalFormatSymbols(Locale.US));
        selectedX = Float.valueOf(df.format(selectedX));
        float selectedY = getYValueForX(selectedX);
//        float selectedY = 2;
//        SleepEntry selectedEntry = getEntry(selectedX);
//        if(mChangeListener!=null && selectedEntry!=selectedGraphEntry)
//            mChangeListener.onChange(selectedGraphEntry,selectedEntry);
//        selectedGraphEntry = selectedEntry;
//        if(mClickListener!=null)
//            mClickListener.onClick(selectedX,selectedEntry);
//        if(highlightClick)
            canvas.drawLine(touchX,chartHeight,touchX,0,markerPaint); //Grid lines

        lastMarkerX = touchX;

        //drawing marker views
        if(drawMarkers) {
            if(markerTest==null)
                markerTest = new SleepMarker(getContext(),markerLayout);
//            if(markerFormatter==null)
//                markerTest.setContent(selectedX+"");
//            else
//                markerTest.setContent(markerFormatter.onContent(selectedX,selectedEntry));
            markerTest.refreshContent(new SleepEntry(selectedX,selectedY),selectedX);

//            float yPos = 100;
//            try {
//                yPos = getYPosOfEntry(selectedEntry);
//            }catch (Exception e){
//                Log.e("SLeepYPos",e.toString());
//            }
//            int markerHeight = markerTest.getHeight();
//            float chartTopSpace = yPos - ((float) barHeight /2);
////            printDebug(yPos+"y, barH"+barHeight+", marH "+markerHeight+" yDOt "+yDotValue+" yN"+chartTopSpace,canvas);
//            yPos = chartTopSpace < markerHeight ? (barHeight * 3) - (barHeight - yPos) : yPos;
//            float markerY = yPos - barHeight;
//            if(setMarkerTop){
//                markerY = markerTopPadding + (markerHeight/2);
//            }
//            if(showMarkerAlways || selectedEntry!=null)
                markerTest.draw(canvas, touchX, awakeGridY, valueStartPosX,valueEndPosX);
        }
    }

    float getYValueForX(float x){
        for(NapEntry nap : napValues){
            if(nap.xValue == x){
                return 1;
            }
        }
        return 0;
    }


    float getIndexFromX(float canvasX){
        return Math.round((canvasX-valueStartPosX)/minuteXValue);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean touchedArea = false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
//            case MotionEvent.ACTION_UP:
                float eventX = event.getX();
                float eventY = event.getY();
                if(eventX>=valueStartPosX && eventX<=valueEndPosX && eventY >= valueEndPosY && eventY <= valueStartPosY) {
                    touchX = event.getX();
                    touchY = event.getY();
                    touchedArea = true;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                eventX = event.getX();
                eventY = event.getY();
                if(eventX>=valueStartPosX && eventX<=valueEndPosX && eventY >= valueEndPosY && eventY <= valueStartPosY) {
                    touchX = event.getX();
                    touchY = event.getY();
                    touchedArea = true;
                }
                break;
            default:
                return false;
        }
        invalidate();
        return touchedArea;
    }

    void printDebug(Canvas canvas,String text){
        Paint mDebugPaint = new Paint();
        mDebugPaint.setColor(Color.YELLOW);
        mDebugPaint.setTextSize(20);
        canvas.drawText(text,30,30,mDebugPaint);
    }
}

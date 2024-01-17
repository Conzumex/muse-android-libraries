package com.conzumex.mfmeter.sleepgraph;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.OverScroller;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.res.ResourcesCompat;

import com.conzumex.mfmeter.FuelIcon;
import com.conzumex.mfmeter.FuelLog;
import com.conzumex.mfmeter.FuelSession;
import com.conzumex.mfmeter.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SleepStageGraph extends View {
    int parentViewWidth,parentViewHeight;
    int chartWidth,chartHeight;
    int chartOffsetV = 0,chartOffsetH = 20;
    float chartGraphWidth,chartGraphHeight;
    float chartGraphStartX,chartGraphStartY;
    float chartGraphEndX,chartGraphEndY;
    float xDotValue,yDotValue;
    int axisWidth = 1;
    /**This maxValue will  be draw the last bar till here
     * */
    float maxXvalue=8f;
    float minXvalue=0;
    float maxYvalue=3;
    float minYvalue=0;
    Paint mPaint,gridPaint,mBarPaint,mLinePaint,labelPaint,edgeGridPaint;
    int barHeight = 75;
    int radiusBar = 7;
    float lineWidth = 4f;
    float textSize = 25;
    int labelCount = 6;
    float prevBarLineY = -1,prevBarLineX = -1;
    int prevBarColor = -1;
    RectF rectSquare;
    int colorBackground = Color.parseColor("#000000");
    int colorXAxis = Color.parseColor("#ffffff");
    int colorXLabel = Color.parseColor("#707070");
    int colorYLabel = Color.parseColor("#707070");
    int colorEdgeHighlight = Color.parseColor("#ffffff");
    int colorYAxis = Color.parseColor("#707070");
    int colorTemp = Color.parseColor("#fcfcfc");
    int gridXColor = Color.parseColor("#707070");
    int gridYColor = Color.parseColor("#707070");

    int[] colorRanges = new int[]{Color.parseColor("#144da7"),Color.parseColor("#6b58b9"),Color.parseColor("#a658b9"),Color.parseColor("#23b8be")};
    List<SleepEntry> entries;
    List<Float> xValues,yValues;

    labelFormatX labelXFormatter;
    labelFormatY labelYFormatter;
    DecimalFormat dc2Point = new DecimalFormat("0.##");
    float chartPaddingH = 50,chartPaddingV = 50;
    boolean drawXAxis = true,drawYAxis = false;
    float insideAxisWidth = 50;
    boolean highlightEdges = true;
    float edgeLineWidth = 2f;

    DashPathEffect gridXEffect;
    DashPathEffect gridYEffect;
    boolean highlightEdgeValues = true;
    boolean changeLabelColors = true;

    public SleepStageGraph(Context context) {
        this(context, null, 0);
    }

    public SleepStageGraph(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SleepStageGraph(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mPaint = new Paint();
        gridPaint = new Paint();
        edgeGridPaint = new Paint();
        mBarPaint = new Paint();
        mLinePaint = new Paint();
        labelPaint = new Paint();

        labelXFormatter = value -> dc2Point.format(value)+"";
        labelYFormatter = value -> dc2Point.format(value)+"";

        gridXEffect = new DashPathEffect(new float[]{10,10}, 0);

        loadDummyData();
    }


    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        parentViewWidth = getWidth();
        parentViewHeight = getHeight();
        chartWidth = parentViewWidth-100;
        chartHeight = parentViewHeight-100;

//        int tempHeight = 600;
//        chartHeight = tempHeight - 100;
        chartGraphHeight = chartHeight - chartOffsetV;
        chartGraphWidth = chartWidth - chartOffsetH - chartPaddingH;
        chartGraphStartX = 0 + (chartOffsetH/2) + (chartPaddingH/2);
        chartGraphStartY = 0 + (chartOffsetV/2);
        chartGraphEndX = chartWidth-(chartOffsetH/2) - (chartPaddingH/2);
        chartGraphEndY = chartHeight-(chartOffsetV/2);

        Paint paint2 = new Paint();
        paint2.setColor(Color.DKGRAY);
        paint2.setStyle(Paint.Style.FILL);
        canvas.drawPaint(paint2);
        Rect viewRect = new Rect(0,0,parentViewWidth,parentViewHeight);
        mPaint.setColor(Color.BLACK);
        mPaint.setStrokeWidth(10);
        canvas.drawRect(viewRect,mPaint);

        mPaint.setStrokeWidth(0);


//        int barFullStartX = 0+1;
//        int barFullStartY = ((tempHeight-100)/2)-50;
//        int barFullEndX = parentViewWidth-100-axisWidth;
//        int barFullEndY = ((tempHeight-100)/2)+50;
//        RectF itemRect = new RectF(barFullStartX,barFullStartY,barFullEndX,barFullEndY);
//        mPaint.setColor(Color.BLUE);
//        canvas.drawRoundRect(itemRect,10,10,mPaint);
//
//        mPaint.setColor(Color.YELLOW);
//        RectF itemRect1 = new RectF(barFullStartX,barFullStartY,barFullStartX+300,barFullEndY);
//        canvas.drawRoundRect(itemRect1,10,10,mPaint);
//
//        mPaint.setColor(Color.RED);
//        RectF itemRect2 = new RectF(barFullStartX+300,barFullStartY-120,barFullEndX,barFullEndY-120);
//        canvas.drawRoundRect(itemRect2,10,10,mPaint);

        calculateDotValues();
        drawLabels(canvas);
        drawValues(canvas);
        drawAxis(canvas);
        if(highlightEdges)
            drawGraphEdges(canvas);
    }

    public void loadData(List<SleepEntry> entries){
        this.entries = entries;
        invalidate();
    }

    void loadDummyData(){
        entries = new ArrayList<>();
        entries.add(new SleepEntry(0,2));
        entries.add(new SleepEntry(3,1));
        entries.add(new SleepEntry(4,3));
        entries.add(new SleepEntry(6,2));
        entries.add(new SleepEntry(7,0));
        entries.add(new SleepEntry(8,2));
    }

    void drawGraphEdges(Canvas canvas){
        float xLabelInterval = (chartGraphWidth)/labelCount;
        edgeGridPaint.setColor(colorEdgeHighlight);
        edgeGridPaint.setStrokeWidth(edgeLineWidth);
        edgeGridPaint.setPathEffect(new DashPathEffect(new float[]{10,10}, 0));
        // (lineWidth/2) for the grgh bsar connecting line width
        float xPos = chartGraphStartX - (lineWidth/2) - (edgeLineWidth/2);
        canvas.drawLine(xPos,chartHeight,xPos,0,edgeGridPaint); //Grid lines
        float endXPos = (labelCount*xLabelInterval)+chartGraphStartX + (lineWidth/2) + (edgeLineWidth/2);
        canvas.drawLine(endXPos,chartHeight,endXPos,0,edgeGridPaint); //Grid lines
    }

    void drawAxis(Canvas canvas){
        mPaint.setStrokeWidth(axisWidth);
        mPaint.setColor(colorYAxis);
        if(drawYAxis)
            canvas.drawLine(chartWidth,0,chartWidth,chartHeight,mPaint);    //yAxis

        mPaint.setColor(colorXAxis);
        if(drawXAxis)
            canvas.drawLine(0,chartHeight,chartWidth+insideAxisWidth,chartHeight,mPaint);   //xAxis
    }

    void drawValues(Canvas canvas){
        Collections.sort(entries);
        mLinePaint.setStrokeWidth(lineWidth);
        gridPaint.setStrokeWidth(1);
        gridPaint.setColor(gridXColor);
        int barColor;
        String tempDeb = "";
        xValues = new ArrayList<>();
        yValues = new ArrayList<>();
        prevBarLineY = -1;
        prevBarColor = -1;
        for(int i=0;i<entries.size();i++){
            float startX = (entries.get(i).xValue * xDotValue) + chartGraphStartX;
            float endX = chartGraphEndX;
            xValues.add(startX);
            if(i!=entries.size()-1) endX = (entries.get(i+1).xValue*xDotValue)+chartGraphStartX;
            if(i!=entries.size()-1) xValues.add(endX);
            Log.d("Lines","start "+startX);
            mPaint.setColor(colorTemp);
            float yPos = (entries.get(i).yValue + 1) * yDotValue;
            yPos = yPos +chartGraphStartY;
//            canvas.drawLine((chartGraphStartX),yPos,(chartGraphEndX),yPos,gridPaint); //Grid lines
            canvas.drawLine((chartGraphStartX-(chartPaddingH/2)-(chartOffsetH/2)),yPos,(chartGraphEndX+chartPaddingH/2+(chartOffsetH/2)+insideAxisWidth ),yPos,gridPaint); //Grid lines
            yPos = yPos - (yDotValue/2);
//            canvas.drawLine(startX,yPos,endX,yPos,mPaint);

            yPos = chartHeight - yPos;
            if(!yValues.contains(yPos))
                yValues.add(yPos);

            float rectTop = yPos - (barHeight/2);
            //todo make line included
            startX = startX - (lineWidth/2);
            endX = endX + (lineWidth/2);
            RectF tempRect = new RectF(startX,rectTop,endX,rectTop+barHeight);
            float itemYVal = entries.get(i).yValue;
            //todo set colors temporary
            barColor=getPosColor((int)itemYVal);
            mBarPaint.setColor(barColor);
            canvas.drawRoundRect(tempRect,radiusBar,radiusBar,mBarPaint);

            if(prevBarLineY!=-1){
//                tempDeb = tempDeb+" {x:"+startX+", prevY:"+prevBarLineY+" ,ypos "+yPos +"} ";
                tempDeb = tempDeb+" {x:"+startX+" - end "+prevBarLineX+"} ";
                float lineX = startX + (lineWidth/2);
                //TODO make dynamic gradient
                mLinePaint.setShader(new LinearGradient(prevBarLineX,(chartGraphEndY-(yDotValue/2)),startX,(chartGraphStartY+(yDotValue/2)), colorRanges,null,  Shader.TileMode.CLAMP));
                canvas.drawLine(lineX,prevBarLineY,lineX,yPos,mLinePaint);
            }

            prevBarLineY = yPos;
            prevBarLineX = endX;
            prevBarColor = barColor;
        }

//        printDebug(tempDeb,canvas);


    }

    void drawLabels(Canvas canvas){
        labelPaint.setColor(colorTemp);
        labelPaint.setTextAlign(Paint.Align.LEFT);
        labelPaint.setTextSize(textSize);
        Set<Float> yVals = SleepEntry.yValsUnique(entries);
        labelPaint.setColor(colorYLabel);
        for(Float yVal : yVals){
            float yPos = (yVal + 1) * yDotValue;
            yPos = yPos +chartGraphStartY;
            float labelVal = (yPos/yDotValue)-1;
            yPos = yPos - (yDotValue/2);
            yPos = chartHeight - yPos;
            if(changeLabelColors)
                labelPaint.setColor(getPosColor((int)labelVal));
            canvas.drawText(labelYFormatter.getLabel(labelVal),chartGraphEndX + (chartPaddingH/2) + 25,yPos+(textSize/2),labelPaint);
        }
        labelPaint.setColor(colorXLabel);
        gridPaint.setColor(gridYColor);
        float xLabelInterval = (chartGraphWidth)/labelCount;
        for(int i=0;i<labelCount+1;i++){
            if(gridXEffect!=null)
                gridPaint.setPathEffect(gridXEffect);
            float gridXpos = (i*xLabelInterval)+chartGraphStartX;
            //to make the graph connected line width include the line
            if(i==0)
                gridXpos = gridXpos - (lineWidth/2);
            else if(i==labelCount)
                gridXpos = gridXpos + (lineWidth/2);

            canvas.drawLine(gridXpos,chartHeight,gridXpos,0,gridPaint); //Grid lines
            gridPaint.setPathEffect(null);
            labelPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
            if(i==0) {
                labelPaint.setTextAlign(Paint.Align.LEFT);
                if(highlightEdgeValues) {
                    labelPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    labelPaint.setColor(colorEdgeHighlight);
                }
            }else if(i==labelCount) {
                labelPaint.setTextAlign(Paint.Align.RIGHT);
                if(highlightEdgeValues) {
                    labelPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    labelPaint.setColor(colorEdgeHighlight);
                }
            }else {
                labelPaint.setTextAlign(Paint.Align.CENTER);
                labelPaint.setColor(colorXLabel);
            }
            //avoiding offset when calculate the x value
            float xValue = ((i*xLabelInterval))/xDotValue;
            canvas.drawText(labelXFormatter.getLabel(xValue),(i*xLabelInterval)+chartGraphStartX,chartGraphEndY + 50,labelPaint);
        }
//        printDebug(xLabelInterval+"",canvas);
    }

    void calculateDotValues(){
        xDotValue = chartGraphWidth / (maxXvalue-minXvalue + 1);
        yDotValue = chartGraphHeight / (maxYvalue - minYvalue + 1);
    }

    void printDebug(String text,Canvas canvas){
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setTextSize(15);
        canvas.drawText(text,10,chartGraphHeight - 50,paint);
    }

    public interface labelFormatX{
        String getLabel(float value);
    }
    public interface labelFormatY{
        String getLabel(float value);
    }

    int getPosColor(int pos){
        if(colorRanges.length>pos){
            return colorRanges[pos];
        }else{
            int val = colorRanges.length%pos;
            return colorRanges[val];
        }
    }

    public void setGridXDash(float width, float gap){
        this.gridXEffect = new DashPathEffect(new float[]{width,gap}, 0);
    }
    public void setGridYDash(float width, float gap){
        this.gridYEffect = new DashPathEffect(new float[]{width,gap}, 0);
    }

    public void setLabelXFormatter(labelFormatX labelXFormatter) {
        this.labelXFormatter = labelXFormatter;
    }

    public void setLabelYFormatter(labelFormatY labelYFormatter) {
        this.labelYFormatter = labelYFormatter;
    }

}

package com.conzumex.livechart;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Live line chart drawings. Make chart as live using handler and runnable like below::
 *<br><br>
 * E.g : -><br>
 *         LiveChart liveChart = findViewById(R.id.live_chart);<br>
 *         liveChart.setLineType(LineType.LINEAR);<br>
 *         liveChart.startYData(50f);<br>
 *         liveChart.setMinY(0);<br>
 *         liveChart.setMaxY(100);<br>
 *         Handler mHandler = new Handler();<br>
 *         Runnable mRunnable = new Runnable() {<br>
 *             @Override<br>
 *             public void run() {<br>
 *                  float lastChartX = liveChart.getLastX();<br>
 *                 liveChart.addYData(lastY);<br>
 *                 int difference = (int) ((lastChartX + liveChart.getxDot()) - liveChart.getWidth());<br>
 *                 liveChart.scrollTo(Math.max(0,difference),0);<br>
 *
 *                 mHandler.postDelayed(this,100);<br>
 *             }<br>
 *         };<br>
 *         mHandler.post(mRunnable);<br>
 *  */
public class LiveChart extends View {
    Paint mPaint,mLinePaint,mTextPaint,mDebugPaint;
    List<Float> yPos;
    List<Float> xPos;
    Path tempPath;
    LineType chartLineType = LineType.LINEAR;
    int colorLine = Color.RED;
    int colorBackground = -1;
    float lineWidth = 5;
    float minY=9999,maxY=-1;
    int animPos = 0;
    int height = 500;
    float chartPaddingTop = 20,chartPaddingBottom = 20;
    int chartHeight;
    int chartWidth;
    float yDot;
    int xDot=-1;
    int xPercentage = 1;
    float centerClipWidth = 150;
    float lastX = 0;
    boolean isEndStart = true;

    public LiveChart(Context context) {
        super(context);
        init(null,context);
    }
    public LiveChart(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs,context);
    }
    void init(AttributeSet attr,Context context){
        if(attr!=null){
            TypedArray typedArray = context.obtainStyledAttributes(attr, R.styleable.LiveChart, 0, 0);
            colorLine = typedArray.getColor(R.styleable.LiveChart_lineColor, colorLine);
            colorBackground = typedArray.getColor(R.styleable.LiveChart_backgroundColor, colorBackground);
            lineWidth =  typedArray.getDimension(R.styleable.LiveChart_lineWidth, lineWidth);
            chartPaddingTop =  typedArray.getDimension(R.styleable.LiveChart_topSpacing, chartPaddingTop);
            chartPaddingBottom =  typedArray.getDimension(R.styleable.LiveChart_bottomSpacing, chartPaddingBottom);
            centerClipWidth =  typedArray.getDimension(R.styleable.LiveChart_centerClipWidth, centerClipWidth);
        }
        mPaint = new Paint();
        mTextPaint = new Paint();
        mTextPaint.setColor(Color.GREEN);
        mTextPaint.setTextSize(30);
        mLinePaint = new Paint();
        mLinePaint.setColor(colorLine);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeCap(Paint.Cap.ROUND);
        mLinePaint.setStrokeWidth(lineWidth);
        mDebugPaint = new Paint();
        mDebugPaint.setColor(Color.parseColor("#f9cecece"));

        tempPath = new Path();

        if(isInEditMode()){
//            colorBackground = Color.BLACK;
            loadDummyData();
        }
    }

    void setMaxValues(){
        for(Float val : yPos){
            minY = Math.min(val,minY);
            maxY = Math.max(val,maxY);
        }
    }

    public void setData(List<Float> xPos, List<Float> yPos){
        this.xPos=xPos;
        this.yPos = yPos;
        setMaxValues();
        invalidate();
    }

    public void addYData(Float yVal){
        yPos.add(yVal);
        invalidate();
    }

    public void startYData(Float yVal){
        yPos = new ArrayList<>();
        yPos.add(yVal);
        invalidate();
    }

    public void startFromEnd(boolean isEndStart){
        this.isEndStart = isEndStart;
        if(yPos!=null && !yPos.isEmpty()){
            float tempY = yPos.get(0);
            float possibleCount = (float) getWidth() /xDot;
            possibleCount = (possibleCount == (int)possibleCount)?possibleCount:possibleCount+1;
            Log.d("StartEnd","ps : "+possibleCount+" width "+chartWidth+" xD "+xDot);
            for(int i=0;i<100;i++){
                yPos.add(tempY);
            }
        }
    }

    public void setMinY(float minY) {
        this.minY = minY;
    }

    public void setMaxY(float maxY) {
        this.maxY = maxY;
    }

    public void setLineType(LineType type){
        chartLineType = type;
        invalidate();
    }

    public void setLineWidth(int lineWidth) {
        this.lineWidth = lineWidth;
    }

    public void setLineColor(int color) {
        this.colorLine = color;
    }

    public void setxPercentage(int xPercentage) {
        this.xPercentage = xPercentage;
    }

    public int getxDot() {
        return xDot;
    }

    public float getLastX() {
        return lastX;
    }

    public void setCenterClipWidth(float centerClipWidth) {
        this.centerClipWidth = centerClipWidth;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        calculateSizes();
    }

    void calculateSizes(){
        chartHeight = (int) (getHeight() - chartPaddingTop - chartPaddingBottom);
        chartWidth = getWidth();
        calculateDotValues();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        if(colorBackground!=-1){
            mPaint.setColor(colorBackground);
            canvas.drawPaint(mPaint);
        }

        if(isEndStart && yPos!=null && yPos.size()==1){
            calculateSizes();
        }

        if(centerClipWidth>0) {
            float width = getWidth();
            float possibleCount = width/xDot;
            float centerXCount = (yPos.size()-(possibleCount/2))*xDot;
            canvas.clipRect(centerXCount - centerClipWidth, 0, centerXCount + centerClipWidth, getHeight(), Region.Op.DIFFERENCE);
//            float centerX = (getWidth()/2);
//            canvas.drawRect(centerX - centerClipWidth-1, 0, centerX + centerClipWidth+1, 500, mDebugPaint);
//            printDebug(canvas,"cex "+centerX+" xco "+centerXCount+" s "+yPos.size());
        }

        if(yPos!=null && !yPos.isEmpty()) {
            DataSet dataSet = new DataSet(yPos);
            if (chartLineType == LineType.LINEAR)
                canvas.drawPath(getLinearPath(dataSet), mLinePaint);
            else if (chartLineType == LineType.CUBIC_BEZIER)
                canvas.drawPath(getBezierPath(dataSet), mLinePaint);
            else
                canvas.drawPath(getHorizontalBezierPath(dataSet), mLinePaint);
        }
    }


    void calculateDotValues(){
        float range = maxY - minY;
        yDot = chartHeight/range;
        if(xDot==-1)
            xDot = (getWidth() /100) * xPercentage;
        if(isEndStart && yPos.size()==1){
            float possibleCount = (float) getWidth() /xDot;
            float yVal = yPos.get(0);
            possibleCount = (possibleCount == (int)possibleCount)?possibleCount:possibleCount+1;
            for(int i=0;i<possibleCount;i++){
                yPos.add(yVal);
            }
        }
    }

    Path getLinearPath(DataSet set){
        tempPath.reset();
        if(set.getEntryCount()>0){
            tempPath.moveTo(0,(maxY - set.getYForIndex(0)) * yDot + chartPaddingTop);
            for(int i=1;i<set.getEntryCount();i++){
                tempPath.lineTo(i * xDot,(maxY - set.getYForIndex(i)) * yDot + chartPaddingTop);
                lastX = i*xDot;
            }
        }
        return tempPath;
    }
    Path getBezierPath(DataSet set){
        tempPath.reset();
        if (xPos.size() >= 1) {

            float prevDx = 0f;
            float prevDy = 0f;
            float curDx = 0f;
            float curDy = 0f;

            // Take an extra point from the left, and an extra from the right.
            // That's because we need 4 points for a cubic bezier (cubic=4), otherwise we get lines moving and doing weird stuff on the edges of the chart.
            // So in the starting `prev` and `cur`, go -2, -1
            // And in the `lastIndex`, add +1

            final int firstIndex = 0 + 1;
            final int lastIndex = 0 + xPos.size()-1;

            Entry prevPrev;
            Entry prev = set.getEntryForIndex(Math.max(firstIndex - 2, 0));
            Entry cur = set.getEntryForIndex(Math.max(firstIndex - 1, 0));
            Entry next = cur;
            int nextIndex = -1;

            if (cur == null) return tempPath;

            // let the spline start
            tempPath.moveTo(cur.getX(), cur.getY());

            for (int j = firstIndex; j <= lastIndex; j++) {

                prevPrev = prev;
                prev = cur;
                cur = nextIndex == j ? next : set.getEntryForIndex(j);

                nextIndex = j + 1 < set.getEntryCount() ? j + 1 : j;
                next = set.getEntryForIndex(nextIndex);

                prevDx = (cur.getX() - prevPrev.getX()) * 0.2f;
                prevDy = (cur.getY() - prevPrev.getY()) * 0.2f;
                curDx = (next.getX() - prev.getX()) * 0.2f;
                curDy = (next.getY() - prev.getY()) * 0.2f;

                tempPath.cubicTo(prev.getX() + prevDx, (prev.getY() + prevDy) ,
                        cur.getX() - curDx,
                        (cur.getY() - curDy) , cur.getX(), cur.getY() );

                lastX = cur.getX();
            }
        }
        return tempPath;
    }
    Path getHorizontalBezierPath(DataSet set){
        tempPath.reset();
        if (set.getEntryCount() >= 1) {

            Entry prev = set.getEntryForIndex(0);
            Entry cur = prev;

            // let the spline start
            tempPath.moveTo(cur.getX(), cur.getY());

            for (int j =  1; j < set.getEntryCount(); j++) {

                prev = cur;
                cur = set.getEntryForIndex(j);

                final float cpx = (prev.getX())
                        + (cur.getX() - prev.getX()) / 2.0f;

                tempPath.cubicTo(
                        cpx, prev.getY() ,
                        cpx, cur.getY(),
                        cur.getX(), cur.getY());

                lastX = cur.getX();
            }
        }
        return tempPath;
    }

    void printDebug(Canvas canvas, String text){
        canvas.drawText(text,10,530,mTextPaint);
    }

    class DataSet{
        List<Float> y;
        DataSet(List<Float> y){
            this.y=y;
        }

        Entry getEntryForIndex(int index){
            return new Entry(index*xDot,y.get(index));
        }

        float getYForIndex(int index){
            return y.get(index);
        }

        int getEntryCount(){
            return y.size();
        }

    }

    class Entry{
        float x,y;

        Entry(float x, float y){
            this.x = x;
            this.y = y;
        }
        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }
    }


    void loadDummyData(){
        yPos = new ArrayList<>();
        yPos.add(25f);
        yPos.add(30f);
        yPos.add(24f);
        yPos.add(20f);
        yPos.add(22f);
        yPos.add(30f);
        yPos.add(28f);
        yPos.add(10f);
        yPos.add(24f);
        yPos.add(20f);
        yPos.add(22f);
        yPos.add(30f);
        yPos.add(25f);
        yPos.add(28f);
        yPos.add(10f);
        yPos.add(24f);
        yPos.add(20f);
        yPos.add(22f);
        yPos.add(30f);
        yPos.add(25f);
        yPos.add(28f);
        yPos.add(10f);
        yPos.add(24f);
        yPos.add(20f);
        yPos.add(22f);
        yPos.add(30f);
        yPos.add(25f);
        yPos.add(28f);
        yPos.add(10f);
        yPos.add(30f);
        yPos.add(25f);
        yPos.add(28f);
        yPos.add(10f);
        yPos.add(24f);
        yPos.add(20f);
        yPos.add(22f);
        yPos.add(30f);
        yPos.add(25f);
        yPos.add(28f);
        yPos.add(10f);
        yPos.add(24f);
        yPos.add(20f);
        yPos.add(22f);
        yPos.add(30f);
        yPos.add(25f);
        yPos.add(28f);
        yPos.add(10f);
        yPos.add(24f);
        yPos.add(20f);
        yPos.add(22f);
        yPos.add(30f);
        yPos.add(25f);
        yPos.add(28f);
        yPos.add(10f);
        yPos.add(30f);
        yPos.add(24f);
        yPos.add(20f);
        yPos.add(22f);
        yPos.add(30f);
        yPos.add(25f);
        yPos.add(28f);
        yPos.add(10f);
        yPos.add(24f);
        yPos.add(20f);
        yPos.add(22f);
        yPos.add(30f);
        yPos.add(25f);
        yPos.add(28f);
        yPos.add(10f);
        yPos.add(24f);
        yPos.add(20f);
        yPos.add(22f);
        yPos.add(30f);
        yPos.add(25f);
        yPos.add(28f);
        yPos.add(10f);
        yPos.add(24f);
        yPos.add(20f);
        yPos.add(22f);
        yPos.add(30f);
        yPos.add(25f);
        yPos.add(28f);
        yPos.add(10f);
        yPos.add(30f);
        yPos.add(25f);
        yPos.add(28f);
        yPos.add(10f);
        yPos.add(24f);
        yPos.add(20f);
        yPos.add(22f);
        yPos.add(30f);
        yPos.add(25f);
        yPos.add(28f);
        yPos.add(10f);
        yPos.add(24f);
        yPos.add(20f);
        yPos.add(22f);
        yPos.add(30f);
        yPos.add(25f);
        yPos.add(28f);
        yPos.add(10f);
        yPos.add(24f);
        yPos.add(20f);
        yPos.add(22f);
        yPos.add(30f);
        yPos.add(25f);
        yPos.add(28f);
        yPos.add(10f);

//        minY = 0;
//        maxY = 40;

        setMaxValues();
    }
}

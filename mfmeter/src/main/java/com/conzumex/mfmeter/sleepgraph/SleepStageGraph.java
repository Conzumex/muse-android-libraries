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
import android.provider.CalendarContract;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.OverScroller;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
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
    /** This maxValue will  be draw the last bar till here
     * */
    float maxXvalue=8f;
    float minXvalue=0;
    float maxYvalue=3;
    float minYvalue=0;
    Paint mPaint,gridPaint,mBarPaint,mLinePaint,labelPaint,edgeGridPaint,markerPaint,noDataPaint;
    int barHeight = 75;
    int radiusBar = 7;
    float lineWidth = 4f;
    float textSize = 25;
    Typeface fontFace = Typeface.DEFAULT;
    String emptyText = "No Data";
    int emptyTextColor = R.color.white;
    int labelCount = 4;
    float granularity = 100f;
    float prevBarLineY = -1,prevBarLineX = -1;
    int prevBarColor = -1;
    RectF rectSquare;
    int colorBackground = Color.parseColor("#000000");
    int colorXAxis = Color.parseColor("#ffffff");
    int colorXLabel = Color.parseColor("#707070");
    int colorYLabel = Color.parseColor("#707070");
    int colorEdgeHighlight = Color.parseColor("#ffffff");
    int colorMarkerLine = Color.parseColor("#ffffff");
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
    boolean highlightClick = true;
    boolean drawMarkers = true;
    int markerLayout = R.layout.marker_default;
    MarkerFormatter markerFormatter;
    ChartListener mClickListener;
    SelectedChangeListener mChangeListener;
    SleepEntry selectedGraphEntry = null;
    float edgeLineWidth = 2f;
    float markerLineWidth = 2f;

    DashPathEffect gridXEffect;
    DashPathEffect gridYEffect;
    boolean highlightEdgeValues = true;
    boolean changeLabelColors = true;

    float touchX=-1, touchY=-1;

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
        markerPaint = new Paint();
        noDataPaint = new Paint();
        mBarPaint = new Paint();
        mLinePaint = new Paint();
        labelPaint = new Paint();

        labelXFormatter = value -> dc2Point.format(value)+"";
        labelYFormatter = value -> dc2Point.format(value)+"";

        gridXEffect = new DashPathEffect(new float[]{10,10}, 0);

        if (attrs == null) return;
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.SleepStageGraph);

        // Set background color via xml (If exists and isn't the default value)
        @ColorInt int newTextColor = attributes.getColor(R.styleable.SleepStageGraph_graphBackgroundColor, colorBackground);
        if (newTextColor != colorBackground) colorBackground = newTextColor;

        if(isInEditMode())
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

//        if(isInEditMode()) {
//            int tempHeight = 700;
//            chartHeight = tempHeight - 100;
//        }
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
        mPaint.setColor(colorBackground);
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
//        if(entries.size()>20)
        if(entries!=null && !entries.isEmpty())
            drawValues(canvas);
        else
            drawEmptyData(canvas);
        drawAxis(canvas);
        if(entries!=null && !entries.isEmpty()) {
//        if(entries.size()>20) {
            if (highlightEdges)
                drawGraphEdges(canvas);
            if (touchX != -1)
                drawMarker(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean touchedArea = false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                float eventX = event.getRawX();
                if(eventX>=chartGraphStartX && eventX<=chartGraphEndX) {
                    touchX = event.getRawX();
                    touchY = event.getRawY();
                    touchedArea = true;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                eventX = event.getRawX();
                if(eventX>=chartGraphStartX && eventX<=chartGraphEndX) {
                    touchX = event.getRawX();
                    touchY = event.getRawY();
                    touchedArea = true;
                }
                break;
            default:
                return false;
        }
        invalidate();
        return touchedArea;
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
        entries.add(new SleepEntry(8,2,9));
        touchX = 600;
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

    void drawEmptyData(Canvas canvas){
        noDataPaint.setColor(getResources().getColor(emptyTextColor));
        noDataPaint.setTextSize(textSize);
        noDataPaint.setTypeface(fontFace);
        canvas.drawText(emptyText,chartGraphWidth/2,chartGraphHeight/2,noDataPaint);
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
            float endX = entries.get(i).xValueClose!=-1?(entries.get(i).xValueClose * xDotValue)+chartGraphStartX:chartGraphEndX;
//            endX = (8*xDotValue)+chartGraphStartX;
            xValues.add(startX);
            if(i!=entries.size()-1) endX = (entries.get(i+1).xValue*xDotValue)+chartGraphStartX;
            if(i!=entries.size()-1) xValues.add(endX);
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

        //for x labels
        labelPaint.setColor(colorXLabel);
        gridPaint.setColor(gridYColor);
        float xLabelInterval = (chartGraphWidth)/labelCount;
        //for the edge lines
        float startGridXpos = chartGraphStartX - (lineWidth/2);
        float endGridXpos = chartGraphWidth + (lineWidth/2)+chartGraphStartX;
        if(gridXEffect!=null)
            gridPaint.setPathEffect(gridXEffect);
        canvas.drawLine(startGridXpos,chartHeight,startGridXpos,0,gridPaint);
        canvas.drawLine(endGridXpos,chartHeight,endGridXpos,0,gridPaint);
        labelPaint.setTextAlign(Paint.Align.LEFT);
        if(highlightEdgeValues) {
            labelPaint.setTypeface(Typeface.create(fontFace, Typeface.BOLD));
            labelPaint.setColor(colorEdgeHighlight);
        }
        float xValueStart = 0;
        canvas.drawText(labelXFormatter.getLabel(xValueStart),chartGraphStartX,chartGraphEndY + 50,labelPaint);
        labelPaint.setTextAlign(Paint.Align.RIGHT);
        if(highlightEdgeValues) {
            labelPaint.setTypeface(Typeface.create(fontFace, Typeface.BOLD));
            labelPaint.setColor(colorEdgeHighlight);
        }
        float xValueEnd = chartGraphWidth/xDotValue;
        canvas.drawText(labelXFormatter.getLabel(xValueEnd),chartGraphWidth+chartGraphStartX,chartGraphEndY + 50,labelPaint);

        //lessing by 1 for count the labels for inside the last and first labels
        float xLabelCountsInside = (chartGraphWidth/xDotValue)-1;
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setColor(colorXLabel);
        labelPaint.setTypeface(Typeface.create(fontFace, Typeface.NORMAL));

        float pointValLabel = xLabelCountsInside/labelCount;
        for(int i=0;i<labelCount;i++){
            float indexAvg = pointValLabel * (i+1);
            int roundPos = Math.round(indexAvg);
            if(roundPos == 1 || roundPos == xLabelCountsInside)   continue;
            float gridXpos = (roundPos*xDotValue)+chartGraphStartX;
            canvas.drawLine(gridXpos,chartHeight,gridXpos,0,gridPaint);
            canvas.drawText(labelXFormatter.getLabel(roundPos),gridXpos,chartGraphEndY + 50,labelPaint);
        }

        gridPaint.setPathEffect(null);
    }

    void drawMarker(Canvas canvas){
        markerPaint.setStrokeWidth(markerLineWidth);
        markerPaint.setColor(colorMarkerLine);
        float selectedX = (touchX - chartGraphStartX) / xDotValue;
        DecimalFormat df = new DecimalFormat("#.#");
        selectedX = Float.valueOf(df.format(selectedX));
        SleepEntry selectedEntry = getEntry(selectedX);
        if(mChangeListener!=null && selectedEntry!=selectedGraphEntry)
            mChangeListener.onChange(selectedGraphEntry,selectedEntry);
        selectedGraphEntry = selectedEntry;
        if(mClickListener!=null)
            mClickListener.onClick(selectedX,selectedEntry);
        if(highlightClick)
            canvas.drawLine(touchX,chartHeight,touchX,0,markerPaint); //Grid lines

        //drawing marker views
        if(drawMarkers) {
            SleepMarker markerTest = new SleepMarker(getContext(),markerLayout);
            if(markerFormatter==null)
                markerTest.setContent(selectedX+"");
            else
                markerTest.setContent(markerFormatter.onContent(selectedX,selectedEntry));

            float yPos = getYPosOfEntry(selectedEntry);
            int markerHeight = markerTest.getHeight();
            float chartTopSpace = yPos - ((float) barHeight /2);
//            printDebug(yPos+"y, barH"+barHeight+", marH "+markerHeight+" yDOt "+yDotValue+" yN"+chartTopSpace,canvas);
            yPos = chartTopSpace < markerHeight ? (barHeight * 3) - (barHeight - yPos) : yPos;
            markerTest.draw(canvas, touchX, yPos - barHeight, chartGraphStartX, chartGraphEndX);
        }
    }

    void calculateDotValues(){
        xDotValue = chartGraphWidth / (maxXvalue-minXvalue + 1);
        yDotValue = chartGraphHeight / (maxYvalue - minYvalue + 1);
    }

    void printDebug(String text,Canvas canvas){
        Paint paint = new Paint();
        paint.setColor(Color.YELLOW);
        paint.setTextSize(25);
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

    /** set the dash effect for the grid lines Vertical*/
    public void setGridXDash(float width, float gap){
        this.gridXEffect = new DashPathEffect(new float[]{width,gap}, 0);
    }
    /** set the dash effect for the grid lines Horizontal*/
    public void setGridYDash(float width, float gap){
        this.gridYEffect = new DashPathEffect(new float[]{width,gap}, 0);
    }

    /** format the labels for xAxis*/
    public void setLabelXFormatter(labelFormatX labelXFormatter) {
        this.labelXFormatter = labelXFormatter;
    }

    /** format the labels for yAxis*/
    public void setLabelYFormatter(labelFormatY labelYFormatter) {
        this.labelYFormatter = labelYFormatter;
    }

    /** set maximum X Value*/
    public void setMaxXvalue(float maxXvalue) {
        this.maxXvalue = maxXvalue;
    }

    /** set minimum X Value*/
    public void setMinXvalue(float minXvalue) {
        this.minXvalue = minXvalue;
    }

    /** set minimum Y Value*/
    public void setMinYvalue(float minYvalue) {
        this.minYvalue = minYvalue;
    }

    /** set max Y Value*/
    public void setMaxYvalue(float maxYvalue) {
        this.maxYvalue = maxYvalue;
    }

    public SleepEntry getEntry(float xValue){
        if(entries==null||entries.isEmpty())
            return null;
        SleepEntry selectedEntry = entries.get(entries.size()-1);
        float minX = selectedEntry.xValue,maxX = -1;
        for(int i=entries.size()-1;i>=0;i--){
            minX = Math.min(entries.get(i).xValue,minX);
            if(entries.get(i).xValueClose==-1) {
                if (entries.get(i).xValue <= xValue) {
                    selectedEntry = entries.get(i);
                    break;
                }
            }else{
                maxX = Math.max(maxX,entries.get(i).xValueClose);
                if (entries.get(i).xValue <= xValue && entries.get(i).xValueClose >= xValue) {
                    selectedEntry = entries.get(i);
                    break;
                }
            }
        }
        if (selectedEntry==null)    selectedEntry = entries.get(0);
        if(maxX!=-1 && (xValue < minX || xValue >maxX))
            return null;
        return selectedEntry;
    }


    private float getYPosOfEntry(SleepEntry entry){
        if(entry==null||entries==null||entries.isEmpty())
            return -1;
        Collections.sort(yValues);
        return yValues.get((int) ((maxYvalue - minYvalue)-((int) entry.yValue)));
    }
    private float getXPosOfEntry(SleepEntry entry){
        if(entry==null||entries==null||entries.isEmpty())
            return -1;
        int pos = entries.indexOf(entry);
        if(pos==-1) return -1;
        return xValues.get(pos);
    }

    /** Format the marker text as String */
    public void setMarkerFormatter(MarkerFormatter mFormatter){
        markerFormatter = mFormatter;
    }
    public interface MarkerFormatter{
        @SuppressWarnings("ConstantConditions")
        CharSequence onContent(float x,@Nullable SleepEntry entry);
    }
    /** Listener for the chart item clicks*/
    public void setChartClickListener(ChartListener mClickListener){
        this.mClickListener = mClickListener;
    }
    public interface ChartListener{
        @SuppressWarnings("ConstantConditions")
        void onClick(float x,@Nullable SleepEntry entry);
    }
    /** Listener for the selected item changes by checking the x value */
    public void setSelectedChangeListener(SelectedChangeListener mListener){
        mChangeListener = mListener;
    }
    public interface SelectedChangeListener{
        @SuppressWarnings("ConstantConditions")
        void onChange(@Nullable SleepEntry oldEntry, @Nullable SleepEntry newEntry);
    }
    /** make the graph clicked markerview enabled or disabled */
    public void setDrawMarkers(boolean mValue){
        drawMarkers = mValue;
    }

    /** set layout for marker layout. Ensure that the layout contains a textview with id of 'textview'*/
    public void setMarkerLayout(int markerLayout){
        this.markerLayout = markerLayout;
    }
    /** make the graph clicked line indicator enabled or disabled */
    public void setHighlightClick(boolean mValue){
        highlightClick = mValue;
    }
    /** set the edge highlighting lines colors */
    public void setColorMarkerLine(@ColorInt int value){
        this.colorMarkerLine = value;
    }
    /** set the edge highlighting lines colors */
    public void setColorEdgeHighlight(@ColorInt int colorEdgeHighlight){
        this.colorEdgeHighlight = colorEdgeHighlight;
    }
    /** make the graph edges highlighted or disabled */
    public void setHighlightEdges(boolean mValue){
        highlightEdges = mValue;
    }

    /** set text to be displayed when data is empty*/
    public void setEmptyText(String text){
        emptyText = text;
    }
    /** set text color to the no data text*/
    public void setEmptyTextColor(@ColorInt int color){
        emptyTextColor = color;
    }
    /** set font family for the texts*/
    public void setFontFace(Typeface fontFace){
        this.fontFace = fontFace;
    }
    @Override
    public void invalidate() {
        super.invalidate();
    }
}

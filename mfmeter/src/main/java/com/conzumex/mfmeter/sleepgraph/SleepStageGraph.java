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
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class SleepStageGraph extends View {
    int parentViewWidth,parentViewHeight;
    int chartWidth,chartHeight;
    float chartValueMinx, chartValueMaxX;
    int axisSpace = 170;
    int chartOffsetV = 0,chartOffsetH = 20;
    int chartOffsetTop = 0;
    int viewBorderOffsetTop = 0;
    int viewBorderOffsetLeft = 10;
    int axisLabelPadding = 15;
    int markerTopPadding = 10;
    float chartGraphWidth,chartGraphHeight;
    float chartGraphStartX,chartGraphStartY;
    float chartGraphEndX,chartGraphEndY;
    float xDotValue,yDotValue;
    int axisWidth = 1;
    /** This maxValue will  be draw the last bar till here
     * */
    float maxXvalue=18f;
    float minXvalue=0;
    float maxYvalue=3;
    float yGranularity=1;
    float minYvalue=0;
    Paint mPaint,gridPaint,mBarPaint,mLinePaint,labelPaint,edgeGridPaint,markerPaint,noDataPaint;
    int barHeight = 75;
    int radiusBar = 7;
    float lineWidth = 4f;
    float textSize = 25;
    Typeface fontFace = Typeface.DEFAULT;
    String emptyText = "No Data";
    int emptyTextColor = Color.parseColor("#ffffff");
    int labelCount = 4;
    float granularity = 100f;
    float prevBarLineY = -1,prevBarLineX = -1;
    int prevBarColor = -1;
    RectF rectSquare;
    int colorBackground = Color.parseColor("#00000000");
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
//    List<Float> xValues,yValues;
    Map<Float,Float> xValues,yValues;

    labelFormatX labelXFormatter;
    labelFormatY labelYFormatter;
    DecimalFormat dc2Point = new DecimalFormat("0.##",new DecimalFormatSymbols(Locale.US));
    float chartPaddingH = 50,chartPaddingV = 50;
    boolean drawXAxis = true,drawYAxis = true;
    float insideAxisWidth = 0;
    boolean highlightEdges = false;
    boolean highlightClick = true;
    boolean drawMarkers = true;
    boolean drawTopBorder = true;
    boolean drawViewTopBorder = true;
    boolean drawViewEndBorder = true;
    boolean setMarkerTop = false;
    boolean showMarkerAlways = true;
    //(beta)to get the edge label size when divide space for labels
    boolean saveEdgeLabelWidth = false;
    int markerLayout = R.layout.marker_default;
    MarkerFormatter markerFormatter;
    ChartListener mClickListener;
    SelectedChangeListener mChangeListener;
    SleepEntry selectedGraphEntry = null;
    float edgeLineWidth = 2f;
    float markerLineWidth = 2f;
    float edgeLabelOffset = 2f;
    SleepMarker markerTest;

    DashPathEffect gridXEffect;
    DashPathEffect gridYEffect;
    boolean highlightEdgeValues = false;
    boolean changeLabelColors = true;
    boolean enableGridX = true;
    boolean enableGridY = false;
    boolean drawYEdges = false;
    boolean setMarkerValueOnly = true;
    boolean showLastMarker = true;
    boolean setEdgeLabelAligned = false;

    float touchX=-1, touchY=-1;
    float lastMarkerX=-1;

    enum XPos {YAXIS, YAXIS_LABEL, XAXIS_START,XAXIS_END,GRID_X_START,GRID_X_END,
        EDGE_LABEL_START,EDGE_LABEL_END,GRID_X_POS,VALUE_X_POS,X_GRID_X_START,X_GRID_X_END,
        TOP_BORDER_START,TOP_BORDER_END, GRAPH_EDGE_START, GRAPH_EDGE_END,END_BORDER_X, TOUCH_END_X}
    public enum Direction {LEFT, RIGHT}

    Direction yAXisDirection = Direction.LEFT;

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
        labelYFormatter = value -> value==2?"Awake Sleep":dc2Point.format(value)+" Sleep";

//        gridXEffect = new DashPathEffect(new float[]{10,10}, 0);

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
        chartWidth = parentViewWidth-axisSpace;
        chartHeight = parentViewHeight-axisSpace;

//        if(isInEditMode()) {
//            int tempHeight = 700;
//            chartHeight = tempHeight - 100;
//        }
        chartGraphHeight = chartHeight - chartOffsetTop;
        chartGraphWidth = chartWidth - chartOffsetH - chartPaddingH;
        chartGraphStartX = (yAXisDirection==Direction.LEFT?axisSpace:0) + (chartOffsetH/2) + (chartPaddingH/2);
        chartGraphStartY = 0 + chartOffsetTop;
        chartGraphEndX = chartWidth-(chartOffsetH/2) - (chartPaddingH/2);
        chartGraphEndY = chartHeight+ chartOffsetTop;

        chartValueMinx = chartGraphStartX;
        chartValueMaxX = chartGraphEndX;

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
        if(entries==null || entries.isEmpty()){
            drawEmptyData(canvas);
            return;
        }
        drawLabels(canvas);
//      if(entries.size()>20)

        drawValues(canvas);
        if(enableGridX){
            drawGridX(canvas);
        }
        drawAxis(canvas);
//       if(entries.size()>20) {
        if (highlightEdges)
            drawGraphEdges(canvas);
        if (touchX != -1)
            drawMarker(canvas);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean touchedArea = false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                float eventX = event.getX();
                float eventY = event.getY();
                if(eventX>=getXPos(XPos.GRAPH_EDGE_START) && eventX<=getXPos(XPos.TOUCH_END_X)&& eventY >= chartGraphStartY && eventY <= chartGraphEndY) {
                    touchX = event.getX();
                    touchY = event.getY();
                    Log.d("Toching"," touchX: "+touchX +" chartGraphEndX: "+ chartGraphEndX +" chartGraphStartX: "+chartGraphStartX+" touchY: "+ eventY +"chartGraphStartY :"+ chartGraphStartY +"chartGraphEndY: "+chartGraphEndY);
//                    if(touchX<chartGraphEndX && touchX>chartGraphStartX && touchY > chartGraphStartY && touchY<chartGraphEndY)
//                    if(touchX<getXPos(XPos.GRAPH_EDGE_END) && touchX>getXPos(XPos.GRAPH_EDGE_START))
                        touchedArea = true;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                eventX = event.getX();
                eventY = event.getY();
                if(eventX>=getXPos(XPos.GRAPH_EDGE_START) && eventX<=getXPos(XPos.TOUCH_END_X) && eventY >= chartGraphStartY && eventY <= chartGraphEndY) {
                    touchX = event.getX();
                    touchY = event.getY();
//                    if(touchX<chartGraphEndX && touchX>chartGraphStartX && touchY > chartGraphStartY && touchY<chartGraphEndY)
//                    if(touchX<getXPos(XPos.GRAPH_EDGE_END) && touchX>getXPos(XPos.GRAPH_EDGE_START))
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
//        entries.add(new SleepEntry(3,1));
        entries.add(new SleepEntry(4,3));
        entries.add(new SleepEntry(6,2,8));
        entries.add(new SleepEntry(12,0));
        entries.add(new SleepEntry(14,2,16));
        touchX = 300;
    }

    void drawGraphEdges(Canvas canvas){
        float xLabelInterval = (chartGraphWidth)/labelCount;
        edgeGridPaint.setColor(colorEdgeHighlight);
        edgeGridPaint.setStrokeWidth(edgeLineWidth);
        edgeGridPaint.setPathEffect(new DashPathEffect(new float[]{10,10}, 0));
        // (lineWidth/2) for the grgh bsar connecting line width
        float xPos = getXPos(XPos.GRAPH_EDGE_START);
        canvas.drawLine(xPos,chartHeight,xPos,chartOffsetTop,edgeGridPaint); //Grid lines
        float endXPos = getXPos(XPos.GRAPH_EDGE_END,labelCount*xLabelInterval);
        canvas.drawLine(endXPos,chartHeight,endXPos,chartOffsetTop,edgeGridPaint); //Grid lines
    }

    void drawAxis(Canvas canvas){
        mPaint.setStrokeWidth(axisWidth);
        mPaint.setColor(colorYAxis);
        if(drawYAxis)
            canvas.drawLine(getXPos(XPos.YAXIS),chartOffsetTop,getXPos(XPos.YAXIS),chartHeight,mPaint);    //yAxis

        mPaint.setColor(colorXAxis);
        if(drawXAxis)
            canvas.drawLine(getXPos(XPos.XAXIS_START),chartHeight,getXPos(XPos.XAXIS_END),chartHeight,mPaint);   //xAxis
    }

    void drawEmptyData(Canvas canvas){
        noDataPaint.setColor(emptyTextColor);
        noDataPaint.setTextSize(textSize);
        noDataPaint.setTypeface(fontFace);
        canvas.drawText(emptyText,chartGraphWidth/2,chartGraphHeight/2,noDataPaint);
    }

    void drawGridX(Canvas canvas){
        Set<Float> yVals = SleepEntry.yValsUnique(minYvalue,maxYvalue,yGranularity);
        for(Float yVal : yVals){
            float yPos = yVal * yDotValue;
            yPos = yPos +chartGraphStartY;
            canvas.drawLine(getXPos(XPos.X_GRID_X_START), yPos, getXPos(XPos.X_GRID_X_END), yPos, gridPaint); //Grid lines
        }
    }

    void drawValues(Canvas canvas){
        Collections.sort(entries);
        mLinePaint.setStrokeWidth(lineWidth);
        gridPaint.setStrokeWidth(1);
        gridPaint.setColor(gridXColor);
        int barColor;
        String tempDeb = "";
        xValues = new HashMap<>();
        yValues = new HashMap<>();
        prevBarLineY = -1;
        prevBarColor = -1;
        for(int i=0;i<entries.size();i++){
            float startX = getXPos(XPos.VALUE_X_POS,entries.get(i).xValue * xDotValue);
            float endX = entries.get(i).xValueClose!=-1?(entries.get(i).xValueClose * xDotValue)+chartGraphStartX:chartGraphEndX;
//            endX = (8*xDotValue)+chartGraphStartX;
            xValues.put(entries.get(i).xValue,startX);
            if(entries.get(i).xValueClose==-1 && i!=entries.size()-1) endX = getXPos(XPos.VALUE_X_POS,entries.get(i+1).xValue*xDotValue);
            if(i!=entries.size()-1) xValues.put(entries.get(i).xValue,endX);
            mPaint.setColor(colorTemp);
            float yPos = (entries.get(i).yValue + 1) * yDotValue;
            yPos = yPos +chartGraphStartY;
//            canvas.drawLine((chartGraphStartX),yPos,(chartGraphEndX),yPos,gridPaint); //Grid lines
//            if(enableGridX)
//                canvas.drawLine(getXPos(XPos.X_GRID_X_START),yPos,getXPos(XPos.X_GRID_X_END),yPos,gridPaint); //Grid lines
            yPos = yPos - (yDotValue/2);
//            canvas.drawLine(startX,yPos,endX,yPos,mPaint);

            yPos = chartHeight - yPos +chartOffsetTop;
            if(!yValues.containsValue(yPos))
                yValues.put(entries.get(i).yValue,yPos);

            float rectTop = yPos - (barHeight/2);
            //todo make line included
            startX = startX - (lineWidth/2);
            endX = endX + (lineWidth/2);
            if(i==0)
                chartValueMinx = startX;
            if(i==entries.size()-1)
                chartValueMaxX = endX;

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
                mLinePaint.setShader(new LinearGradient(prevBarLineX,(chartGraphEndY-(yDotValue/2) -chartOffsetTop),startX,(chartGraphStartY+(yDotValue/2) ), colorRanges,null,  Shader.TileMode.CLAMP));
                canvas.drawLine(lineX,prevBarLineY,lineX,yPos,mLinePaint);
            }

            if(entries.get(i).xValueClose==-1)
                prevBarLineY = yPos;
            else
                prevBarLineY = -1;
            prevBarLineX = endX;
            prevBarColor = barColor;
        }

        if(drawTopBorder)
            canvas.drawLine(getXPos(XPos.TOP_BORDER_START),chartOffsetTop,getXPos(XPos.TOP_BORDER_END),chartOffsetTop,gridPaint); //Grid lines

        if(drawViewTopBorder)
            canvas.drawLine(getXPos(XPos.TOP_BORDER_START),viewBorderOffsetTop,getXPos(XPos.TOP_BORDER_END),viewBorderOffsetTop,gridPaint); //Grid lines

        if(drawViewEndBorder)
            canvas.drawLine(getXPos(XPos.END_BORDER_X),chartOffsetTop,getXPos(XPos.END_BORDER_X),chartGraphHeight+chartOffsetTop,gridPaint); //Grid lines

    }

    void drawLabels(Canvas canvas){
        labelPaint.setColor(colorTemp);
        if(yAXisDirection == Direction.RIGHT)
            labelPaint.setTextAlign(Paint.Align.LEFT);
        else
            labelPaint.setTextAlign(Paint.Align.RIGHT);
        labelPaint.setTextSize(textSize);
        Set<Float> yVals = SleepEntry.yValsUnique(minYvalue,maxYvalue,yGranularity);
        labelPaint.setColor(colorYLabel);
        for(Float yVal : yVals){
            float yPos = (yVal + 1) * yDotValue;
            yPos = yPos +chartGraphStartY - chartOffsetTop;
            float labelVal = (yPos/yDotValue)-1;
            yPos = yPos - (yDotValue/2);
            yPos = chartHeight - yPos;
            if(changeLabelColors)
                labelPaint.setColor(getPosColor((int)labelVal));
            canvas.drawText(labelYFormatter.getLabel(labelVal),getXPos(XPos.YAXIS_LABEL),yPos+(textSize/2),labelPaint);
        }

        //for x labels
        labelPaint.setColor(colorXLabel);
        gridPaint.setColor(gridYColor);
        float xLabelInterval = (chartGraphWidth)/labelCount;
        //for the edge lines
        float startGridXpos = getXPos(XPos.GRID_X_START);
        float endGridXpos = getXPos(XPos.GRID_X_END);
        if(gridXEffect!=null)
            gridPaint.setPathEffect(gridXEffect);
        if(drawYEdges) {
            canvas.drawLine(startGridXpos, chartHeight, startGridXpos, chartOffsetTop, gridPaint);
            canvas.drawLine(endGridXpos, chartHeight, endGridXpos, chartOffsetTop, gridPaint);
        }
        labelPaint.setTextAlign(setEdgeLabelAligned?Paint.Align.LEFT:Paint.Align.CENTER);
        if(highlightEdgeValues) {
            labelPaint.setTypeface(Typeface.create(fontFace, Typeface.BOLD));
            labelPaint.setColor(colorEdgeHighlight);
        }
        float xValueStart = 0;
        float xValueEnd = chartGraphWidth/xDotValue;
        Rect endTextBounds = new Rect();
        labelPaint.getTextBounds(labelXFormatter.getLabel(xValueEnd), 0, labelXFormatter.getLabel(xValueEnd).length(), endTextBounds);
        Rect startTextBounds = new Rect();
        labelPaint.getTextBounds(labelXFormatter.getLabel(xValueStart), 0, labelXFormatter.getLabel(xValueStart).length(), startTextBounds);

        float xPosStart = getXPos(XPos.EDGE_LABEL_START);
        float labelCuttWidth = xPosStart-(startTextBounds.width()/2);
        int offsetLabelCuttoff = setEdgeLabelAligned?0:labelCuttWidth<0?(int)-labelCuttWidth:0;
        canvas.drawText(labelXFormatter.getLabel(xValueStart),xPosStart+offsetLabelCuttoff,chartGraphEndY-chartOffsetTop + 50,labelPaint);

        labelPaint.setTextAlign(setEdgeLabelAligned?Paint.Align.RIGHT:Paint.Align.CENTER);
        if(highlightEdgeValues) {
            labelPaint.setTypeface(Typeface.create(fontFace, Typeface.BOLD));
            labelPaint.setColor(colorEdgeHighlight);
        }

        float xPosEnd = getXPos(XPos.EDGE_LABEL_END);
        float labelCuttWidthEnd = xPosEnd+(endTextBounds.width()/2);
        int offsetLabelCuttoffEnd = setEdgeLabelAligned?0:labelCuttWidthEnd>parentViewWidth?(int)(labelCuttWidthEnd-parentViewWidth):0;
        canvas.drawText(labelXFormatter.getLabel(xValueEnd),xPosEnd-offsetLabelCuttoffEnd,chartGraphEndY-chartOffsetTop + 50,labelPaint);

        float labelAvailableSpace = chartGraphWidth;
        if(saveEdgeLabelWidth)
            labelAvailableSpace = chartGraphWidth - (endTextBounds.width()*edgeLabelOffset) - (startTextBounds.width()*edgeLabelOffset);

        //lessing by 1 for count the labels for inside the last and first labels
        float xLabelCountsInside = (labelAvailableSpace/xDotValue)-1;
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setColor(colorXLabel);
        labelPaint.setTypeface(Typeface.create(fontFace, Typeface.NORMAL));

        float pointValLabel = xLabelCountsInside/labelCount;
        for(int i=0;i<labelCount-1;i++){
            float indexAvg = pointValLabel * (i+1);
            int roundPos = Math.round(indexAvg);
            float indexBal = indexAvg - roundPos;
            if(roundPos == 1 || roundPos == xLabelCountsInside)   continue;
            float gridXpos = getXPos(XPos.GRID_X_POS,roundPos);
            gridXpos = gridXpos - getXAdjust(indexBal);
            if(enableGridY)
                canvas.drawLine(gridXpos,chartHeight,gridXpos,0+chartOffsetTop,gridPaint);
            canvas.drawText(labelXFormatter.getLabel(roundPos),gridXpos,chartGraphEndY -chartOffsetTop+ 50,labelPaint);
        }

        gridPaint.setPathEffect(null);
    }

    void drawMarker(Canvas canvas){
        if(setMarkerValueOnly && (touchX<chartValueMinx || touchX>chartValueMaxX)) {
            if(!showLastMarker)
                return;
            else{
                touchX = lastMarkerX;
            }
        }
        markerPaint.setStrokeWidth(markerLineWidth);
        markerPaint.setColor(colorMarkerLine);

        //valueTouchX for finding the entries with including linewidth
        float valueTouchX = touchX<(chartValueMinx+lineWidth/2)?touchX+(lineWidth/2):(touchX>(chartValueMaxX-lineWidth/2)?touchX-(lineWidth/2):touchX);
        float selectedX = (valueTouchX - chartGraphStartX) / xDotValue;
        DecimalFormat df = new DecimalFormat("#.#",new DecimalFormatSymbols(Locale.US));
        selectedX = Float.valueOf(df.format(selectedX));
        SleepEntry selectedEntry = getEntry(selectedX);
        if(mChangeListener!=null && selectedEntry!=selectedGraphEntry)
            mChangeListener.onChange(selectedGraphEntry,selectedEntry);
        selectedGraphEntry = selectedEntry;
        if(mClickListener!=null)
            mClickListener.onClick(selectedX,selectedEntry);
        if(highlightClick)
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
            markerTest.refreshContent(selectedEntry,selectedX);

            float yPos = 100;
            try {
                yPos = getYPosOfEntry(selectedEntry);
            }catch (Exception e){
                Log.e("SLeepYPos",e.toString());
            }
            int markerHeight = markerTest.getHeight();
            float chartTopSpace = yPos - ((float) barHeight /2);
//            printDebug(yPos+"y, barH"+barHeight+", marH "+markerHeight+" yDOt "+yDotValue+" yN"+chartTopSpace,canvas);
            yPos = chartTopSpace < markerHeight ? (barHeight * 3) - (barHeight - yPos) : yPos;
            float markerY = yPos - barHeight;
            if(setMarkerTop){
                markerY = markerTopPadding + (markerHeight/2);
            }
            if(showMarkerAlways || selectedEntry!=null)
                markerTest.draw(canvas, touchX, markerY, chartGraphStartX, getXPos(XPos.TOUCH_END_X));
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
        canvas.drawText(text,10,chartGraphHeight + 150,paint);
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
    /** set label Granularity for Y Value*/
    public void setyGranularity(float granularity) {
        this.yGranularity = granularity;
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
                }else if (entries.get(i).xValue <= xValue && entries.get(i).xValueClose < xValue) {
                    return null;
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
        if(yValues.size()==1)
            return yValues.get(0);
//        return yValues.get(entries.indexOf(entry));
        int graphMax = (int) (maxYvalue - minYvalue);
        return yValues.get(entry.yValue);
    }
    private float getXPosOfEntry(SleepEntry entry){
        if(entry==null||entries==null||entries.isEmpty())
            return -1;
        int pos = entries.indexOf(entry);
        if(pos==-1) return -1;
        return xValues.get(pos);
    }
    private float getXPos(XPos type){
        if(yAXisDirection == Direction.RIGHT) {
            if (type == XPos.YAXIS) {
                return chartWidth;
            } else if (type==XPos.YAXIS_LABEL) {
                return chartGraphEndX + (chartPaddingH/2) + axisLabelPadding;
            } else if (type==XPos.XAXIS_START) {
                return 0;
            } else if (type==XPos.XAXIS_END) {
                return chartWidth+insideAxisWidth;
            } else if (type==XPos.GRID_X_START) {
                return chartGraphStartX - (lineWidth/2);
            } else if (type==XPos.GRID_X_END) {
                return chartGraphWidth + (lineWidth/2)+chartGraphStartX;
            } else if (type==XPos.EDGE_LABEL_START) {
                return chartGraphStartX;
            } else if (type==XPos.EDGE_LABEL_END) {
                return chartGraphWidth+chartGraphStartX;
            } else if (type==XPos.X_GRID_X_START) {
                return (chartGraphStartX-(chartPaddingH/2)-(chartOffsetH/2));
            } else if (type==XPos.X_GRID_X_END) {
                return (chartGraphEndX+chartPaddingH/2+(chartOffsetH/2)+insideAxisWidth );
            } else if (type==XPos.TOP_BORDER_START) {
                return (chartGraphStartX-(chartPaddingH/2)-(chartOffsetH/2));
            } else if (type==XPos.TOP_BORDER_END) {
                return (chartGraphEndX+chartPaddingH/2+(chartOffsetH/2)+insideAxisWidth );
            } else if (type==XPos.GRAPH_EDGE_START) {
                return chartGraphStartX - (lineWidth/2) - (edgeLineWidth/2);
            } else if (type==XPos.TOUCH_END_X) {
                return chartGraphEndX;
            } else if (type==XPos.END_BORDER_X) {
                return 0;
            }
        }else{
            if (type == XPos.YAXIS) {
                return axisSpace;
            } else if (type==XPos.YAXIS_LABEL) {
                return 0 + axisSpace - axisLabelPadding;
            } else if (type==XPos.XAXIS_START) {
                return axisSpace - insideAxisWidth;
            } else if (type==XPos.XAXIS_END) {
                return chartWidth+axisSpace - (chartOffsetH/2);
            } else if (type==XPos.GRID_X_START) {
                return chartGraphStartX - (lineWidth/2);
            } else if (type==XPos.GRID_X_END) {
                return chartGraphWidth + (lineWidth/2)+chartGraphStartX;
            } else if (type==XPos.EDGE_LABEL_START) {
                return chartGraphStartX;
            } else if (type==XPos.EDGE_LABEL_END) {
                return chartGraphWidth+chartGraphStartX;
            } else if (type==XPos.X_GRID_X_START) {
                return (chartGraphStartX-(chartPaddingH/2)-(chartOffsetH/2)) - insideAxisWidth;
            } else if (type==XPos.X_GRID_X_END) {
                return (chartGraphEndX+chartPaddingH/2+axisSpace);
            } else if (type==XPos.TOP_BORDER_START) {
                return (chartGraphStartX-(chartPaddingH/2)-(chartOffsetH/2));
            } else if (type==XPos.TOP_BORDER_END) {
                return (chartGraphEndX+chartPaddingH/2+axisSpace);
            } else if (type==XPos.GRAPH_EDGE_START) {
                return chartGraphStartX - (lineWidth/2) - (edgeLineWidth/2);
            } else if (type==XPos.END_BORDER_X) {
//                return chartGraphEndX+chartPaddingH/2+axisSpace+(chartOffsetH/2)-1;
                return (chartGraphEndX+(chartPaddingH/2)+axisSpace);
            } else if (type==XPos.TOUCH_END_X) {
                return chartGraphEndX+axisSpace;
            }
        }
        return 0;
    }
    private float getXPos(XPos type,float roundPos){
        if(yAXisDirection == Direction.RIGHT) {
            if (type==XPos.GRID_X_POS) {
                return (roundPos*xDotValue)+chartGraphStartX;
            } else if (type==XPos.VALUE_X_POS) {
                return roundPos + chartGraphStartX;
            } else if (type==XPos.GRAPH_EDGE_END) {
                return roundPos + chartGraphStartX + (lineWidth/2) + (edgeLineWidth/2);
            }
        }else{
            if (type==XPos.GRID_X_POS) {
                return (roundPos*xDotValue)+chartGraphStartX;
            } else if (type==XPos.VALUE_X_POS) {
                return roundPos + chartGraphStartX;
            } else if (type==XPos.GRAPH_EDGE_END) {
                return roundPos + chartGraphStartX + (lineWidth/2) + (edgeLineWidth/2);
            }
        }
        return 0;
    }

    private float getXAdjust(float value){
        return value*xDotValue;
    }

    /** Format the marker text as String */
    public void setMarkerFormatter(MarkerFormatter mFormatter){
        markerFormatter = mFormatter;
    }
    public interface MarkerFormatter{
        @SuppressWarnings("ConstantConditions")
        CharSequence onContent(float x,@Nullable SleepEntry entry);
    }
    /** set markerView*/
    public void setMarkerView(SleepMarker mMarker){
        this.markerTest = mMarker;
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
    /** set font size for the texts*/
    public void setTextSize(float textSize){
        this.textSize = textSize;
    }
    /** set chart top offset for bigger markers*/
    public void setOffsetTop(int offsetTop){
        this.chartOffsetTop = offsetTop;
    }
    /** set chart YAxis inside offset for grid draw axis inside*/
    public void setYAxisInsideOffset(int offset){
        this.insideAxisWidth = offset;
    }
    /** set direction of YAxis*/
    public void setYAxisDirection(Direction direction){
        this.yAXisDirection = direction;
    }
    /** set axis start margin*/
    public void setAxisLabelPadding(int padding){
        this.axisLabelPadding = padding;
    }
    /** set marker on top*/
    public void setMarkerOnTop(boolean isSet){
        this.setMarkerTop = isSet;
    }
    /** set marker always draw if there is no entries for the clicked pos*/
    public void setShowMarkerAlways(boolean isSet){
        this.showMarkerAlways = isSet;
    }
    /** set marker margin on top, if set the marker always on top*/
    public void setMarkerTopPadding(int topMarkerPadding){
        this.markerTopPadding = topMarkerPadding;
    }
    /** draw view border on top */
    public void setDrawViewTopBorder(boolean draw){
        this.drawViewTopBorder = draw;
    }
    /** draw view border on graph end */
    public void setDrawViewEndBorder(boolean draw){
        this.drawViewEndBorder = draw;
    }
    /** enable grid lines*/
    public void enbleGridLines(boolean draw){
        this.enableGridX = draw;
        this.enableGridY = draw;
    }
    /** enable gridX line*/
    public void enbleXGridLine(boolean draw){
        this.enableGridX = draw;
    }
    /** enable gridY line*/
    public void enbleYGridLine(boolean draw){
        this.enableGridY = draw;
    }
    /** enable endge value highlighting*/
    public void enbleEdgeValuesHighlighted(boolean draw){
        this.highlightEdgeValues = draw;
    }
    /** set color ranges*/
    public void setColorRanges(int[] colors){
        this.colorRanges = colors;
    }
    /** set Xaxis label space.
     *
     * default is 100*/
    public void setAxisSpace(int width){
        this.axisSpace = width;
    }
    /** set Xaxis label count.
     *
     * default is 4*/
    public void setLabelCount(int count){
        this.labelCount = count;
    }
    /** set Highlighter allow outside chart x values range
     *
     * default is : it don't show marker outside the range*/
    public void setShowMarkerOutsideX(boolean show){
        this.setMarkerValueOnly = !show;
    }
    /** set show last marker when touch outside x range
     *
     * default is : it don't show marker outside the range*/
    public void setShowLastMarker(boolean show){
        this.showLastMarker = show;
    }
    /** set edge label offset to prevent label overlapping
     *
     * default is : 2f*/
    public void setEdgeLabelOffset(float offset){
        this.edgeLabelOffset = offset;
    }

    /** set edge label alignment to left and right
     *
     * default is : false*/
    public void setEdgeLabelAligned(boolean isAligned){
        this.setEdgeLabelAligned = isAligned;
    }

    /** set chart horizontal offset
     *
     * default is : 20*/
    public void setChartOffsetH(int chartOffsetH) {
        this.chartOffsetH = chartOffsetH;
    }

    /** set chart horizontal padding
     *
     * default is : 50*/
    public void setChartPaddingH(int padding) {
        this.chartPaddingH = padding;
    }

    /** (beta)set chart Edge lable width saving when dividing for lablels
     *
     * default is : false*/
    public void setSaveEdgeLabelWidth(boolean isSave) {
        this.saveEdgeLabelWidth = isSave;
    }

    @Override
    public void invalidate() {
        super.invalidate();
    }
}

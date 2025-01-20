package com.conzumex.bpprogressbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BpProgressBar extends View {
    //Paints
    Paint paintPath;
    Paint paintProgress;
    Paint paintIndicator;
    Paint paintText;
    Paint paintGap;
    Paint paintBg;
    Paint paintMarker;
    //rect
    Rect textMeasurer;
    Rect textMeasurerBottom;
    Rect textRangeMeasurer;

    //marker
    Path topTriangle,bottomTriangle;

    //variables
    String textTop = "DIA";
    String textBottom = "SYS";
    String maxRangeText = "999";
    float lineWidth = 33,gapWidth=8, markerWidth = 10;
    float textSize = 40;
    float linePadding = 20, textTitlePadding = 10;
    float upperTextPosY = 0,lowerTextPosY = 0, linePosY = 0;
    float rangeDividerStartX = 0,rangeDividerEndX = 0;
    float indicatorStartX = 0,indicatorEndX = 0;
    float dividerTopY = 0,dividerBottomY = 0;
    float markerTriangleSize = 8;
    int colorBg = Color.BLACK;
    int colorGap = Color.RED;
    int colorMarker = Color.BLUE;
    int colorProgress = Color.GREEN;
    int colorPath = Color.WHITE;
    int colorText = Color.GRAY;
    boolean showMarker = true;
    List<Integer> topRanges = Arrays.asList(0,120,130,140,160,180,190);
    List<Integer> bottomRanges = Arrays.asList(0,80,85,90,100,110,120);
    List<Float> gapXpos, rangeXpos;
    int progressPos = -1;
    int topRangePos,bottomRangePos;
    int topProgress = -1,bottomProgress=-1;
    Typeface fontFace = null;

    public BpProgressBar(Context context) {
        super(context);
        init(context,null);
    }

    public BpProgressBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.BpProgressBar, 0, 0);
            topProgress = typedArray.getInteger(R.styleable.BpProgressBar_topProgress, topProgress);
            bottomProgress = typedArray.getInteger(R.styleable.BpProgressBar_bottomProgress, bottomProgress);
            textSize = typedArray.getDimension(R.styleable.BpProgressBar_textSize, textSize);
            colorProgress = typedArray.getColor(R.styleable.BpProgressBar_progressColor, colorProgress);
            colorPath = typedArray.getColor(R.styleable.BpProgressBar_pathColor, colorPath);
            colorBg = typedArray.getColor(R.styleable.BpProgressBar_backgroundColor, colorBg);
            colorMarker = typedArray.getColor(R.styleable.BpProgressBar_markerColor, colorMarker);
            colorGap = typedArray.getColor(R.styleable.BpProgressBar_gapColor, colorGap);
            colorText = typedArray.getColor(R.styleable.BpProgressBar_textColor, colorText);
            markerTriangleSize =  typedArray.getDimension(R.styleable.BpProgressBar_markerSize, markerTriangleSize);
            lineWidth =  typedArray.getDimension(R.styleable.BpProgressBar_progressWidth, lineWidth);
            markerWidth =  typedArray.getDimension(R.styleable.BpProgressBar_markerWidth, markerWidth);
            gapWidth =  typedArray.getDimension(R.styleable.BpProgressBar_gapWidth, gapWidth);
            linePadding = (int) typedArray.getDimension(R.styleable.BpProgressBar_progressPadding, linePadding);
            markerTriangleSize = (int) typedArray.getDimension(R.styleable.BpProgressBar_markerSize, markerTriangleSize);
            showMarker = typedArray.getBoolean(R.styleable.BpProgressBar_showMarker, showMarker);
            int fontId = typedArray.getResourceId(R.styleable.BpProgressBar_customFont,0);
            if(fontId!=0) {
                fontFace = ResourcesCompat.getFont(context, fontId);
            }
            typedArray.recycle();
        }
        paintIndicator = new Paint();
        paintGap = new Paint();
        paintText = new Paint();
        paintPath = new Paint();
        paintProgress = new Paint();
        paintBg = new Paint();
        paintMarker = new Paint();

        //rects
        textMeasurer = new Rect();
        textMeasurerBottom = new Rect();
        textRangeMeasurer = new Rect();

//        if(isInEditMode()){
//            topProgress=0;
//            bottomProgress=0;
//        }

    }

    void setPaintsPaths(){

        //path
        paintPath.setColor(colorPath);
        paintPath.setStrokeWidth(lineWidth);
        paintPath.setStyle(Paint.Style.STROKE);
        paintPath.setStrokeCap(Paint.Cap.ROUND);

        paintProgress.setColor(colorProgress);
        paintProgress.setStrokeWidth(lineWidth);
        paintProgress.setStyle(Paint.Style.STROKE);
        paintProgress.setStrokeCap(Paint.Cap.ROUND);

        paintBg.setColor(colorBg);

        paintGap.setColor(colorGap);
        paintGap.setStrokeWidth(gapWidth);
        paintGap.setStyle(Paint.Style.STROKE);

        paintMarker.setColor(colorMarker);
        paintMarker.setStrokeWidth(markerWidth);
        paintMarker.setStyle(Paint.Style.FILL_AND_STROKE);
        //text
        paintText.setColor(colorText);
        paintText.setTextSize(textSize);
        if(fontFace!=null)
            paintText.setTypeface(fontFace);
    }

    int calculateHeight(){
        return (int) (textSize+linePadding+lineWidth+linePadding+textSize);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // new height you want
        int newht = (int) Math.max(calculateHeight(),lowerTextPosY);
        int hM = MeasureSpec.getMode(heightMeasureSpec);
        int hS = MeasureSpec.getSize(heightMeasureSpec);
        int height;
        // Measure Height of custom view
        if (hM == MeasureSpec.EXACTLY) {
            // Must be of height size
            height = hS;
        } else if (hM == MeasureSpec.AT_MOST) {
            // Can't be bigger than new
            // height and height size
            height = Math.min(newht, hS);
        } else {
            // Be whatever you want
            height = newht;
        }
        // for making the desired size
        setMeasuredDimension(widthMeasureSpec, height);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        setPaintsPaths();

        canvas.drawPaint(paintBg);
        paintText.getTextBounds(textTop,0,textTop.length(),textMeasurer);
        paintText.getTextBounds(textBottom,0,textBottom.length(),textMeasurerBottom);
        textMeasurer.right = Math.max(textMeasurer.right,textMeasurerBottom.right);
        textMeasurer.bottom = Math.max(textMeasurer.bottom,textMeasurerBottom.bottom);
        paintText.getTextBounds(maxRangeText,0,maxRangeText.length(),textRangeMeasurer);
        upperTextPosY = textMeasurer.height();
        paintText.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(textTop,0,upperTextPosY,paintText);
        linePosY = upperTextPosY+linePadding+((float) lineWidth /2);
        indicatorStartX = (float) lineWidth /2;
        indicatorEndX = getWidth()-((float) lineWidth /2);
        canvas.drawLine(indicatorStartX,linePosY,indicatorEndX,linePosY,paintPath);
        lowerTextPosY=upperTextPosY+linePadding+lineWidth+linePadding+textMeasurer.height();
        canvas.drawText(textBottom,0,lowerTextPosY,paintText);

        rangeDividerStartX = (float) lineWidth /2+textMeasurer.width()+textTitlePadding+textRangeMeasurer.width();
        rangeDividerEndX = getWidth()-((float) lineWidth /2) - ((float) textRangeMeasurer.width() /2);
        dividerTopY = linePosY - ((float) lineWidth /2);
        dividerBottomY = dividerTopY + lineWidth;

        topRangePos = getRangePos(topProgress,topRanges);
        bottomRangePos = getRangePos(bottomProgress,bottomRanges);
        progressPos = Math.max(topRangePos+1,bottomRangePos+1);
        progressPos = Math.min(topRanges.size()-1,progressPos);

        drawGapsTexts(canvas);
        drawProgress(canvas);
        drawGaps(canvas);
        if(showMarker)
            drawMarkers(canvas);

    }

    void drawMarkers(Canvas canvas){
        //top progress
        if(topProgress>-1) {
            int rangeNextPos = topRangePos + 1;
            int lastTopIndex = topRanges.size() - 1;
            float markerX;
            if (topRangePos == lastTopIndex) {
                markerX = indicatorEndX;
            } else if (rangeNextPos == 0) {
                markerX = indicatorStartX;
            } else {
                float rangeArea = rangeXpos.get(rangeNextPos) - rangeXpos.get(topRangePos);
                float rangeVals = topRanges.get(rangeNextPos) - topRanges.get(topRangePos);
                float rangePixVal = rangeArea / rangeVals;
                float rangeProgress = topProgress - topRanges.get(topRangePos);
                markerX = rangeXpos.get(topRangePos) + (rangeProgress * rangePixVal);
            }
            canvas.drawLine(markerX, dividerTopY, markerX, dividerBottomY, paintMarker);

            topTriangle = new Path();
            topTriangle.moveTo(markerX, dividerTopY);
            topTriangle.lineTo(markerX + markerTriangleSize, dividerTopY - (markerTriangleSize * 2));
            topTriangle.lineTo(markerX - markerTriangleSize, dividerTopY - (markerTriangleSize * 2));
            topTriangle.lineTo(markerX, dividerTopY);
            canvas.drawPath(topTriangle, paintMarker);
        }

        //bottom progress
        if(bottomProgress>-1) {
            int bottomRangeNextPos = bottomRangePos + 1;
            int bottomLastTopIndex = bottomRanges.size() - 1;
            float bottomMarkerX;
            if (bottomRangePos == bottomLastTopIndex) {
                bottomMarkerX = indicatorEndX;
            } else if (bottomRangeNextPos == 0) {
                bottomMarkerX = indicatorStartX;
            } else {
                float rangeArea = rangeXpos.get(bottomRangeNextPos) - rangeXpos.get(bottomRangePos);
                float rangeVals = bottomRanges.get(bottomRangeNextPos) - bottomRanges.get(bottomRangePos);
                float rangePixVal = rangeArea / rangeVals;
                float rangeProgress = bottomProgress - bottomRanges.get(bottomRangePos);
                bottomMarkerX = rangeXpos.get(bottomRangePos) + (rangeProgress * rangePixVal);
            }
            canvas.drawLine(bottomMarkerX, dividerTopY, bottomMarkerX, dividerBottomY, paintMarker);

            bottomTriangle = new Path();
            bottomTriangle.moveTo(bottomMarkerX, dividerBottomY);
            bottomTriangle.lineTo(bottomMarkerX + markerTriangleSize, dividerBottomY + (markerTriangleSize * 2));
            bottomTriangle.lineTo(bottomMarkerX - markerTriangleSize, dividerBottomY + (markerTriangleSize * 2));
            bottomTriangle.lineTo(bottomMarkerX, dividerBottomY);
            canvas.drawPath(bottomTriangle, paintMarker);
        }
    }

    int getRangePos(float progress,List<Integer> ranges){
        int pos = -1;
        for(int i=0;i<ranges.size();i++){
            if(ranges.get(i)<=progress){
                pos = i;
            }
        }
        return pos;
    }

    void drawProgress(Canvas canvas){
        int progress = progressPos;
        float xPos = rangeXpos.get(progress);
        if(!(progress>0))
            return;
        xPos = (progress==rangeXpos.size()-1)?xPos:(xPos-((float) lineWidth /2));
        canvas.drawLine(indicatorStartX,linePosY,xPos,linePosY,paintProgress);

        if((xPos-indicatorStartX)>lineWidth && progress!=rangeXpos.size()-1) {
            paintProgress.setStrokeCap(Paint.Cap.SQUARE);
            canvas.drawLine(xPos - lineWidth, linePosY, xPos, linePosY, paintProgress);
            paintProgress.setStrokeCap(Paint.Cap.ROUND);
        }
    }

    void drawGaps(Canvas canvas){
        for(int i=0;i<gapXpos.size();i++){
            float tempX = gapXpos.get(i);
            canvas.drawLine(tempX,dividerTopY-0.1f,tempX,dividerBottomY,paintGap);
        }
    }
    void drawGapsTexts(Canvas canvas){
        //less 3 of size to get the correct pos for line
        int midGapCount = topRanges.size() - 3;
        float rangeDividerLength = rangeDividerEndX - rangeDividerStartX;
        float rangeDrawX = rangeDividerLength/midGapCount;
        gapXpos =  new ArrayList<>();
        rangeXpos =  new ArrayList<>();
        rangeXpos.add(indicatorStartX);
        for(int i=0;i<=midGapCount;i++){
            float tempX = rangeDividerStartX + (i*rangeDrawX);
            gapXpos.add(tempX);
            rangeXpos.add(tempX);
            paintText.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(getTopRangeText(i),tempX,upperTextPosY,paintText);
            canvas.drawText(getBottomRangeText(i),tempX,lowerTextPosY,paintText);
        }
        rangeXpos.add(indicatorEndX);
    }

    void printDebug(Canvas canvas,String e){
        Paint paintDebug =  new Paint();
        paintDebug.setColor(Color.WHITE);
        canvas.drawText(e,50,100,paintDebug);
    }

    String getTopRangeText(int pos){
        return topRanges.get(pos+1)+"";
    }

    String getBottomRangeText(int pos){
        return bottomRanges.get(pos+1)+"";
    }

    /** Set the top and bottom progress
     * @param top set the top progress value from the top ranges
     * @param bottom set the bottom progress value
     * */
    public void setProgresses(int top, int bottom){
        this.topProgress = top;
        this.bottomProgress = bottom;
        invalidate();
    }

    /**Sets the top progress percentage from the range
     * */
    public void setTopProgress(int top){
        topProgress = top;
        invalidate();
    }

    /**Sets the bottom progress percentage from the range
     * */
    public void setBottomProgress(int bottomProgress) {
        this.bottomProgress = bottomProgress;
        invalidate();
    }

    /**Set ranges including outer bounds for top and bottom.
     * @param topRanges List of integer values. including min and max showing in the bar.
     * @param bottomRanges List of integer values. including min and max showing in the bar.
     * <br><br>
     * E.g : >> if the ranges we want to show like 70-80-90-100, and values atmost minimum can be
     * 30 and maximum can be 140. so we can give it as 30-70-80-90-100-140.
     * the ranges 30-70 will draw between the start and first range position on the bar.
     * like that 100-140 range will show in the end of the bar after the last range position.
     * and bar will show the ranges only 70-80-90-100. and the first and last items will be in the edges of bar.
     * */
    public void setRanges(List<Integer> topRanges, List<Integer> bottomRanges){
        this.topRanges = topRanges;
        this.bottomRanges = bottomRanges;
        invalidate();
    }

    /**Set top range including outer bounds for top.
     * @param topRanges List of integer values. including min and max showing in the bar.
     * <br><br>
     * E.g : >> if the ranges we want to show like 70-80-90-100, and values atmost minimum can be
     * 30 and maximum can be 140. so we can give it as 30-70-80-90-100-140.
     * the ranges 30-70 will draw between the start and first range position on the bar.
     * like that 100-140 range will show in the end of the bar after the last range position.
     * and bar will show the ranges only 70-80-90-100. and the first and last items will be in the edges of bar.
     * */
    public void setTopRanges(List<Integer> topRanges){
        this.topRanges = topRanges;
        invalidate();
    }

    /**Set bottom range including outer bounds for bottom.
     * @param bottomRanges List of integer values. including min and max showing in the bar.
     * <br><br>
     * E.g : >> if the ranges we want to show like 70-80-90-100, and values atmost minimum can be
     * 30 and maximum can be 140. so we can give it as 30-70-80-90-100-140.
     * the ranges 30-70 will draw between the start and first range position on the bar.
     * like that 100-140 range will show in the end of the bar after the last range position.
     * and bar will show the ranges only 70-80-90-100. and the first and last items will be in the edges of bar.
     * */
    public void setBottomRanges(List<Integer> bottomRanges){
        this.bottomRanges = bottomRanges;
        invalidate();
    }

    /**Sets the progress bar width
     * */
    public void setProgressWidth(float width){
        lineWidth = width;
    }

    /**Sets the progress bar gap width of the ranges
     * */
    public void setGapWidth(float width){
        gapWidth = width;
    }

    /**Sets the progress bar marker line width of the ranges
     * */
    public void setMarkerWidth(float width){
        markerWidth = width;
    }

    /**Sets the marker triangle size
     * */
    public void setMarkerSize(float size){
        markerTriangleSize = size;
    }

    /**Sets the progress text size
     * */
    public void setTextSize(float size){
        textSize = size;
    }

    /**Sets the progress color
     * */
    public void setPathColor(int color){
        colorPath = color;
    }

    /**Sets the progress color
     * */
    public void setProgressColor(int color){
        colorProgress = color;
    }

    /**Sets the background color
     * */
    public void setBackgroundColor(int color){
        colorBg = color;
    }

    /**Sets the gap color
     * */
    public void setGapColor(int color){
        colorGap = color;
    }

    /**Sets the text color
     * */
    public void setTextColor(int color){
        colorText = color;
    }

    /**Sets the marker color
     * */
    public void setMarkerColor(int color){
        colorMarker = color;
    }

    /**Sets marker visible or not
     * */
    public void setShowMarker(boolean show){
        showMarker = show;
    }

    /**Set text on top progress
     * */
    public void setTopText(String text){
        textTop = text;
    }

    /**Set text on bottom progress
     * */
    public void setBottomText(String text){
        textBottom = text;
    }

}

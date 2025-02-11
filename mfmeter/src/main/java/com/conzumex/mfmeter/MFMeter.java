package com.conzumex.mfmeter;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.OverScroller;
import android.widget.Scroller;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MFMeter extends View {
    int parentViewWidth,parentViewHeight;
    //new test
    float lastX,lastY;
    VelocityTracker velocityTracker;
    OverScroller scroller ;
    float lastScrollX;
    Paint mPaint;
    RectF rectSquare;
    int mSectionDivider = 310,mItemHeight = 500;
    int markerGapTop = 40, markerGap = 70, markerGapBottom = 60;
    int hourCount = 24, hourSectionsCount = 6;
    int markerStartMargin = markerGap + 10;
    int mItemWidth = (hourCount * hourSectionsCount * markerGap) + (2 * markerStartMargin);
    int mItemStartX = 0;
    int mItemEndX = mItemWidth;
    float mDividerWidth = 3f;
    int markerHeightMin = 40, markerHeightMax = 60;
    int sessionGapBottom = 30, sessionMarkerHeight = 60;
    int markerWidth = 10;
    int sessionMarkerWidth = 15;
    int logGapBottom = 30, logHeight = 40, logGapTop = 20;
    int iconLineHeight = 200,iconLineWidth = 5, iconLineMinusMarginBottom = 5;
    int iconSize = 75;

    int colorBackground = Color.parseColor("#191919");
    int colorDivider = Color.parseColor("#1E707070");
    int colorMinuteMarker = Color.parseColor("#4d4d4d");
    int colorHourMarker = Color.parseColor("#ffffff");
    int colorHourText = Color.parseColor("#ffffff");
    //sessions make as objects
    int colorSessionMarker = Color.parseColor("#f71e1e");
    int colorSession = Color.parseColor("#40CB2020");
    //icons
    int colorIconLine = Color.parseColor("#4d4d4d");
    //logs
    int colorLog = Color.parseColor("#40515151");
    int colorLogText = Color.parseColor("#f71e1e");

    float sizeTextHour = 12 * getResources().getDisplayMetrics().scaledDensity;
    float sizeTextLog = 12 * getResources().getDisplayMetrics().scaledDensity;

    Typeface fontFace = null;
    boolean isFillBackground = true;
    boolean snapToSessions = false;
    List<Integer> snapPositions = new ArrayList<>();
    int currentSnapPos = -1;
    int scrollStartX = 0;
    boolean isScrolling = false;

    //Draw Items
    List<FuelLog> logItems = new ArrayList<>();
    List<FuelSession> sessionItems = new ArrayList<>();
    List<FuelIcon> iconItems = new ArrayList<>();

    public MFMeter(Context context) {
        this(context, null, 0);
    }

    public MFMeter(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MFMeter(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mPaint = new Paint();
        mPaint = getBackgroundPaint(mPaint);

        rectSquare = new RectF(0, 0, mItemWidth, mItemHeight);

        scroller = new OverScroller(context, new AccelerateDecelerateInterpolator());

        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.MFMeter, 0, 0);
        // Set a custom font family via its reference
        int fontId = typedArray.getResourceId(R.styleable.MFMeter_meterFontFamily,0);
        if(fontId!=0) {
            fontFace = ResourcesCompat.getFont(context, fontId);
        }
        //set text size
        if (typedArray.hasValue(R.styleable.MFMeter_meterTextSize)) {
            sizeTextHour = typedArray.getDimensionPixelSize(R.styleable.MFMeter_meterTextSize, 0);
            sizeTextLog = typedArray.getDimensionPixelSize(R.styleable.MFMeter_meterTextSize, 0);
        }
        //set background fill
        if (typedArray.hasValue(R.styleable.MFMeter_meterFillBackground)) {
            isFillBackground = typedArray.getBoolean(R.styleable.MFMeter_meterFillBackground, false);
        }
        //set snap sessions
        if (typedArray.hasValue(R.styleable.MFMeter_meterSnapSessions)) {
            snapToSessions = typedArray.getBoolean(R.styleable.MFMeter_meterSnapSessions, false);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        parentViewWidth = MeasureSpec.getSize(widthMeasureSpec);
        parentViewHeight = MeasureSpec.getSize(heightMeasureSpec);
        mItemEndX = mItemWidth - parentViewWidth;
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // Try for a width based on our minimum
        int minw = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
        int w = resolveSizeAndState(minw, widthMeasureSpec, 1);

        // Whatever the width ends up being, ask for a height that would let the pie
        // get as big as it can
        int minh = mItemHeight + getPaddingBottom() + getPaddingTop();
        int h = resolveSizeAndState(minh, heightMeasureSpec, 0);

        setMeasuredDimension(w, h);
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
            invalidate();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){

        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
        velocityTracker.addMovement(event);
        Log.d("Touch",event.toString()+" e "+event.getActionMasked());
        int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN :
                lastX = -event.getRawX();
                lastScrollX = -event.getRawX();
                lastY = event.getRawY();
                scrollStartX = getScrollX();
                isScrolling = true;
                break;
            case  MotionEvent.ACTION_MOVE :
                moveImage(-event.getRawX());
            {
                //for scroll by effect. without this fling working fine.
                float currentMoveX = -event.getRawX();
                int deltaMoveX = (int) ((currentMoveX - lastScrollX));
                int maxScrollLeft = getScrollX() - mItemStartX;
                int maxScrollRight = mItemEndX - getScrollX();
                if(deltaMoveX>0 && deltaMoveX<maxScrollRight+1){
                    scrollBy(deltaMoveX, 0);
                    invalidate();
                }else if(deltaMoveX<0 && -deltaMoveX<maxScrollLeft+1){
                    scrollBy(deltaMoveX, 0);
                    invalidate();
                }
                lastScrollX = currentMoveX;
            }
            break;
            case MotionEvent.ACTION_CANCEL :
                velocityTracker.recycle();
                velocityTracker = null;
                isScrolling = false;
                break;
            case MotionEvent.ACTION_UP :
                velocityTracker.computeCurrentVelocity(1000);

                scroller.fling(
                        getScrollX(), getScrollY(),
                        (int) -velocityTracker.getXVelocity(), 0,
                        0, mItemEndX,
                        0, 0,
                        0, 0
                );
                invalidate();
                velocityTracker.recycle();
                velocityTracker = null;
                isScrolling = false;
                break;
        }
        return true;
    }

    private void moveImage(Float xPos) {
        if(xPos>mItemStartX-1 && xPos<mItemEndX +1) {
            float disX = xPos - lastX;

            if (getScrollX() - disX < 0) {
                disX = getScrollX() - 0;
            } else if (getScrollX() - disX > mItemEndX) {
                disX = getScrollX() - mItemEndX;
            }

            scrollBy((int) disX, (int) 0);

            lastX = xPos;
            lastY = 0;
        }
    }

    public void setLogItems(List<FuelLog> logItems) {
        this.logItems = logItems;
        invalidate();
    }

    public void setSessionItems(List<FuelSession> sessionItems) {
        this.sessionItems = sessionItems;
        invalidate();
    }

    public void addIcons(List<FuelIcon> iconItems){
        this.iconItems = iconItems;
        invalidate();
    }

    public void loadData(List<FuelSession> sessions, List<FuelLog> logs, List<FuelIcon> icons){
        sessionItems = sessions;
        logItems = logs;
        iconItems = icons;
        invalidate();
    }
    
    public void setSnapPos(int pos){
        currentSnapPos = pos;
        invalidate();
    }

    public void setSnapEnabled(boolean enabled){
        snapToSessions = enabled;
        invalidate();
    }

    public int getSnapPos(){
        return currentSnapPos;
    }

    public boolean isSnapEnabled(){
        return snapToSessions;
    }

    public void scrollToSnapPos(int pos){
        if(snapPositions.isEmpty() || snapPositions.size()<=pos) {
            Log.d("SNAP","Items empty or beyond limit");
            return;
        }

        scrollTo(snapPositions.get(pos),0);
    }

    public void scrollToSnapPos(int pos,boolean isChangeSnapped){
        if(snapPositions.isEmpty() || snapPositions.size()<=pos) {
            Log.d("SNAP","Items empty or beyond limit");
            return;
        }

        scrollTo(snapPositions.get(pos),0);
        if(isChangeSnapped) {
            currentSnapPos = pos;
            scrollStartX = snapPositions.get(pos);
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
        if(isFillBackground)
            canvas.drawRoundRect(rectSquare, 10, 10, getBackgroundPaint(mPaint));
        canvas.drawLine(0, mSectionDivider, mItemWidth, mSectionDivider, getDividerPaint(mPaint));
        drawBottomSection(canvas);
        drawTopSection(canvas);
        canvas.restore();
        if(snapToSessions && !isScrolling)
            validateSnapping();
    }

    void validateSnapping(){
        if(snapPositions.isEmpty()) {
            Log.d("SNAP","Items empty");
            return;
        }

        if(snapPositions.size()<=currentSnapPos){
            Log.d("SNAP","Position beyond limit");
            currentSnapPos = -1;
        }
        if(snapPositions.size()>0 && currentSnapPos == -1){     //for the initial snap position
            scrollTo(snapPositions.get(0),0);
            currentSnapPos = 0;
            scrollStartX = snapPositions.get(0);
        }else{
            int scrolled = getScrollX() - scrollStartX;
            if(scrolled > 300){         //for right scroll
                if(currentSnapPos<snapPositions.size()-1)
                    currentSnapPos++;
            }else if(scrolled<-300){   //for left scroll
                if(currentSnapPos>0)
                    currentSnapPos--;
            }

            scrollTo(snapPositions.get(currentSnapPos),0);
            scrollStartX = snapPositions.get(currentSnapPos);
        }
    }

    void drawBottomSection(Canvas canvas){
        int markerStartPos = mSectionDivider + markerGapTop;
        int totalMarkers = hourCount * hourSectionsCount;
        int markerXPos = 0, hour = 0;
        String am_pm = "am";
        for(int i=0;i<totalMarkers;i++){
            markerXPos = markerStartMargin + (markerGap*i);
            if(i%hourSectionsCount==0){
                canvas.drawLine(markerXPos, markerStartPos, markerXPos, markerStartPos+markerHeightMax, getMarkerPaint(mPaint));
                canvas.drawText((hour==0?12:hour)+":00 "+am_pm, markerXPos, markerStartPos+markerHeightMax + markerGapBottom, getMarkerTextPaint(mPaint));
                hour++;
                if(hour>11){
                    am_pm = "pm";
                    hour=0;
                }
            }else {
                canvas.drawLine(markerXPos, markerStartPos, markerXPos, markerStartPos + markerHeightMin, getMarkerGreyPaint(mPaint));
            }
        }

        if(markerXPos!=0) {
            canvas.drawLine(markerXPos + markerGap, markerStartPos, markerXPos + markerGap, markerStartPos + markerHeightMax, getMarkerPaint(mPaint));
            canvas.drawText("12:00 am", markerXPos + markerGap, markerStartPos + markerHeightMax + markerGapBottom, getMarkerTextPaint(mPaint));
        }
    }

    void drawTopSection(Canvas canvas){
        drawSessions(canvas);
        drawLogs(canvas);
        drawIcons(canvas);
    }


    void drawSessions(Canvas canvas){
        if(!sessionItems.isEmpty()){
            snapPositions = new ArrayList<>();
            for(FuelSession item : sessionItems){
                drawSessionItem(canvas,item);
                snapPositions.add(getSessionItemPos(item));
            }
            Collections.sort(snapPositions);
        }
    }

    void drawSessionItem(Canvas canvas,FuelSession session){
        int sessionBottomPos = mSectionDivider - sessionGapBottom;
        int sessionMarkerStartPos = markerStartMargin + (markerGap*(session.getStartMinutes()/10));
        int sessionMarkerEndPos = markerStartMargin + (markerGap*(session.getEndMinutes()/10));

        canvas.drawRect(new RectF(sessionMarkerStartPos,sessionBottomPos,sessionMarkerEndPos,sessionBottomPos-sessionMarkerHeight), getSessionPaint(mPaint,session));
        canvas.drawLine(sessionMarkerStartPos, sessionBottomPos, sessionMarkerStartPos, sessionBottomPos - sessionMarkerHeight, getSessionMarkerPaint(mPaint,session));
        canvas.drawLine(sessionMarkerEndPos, sessionBottomPos, sessionMarkerEndPos, sessionBottomPos - sessionMarkerHeight, getSessionMarkerPaint(mPaint,session));
    }
    void drawLogs(Canvas canvas){
        if(!logItems.isEmpty()){
            for(FuelLog item : logItems){
                drawLogItem(canvas,item);
            }
        }
    }
    void drawLogItem(Canvas canvas, FuelLog logItem){
        int logBottomPos = mSectionDivider - sessionGapBottom - sessionMarkerHeight - logGapBottom;
        int logMarkerStartPos = markerStartMargin + (markerGap*(logItem.getStartMinutes()/10));
        int logMarkerEndPos = markerStartMargin + (markerGap*(logItem.getEndMinutes()/10));
        int textCenterPos = logMarkerStartPos+((logMarkerEndPos - logMarkerStartPos)/2);

        canvas.drawRect(new RectF(logMarkerStartPos,logBottomPos,logMarkerEndPos,logBottomPos-logHeight), getLogPaint(mPaint));
        canvas.drawText(logItem.getDuration(), textCenterPos, logBottomPos - logHeight - logGapTop, getLogTextPaint(mPaint));
    }

    void drawIcons(Canvas canvas){
        if(!iconItems.isEmpty()){
            for(FuelIcon item : iconItems){
                drawIconItem(canvas,item);
            }
        }
    }
    void drawIconItem(Canvas canvas,FuelIcon item) {
        int sessionBottomPos = mSectionDivider - sessionGapBottom + iconLineMinusMarginBottom;
        int iconPos = markerStartMargin + (markerGap * (item.getTimeMinutes() / 10));

        canvas.drawLine(iconPos, sessionBottomPos, iconPos, sessionBottomPos - iconLineHeight, getIconPaint(mPaint));

        int iconBoxStartX = iconPos - (iconSize / 2);
        int iconBoxEndX = iconBoxStartX + iconSize;
        int iconBoxStartY = sessionBottomPos - iconLineHeight - iconSize ;
        int iconBoxEndY = iconBoxStartY + iconSize;
        Drawable d = AppCompatResources.getDrawable(getContext(), item.icon);
        if (d != null){
            d.setBounds(iconBoxStartX, iconBoxStartY, iconBoxEndX, iconBoxEndY);
            d.draw(canvas);
        }
    }

    private int getSessionItemPos(FuelSession session){
        int sessionMarkerStartPos = markerStartMargin + (markerGap*(session.getStartMinutes()/10));
        int sessionMarkerEndPos = markerStartMargin + (markerGap*(session.getEndMinutes()/10));
        int midVal = (sessionMarkerStartPos + sessionMarkerEndPos) / 2;
        int movePos = midVal - (parentViewWidth / 2);
        if(movePos<mItemStartX)
            movePos = mItemStartX;
        else if(movePos>mItemEndX)
            movePos = mItemEndX;
        return movePos;
    }

    private int getSessionItemPos(int startPos,int endPos){
        int midVal = (startPos + endPos) / 2;
        int movePos = midVal - (parentViewWidth / 2);
        if(movePos<mItemStartX)
            movePos = mItemStartX;
        else if(movePos>mItemEndX)
            movePos = mItemEndX;
        return movePos;
    }


    Paint getBackgroundPaint(Paint paint){
        paint.reset();
        paint.setColor(colorBackground);
        return paint;
    }

    Paint getDividerPaint(Paint paint){
        paint.reset();
        paint.setColor(colorDivider);
        paint.setStrokeWidth(mDividerWidth);
        return paint;
    }

    Paint getIconPaint(Paint paint){
        paint.reset();
        paint.setColor(colorIconLine);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(iconLineWidth);
        return paint;
    }

    Paint getMarkerPaint(Paint paint){
        paint.reset();
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(markerWidth);
        paint.setColor(colorHourMarker);
        return paint;
    }
    Paint getMarkerGreyPaint(Paint paint){
        paint.reset();
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(markerWidth);
        paint.setColor(colorMinuteMarker);
        return paint;
    }
    Paint getSessionMarkerPaint(Paint paint,FuelSession item){
        paint.reset();
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(sessionMarkerWidth);
        paint.setColor(item.markerColor);
        return paint;
    }
    Paint getSessionPaint(Paint paint,FuelSession item){
        paint.reset();
        paint.setColor(item.sessionColor);
        return paint;
    }
    Paint getLogPaint(Paint paint){
        paint.reset();
        paint.setColor(colorLog);
        return paint;
    }
    Paint getMarkerTextPaint(Paint paint){
        paint.reset();
        paint.setColor(colorHourText);
        paint.setTextSize(sizeTextHour);
        if(fontFace!=null)
            paint.setTypeface(fontFace);
        paint.setTextAlign(Paint.Align.CENTER);
        return paint;
    }
    Paint getLogTextPaint(Paint paint){
        paint.reset();
        paint.setColor(colorLogText);
        paint.setTextSize(sizeTextLog);
        if(fontFace!=null)
            paint.setTypeface(fontFace);
        paint.setTextAlign(Paint.Align.CENTER);
        return paint;
    }

    public void setColorLogText(int colorLogText) {
        this.colorLogText = colorLogText;
    }

    public void setColorLog(int colorLog) {
        this.colorLog = colorLog;
    }

    public void setFillBackground(boolean fillBackground) {
        isFillBackground = fillBackground;
    }
}

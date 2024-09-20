package com.conzumex.circleseekbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.conzumex.cicleseekbar.R;
import com.conzumex.circleseekbar.marker.Marker;


public class CircleSeekBar extends View {

    public static final int MIN = 0;
    public static final int MAX = 100;

    private static final int ANGLE_OFFSET = -90;
    private static final float INVALID_VALUE = -1;
    private static final int TEXT_SIZE_DEFAULT = 72;

    /**
     * Current point value.
     */
    private int mProgressDisplay = MIN;
    int defProgress = 10;
    private int mSecondaryProgressDisplay = MIN;
    /**
     * The min value of progress value.
     */
    private int mMin = MIN;

    /**
     * The maximum value that {@link CircleSeekBar } can be set.
     */
    private int mMax = MAX;

    /**
     * The increment/decrement value for each movement of progress.
     */
    private int mStep = 1;

    private int mArcWidth = 12;
    private int mProgressWidth = 12;

    //
    // internal variables
    //
    /**
     * The counts of point update to determine whether to change previous progress.
     */
    private int mUpdateTimes = 0;
    private float mPreviousProgress = -1;
    private float mCurrentProgress = 0;

    /**
     * Determine whether reach max of point.
     */
    private boolean isMax = false;

    /**
     * Determine whether reach min of point.
     */
    private boolean isMin = false;

    // For Arc
    private RectF mArcRect = new RectF();
    private RectF mSecondaryArcRect = new RectF();
    private Paint mArcPaint;

    // For Progress
    private Paint mProgressPaint;
    private Paint mSecondaryProgressPaint;
    private Paint mDashPaint;
    private Paint mMarkerPaint;
    private float mProgressSweep;
    private float mSecondaryProgressSweep;

    //For Text progress
    private Paint mTextPaint;
    private int mTextSize = TEXT_SIZE_DEFAULT;
    private Rect mTextRect = new Rect();
    private boolean mIsShowText = false;
    private boolean mIsShowThumb = true;
    private boolean mDrawMarker = true;
    private boolean showMarker = false;

    private int mCenterX;
    private int mCenterY;
    private int mCircleRadius;
    private int capAdjustment=0;

    /**
     * The drawable for circle indicator of Seekbar
     */
    Drawable mThumbDrawable;

    // Coordinator (X, Y) of Indicator icon
    private int mThumbX;
    private int mThumbY;
    private int mThumbSize;

    private int mPadding;
    private double mAngle;
    private double mSecondaryAngle;
    private boolean mIsThumbSelected = false;
    private boolean mIsMarkerShown = false;
    private boolean mIsClickEnabled = true;
    private OnSeekBarChangedListener mOnSeekBarChangeListener;

    //for marker
    int markerLayout = R.layout.marker_default;
    Marker markerView;
    public CircleSeekBar(Context context) {
        super(context);
        init(context, null);
    }


    public CircleSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public void setStep(int mStep) {
        this.mStep = mStep;
    }

    public void setThumbDrawable(Drawable mIndicatorIcon) {
        this.mThumbDrawable = mIndicatorIcon;
    }

    public void setArcWidth(int mArcWidth) {
        this.mArcWidth = mArcWidth;
    }

    public void setProgressWidth(int mProgressWidth) {
        this.mProgressWidth = mProgressWidth;
    }

    public void setTextSize(int mTextSize) {
        this.mTextSize = mTextSize;
    }

    public void setIsShowText(boolean mIsShowText) {
        this.mIsShowText = mIsShowText;
    }

    public void setProgressDisplay(int progressDisplay) {
        mProgressDisplay = progressDisplay;
        mProgressDisplay = (mProgressDisplay > mMax) ? mMax : mProgressDisplay;
        mProgressDisplay = (mProgressDisplay < mMin) ? mMin : mProgressDisplay;
        mProgressSweep = (float) mProgressDisplay / valuePerDegree();
        mAngle = Math.PI / 2 - (mProgressSweep * Math.PI) / 180;
        invalidate();
    }

    public void setSecondaryProgressDisplay(int progressDisplay) {
        mSecondaryProgressDisplay = progressDisplay;
        mSecondaryProgressDisplay = (mSecondaryProgressDisplay > mMax) ? mMax : mSecondaryProgressDisplay;
        mSecondaryProgressDisplay = (mSecondaryProgressDisplay < mMin) ? mMin : mSecondaryProgressDisplay;
        mSecondaryProgressSweep = (float) mSecondaryProgressDisplay / valuePerDegree();
        mSecondaryAngle = Math.PI / 2 - (mSecondaryProgressSweep * Math.PI) / 180;
        invalidate();
    }

    public void setProgressDisplayAndInvalidate(int progressDisplay) {
        setProgressDisplay(progressDisplay);
        if(mOnSeekBarChangeListener != null) {
            mOnSeekBarChangeListener.onPointsChanged(this, mProgressDisplay, false);
        }
        invalidate();
    }

    public void setShowThumb(boolean mShowThumb) {
        this.mIsShowThumb = mShowThumb;
        invalidate();
    }
    public boolean getShowThumb(){
        return mIsShowThumb;
    }

    public int getProgressDisplay() {
        return mProgressDisplay;
    }

    public int getMin() {
        return mMin;
    }

    public int getMax() {
        return mMax;
    }

    public int getStep() {
        return mStep;
    }

    public float getCurrentProgress() {
        return mCurrentProgress;
    }

    public double getAngle() {
        return mAngle;
    }

    private void init(Context context, AttributeSet attrs) {

        final float density = context.getResources().getDisplayMetrics().density;
        int progressColor = ContextCompat.getColor(context, R.color.color_progress);
        int secondaryprogressColor = ContextCompat.getColor(context, R.color.color_secondary_progress);
        int arcColor = ContextCompat.getColor(context, R.color.color_arc);
        int dashColor = ContextCompat.getColor(context, R.color.color_arc);
        int textColor = ContextCompat.getColor(context, R.color.color_text);
        int markerColor = Color.parseColor("#ff0073");
        mProgressWidth = (int) (density * mProgressWidth);
        mArcWidth = (int) (density * mArcWidth);
        mTextSize = (int) (density * mTextSize);

        mThumbDrawable = ContextCompat.getDrawable(context, R.drawable.ic_ring_strain_goal_flag);
        if (attrs != null) {
            final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleSeekBar, 0, 0);
            Drawable indicator = typedArray.getDrawable(R.styleable.CircleSeekBar_csb_thumbDrawable);
            if (indicator != null) mThumbDrawable = indicator;

            mProgressDisplay = typedArray.getInteger(R.styleable.CircleSeekBar_csb_progress, defProgress);
            mSecondaryProgressDisplay = typedArray.getInteger(R.styleable.CircleSeekBar_csb_secondary_progress, 20);
            mThumbSize = typedArray.getDimensionPixelSize(R.styleable.CircleSeekBar_csb_thumbSize, 50);

            mMin = typedArray.getInteger(R.styleable.CircleSeekBar_csb_min, mMin);
            mMax = typedArray.getInteger(R.styleable.CircleSeekBar_csb_max, mMax);
            mStep = typedArray.getInteger(R.styleable.CircleSeekBar_csb_step, mStep);
            capAdjustment = typedArray.getInteger(R.styleable.CircleSeekBar_csb_cap_adjustment, capAdjustment);


            mTextSize = (int) typedArray.getDimension(R.styleable.CircleSeekBar_csb_textSize, mTextSize);
            textColor = typedArray.getColor(R.styleable.CircleSeekBar_csb_textColor, textColor);
            mIsShowText = typedArray.getBoolean(R.styleable.CircleSeekBar_csb_isShowText, mIsShowText);
            mIsShowThumb = typedArray.getBoolean(R.styleable.CircleSeekBar_csb_isShowThumb, mIsShowThumb);
            mIsClickEnabled = typedArray.getBoolean(R.styleable.CircleSeekBar_csb_isClickable, mIsClickEnabled);

            mProgressWidth = (int) typedArray.getDimension(R.styleable.CircleSeekBar_csb_progressWidth, mProgressWidth);
            progressColor = typedArray.getColor(R.styleable.CircleSeekBar_csb_progressColor, progressColor);
            secondaryprogressColor = typedArray.getColor(R.styleable.CircleSeekBar_csb_secondaryprogressColor, secondaryprogressColor);
            dashColor = typedArray.getColor(R.styleable.CircleSeekBar_csb_dash_line_color, dashColor);

            mArcWidth = (int) typedArray.getDimension(R.styleable.CircleSeekBar_csb_arcWidth, mArcWidth);
            arcColor = typedArray.getColor(R.styleable.CircleSeekBar_csb_arcColor, arcColor);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                int all = getPaddingLeft() + getPaddingRight() + getPaddingBottom() + getPaddingTop() + getPaddingEnd() + getPaddingStart();
                mPadding = all / 6;
            } else {
                mPadding = (getPaddingLeft() + getPaddingRight() + getPaddingBottom() + getPaddingTop()) / 4;
            }
            typedArray.recycle();
        }

        // range check
        mProgressDisplay = (mProgressDisplay > mMax) ? mMax : mProgressDisplay;
        mProgressDisplay = (mProgressDisplay < mMin) ? mMin : mProgressDisplay;

        mProgressSweep = (float) mProgressDisplay / valuePerDegree();
        mAngle = Math.PI / 2 - (mProgressSweep * Math.PI) / 180;
        mCurrentProgress = Math.round(mProgressSweep * valuePerDegree());

        //for secondary
        mSecondaryProgressDisplay = (mSecondaryProgressDisplay > mMax) ? mMax : mSecondaryProgressDisplay;
        mSecondaryProgressDisplay = (mSecondaryProgressDisplay < mMin) ? mMin : mSecondaryProgressDisplay;

        mSecondaryProgressSweep = (float) mSecondaryProgressDisplay / valuePerDegree();
        mSecondaryAngle = Math.PI / 2 - (mSecondaryProgressSweep * Math.PI) / 180;
//        mCurrentProgress = Math.round(mSecondaryProgressSweep * valuePerDegree());

        mArcPaint = new Paint();
        mArcPaint.setColor(arcColor);
        mArcPaint.setAntiAlias(true);
        mArcPaint.setStyle(Paint.Style.STROKE);
        mArcPaint.setStrokeWidth(mArcWidth);

        mProgressPaint = new Paint();
        mProgressPaint.setColor(progressColor);
        mProgressPaint.setAntiAlias(true);
        mProgressPaint.setStyle(Paint.Style.STROKE);
        mProgressPaint.setStrokeWidth(mProgressWidth);

        mSecondaryProgressPaint = new Paint();
        mSecondaryProgressPaint.setColor(secondaryprogressColor);
        mSecondaryProgressPaint.setAntiAlias(true);
        mSecondaryProgressPaint.setStyle(Paint.Style.STROKE);
        mSecondaryProgressPaint.setStrokeWidth(mProgressWidth);

        mDashPaint = new Paint();
        mDashPaint.setColor(dashColor);
        mDashPaint.setStrokeWidth(3);
        mDashPaint.setPathEffect(new DashPathEffect(new float[]{10,10},1));

        mTextPaint = new Paint();
        mTextPaint.setColor(textColor);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextSize(mTextSize);

        mMarkerPaint = new Paint();
        mMarkerPaint.setColor(markerColor);
        mMarkerPaint.setStyle(Paint.Style.STROKE);
        mMarkerPaint.setStrokeWidth(5);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        final int min = Math.min(w, h);

        // find circle's rectangle points
        int alignLeft = (w - min) / 2;
        int alignTop = (h - min) / 2;
        int alignRight = alignLeft + min;
        int alignBottom = alignTop + min;

        // save circle coordinates
        mCenterX = alignRight / 2 + (w - alignRight) / 2;
        mCenterY = alignBottom / 2 + (h - alignBottom) / 2;


        float progressDiameter = min - mPadding - (mThumbSize*4);
        mCircleRadius = (int) (progressDiameter / 2);
        float top = h / 2 - (progressDiameter / 2);
        float left = w / 2 - (progressDiameter / 2);
        mArcRect.set(left, top, left + progressDiameter, top + progressDiameter);
        mSecondaryArcRect.set(left, top, left + progressDiameter, top + progressDiameter);

        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if(mIsShowText) {
            // draw the text
            String textPoint = String.valueOf(mProgressDisplay);
            mTextPaint.getTextBounds(textPoint, 0, textPoint.length(), mTextRect);
            // center the text
            int xPos = canvas.getWidth() / 2 - mTextRect.width() / 2;
            int yPos = (int) ((mArcRect.centerY()) - ((mTextPaint.descent() + mTextPaint.ascent()) / 2));
            canvas.drawText(String.valueOf(mProgressDisplay), xPos, yPos, mTextPaint);
        }

        if(isInEditMode()) {
            canvas.drawPaint(new Paint());
            mThumbSize = 50;
        }

        // draw the arc and progress
        canvas.drawCircle(mCenterX, mCenterY, mCircleRadius, mArcPaint);
        mProgressPaint.setStrokeCap(Paint.Cap.ROUND);


        mSecondaryProgressPaint.setStrokeCap(Paint.Cap.ROUND);
//        mSecondaryProgressPaint.setColor(Color.RED);

        // find thumb position
        mThumbX = (int) (mCenterX + mCircleRadius * Math.cos(mAngle));
        mThumbY = (int) (mCenterY - mCircleRadius * Math.sin(mAngle));

        double tempAngle = Math.PI / 2 - ((mProgressSweep+(capAdjustment)) * Math.PI) / 180;
        int tempMThumbX = (int) (mCenterX + mCircleRadius * Math.cos(tempAngle));
        int tempMThumbY = (int) (mCenterY - mCircleRadius * Math.sin(tempAngle));

//        printDebug("radius "+mCircleRadius+",    arcwidth    "+mProgressWidth+" : "+tempMThumbY+" sweep "+mProgressSweep,canvas);

        int mOuterThumbX = (int) (mCenterX + (mCircleRadius+mThumbSize*1.5) * Math.cos(tempAngle));
        int mOuterThumbY = (int) (mCenterY - (mCircleRadius+mThumbSize*1.5) * Math.sin(tempAngle));

        int mOuterLineX = (int) (mCenterX + (mCircleRadius+mThumbSize) * Math.cos(tempAngle));
        int mOuterLineY = (int) (mCenterY - (mCircleRadius+mThumbSize) * Math.sin(tempAngle));

        if(mIsShowThumb)
            canvas.drawLine(tempMThumbX,tempMThumbY,mOuterLineX,mOuterLineY,mDashPaint);

        canvas.drawArc(mArcRect, ANGLE_OFFSET, mProgressSweep, false, mProgressPaint);
        canvas.drawArc(mSecondaryArcRect, ANGLE_OFFSET, mSecondaryProgressSweep, false, mSecondaryProgressPaint);

        if(mIsShowThumb) {
            mThumbDrawable.setBounds(mOuterThumbX - mThumbSize / 2, mOuterThumbY - mThumbSize / 2,
                    mOuterThumbX + mThumbSize / 2, mOuterThumbY + mThumbSize / 2);
            mThumbDrawable.draw(canvas);
        }

        if(mDrawMarker && showMarker) {
            drawMarker(tempAngle, canvas);
        }
    }

    void drawMarker(double tempAngle,Canvas canvas){
//        canvas.drawLine(x,y+20,x,y,mMarkerPaint);
        if(markerView==null)
            markerView = new Marker(getContext(),markerLayout);
        markerView.refreshContent(mProgressDisplay,mSecondaryProgressDisplay);

        int markerHeight = markerView.getHeight();
        int markerWidth = markerView.getWidth();

//        canvas.save(); // first save the state of the canvas
//        canvas.rotate(45); // rotate it
        // find marker position
        int x = (int) (mCenterX + (mCircleRadius - (markerWidth / 2.9)) * Math.cos(tempAngle));
        int y = (int) (mCenterY - (mCircleRadius - (markerHeight / 2.9)) * Math.sin(tempAngle));
//        printDebug(markerHeight+" "+markerWidth+" thumbx "+mThumbX+" : "+mThumbY+" mT "+x+": "+y+" w "+getWidth(),canvas);

        markerView.draw(canvas, x, y,0,getWidth());
//        canvas.restore();
    }

    void printDebug(String text,Canvas canvas){
        Paint paint = new Paint();
        paint.setColor(Color.YELLOW);
        paint.setTextSize(25);
        canvas.drawText(text,10,50,paint);
    }

    private float valuePerDegree() {
        return mMax / 360.0f;
    }

    /**
     * Invoked when slider starts moving or is currently moving. This method calculates and sets position and angle of the thumb.
     *
     * @param touchX Where is the touch identifier now on X axis
     * @param touchY Where is the touch identifier now on Y axis
     */
    private void updateProgressState(int touchX, int touchY) {
        int distanceX = touchX - mCenterX;
        int distanceY = mCenterY - touchY;
        //noinspection SuspiciousNameCombination
        double c = Math.sqrt(Math.pow(distanceX, 2) + Math.pow(distanceY, 2));
        mAngle = Math.acos(distanceX / c);
        if (distanceY < 0) {
            mAngle = -mAngle;
        }
        mProgressSweep = (float) (90 - (mAngle * 180) / Math.PI);
        if (mProgressSweep < 0) mProgressSweep += 360;
        int progress = Math.round(mProgressSweep * valuePerDegree());
        updateProgress(progress, true);
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        //to check the click is enabled or not
//        if(!mIsClickEnabled){
//            return false;
//        }
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN: {
//                // start moving the thumb (this is the first touch)
//                int x = (int) event.getX();
//                int y = (int) event.getY();
//                if (x < mThumbX + mThumbSize && x > mThumbX - mThumbSize && y < mThumbY + mThumbSize
//                        && y > mThumbY - mThumbSize) {
//                    getParent().requestDisallowInterceptTouchEvent(true);
//                    mIsThumbSelected = true;
//                    updateProgressState(x, y);
//                    if(mOnSeekBarChangeListener != null) {
//                        mOnSeekBarChangeListener.onStartTrackingTouch(this);
//                    }
//                }
//                break;
//            }
//
//            case MotionEvent.ACTION_MOVE: {
//                // still moving the thumb (this is not the first touch)
//                if (mIsThumbSelected) {
//                    int x = (int) event.getX();
//                    int y = (int) event.getY();
//                    updateProgressState(x, y);
//                }
//                break;
//            }
//
//            case MotionEvent.ACTION_UP: {
//                // finished moving (this is the last touch)
//                getParent().requestDisallowInterceptTouchEvent(false);
//                mIsThumbSelected = false;
//                if(mOnSeekBarChangeListener != null)
//                    mOnSeekBarChangeListener.onStopTrackingTouch(this);
//                break;
//            }
//        }
//
//        // redraw the whole component
//        return true;
//    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //to check the click is enabled or not
        if(!mIsClickEnabled){
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                // start moving the thumb (this is the first touch)
                int x = (int) event.getX();
                int y = (int) event.getY();
                if (x < mThumbX + mThumbSize && x > mThumbX - mThumbSize && y < mThumbY + mThumbSize
                        && y > mThumbY - mThumbSize) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    showMarker = !showMarker;
                    invalidate();
                }
                break;
            }

//            case MotionEvent.ACTION_MOVE: {
//                // still moving the thumb (this is not the first touch)
//                if (mIsThumbSelected) {
//                    int x = (int) event.getX();
//                    int y = (int) event.getY();
//                    updateProgressState(x, y);
//                }
//                break;
//            }

//            case MotionEvent.ACTION_UP: {
//                // finished moving (this is the last touch)
//                getParent().requestDisallowInterceptTouchEvent(false);
////                mIsThumbSelected = false;
////                if(mOnSeekBarChangeListener != null)
////                    mOnSeekBarChangeListener.onStopTrackingTouch(this);
//                break;
//            }
        }

        // redraw the whole component
        return true;
    }

    private void updateProgress(int progress, boolean fromUser) {

        // detect points change closed to max or min
        final int maxDetectValue = (int) ((double) mMax * 0.99);
        final int minDetectValue = (int) ((double) mMax * 0.005) + mMin;

        mUpdateTimes++;
        if (progress == INVALID_VALUE) {
            return;
        }

        // avoid accidentally touch to become max from original point
        if (progress > maxDetectValue && mPreviousProgress == INVALID_VALUE) {
            return;
        }


        // record previous and current progress change
        if (mUpdateTimes == 1) {
            mCurrentProgress = progress;
        } else {
            mPreviousProgress = mCurrentProgress;
            mCurrentProgress = progress;
        }

        mProgressDisplay = progress - (progress % mStep);

        /**
         * Determine whether reach max or min to lock point update event.
         *
         * When reaching max, the progress will drop from max (or maxDetectPoints ~ max
         * to min (or min ~ minDetectPoints) and vice versa.
         *
         * If reach max or min, stop increasing / decreasing to avoid exceeding the max / min.
         */
        if (mUpdateTimes > 1 && !isMin && !isMax) {
            if (mPreviousProgress >= maxDetectValue && mCurrentProgress <= minDetectValue &&
                    mPreviousProgress > mCurrentProgress) {
                isMax = true;
                progress = mMax;
                mProgressDisplay = mMax;
                mProgressSweep = 360;
                if (mOnSeekBarChangeListener != null) {
                    mOnSeekBarChangeListener.onPointsChanged(this, progress, fromUser);
                }
                invalidate();
            } else if ((mCurrentProgress >= maxDetectValue
                    && mPreviousProgress <= minDetectValue
                    && mCurrentProgress > mPreviousProgress) || mCurrentProgress <= mMin) {
                isMin = true;
                progress = mMin;
                mProgressDisplay = mMin;
                mProgressSweep = mMin / valuePerDegree();
                if (mOnSeekBarChangeListener != null) {
                    mOnSeekBarChangeListener.onPointsChanged(this, progress, fromUser);
                }
                invalidate();
            }
        } else {

            // Detect whether decreasing from max or increasing from min, to unlock the update event.
            // Make sure to check in detect range only.
            if (isMax & (mCurrentProgress < mPreviousProgress) && mCurrentProgress >= maxDetectValue) {
                isMax = false;
            }
            if (isMin
                    && (mPreviousProgress < mCurrentProgress)
                    && mPreviousProgress <= minDetectValue && mCurrentProgress <= minDetectValue
                    && mProgressDisplay >= mMin) {
                isMin = false;
            }
        }

        if (!isMax && !isMin) {
            progress = (progress > mMax) ? mMax : progress;
            progress = (progress < mMin) ? mMin : progress;

            if (mOnSeekBarChangeListener != null) {
                progress = progress - (progress % mStep);

                mOnSeekBarChangeListener.onPointsChanged(this, progress, fromUser);
            }
            invalidate();
        }
    }

    public void setSeekBarChangeListener(OnSeekBarChangedListener seekBarChangeListener) {
        this.mOnSeekBarChangeListener = seekBarChangeListener;
    }


    public interface OnSeekBarChangedListener {
        /**
         * Notification that the point value has changed.
         *
         * @param circleSeekBar The CircleSeekBar view whose value has changed
         * @param points        The current point value.
         * @param fromUser      True if the point change was triggered by the user.
         */
        void onPointsChanged(CircleSeekBar circleSeekBar, int points, boolean fromUser);

        void onStartTrackingTouch(CircleSeekBar circleSeekBar);

        void onStopTrackingTouch(CircleSeekBar circleSeekBar);
    }


    /** set markerView*/
    public void setMarkerView(Marker mMarker){
        this.markerView = mMarker;
    }

}

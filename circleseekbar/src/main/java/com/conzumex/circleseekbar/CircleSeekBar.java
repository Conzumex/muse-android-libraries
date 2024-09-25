package com.conzumex.circleseekbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Range;
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
    int defProgress = 50;
    private int mSecondaryProgressDisplay = MIN;
    private int mRangeMin = 50;
    private int mRangeMax = 70;
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
    private int mRangeArcWidth = 2;
    private int mProgressWidth = 12;
    private int mFlagLineWidth = 2;
    private int mRangeDistance = 50;
    private float mRangeEraseOffset = 4f;
    private float mRangeValueOffset = 10f;
    private float mFlagOffset = 50f;
    private float mMarkerOffset = 20f;

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
    private RectF mRangeArcRect = new RectF();
    private RectF mRangeArcTextRect = new RectF();
    private RectF mRangeArcTextEndRect = new RectF();
    private RectF mMarkerArcRect = new RectF();
    private Paint mArcPaint;
    private Paint mArcRangePaint;
    private Paint mRangeCirclePaint;

    // For Progress
    private Paint mProgressPaint;
    private Paint mSecondaryProgressPaint;
    private Paint mFlagLinePaint;
    private Paint mMarkerPaint;
    private float mProgressSweep;
    private float mSecondaryProgressSweep;
    private float mRangeStartSweep;
    private float mRangeEndSweep;

    //For Text progress
    private Paint mTextPaint;
    private Paint mRangeTextPaint;
    private Paint mRangeTextEraserPaint;
    private int mTextSize = TEXT_SIZE_DEFAULT;
    private int mRangeTextSize = 50;
    private Rect mTextRect = new Rect();
    private boolean mIsShowText = false;
    private boolean mIsShowThumb = true;
    private boolean mIsShowRange = true;
    private boolean mIsShowRangeTexts = true;
    private boolean mDrawMarker = true;
    private boolean showMarker = false;

    private int mCenterX;
    private int mCenterY;
    private int mCircleRadius;
    private int capAdjustment=0;
    String rangeText = "OPTIMAL";

    /**
     * The drawable for circle indicator of Seekbar
     */
    Drawable mThumbDrawable;

    // Coordinator (X, Y) of Indicator icon
    private int mThumbX;
    private int mThumbY;
    private int mOuterThumbX;
    private int mOuterThumbY;
    private int mThumbSize;
    private int mRangeCircleSize = 15;

    private int mPadding;
    private double mAngle;
    private double mSecondaryAngle;
    private double mRangeStartAngle;
    private double mRangeEndAngle;
    private boolean mIsThumbSelected = false;
    private boolean mIsMarkerShown = false;
    private boolean mIsClickEnabled = true;
    private OnSeekBarChangedListener mOnSeekBarChangeListener;
    private onThumbClicked mThumbClickListener;
    private RangeTextFormatter mRangeTextFormatter;

    //for marker
    int markerLayout = R.layout.marker_default;
    Marker markerView;

    private Canvas bitMapCanvas;
    private Bitmap frameBitmap;
    private Paint paint;
    private PorterDuffXfermode porterDuffXfermode;

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
    public void setRangeArcWidth(int mArcWidth) {
        this.mRangeArcWidth = mArcWidth;
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

    public void setRange(int minRange, int maxRange) {
        mRangeMin = minRange;
        mRangeMax = maxRange;

        mRangeMin = Math.min(mRangeMin, mMax);
        mRangeMin = Math.max(mRangeMin, mMin);
        mRangeStartSweep = (float) mRangeMin / valuePerDegree();
        mRangeStartAngle = Math.PI / 2 - (mRangeStartSweep * Math.PI) / 180;

        mRangeMax = Math.min(mRangeMax, mMax);
        mRangeMax = Math.max(mRangeMax, mMin);
        mRangeEndSweep = (float) mRangeMax / valuePerDegree();
        mRangeEndAngle = Math.PI / 2 - (mRangeEndSweep * Math.PI) / 180;

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
        int rangeColor = ContextCompat.getColor(context, R.color.color_range);
        int dashColor = ContextCompat.getColor(context, R.color.color_arc);
        int textColor = ContextCompat.getColor(context, R.color.color_text);
        int markerColor = Color.parseColor("#ff0073");
        mProgressWidth = (int) (density * mProgressWidth);
        mArcWidth = (int) (density * mArcWidth);
        mRangeArcWidth = (int) (density * mRangeArcWidth);
        mTextSize = (int) (density * mTextSize);

        mThumbDrawable = ContextCompat.getDrawable(context, R.drawable.ic_ring_strain_goal_flag);
        if (attrs != null) {
            final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleSeekBar, 0, 0);
            Drawable indicator = typedArray.getDrawable(R.styleable.CircleSeekBar_csb_thumbDrawable);
            if (indicator != null) mThumbDrawable = indicator;

            mProgressDisplay = typedArray.getInteger(R.styleable.CircleSeekBar_csb_progress, defProgress);
            mSecondaryProgressDisplay = typedArray.getInteger(R.styleable.CircleSeekBar_csb_secondary_progress, 20);
            mThumbSize = typedArray.getDimensionPixelSize(R.styleable.CircleSeekBar_csb_thumbSize, 50);
            mRangeCircleSize = typedArray.getDimensionPixelSize(R.styleable.CircleSeekBar_csb_rangeCircleSize, mRangeCircleSize);
            mRangeEraseOffset = typedArray.getDimension(R.styleable.CircleSeekBar_csb_rangeEraseOffset, mRangeEraseOffset);
            mRangeValueOffset = typedArray.getDimension(R.styleable.CircleSeekBar_csb_rangeValueOffset, mRangeValueOffset);
            mFlagOffset = typedArray.getDimension(R.styleable.CircleSeekBar_csb_flagOffset, mFlagOffset);

            mMin = typedArray.getInteger(R.styleable.CircleSeekBar_csb_min, mMin);
            mMax = typedArray.getInteger(R.styleable.CircleSeekBar_csb_max, mMax);
            mStep = typedArray.getInteger(R.styleable.CircleSeekBar_csb_step, mStep);
            mRangeDistance = typedArray.getInteger(R.styleable.CircleSeekBar_csb_range_distance, mRangeDistance);
            capAdjustment = typedArray.getInteger(R.styleable.CircleSeekBar_csb_cap_adjustment, capAdjustment);
            mRangeMin = typedArray.getInteger(R.styleable.CircleSeekBar_csb_rangeStart, mRangeMin);
            mRangeMax = typedArray.getInteger(R.styleable.CircleSeekBar_csb_rangeEnd, mRangeMax);


            mRangeTextSize = (int) typedArray.getDimension(R.styleable.CircleSeekBar_csb_rangeTextSize, mRangeTextSize);
            mTextSize = (int) typedArray.getDimension(R.styleable.CircleSeekBar_csb_textSize, mTextSize);
            textColor = typedArray.getColor(R.styleable.CircleSeekBar_csb_textColor, textColor);
            mIsShowText = typedArray.getBoolean(R.styleable.CircleSeekBar_csb_isShowText, mIsShowText);
            mIsShowThumb = typedArray.getBoolean(R.styleable.CircleSeekBar_csb_isShowThumb, mIsShowThumb);
            mIsShowRange = typedArray.getBoolean(R.styleable.CircleSeekBar_csb_isShowRange, mIsShowRange);
            mIsShowRangeTexts = typedArray.getBoolean(R.styleable.CircleSeekBar_csb_isShowRangeText, mIsShowRangeTexts);
            mIsClickEnabled = typedArray.getBoolean(R.styleable.CircleSeekBar_csb_isClickable, mIsClickEnabled);

            mProgressWidth = (int) typedArray.getDimension(R.styleable.CircleSeekBar_csb_progressWidth, mProgressWidth);
            mFlagLineWidth = (int) typedArray.getDimension(R.styleable.CircleSeekBar_csb_flagLineWidth, mFlagLineWidth);
            progressColor = typedArray.getColor(R.styleable.CircleSeekBar_csb_progressColor, progressColor);
            secondaryprogressColor = typedArray.getColor(R.styleable.CircleSeekBar_csb_secondaryprogressColor, secondaryprogressColor);
            dashColor = typedArray.getColor(R.styleable.CircleSeekBar_csb_dash_line_color, dashColor);

            mArcWidth = (int) typedArray.getDimension(R.styleable.CircleSeekBar_csb_arcWidth, mArcWidth);
            mRangeArcWidth = (int) typedArray.getDimension(R.styleable.CircleSeekBar_csb_rangeWidth, mRangeArcWidth);
            arcColor = typedArray.getColor(R.styleable.CircleSeekBar_csb_arcColor, arcColor);
            rangeColor = typedArray.getColor(R.styleable.CircleSeekBar_csb_rangeColor, rangeColor);

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

        //for outsideRange
        mRangeMin = Math.min(mRangeMin, mMax);
        mRangeMin = Math.max(mRangeMin, mMin);
        mRangeStartSweep = (float) mRangeMin / valuePerDegree();
        mRangeStartAngle = Math.PI / 2 - (mRangeStartSweep * Math.PI) / 180;

        mRangeMax = Math.min(mRangeMax, mMax);
        mRangeMax = Math.max(mRangeMax, mMin);
        mRangeEndSweep = (float) mRangeMax / valuePerDegree();
        mRangeEndAngle = Math.PI / 2 - (mRangeEndSweep * Math.PI) / 180;

        mArcPaint = new Paint();
        mArcPaint.setColor(arcColor);
        mArcPaint.setAntiAlias(true);
        mArcPaint.setStyle(Paint.Style.STROKE);
        mArcPaint.setStrokeWidth(mArcWidth);

        mArcRangePaint = new Paint();
        mArcRangePaint.setColor(rangeColor);
        mArcRangePaint.setAntiAlias(true);
        mArcRangePaint.setStyle(Paint.Style.STROKE);
        mArcRangePaint.setStrokeWidth(mRangeArcWidth);
        mArcRangePaint.setPathEffect(new DashPathEffect(new float[]{10,10},1));

        mRangeCirclePaint = new Paint();
        mRangeCirclePaint.setColor(rangeColor);
        mRangeCirclePaint.setStyle(Paint.Style.FILL);

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

        mFlagLinePaint = new Paint();
        mFlagLinePaint.setColor(dashColor);
        mFlagLinePaint.setStrokeWidth(mFlagLineWidth);

        mTextPaint = new Paint();
        mTextPaint.setColor(textColor);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextSize(mTextSize);

        mRangeTextPaint = new Paint();
        mRangeTextPaint.setColor(rangeColor);
        mRangeTextPaint.setAntiAlias(true);
        mRangeTextPaint.setStyle(Paint.Style.FILL);
        mRangeTextPaint.setTextSize(mRangeTextSize);

        mRangeTextEraserPaint = new Paint();
        mRangeTextEraserPaint.setColor(rangeColor);
        mRangeTextEraserPaint.setAntiAlias(true);
        mRangeTextEraserPaint.setStyle(Paint.Style.STROKE);
        mRangeTextEraserPaint.setTextSize(55);
        mRangeTextEraserPaint.setStrokeWidth(mRangeArcWidth*2f);

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


        float progressDiameter = min - mPadding - (mRangeDistance*2) - Math.max(mRangeCircleSize,mRangeTextSize) - (mRangeDistance*1.4f) - mRangeArcWidth;
        mCircleRadius = (int) (progressDiameter / 2);
        float top = h / 2 - (progressDiameter / 2);
        float left = w / 2 - (progressDiameter / 2);
        mArcRect.set(left, top, left + progressDiameter, top + progressDiameter);
        mSecondaryArcRect.set(left, top, left + progressDiameter, top + progressDiameter);
        mRangeArcRect.set(left-mRangeDistance, top-mRangeDistance, left + progressDiameter+mRangeDistance, top + progressDiameter+mRangeDistance);
        mRangeArcTextRect.set(left-(mRangeDistance*1.4f), top-(mRangeDistance*1.4f), left + progressDiameter+(mRangeDistance*1.4f), top + progressDiameter+(mRangeDistance*1.4f));
        mRangeArcTextEndRect.set(left-(mRangeDistance/1.5f), top-(mRangeDistance/1.5f), left + progressDiameter+(mRangeDistance/1.5f), top + progressDiameter+(mRangeDistance/1.5f));
        mMarkerArcRect.set(left-(mMarkerOffset), top-(mMarkerOffset), left + progressDiameter+(mMarkerOffset), top + progressDiameter+(mMarkerOffset));

        if (bitMapCanvas == null) {
            bitMapCanvas = new Canvas();
            frameBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            bitMapCanvas.setBitmap(frameBitmap);
            porterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
        }

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

//        if(isInEditMode()) {
//            canvas.drawPaint(new Paint());
//            mThumbSize = 50;
//            mMax = 1000;
//        }

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

        mOuterThumbX = (int) (mCenterX + (mCircleRadius-(mThumbSize/2) - mFlagOffset) * Math.cos(tempAngle));
        mOuterThumbY = (int) (mCenterY - (mCircleRadius-(mThumbSize/2) - mFlagOffset) * Math.sin(tempAngle));

        int mOuterLineX = (int) (mCenterX + (mCircleRadius - mFlagOffset) * Math.cos(tempAngle));
        int mOuterLineY = (int) (mCenterY - (mCircleRadius - mFlagOffset) * Math.sin(tempAngle));

        if(mIsShowThumb)
            canvas.drawLine(tempMThumbX,tempMThumbY,mOuterLineX,mOuterLineY,mFlagLinePaint);

        canvas.drawArc(mArcRect, ANGLE_OFFSET, mProgressSweep, false, mProgressPaint);
        canvas.drawArc(mSecondaryArcRect, ANGLE_OFFSET, mSecondaryProgressSweep, false, mSecondaryProgressPaint);

        //for range
        if(mIsShowRange) {
            float endAngle = mRangeEndSweep - mRangeStartSweep;
            float startAngle = ANGLE_OFFSET + mRangeStartSweep;
            //todo draw on bitmap canvas to make it erasable
            canvas.drawArc(mRangeArcRect, startAngle, endAngle, false, mArcRangePaint);

            int mRangeStartX = (int) (mCenterX + (mCircleRadius + mRangeDistance) * Math.cos(mRangeStartAngle));
            int mRangeStartY = (int) (mCenterY - (mCircleRadius + mRangeDistance) * Math.sin(mRangeStartAngle));
            canvas.drawCircle(mRangeStartX, mRangeStartY, mRangeCircleSize, mRangeCirclePaint);

            int mRangeEndX = (int) (mCenterX + (mCircleRadius + mRangeDistance) * Math.cos(mRangeEndAngle));
            int mRangeEndY = (int) (mCenterY - (mCircleRadius + mRangeDistance) * Math.sin(mRangeEndAngle));
            canvas.drawCircle(mRangeEndX, mRangeEndY, mRangeCircleSize, mRangeCirclePaint);

            Path circlePath = new Path();
            circlePath.addArc(mRangeArcTextRect, startAngle, endAngle);

            mRangeTextPaint.setTextAlign(Paint.Align.CENTER);
            mRangeTextEraserPaint.setTextAlign(Paint.Align.CENTER);
            mRangeTextEraserPaint.setXfermode(porterDuffXfermode);
            mRangeTextEraserPaint.setColor(Color.GREEN);
            canvas.drawTextOnPath(rangeText, circlePath,  0, 0, mRangeTextPaint);

            //for range texts
            if(mIsShowRangeTexts) {
                float endRangeTextAngle = getSweepValue(mRangeMin) - getSweepValue(mRangeMax);
                float startRangeTextAngle = ANGLE_OFFSET + getSweepValue(mRangeMax);
                Path circleRangeTextPath = new Path();
                circleRangeTextPath.addArc(mRangeArcTextEndRect, startAngle - mRangeValueOffset, endAngle + (mRangeValueOffset * 2));

                mRangeTextPaint.setTextAlign(Paint.Align.LEFT);
                canvas.drawTextOnPath(getRangeText(mRangeMin), circleRangeTextPath, 0, 0, mRangeTextPaint);
                mRangeTextPaint.setTextAlign(Paint.Align.RIGHT);
                canvas.drawTextOnPath(getRangeText(mRangeMax), circleRangeTextPath, 0, 0, mRangeTextPaint);
            }
            int rangeMid = (mRangeMax+mRangeMin)/2;
            float endEraseAngle = getSweepValue(rangeMid-mRangeEraseOffset) - getSweepValue(rangeMid+mRangeEraseOffset);
            float startEraseAngle = ANGLE_OFFSET + getSweepValue(rangeMid+mRangeEraseOffset);
            mRangeTextEraserPaint.setColor(Color.GREEN);
            //todo for erase the line with this range
//            bitMapCanvas.drawArc(mRangeArcRect, startEraseAngle, endEraseAngle, false, mRangeTextEraserPaint);

//            printDebug("startAngle "+startAngle +" -:- "+endAngle+" :erase "+startEraseAngle+" -:- "+endEraseAngle+" :  sweep : "+mRangeStartSweep+" -:- "+mRangeEndSweep,canvas);
//            bitMapCanvas.drawTextOnPath("_______", circleErasePath,  0, 0, mRangeTextEraserPaint);
            canvas.drawBitmap(frameBitmap, 0, 0, null);
        }

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

        int yAdjust = markerWidth>markerHeight?markerWidth-markerHeight:0;
        int xAdjust = markerHeight>markerWidth?markerHeight-markerWidth:0;

//        canvas.save(); // first save the state of the canvas
//        canvas.rotate(45); // rotate it
        // find marker position
//        int x = (int) (mCenterX + (mCircleRadius - (mMarkerOffset- xAdjust)) * Math.cos(tempAngle));
//        int y = (int) (mCenterY - (mCircleRadius - (mMarkerOffset- yAdjust)) * Math.sin(tempAngle));


        int x = (int) (mCenterX + (mCircleRadius - (mFlagOffset*2) - mThumbSize - mMarkerOffset) * Math.cos(tempAngle));
        int y = (int) (mCenterY - (mCircleRadius - (mFlagOffset*2) - mThumbSize - mMarkerOffset) * Math.sin(tempAngle));

//        printDebug("y:"+y+" thu"+mThumbY+" x:"+x+" thumbx :"+mThumbX,canvas);
        if(mThumbX>x){
            x = x - (markerWidth/2);
        }else if(mThumbX<x){
            x = x + (markerWidth/2);
        }else if(mThumbY<y){
            y = y + (markerHeight/2);
        }else if(mThumbY>y){
//            printDebug("y:"+y+" thu"+mThumbY,canvas);
            y = y - (markerHeight/2);
        }


        //to check it is going out of boundary
            x = Math.max(x, (markerWidth / 2));
            x = Math.min(x, (getWidth() - (markerWidth/2)));

            y = Math.max(y, (markerHeight/2));
            y = Math.min(y, getHeight() - (markerHeight/2));

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
                if (x < mOuterThumbX + mThumbSize && x > mOuterThumbX - mThumbSize && y < mOuterThumbY + mThumbSize
                        && y > mOuterThumbY - mThumbSize) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    showMarker = !showMarker;
                    if(mThumbClickListener != null)
                        mThumbClickListener.onMarkerVisible(showMarker);
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

    public void setThumbClickListener(onThumbClicked thumbClickListener) {
        this.mThumbClickListener = thumbClickListener;
    }

    public void setRangeTextFormatter(RangeTextFormatter textFormatter) {
        this.mRangeTextFormatter = textFormatter;
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

    public interface onThumbClicked{
        void onMarkerVisible(boolean isShowing);
    }

    public interface RangeTextFormatter{
        String getText(int range);
    }

    String getRangeText(int value){
        if(mRangeTextFormatter!=null)
            return mRangeTextFormatter.getText(value);
        return value+"";
    }

    /** set markerView*/
    public void setMarkerView(Marker mMarker){
        this.markerView = mMarker;
    }

    private float getSweepValue(float sweep){
        return (float) sweep / valuePerDegree();
    }

    public void setShowRange(boolean mIsShowRange) {
        this.mIsShowRange = mIsShowRange;
        invalidate();
    }

    public void setShowMarker(boolean mIsShowRange) {
        this.mDrawMarker = mIsShowRange;
        invalidate();
    }

    public void setRangeStart(int rangeStart) {
        this.mRangeMin = rangeStart;
        mRangeMin = Math.min(mRangeMin, mMax);
        mRangeMin = Math.max(mRangeMin, mMin);
        mRangeStartSweep = (float) mRangeMin / valuePerDegree();
        mRangeStartAngle = Math.PI / 2 - (mRangeStartSweep * Math.PI) / 180;
        invalidate();
    }

    public void setRangeEnd(int rangeStart) {
        this.mRangeMax = rangeStart;
        mRangeMax = Math.min(mRangeMax, mMax);
        mRangeMax = Math.max(mRangeMax, mMin);
        mRangeEndSweep = (float) mRangeMax / valuePerDegree();
        mRangeEndAngle = Math.PI / 2 - (mRangeEndSweep * Math.PI) / 180;
        invalidate();
    }
}

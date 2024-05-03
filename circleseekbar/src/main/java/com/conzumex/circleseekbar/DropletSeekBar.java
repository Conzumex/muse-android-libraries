package com.conzumex.circleseekbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.conzumex.cicleseekbar.R;


public class DropletSeekBar extends View {

//    public static final int MIN = 0;
//    public static final int MAX = 100;
//
//    private static final int ANGLE_OFFSET = -90;
//    private static final float INVALID_VALUE = -1;
//    private static final int TEXT_SIZE_DEFAULT = 72;
//
//    /**
//     * Current point value.
//     */
//    private int mProgressDisplay = MIN;
//    /**
//     * The min value of progress value.
//     */
//    private int mMin = MIN;
//
//    /**
//     * The maximum value that {@link DropletSeekBar } can be set.
//     */
//    private int mMax = MAX;
//
//    /**
//     * The increment/decrement value for each movement of progress.
//     */
//    private int mStep = 1;
//
//    private int mArcWidth = 8;
//    private int mProgressWidth = 12;
//
//    //
//    // internal variables
//    //
//    /**
//     * The counts of point update to determine whether to change previous progress.
//     */
//    private int mUpdateTimes = 0;
//    private float mPreviousProgress = -1;
//    private float mCurrentProgress = 0;
//
//    /**
//     * Determine whether reach max of point.
//     */
//    private boolean isMax = false;
//
//    /**
//     * Determine whether reach min of point.
//     */
//    private boolean isMin = false;
//
//    // For Arc
//    private RectF mArcRect = new RectF();
//    private Paint mArcPaint;
//
//    // For Progress
//    private Paint mProgressPaint;
//    private float mProgressSweep;

    //For Text progress
    private Paint mPaint;
    private Paint mProgressPaint;
    Paint mPaintQuad;
    Path tempPath2,tempPath;
    int progressColor = Color.WHITE;
    int[] gradientColors;
//    private int mTextSize = TEXT_SIZE_DEFAULT;
//    private Rect mTextRect = new Rect();
//    private boolean mIsShowText = true;
//
//    private int mCenterX;
//    private int mCenterY;
//    private int mCircleRadius;

    /**
     * The drawable for circle indicator of Seekbar
     */
    Drawable mThumbDrawable;

    // Coordinator (X, Y) of Indicator icon
//    private int mThumbX;
//    private int mThumbY;
    private int mThumbSize;
//
//    private int mPadding;
//    private double mAngle;
//    private boolean mIsThumbSelected = false;

//    private OnSeekBarChangedListener mOnSeekBarChangeListener;

    //values
    float progressWidth,thumbStrokeWidth,clipLineWidth;
    float offset,thumbSize,minThumbWidth,maxProgress;
    float minProgress,progressPosition,viewHeight;
    int currentProgress = 40;
    float progressStep = 0;

    public DropletSeekBar(Context context) {
        super(context);
        init(context, null);
    }


    public DropletSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public void setProgress(int mProgress) {
        this.currentProgress = mProgress;
        invalidate();
    }

    public void setProgressColor(int color){
        this.progressColor = color;
    }

    public void setProgressGradient(int[] colors){
        gradientColors = colors;
    }

    public void setThumbDrawable(Drawable mIndicatorIcon) {
        this.mThumbDrawable = mIndicatorIcon;
    }
//
//    public void setArcWidth(int mArcWidth) {
//        this.mArcWidth = mArcWidth;
//    }
//
//    public void setProgressWidth(int mProgressWidth) {
//        this.mProgressWidth = mProgressWidth;
//    }
//
//    public void setTextSize(int mTextSize) {
//        this.mTextSize = mTextSize;
//    }
//
//    public void setIsShowText(boolean mIsShowText) {
//        this.mIsShowText = mIsShowText;
//    }

//    public void setProgressDisplay(int progressDisplay) {
//        mProgressDisplay = progressDisplay;
//        mProgressDisplay = (mProgressDisplay > mMax) ? mMax : mProgressDisplay;
//        mProgressDisplay = (mProgressDisplay < mMin) ? mMin : mProgressDisplay;
//        mProgressSweep = (float) mProgressDisplay / valuePerDegree();
//        mAngle = Math.PI / 2 - (mProgressSweep * Math.PI) / 180;
//    }
//
//    public void setProgressDisplayAndInvalidate(int progressDisplay) {
//        setProgressDisplay(progressDisplay);
//        if(mOnSeekBarChangeListener != null) {
//            mOnSeekBarChangeListener.onPointsChanged(this, mProgressDisplay, false);
//        }
//        invalidate();
//    }

//    public int getProgressDisplay() {
//        return mProgressDisplay;
//    }
//
//    public int getMin() {
//        return mMin;
//    }
//
//    public int getMax() {
//        return mMax;
//    }
//
//    public int getStep() {
//        return mStep;
//    }
//
//    public float getCurrentProgress() {
//        return mCurrentProgress;
//    }
//
//    public double getAngle() {
//        return mAngle;
//    }

    private void init(Context context, AttributeSet attrs) {

//        final float density = context.getResources().getDisplayMetrics().density;
//        int progressColor = ContextCompat.getColor(context, R.color.color_progress);
//        int arcColor = ContextCompat.getColor(context, R.color.color_arc);
//        int textColor = ContextCompat.getColor(context, R.color.color_text);
//        mProgressWidth = (int) (density * mProgressWidth);
//        mArcWidth = (int) (density * mArcWidth);
//        mTextSize = (int) (density * mTextSize);

        mThumbDrawable = ContextCompat.getDrawable(context, R.drawable.ic_circle_seekbar);
        if (attrs != null) {
            final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.DropletSeekBar, 0, 0);
            Drawable indicator = typedArray.getDrawable(R.styleable.DropletSeekBar_thumbDrawable);
            if (indicator != null) mThumbDrawable = indicator;

            currentProgress = typedArray.getInteger(R.styleable.DropletSeekBar_currentProgress, currentProgress);
            mThumbSize = typedArray.getDimensionPixelSize(R.styleable.DropletSeekBar_thumbSize, 55);

            int colorsId = typedArray.getResourceId(R.styleable.DropletSeekBar_gradientColors, 0);
            if(colorsId!=0){
                int[] colorsArray = typedArray.getResources().getIntArray(colorsId);
                this.gradientColors = colorsArray;
            }

//            mMin = typedArray.getInteger(R.styleable.CircleSeekBar_csb_min, mMin);
//            mMax = typedArray.getInteger(R.styleable.CircleSeekBar_csb_max, mMax);
//            mStep = typedArray.getInteger(R.styleable.CircleSeekBar_csb_step, mStep);
//
//
//            mTextSize = (int) typedArray.getDimension(R.styleable.CircleSeekBar_csb_textSize, mTextSize);
//            textColor = typedArray.getColor(R.styleable.CircleSeekBar_csb_textColor, textColor);
//            mIsShowText = typedArray.getBoolean(R.styleable.CircleSeekBar_csb_isShowText, mIsShowText);
//
//            mProgressWidth = (int) typedArray.getDimension(R.styleable.CircleSeekBar_csb_progressWidth, mProgressWidth);
//            progressColor = typedArray.getColor(R.styleable.CircleSeekBar_csb_progressColor, progressColor);
//
//            mArcWidth = (int) typedArray.getDimension(R.styleable.CircleSeekBar_csb_arcWidth, mArcWidth);
//            arcColor = typedArray.getColor(R.styleable.CircleSeekBar_csb_arcColor, arcColor);
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//                int all = getPaddingLeft() + getPaddingRight() + getPaddingBottom() + getPaddingTop() + getPaddingEnd() + getPaddingStart();
//                mPadding = all / 6;
//            } else {
//                mPadding = (getPaddingLeft() + getPaddingRight() + getPaddingBottom() + getPaddingTop()) / 4;
//            }
            typedArray.recycle();
        }
        mPaint = new Paint();
        mProgressPaint = new Paint();
        mPaintQuad = new Paint();
        tempPath2 = new Path();
        tempPath = new Path();

        progressWidth = 20f;
        thumbStrokeWidth = 10f;
        clipLineWidth = 1f;
        offset = 20;
        thumbSize = 60;
        minThumbWidth = thumbSize + thumbStrokeWidth;
//
//        // range check
//        mProgressDisplay = (mProgressDisplay > mMax) ? mMax : mProgressDisplay;
//        mProgressDisplay = (mProgressDisplay < mMin) ? mMin : mProgressDisplay;
//
//        mProgressSweep = (float) mProgressDisplay / valuePerDegree();
//        mAngle = Math.PI / 2 - (mProgressSweep * Math.PI) / 180;
//        mCurrentProgress = Math.round(mProgressSweep * valuePerDegree());
//
//        mArcPaint = new Paint();
//        mArcPaint.setColor(arcColor);
//        mArcPaint.setAntiAlias(true);
//        mArcPaint.setStyle(Paint.Style.STROKE);
//        mArcPaint.setStrokeWidth(mArcWidth);
//
//        mProgressPaint = new Paint();
//        mProgressPaint.setColor(progressColor);
//        mProgressPaint.setAntiAlias(true);
//        mProgressPaint.setStyle(Paint.Style.STROKE);
//        mProgressPaint.setStrokeWidth(mProgressWidth);
//
//        mTextPaint = new Paint();
//        mTextPaint.setColor(textColor);
//        mTextPaint.setAntiAlias(true);
//        mTextPaint.setStyle(Paint.Style.FILL);
//        mTextPaint.setTextSize(mTextSize);
    }

//    @Override
//    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
//        final int min = Math.min(w, h);
//
//        // find circle's rectangle points
//        int alignLeft = (w - min) / 2;
//        int alignTop = (h - min) / 2;
//        int alignRight = alignLeft + min;
//        int alignBottom = alignTop + min;
//
//        // save circle coordinates
//        mCenterX = alignRight / 2 + (w - alignRight) / 2;
//        mCenterY = alignBottom / 2 + (h - alignBottom) / 2;
//
//
//        float progressDiameter = min - mPadding;
//        mCircleRadius = (int) (progressDiameter / 2);
//        float top = h / 2 - (progressDiameter / 2);
//        float left = w / 2 - (progressDiameter / 2);
//        mArcRect.set(left, top, left + progressDiameter, top + progressDiameter);
//
//        super.onSizeChanged(w, h, oldw, oldh);
//    }

    void printDebug(String text,Canvas canvas){
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setTextSize(25);
        canvas.drawText(text,10,getHeight() - 50,paint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // new height you want
        int newht = 150;
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
    protected void onDraw(Canvas canvas) {

        maxProgress = getWidth()-minThumbWidth;
        minProgress = minThumbWidth;
        viewHeight = (progressWidth * 2) + thumbSize;
        float maxProgressWidth = maxProgress - minProgress;
        progressStep = maxProgressWidth / 100;
        progressPosition = (currentProgress * progressStep) + minProgress;

//        printDebug(maxProgress+""+" -"+ minProgress+"",canvas);
//        if(isInEditMode()) {
//            Paint bg = new Paint();
//            bg.setColor(Color.BLACK);
//
////            bg.setShader(tempShader);
////            canvas.drawPaint(bg);
//            canvas.drawRect(new Rect(0,0,getWidth(),150),bg);
//        }
        mProgressPaint.setStrokeWidth(progressWidth);
        mProgressPaint.setStrokeCap(Paint.Cap.ROUND);
        if(gradientColors==null)
            mProgressPaint.setColor(progressColor);
        else
            mProgressPaint.setShader(getGradientShader());

        canvas.drawLine(offset,viewHeight,getWidth()-offset,viewHeight,mProgressPaint);
        if(gradientColors==null)
            mPaint.setColor(progressColor);
        else
            mPaint.setShader(getGradientShader());
        mPaint.setStrokeWidth(thumbStrokeWidth);
        mPaint.setStyle(Paint.Style.STROKE);
        float thumbCircle = thumbSize/2;
        canvas.drawCircle(progressPosition, (viewHeight/2)+thumbStrokeWidth, thumbCircle, mPaint);

        mPaintQuad.setStyle(Paint.Style.FILL);
        if(gradientColors==null)
            mPaintQuad.setColor(progressColor);
        else
            mPaintQuad.setShader(getGradientShader());
        float clipLineInside = (viewHeight - thumbStrokeWidth) + clipLineWidth;
        //move path to line and circle joint for the right clip
        float clipRightStart = progressPosition + thumbStrokeWidth;
        tempPath.moveTo(clipRightStart, clipLineInside);
        tempPath.quadTo(clipRightStart + (thumbStrokeWidth*2), thumbSize+(thumbStrokeWidth*2), (clipRightStart + (thumbStrokeWidth*2) + thumbStrokeWidth/2), thumbSize-thumbStrokeWidth);
        tempPath.quadTo(clipRightStart + (thumbStrokeWidth*2), thumbSize+(thumbStrokeWidth*3), clipRightStart + (thumbStrokeWidth*2) + (thumbStrokeWidth*3), clipLineInside);
        tempPath.lineTo(clipRightStart, clipLineInside);
        //move path to line and circle joint for the left clip
        float clipLeftStart = progressPosition - thumbStrokeWidth;
        tempPath2.moveTo(clipLeftStart, clipLineInside);
        tempPath2.quadTo(clipLeftStart - (thumbStrokeWidth*2), thumbSize+(thumbStrokeWidth*2), clipLeftStart - (thumbStrokeWidth*2) - (thumbStrokeWidth/2), thumbSize-thumbStrokeWidth);
        tempPath2.quadTo(clipLeftStart - (thumbStrokeWidth*2), thumbSize+(thumbStrokeWidth*3), clipLeftStart - (thumbStrokeWidth*2) - (thumbStrokeWidth*3), clipLineInside);
        tempPath2.lineTo(clipLeftStart, clipLineInside);
        canvas.drawPath(tempPath,mPaintQuad);
        canvas.drawPath(tempPath2,mPaintQuad);

        //
//        tempPath.moveTo(160, 91);
//        tempPath.quadTo(180, 70, 185, 50);
//        tempPath.quadTo(180, 80, 210, 91);
//        tempPath.lineTo(160, 91);
//        Path tempPath2 = new Path();
//        tempPath2.moveTo(140, 91);
//        tempPath2.quadTo(120, 70, 115, 50);
//        tempPath2.quadTo(120, 80, 90, 91);
//        tempPath2.lineTo(140, 91);
        //

        mThumbDrawable.setBounds((int) (progressPosition - mThumbSize / 2), (int) (thumbSize - mThumbSize / 2),
                (int) (progressPosition + mThumbSize / 2), (int) (thumbSize + mThumbSize / 2));
        mThumbDrawable.draw(canvas);
    }

//    private float valuePerDegree() {
//        return mMax / 360.0f;
//    }
//
//    /**
//     * Invoked when slider starts moving or is currently moving. This method calculates and sets position and angle of the thumb.
//     *
//     * @param touchX Where is the touch identifier now on X axis
//     * @param touchY Where is the touch identifier now on Y axis
//     */
//    private void updateProgressState(int touchX, int touchY) {
//        int distanceX = touchX - mCenterX;
//        int distanceY = mCenterY - touchY;
//        //noinspection SuspiciousNameCombination
//        double c = Math.sqrt(Math.pow(distanceX, 2) + Math.pow(distanceY, 2));
//        mAngle = Math.acos(distanceX / c);
//        if (distanceY < 0) {
//            mAngle = -mAngle;
//        }
//        mProgressSweep = (float) (90 - (mAngle * 180) / Math.PI);
//        if (mProgressSweep < 0) mProgressSweep += 360;
//        int progress = Math.round(mProgressSweep * valuePerDegree());
//        updateProgress(progress, true);
//    }
//
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
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
//
//    private void updateProgress(int progress, boolean fromUser) {
//
//        // detect points change closed to max or min
//        final int maxDetectValue = (int) ((double) mMax * 0.99);
//        final int minDetectValue = (int) ((double) mMax * 0.005) + mMin;
//
//        mUpdateTimes++;
//        if (progress == INVALID_VALUE) {
//            return;
//        }
//
//        // avoid accidentally touch to become max from original point
//        if (progress > maxDetectValue && mPreviousProgress == INVALID_VALUE) {
//            return;
//        }
//
//
//        // record previous and current progress change
//        if (mUpdateTimes == 1) {
//            mCurrentProgress = progress;
//        } else {
//            mPreviousProgress = mCurrentProgress;
//            mCurrentProgress = progress;
//        }
//
//        mProgressDisplay = progress - (progress % mStep);
//
//        /**
//         * Determine whether reach max or min to lock point update event.
//         *
//         * When reaching max, the progress will drop from max (or maxDetectPoints ~ max
//         * to min (or min ~ minDetectPoints) and vice versa.
//         *
//         * If reach max or min, stop increasing / decreasing to avoid exceeding the max / min.
//         */
//        if (mUpdateTimes > 1 && !isMin && !isMax) {
//            if (mPreviousProgress >= maxDetectValue && mCurrentProgress <= minDetectValue &&
//                    mPreviousProgress > mCurrentProgress) {
//                isMax = true;
//                progress = mMax;
//                mProgressDisplay = mMax;
//                mProgressSweep = 360;
//                if (mOnSeekBarChangeListener != null) {
//                    mOnSeekBarChangeListener.onPointsChanged(this, progress, fromUser);
//                }
//                invalidate();
//            } else if ((mCurrentProgress >= maxDetectValue
//                    && mPreviousProgress <= minDetectValue
//                    && mCurrentProgress > mPreviousProgress) || mCurrentProgress <= mMin) {
//                isMin = true;
//                progress = mMin;
//                mProgressDisplay = mMin;
//                mProgressSweep = mMin / valuePerDegree();
//                if (mOnSeekBarChangeListener != null) {
//                    mOnSeekBarChangeListener.onPointsChanged(this, progress, fromUser);
//                }
//                invalidate();
//            }
//        } else {
//
//            // Detect whether decreasing from max or increasing from min, to unlock the update event.
//            // Make sure to check in detect range only.
//            if (isMax & (mCurrentProgress < mPreviousProgress) && mCurrentProgress >= maxDetectValue) {
//                isMax = false;
//            }
//            if (isMin
//                    && (mPreviousProgress < mCurrentProgress)
//                    && mPreviousProgress <= minDetectValue && mCurrentProgress <= minDetectValue
//                    && mProgressDisplay >= mMin) {
//                isMin = false;
//            }
//        }
//
//        if (!isMax && !isMin) {
//            progress = (progress > mMax) ? mMax : progress;
//            progress = (progress < mMin) ? mMin : progress;
//
//            if (mOnSeekBarChangeListener != null) {
//                progress = progress - (progress % mStep);
//
//                mOnSeekBarChangeListener.onPointsChanged(this, progress, fromUser);
//            }
//            invalidate();
//        }
//    }
//
//    public void setSeekBarChangeListener(OnSeekBarChangedListener seekBarChangeListener) {
//        this.mOnSeekBarChangeListener = seekBarChangeListener;
//    }
//
//
//    public interface OnSeekBarChangedListener {
//        /**
//         * Notification that the point value has changed.
//         *
//         * @param circleSeekBar The CircleSeekBar view whose value has changed
//         * @param points        The current point value.
//         * @param fromUser      True if the point change was triggered by the user.
//         */
//        void onPointsChanged(DropletSeekBar circleSeekBar, int points, boolean fromUser);
//
//        void onStartTrackingTouch(DropletSeekBar circleSeekBar);
//
//        void onStopTrackingTouch(DropletSeekBar circleSeekBar);
//    }


    private Shader getGradientShader(){
        return new LinearGradient(0, 0, getWidth(), 0, gradientColors,null, Shader.TileMode.MIRROR);
    }

}

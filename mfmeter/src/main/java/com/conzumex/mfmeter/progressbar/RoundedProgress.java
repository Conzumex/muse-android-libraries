package com.conzumex.mfmeter.progressbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.conzumex.mfmeter.R;

public class RoundedProgress extends View {
    //Colors
    int backgroundColor = Color.parseColor("#cecece");
    int progressColor = Color.parseColor("#f7331e");
    int textColor = Color.parseColor("#000000");
    Paint mPaint;
    int parentViewWidth;
    int parentViewHeight;
    float progressWidth;
    int totalProgressPixels;
//    int progressPixelScaleFactor = 10;
    float radius = 20;
    int progressMin = 0;
    int progressMax = 100;
    int curProgress = 99;
    float textPaddingStart = 20;
    float textPaddingEnd = 20;
    float textSize = 25;
    int textMeasureWidth;
    int textMeasureHeight;
    Typeface fontFace = Typeface.DEFAULT_BOLD;
    TextFormatter valueFormatter;
    public RoundedProgress(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        mPaint = new Paint();

        if (attrs == null) return;
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.RoundedProgress);

        // Set text size from xml attributes (If exists and isn't the default value)
        float newTextSize = attributes.getDimension(R.styleable.RoundedProgress_rTextSize, -1);
        if (newTextSize != -1)
            textSize = newTextSize;

        // Set text padding from xml attributes (If exists and isn't the default value)
        float newTextStartPadding= attributes.getDimension(R.styleable.RoundedProgress_rTextPaddingStart, -1);
        if (newTextStartPadding != -1)
            textPaddingStart = newTextStartPadding;

        // Set text padding from xml attributes (If exists and isn't the default value)
        float newTextEndPadding= attributes.getDimension(R.styleable.RoundedProgress_rTextPaddingEnd, -1);
        if (newTextEndPadding != -1)
            textPaddingEnd = newTextEndPadding;

        // Set corner radius from xml attributes (If exists and isn't the default value)
        float newCornerRadius= attributes.getDimension(R.styleable.RoundedProgress_rCornerRadius, -1);
        if (newCornerRadius != -1)
            radius = newCornerRadius;

        // Set progress bar text color via xml (If exists and isn't the default value)
        @ColorInt int newTextColor = attributes.getColor(R.styleable.RoundedProgress_rTextColor, -1);
        if (newTextColor != -1) textColor = newTextColor;

        // Set progress bar text color via xml (If exists and isn't the default value)
        @ColorInt int newProgressColor = attributes.getColor(R.styleable.RoundedProgress_rProgressColor, -1);
        if (newProgressColor != -1) progressColor = newProgressColor;

        // Set progress bar text color via xml (If exists and isn't the default value)
        @ColorInt int newBackgroundColor = attributes.getColor(R.styleable.RoundedProgress_rBackgroundColor, -1);
        if (newBackgroundColor != -1) backgroundColor = newBackgroundColor;

        // Set a custom font family via its reference
        int fontId = attributes.getResourceId(R.styleable.RoundedProgress_rFontFamily,-1);
        if (fontId != -1) {
            fontFace = ResourcesCompat.getFont(context,fontId);
        }

        // Set Progress value from xml attributes (If exists and isn't the default value)
        int progressVal = attributes.getInteger(R.styleable.RoundedProgress_rProgress, -1);
        if (progressVal != -1) curProgress = progressVal;

        valueFormatter = progress -> progress+"%";

    }

    public RoundedProgress(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs);
    }

    public RoundedProgress(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        parentViewWidth = getWidth();
        parentViewHeight = getHeight();
        totalProgressPixels = parentViewWidth;
        progressWidth = parentViewWidth;
        progressWidth = parentViewWidth - textPaddingStart - getTextWidth(valueFormatter.getText(curProgress)) - textPaddingEnd;
//        //todo make temporary height when developing
//        parentViewHeight = 200;
        mPaint.setColor(backgroundColor);
        RectF progressBackground = new RectF(0,0,parentViewWidth,parentViewHeight);
        canvas.drawRoundRect(progressBackground,radius,radius,mPaint);
        mPaint.setColor(progressColor);
        float progressEnd = getProgressEndX();
        RectF progressItem = new RectF(0,0,progressEnd,parentViewHeight);
        canvas.drawRoundRect(progressItem,radius,radius,mPaint);

        mPaint.setTypeface(fontFace);
        mPaint.setTextSize(textSize);
        mPaint.setColor(textColor);
        canvas.drawText(valueFormatter.getText(curProgress),progressEnd + textPaddingStart,(parentViewHeight/2)+(textMeasureHeight/2),mPaint);
    }

    float getProgressEndX(){
        float tempProgress = curProgress;
        if(curProgress<progressMin){
            tempProgress = progressMin;
        }else if(curProgress>progressMax){
            tempProgress = progressMax;
        }
        if(tempProgress<(radius/10)){
            tempProgress =  radius/10;
        }
        tempProgress = tempProgress*((float)progressWidth/100);

        return tempProgress;
    }

    int getTextWidth(String text){
        Paint paint = new Paint();
        Rect bounds = new Rect();

        textMeasureWidth = 0;
        textMeasureHeight = 0;

        paint.setTypeface(fontFace);// your preference here
        paint.setTextSize(textSize);// have this the same as your text size

        paint.getTextBounds(text, 0, text.length(), bounds);

        textMeasureHeight =  bounds.height();
        textMeasureWidth =  bounds.width();
        return textMeasureWidth;
    }

    public void setValueFormatter(TextFormatter formatter){
        this.valueFormatter = formatter;
    }

    public interface TextFormatter{
        String getText(int progress);
    }
}

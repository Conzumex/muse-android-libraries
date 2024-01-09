package com.conzumex.charts.charts;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;

import com.conzumex.charts.R;
import com.conzumex.charts.animation.ChartAnimator;
import com.conzumex.charts.data.CandleData;
import com.conzumex.charts.data.CandleEntry;
import com.conzumex.charts.highlight.Highlight;
import com.conzumex.charts.interfaces.dataprovider.CandleDataProvider;
import com.conzumex.charts.interfaces.datasets.ICandleDataSet;
import com.conzumex.charts.renderer.CandleStickChartRenderer;
import com.conzumex.charts.utils.ColorTemplate;
import com.conzumex.charts.utils.Transformer;
import com.conzumex.charts.utils.ViewPortHandler;

public class RoundedCandleChart extends CandleStickChart {
    public RoundedCandleChart(Context context) {
        super(context);
    }

    public RoundedCandleChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        readRadiusAttr(context, attrs);
    }

    public RoundedCandleChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        readRadiusAttr(context, attrs);
    }

    private void readRadiusAttr(Context context, AttributeSet attrs){
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.RoundedCandleChart, 0, 0);
        try {
            int topRadius = a.getDimensionPixelSize(R.styleable.RoundedCandleChart_candleTopRadius, 0);
            int bottomRadius = a.getDimensionPixelSize(R.styleable.RoundedCandleChart_candleBottomRadius, 0);
            int radius = a.getDimensionPixelSize(R.styleable.RoundedCandleChart_candleRadius, 0);
            if(radius!=0)
                setRadius(radius);
            else
                setRadius(topRadius,bottomRadius);
        } finally {
            a.recycle();
        }
    }

    public void setRadius(int radius) {
        setRenderer(new RoundedCandleStickChartRenderer(this, getAnimator(), getViewPortHandler(), radius));
    }

    public void setRadius(int topRadius, int bottomRadius) {
        setRenderer(new RoundedCandleStickChartRenderer(this, getAnimator(), getViewPortHandler(), topRadius, bottomRadius));
    }

    private class RoundedCandleStickChartRenderer extends CandleStickChartRenderer {
        private float[] mShadowBuffers = new float[8];
        private float[] mBodyBuffers = new float[4];
        private float[] mRangeBuffers = new float[4];
        private float[] mOpenBuffers = new float[4];
        private float[] mCloseBuffers = new float[4];
        float[] corners = new float[]{
                    0, 0,        // Top left radius in px
                    0, 0,        // Top right radius in px
                    0, 0,          // Bottom right radius in px
                    0, 0           // Bottom left radius in px
        };

        float mRadius = 15;

        RoundedCandleStickChartRenderer(CandleDataProvider chart, ChartAnimator animator, ViewPortHandler viewPortHandler, int mRadius) {
            super(chart, animator, viewPortHandler);
            corners[0]  = mRadius;
            corners[1]  = mRadius;
            corners[2]  = mRadius;
            corners[3]  = mRadius;
            corners[4]  = mRadius;
            corners[5]  = mRadius;
            corners[6]  = mRadius;
            corners[7]  = mRadius;
        }

        RoundedCandleStickChartRenderer(CandleDataProvider chart, ChartAnimator animator, ViewPortHandler viewPortHandler, int topRadius, int bottomRadius) {
            super(chart, animator, viewPortHandler);
            corners[0]  = topRadius;
            corners[1]  = topRadius;
            corners[2]  = topRadius;
            corners[3]  = topRadius;
            corners[4]  = bottomRadius;
            corners[5]  = bottomRadius;
            corners[6]  = bottomRadius;
            corners[7]  = bottomRadius;
        }

        @Override
        public void drawHighlighted(Canvas c, Highlight[] indices) {
            CandleData candleData = mChart.getCandleData();

            for (Highlight high : indices) {

                ICandleDataSet set = candleData.getDataSetByIndex(high.getDataSetIndex());

                if (set == null || !set.isHighlightEnabled())
                    continue;

                CandleEntry e = set.getEntryForXValue(high.getX(), high.getY());

                if (!isInBoundsX(e, set))
                    continue;

                Transformer trans = mChart.getTransformer(set.getAxisDependency());

                mHighlightPaint.setColor(set.getHighLightColor());


                boolean isStack = false;

                final float y1;
                final float y2;


                    y1 = e.getY();
                    y2 = 0.f;


//                prepareBarHighlight(e.getX(), y1, y2, barData.getBarWidth() / 2f, trans);
//
//                setHighlightDrawPos(high, mBarRect);
//                Path path = new Path();
//                path.addRoundRect(mBarRect, corners, Path.Direction.CW);
//                c.drawPath(path, mHighlightPaint);
//                c.drawRoundRect(mBarRect, mRadius, mRadius, mHighlightPaint);
            }
        }

        @Override
        protected void drawDataSet(Canvas c, ICandleDataSet dataSet) {

            Transformer trans = mChart.getTransformer(dataSet.getAxisDependency());

            float phaseY = mAnimator.getPhaseY();
            float barSpace = dataSet.getBarSpace();
            boolean showCandleBar = dataSet.getShowCandleBar();

            mXBounds.set(mChart, dataSet);

            mRenderPaint.setStrokeWidth(dataSet.getShadowWidth());

            // draw the body
            for (int j = mXBounds.min; j <= mXBounds.range + mXBounds.min; j++) {

                // get the entry
                CandleEntry e = dataSet.getEntryForIndex(j);

                if (e == null)
                    continue;

                final float xPos = e.getX();

                final float open = e.getOpen();
                final float close = e.getClose();
                final float high = e.getHigh();
                final float low = e.getLow();

                if (showCandleBar) {
                    // calculate the shadow

                    mShadowBuffers[0] = xPos;
                    mShadowBuffers[2] = xPos;
                    mShadowBuffers[4] = xPos;
                    mShadowBuffers[6] = xPos;

                    if (open > close) {
                        mShadowBuffers[1] = high * phaseY;
                        mShadowBuffers[3] = open * phaseY;
                        mShadowBuffers[5] = low * phaseY;
                        mShadowBuffers[7] = close * phaseY;
                    } else if (open < close) {
                        mShadowBuffers[1] = high * phaseY;
                        mShadowBuffers[3] = close * phaseY;
                        mShadowBuffers[5] = low * phaseY;
                        mShadowBuffers[7] = open * phaseY;
                    } else {
                        mShadowBuffers[1] = high * phaseY;
                        mShadowBuffers[3] = open * phaseY;
                        mShadowBuffers[5] = low * phaseY;
                        mShadowBuffers[7] = mShadowBuffers[3];
                    }

                    trans.pointValuesToPixel(mShadowBuffers);

                    // draw the shadows

                    if (dataSet.getShadowColorSameAsCandle()) {

                        if (open > close)
                            mRenderPaint.setColor(
                                    dataSet.getDecreasingColor() == ColorTemplate.COLOR_NONE ?
                                            dataSet.getColor(j) :
                                            dataSet.getDecreasingColor()
                            );

                        else if (open < close)
                            mRenderPaint.setColor(
                                    dataSet.getIncreasingColor() == ColorTemplate.COLOR_NONE ?
                                            dataSet.getColor(j) :
                                            dataSet.getIncreasingColor()
                            );

                        else
                            mRenderPaint.setColor(
                                    dataSet.getNeutralColor() == ColorTemplate.COLOR_NONE ?
                                            dataSet.getColor(j) :
                                            dataSet.getNeutralColor()
                            );

                    } else {
                        mRenderPaint.setColor(
                                dataSet.getShadowColor() == ColorTemplate.COLOR_NONE ?
                                        dataSet.getColor(j) :
                                        dataSet.getShadowColor()
                        );
                    }

                    mRenderPaint.setStyle(Paint.Style.STROKE);

                    c.drawLines(mShadowBuffers, mRenderPaint);

                    // calculate the body

                    mBodyBuffers[0] = xPos - 0.5f + barSpace;
                    mBodyBuffers[1] = close * phaseY;
                    mBodyBuffers[2] = (xPos + 0.5f - barSpace);
                    mBodyBuffers[3] = open * phaseY;

                    trans.pointValuesToPixel(mBodyBuffers);

                    // draw body differently for increasing and decreasing entry
                    if (open > close) { // decreasing

                        if (dataSet.getDecreasingColor() == ColorTemplate.COLOR_NONE) {
                            mRenderPaint.setColor(dataSet.getColor(j));
                        } else {
                            mRenderPaint.setColor(dataSet.getDecreasingColor());
                        }

                        mRenderPaint.setStyle(dataSet.getDecreasingPaintStyle());

                        Log.d(LOG_TAG,"Custom tag 1");
                        c.drawRect(
                                mBodyBuffers[0], mBodyBuffers[3],
                                mBodyBuffers[2], mBodyBuffers[1],
                                mRenderPaint);

                    } else if (open < close) {

                        if (dataSet.getIncreasingColor() == ColorTemplate.COLOR_NONE) {
                            mRenderPaint.setColor(dataSet.getColor(j));
                        } else {
                            mRenderPaint.setColor(dataSet.getIncreasingColor());
                        }

                        mRenderPaint.setStyle(dataSet.getIncreasingPaintStyle());
                        Log.d(LOG_TAG,"Custom tag 2");
//                        c.drawRect(
//                                mBodyBuffers[0], mBodyBuffers[1],
//                                mBodyBuffers[2], mBodyBuffers[3],
//                                mRenderPaint);

//                        c.drawRoundRect(mBodyBuffers[0], mBodyBuffers[1],
//                                mBodyBuffers[2], mBodyBuffers[3], mRadius, mRadius, mRenderPaint);

                        Path path = new Path();
                        RectF rect = new RectF(mBodyBuffers[0], mBodyBuffers[1], mBodyBuffers[2],mBodyBuffers[3]);
                        path.addRoundRect(rect, corners, Path.Direction.CW);
//                        mRenderPaint.setColor(Color.parseColor("#ffcccc"));
                        c.drawPath(path, mRenderPaint);
                    } else { // equal values

                        if (dataSet.getNeutralColor() == ColorTemplate.COLOR_NONE) {
                            mRenderPaint.setColor(dataSet.getColor(j));
                        } else {
                            mRenderPaint.setColor(dataSet.getNeutralColor());
                        }

                        Log.d(LOG_TAG,"Custom tag 3");
                        c.drawLine(
                                mBodyBuffers[0], mBodyBuffers[1],
                                mBodyBuffers[2], mBodyBuffers[3],
                                mRenderPaint);
                    }
                } else {

                    mRangeBuffers[0] = xPos;
                    mRangeBuffers[1] = high * phaseY;
                    mRangeBuffers[2] = xPos;
                    mRangeBuffers[3] = low * phaseY;

                    mOpenBuffers[0] = xPos - 0.5f + barSpace;
                    mOpenBuffers[1] = open * phaseY;
                    mOpenBuffers[2] = xPos;
                    mOpenBuffers[3] = open * phaseY;

                    mCloseBuffers[0] = xPos + 0.5f - barSpace;
                    mCloseBuffers[1] = close * phaseY;
                    mCloseBuffers[2] = xPos;
                    mCloseBuffers[3] = close * phaseY;

                    trans.pointValuesToPixel(mRangeBuffers);
                    trans.pointValuesToPixel(mOpenBuffers);
                    trans.pointValuesToPixel(mCloseBuffers);

                    // draw the ranges
                    int barColor;

                    if (open > close)
                        barColor = dataSet.getDecreasingColor() == ColorTemplate.COLOR_NONE
                                ? dataSet.getColor(j)
                                : dataSet.getDecreasingColor();
                    else if (open < close)
                        barColor = dataSet.getIncreasingColor() == ColorTemplate.COLOR_NONE
                                ? dataSet.getColor(j)
                                : dataSet.getIncreasingColor();
                    else
                        barColor = dataSet.getNeutralColor() == ColorTemplate.COLOR_NONE
                                ? dataSet.getColor(j)
                                : dataSet.getNeutralColor();

                    mRenderPaint.setColor(barColor);
                    c.drawLine(
                            mRangeBuffers[0], mRangeBuffers[1],
                            mRangeBuffers[2], mRangeBuffers[3],
                            mRenderPaint);
                    c.drawLine(
                            mOpenBuffers[0], mOpenBuffers[1],
                            mOpenBuffers[2], mOpenBuffers[3],
                            mRenderPaint);
                    c.drawLine(
                            mCloseBuffers[0], mCloseBuffers[1],
                            mCloseBuffers[2], mCloseBuffers[3],
                            mRenderPaint);
                }
            }
        }

//        @Override
//        protected void drawDataSet(Canvas c, ICandleDataSet dataSet, int index) {
//            Transformer trans = mChart.getTransformer(dataSet.getAxisDependency());
//
//            mBarBorderPaint.setColor(dataSet.getBarBorderColor());
//            mBarBorderPaint.setStrokeWidth(Utils.convertDpToPixel(dataSet.getBarBorderWidth()));
//
//            final boolean drawBorder = dataSet.getBarBorderWidth() > 0.f;
//
//            float phaseX = mAnimator.getPhaseX();
//            float phaseY = mAnimator.getPhaseY();
//
//            // draw the bar shadow before the values
//            if (mChart.isDrawBarShadowEnabled()) {
//                mShadowPaint.setColor(dataSet.getBarShadowColor());
//
//                BarData barData = mChart.getBarData();
//
//                final float barWidth = barData.getBarWidth();
//                final float barWidthHalf = barWidth / 2.0f;
//                float x;
//
//                for (int i = 0, count = Math.min((int) (Math.ceil((float) (dataSet.getEntryCount()) * phaseX)), dataSet.getEntryCount());
//                     i < count;
//                     i++) {
//
//                    BarEntry e = dataSet.getEntryForIndex(i);
//
//                    x = e.getX();
//
//                    mBarShadowRectBuffer.left = x - barWidthHalf;
//                    mBarShadowRectBuffer.right = x + barWidthHalf;
//
//                    trans.rectValueToPixel(mBarShadowRectBuffer);
//
//                    if (!mViewPortHandler.isInBoundsLeft(mBarShadowRectBuffer.right))
//                        continue;
//
//                    if (!mViewPortHandler.isInBoundsRight(mBarShadowRectBuffer.left))
//                        break;
//
//                    mBarShadowRectBuffer.top = mViewPortHandler.contentTop();
//                    mBarShadowRectBuffer.bottom = mViewPortHandler.contentBottom();
//
//                    Path path = new Path();
//                    path.addRoundRect(mBarShadowRectBuffer, corners, Path.Direction.CW);
//                    c.drawPath(path, mShadowPaint);
//
////                    c.drawRoundRect(mBarShadowRectBuffer, mRadius, mRadius, mShadowPaint);
//                }
//            }
//
//            // initialize the buffer
//            BarBuffer buffer = mBarBuffers[index];
//            buffer.setPhases(phaseX, phaseY);
//            buffer.setDataSet(index);
//            buffer.setInverted(mChart.isInverted(dataSet.getAxisDependency()));
//            buffer.setBarWidth(mChart.getBarData().getBarWidth());
//
//            buffer.feed(dataSet);
//
//            trans.pointValuesToPixel(buffer.buffer);
//
//            final boolean isSingleColor = dataSet.getColors().size() == 1;
//
//            if (isSingleColor) {
//                mRenderPaint.setColor(dataSet.getColor());
//            }
//
//            for (int j = 0; j < buffer.size(); j += 4) {
//
//                if (!mViewPortHandler.isInBoundsLeft(buffer.buffer[j + 2]))
//                    continue;
//
//                if (!mViewPortHandler.isInBoundsRight(buffer.buffer[j]))
//                    break;
//
//                if (!isSingleColor) {
//                    // Set the color for the currently drawn value. If the index
//                    // is out of bounds, reuse colors.
//                    mRenderPaint.setColor(dataSet.getColor(j / 4));
//                }
//
//                BarDataSet mBarData = (BarDataSet) dataSet;
//                if (mBarData.getFills() != null) {
//                    List<Fill> gradientColors = mBarData.getFills();
//                    mRenderPaint.setShader(
//                            new LinearGradient(
//                                    buffer.buffer[j],
//                                    buffer.buffer[j + 3],
//                                    buffer.buffer[j],
//                                    buffer.buffer[j + 1],
//                                    gradientColors.get(0).getGradientColors()[0],
//                                    gradientColors.get(0).getGradientColors()[1],
//                                    android.graphics.Shader.TileMode.MIRROR));
//                }
//
//
//
//                Path path = new Path();
//                RectF rect = new RectF(buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2],buffer.buffer[j + 3]);
//                path.addRoundRect(rect, corners, Path.Direction.CW);
//                c.drawPath(path, mRenderPaint);
//
////                c.drawRoundRect(buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2],
////                        buffer.buffer[j + 3], mRadius, mRadius, mRenderPaint);
//
//                if (drawBorder) {
////                    c.drawRoundRect(buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2],
////                            buffer.buffer[j + 3], mRadius, mRadius, mBarBorderPaint);
//
//                    c.drawPath(path, mBarBorderPaint);
//                }
//            }
//        }
    }
}

package com.conzumex.charts.charts;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;

import com.conzumex.charts.R;
import com.conzumex.charts.animation.ChartAnimator;
import com.conzumex.charts.buffer.BarBuffer;
import com.conzumex.charts.data.BarData;
import com.conzumex.charts.data.BarDataSet;
import com.conzumex.charts.data.BarEntry;
import com.conzumex.charts.highlight.Highlight;
import com.conzumex.charts.highlight.Range;
import com.conzumex.charts.interfaces.dataprovider.BarDataProvider;
import com.conzumex.charts.interfaces.datasets.IBarDataSet;
import com.conzumex.charts.renderer.BarChartRenderer;
import com.conzumex.charts.utils.Fill;
import com.conzumex.charts.utils.Transformer;
import com.conzumex.charts.utils.Utils;
import com.conzumex.charts.utils.ViewPortHandler;

import java.util.List;

public class RoundedBarChart extends BarChart {
    public RoundedBarChart(Context context) {
        super(context);
    }

    public RoundedBarChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        readRadiusAttr(context, attrs);
    }

    public RoundedBarChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        readRadiusAttr(context, attrs);
    }

    private void readRadiusAttr(Context context, AttributeSet attrs){
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.RoundedBarChart, 0, 0);
        try {
            int topRadius = a.getDimensionPixelSize(R.styleable.RoundedBarChart_top_radius, 0);
            int bottomRadius = a.getDimensionPixelSize(R.styleable.RoundedBarChart_bottom_radius, 0);
            int radius = a.getDimensionPixelSize(R.styleable.RoundedBarChart_radius, 0);
            if(radius!=0)
                setRadius(radius);
            else
                setRadius(topRadius,bottomRadius);
        } finally {
            a.recycle();
        }
    }

    public void setRadius(int radius) {
        setRenderer(new RoundedBarChartRenderer(this, getAnimator(), getViewPortHandler(), radius));
    }

    public void setRadius(int topRadius, int bottomRadius) {
        setRenderer(new RoundedBarChartRenderer(this, getAnimator(), getViewPortHandler(), topRadius, bottomRadius));
    }

    public class RoundedBarChartRenderer extends BarChartRenderer {
        private RectF mBarShadowRectBuffer = new RectF();
        float[] corners = new float[]{
                0, 0,        // Top left radius in px
                0, 0,        // Top right radius in px
                0, 0,          // Bottom right radius in px
                0, 0           // Bottom left radius in px
        };

        RoundedBarChartRenderer(BarDataProvider chart, ChartAnimator animator, ViewPortHandler viewPortHandler, int mRadius) {
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

        RoundedBarChartRenderer(BarDataProvider chart, ChartAnimator animator, ViewPortHandler viewPortHandler, int topRadius, int bottomRadius) {
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
            BarData barData = mChart.getBarData();

            for (Highlight high : indices) {

                IBarDataSet set = barData.getDataSetByIndex(high.getDataSetIndex());

                if (set == null || !set.isHighlightEnabled())
                    continue;

                BarEntry e = set.getEntryForXValue(high.getX(), high.getY());

                if (!isInBoundsX(e, set))
                    continue;

                Transformer trans = mChart.getTransformer(set.getAxisDependency());

                mHighlightPaint.setColor(set.getHighLightColor());
                mHighlightPaint.setAlpha(set.getHighLightAlpha());

                boolean isStack = high.getStackIndex() >= 0 && e.isStacked();

                final float y1;
                final float y2;

                if (isStack) {

                    if (mChart.isHighlightFullBarEnabled()) {

                        y1 = e.getPositiveSum();
                        y2 = -e.getNegativeSum();

                    } else {

                        Range range = e.getRanges()[high.getStackIndex()];

                        y1 = range.from;
                        y2 = range.to;
                    }

                } else {
                    y1 = e.getY();
                    y2 = 0.f;
                }

                prepareBarHighlight(e.getX(), y1, y2, barData.getBarWidth() / 2f, trans);

                setHighlightDrawPos(high, mBarRect);
                Path path = new Path();
                path.addRoundRect(mBarRect, corners, Path.Direction.CW);
                c.drawPath(path, mHighlightPaint);
//                c.drawRoundRect(mBarRect, mRadius, mRadius, mHighlightPaint);
            }
        }

        @Override
        protected void drawDataSet(Canvas c, IBarDataSet dataSet, int index) {
            Transformer trans = mChart.getTransformer(dataSet.getAxisDependency());

            mBarBorderPaint.setColor(dataSet.getBarBorderColor());
            mBarBorderPaint.setStrokeWidth(Utils.convertDpToPixel(dataSet.getBarBorderWidth()));

            final boolean drawBorder = dataSet.getBarBorderWidth() > 0.f;

            float phaseX = mAnimator.getPhaseX();
            float phaseY = mAnimator.getPhaseY();

            // draw the bar shadow before the values
            if (mChart.isDrawBarShadowEnabled()) {
                mShadowPaint.setColor(dataSet.getBarShadowColor());

                BarData barData = mChart.getBarData();

                final float barWidth = barData.getBarWidth();
                final float barWidthHalf = barWidth / 2.0f;
                float x;

                for (int i = 0, count = Math.min((int) (Math.ceil((float) (dataSet.getEntryCount()) * phaseX)), dataSet.getEntryCount());
                     i < count;
                     i++) {

                    BarEntry e = dataSet.getEntryForIndex(i);

                    x = e.getX();

                    mBarShadowRectBuffer.left = x - barWidthHalf;
                    mBarShadowRectBuffer.right = x + barWidthHalf;

                    trans.rectValueToPixel(mBarShadowRectBuffer);

                    if (!mViewPortHandler.isInBoundsLeft(mBarShadowRectBuffer.right))
                        continue;

                    if (!mViewPortHandler.isInBoundsRight(mBarShadowRectBuffer.left))
                        break;

                    mBarShadowRectBuffer.top = mViewPortHandler.contentTop();
                    mBarShadowRectBuffer.bottom = mViewPortHandler.contentBottom();

                    Path path = new Path();
                    path.addRoundRect(mBarShadowRectBuffer, corners, Path.Direction.CW);
                    c.drawPath(path, mShadowPaint);

//                    c.drawRoundRect(mBarShadowRectBuffer, mRadius, mRadius, mShadowPaint);
                }
            }

            // initialize the buffer
            BarBuffer buffer = mBarBuffers[index];
            buffer.setPhases(phaseX, phaseY);
            buffer.setDataSet(index);
            buffer.setInverted(mChart.isInverted(dataSet.getAxisDependency()));
            buffer.setBarWidth(mChart.getBarData().getBarWidth());

            buffer.feed(dataSet);

            trans.pointValuesToPixel(buffer.buffer);

            final boolean isSingleColor = dataSet.getColors().size() == 1;

            if (isSingleColor) {
                mRenderPaint.setColor(dataSet.getColor());
            }

            for (int j = 0; j < buffer.size(); j += 4) {

                if (!mViewPortHandler.isInBoundsLeft(buffer.buffer[j + 2]))
                    continue;

                if (!mViewPortHandler.isInBoundsRight(buffer.buffer[j]))
                    break;

                if (!isSingleColor) {
                    // Set the color for the currently drawn value. If the index
                    // is out of bounds, reuse colors.
                    mRenderPaint.setColor(dataSet.getColor(j / 4));
                }

                BarDataSet mBarData = (BarDataSet) dataSet;
                if (mBarData.getFills() != null) {
                    List<Fill> gradientColors = mBarData.getFills();
                    mRenderPaint.setShader(
                            new LinearGradient(
                                    buffer.buffer[j],
                                    buffer.buffer[j + 3],
                                    buffer.buffer[j],
                                    buffer.buffer[j + 1],
                                    gradientColors.get(0).getGradientColors()[0],
                                    gradientColors.get(0).getGradientColors()[1],
                                    android.graphics.Shader.TileMode.MIRROR));
                }



                Path path = new Path();
                RectF rect = new RectF(buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2],buffer.buffer[j + 3]);
                path.addRoundRect(rect, corners, Path.Direction.CW);
                c.drawPath(path, mRenderPaint);

//                c.drawRoundRect(buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2],
//                        buffer.buffer[j + 3], mRadius, mRadius, mRenderPaint);

                if (drawBorder) {
//                    c.drawRoundRect(buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2],
//                            buffer.buffer[j + 3], mRadius, mRadius, mBarBorderPaint);

                    c.drawPath(path, mBarBorderPaint);
                }
            }
        }
    }
}

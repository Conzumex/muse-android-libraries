package com.conzumex.charts.renderer;

import android.graphics.Canvas;
import android.util.Log;

import com.conzumex.charts.animation.ChartAnimator;
import com.conzumex.charts.charts.Chart;
import com.conzumex.charts.charts.RoundedBarChart;
import com.conzumex.charts.charts.RoundedCombinedChart.DrawOrder;
import com.conzumex.charts.charts.RoundedCombinedChart;
import com.conzumex.charts.data.ChartData;
import com.conzumex.charts.data.CombinedData;
import com.conzumex.charts.highlight.Highlight;
import com.conzumex.charts.utils.ViewPortHandler;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Renderer class that is responsible for rendering multiple different data-types.
 */
public class RoundedCombinedChartRenderer extends DataRenderer {

    /**
     * all rederers for the different kinds of data this combined-renderer can draw
     */
    protected List<DataRenderer> mRenderers = new ArrayList<DataRenderer>(5);

    protected WeakReference<Chart> mChart;
    float[] corners;
    public RoundedCombinedChartRenderer(RoundedCombinedChart chart, ChartAnimator animator, ViewPortHandler viewPortHandler,float[] corners) {
        super(animator, viewPortHandler);
        mChart = new WeakReference<Chart>(chart);
        createRenderers();
        this.corners = corners;
    }

    /**
     * Creates the renderers needed for this combined-renderer in the required order. Also takes the DrawOrder into
     * consideration.
     */
    public void createRenderers() {

        mRenderers.clear();

        RoundedCombinedChart chart = (RoundedCombinedChart)mChart.get();
        if (chart == null)
            return;
        Log.d("RoundCombined","corners "+corners==null?"is null":"not null");
        float[] corners = chart.getCorners();

        DrawOrder[] orders = chart.getDrawOrder();

        for (DrawOrder order : orders) {

            switch (order) {
                case BAR:
                    if (chart.getBarData() != null)
                        mRenderers.add(new BarChartRenderer(chart, mAnimator, mViewPortHandler));
                    break;
                case BUBBLE:
                    if (chart.getBubbleData() != null)
                        mRenderers.add(new BubbleChartRenderer(chart, mAnimator, mViewPortHandler));
                    break;
                case LINE:
                    if (chart.getLineData() != null)
                        mRenderers.add(new LineChartRenderer(chart, mAnimator, mViewPortHandler));
                    break;
                case CANDLE:
                    if (chart.getCandleData() != null)
                        mRenderers.add(new RoundedCandleStickChartRenderer(chart, mAnimator, mViewPortHandler, corners));
                    break;
                case SCATTER:
                    if (chart.getScatterData() != null)
                        mRenderers.add(new ScatterChartRenderer(chart, mAnimator, mViewPortHandler));
                    break;
            }
        }
    }

    @Override
    public void initBuffers() {

        for (DataRenderer renderer : mRenderers)
            renderer.initBuffers();
    }

    @Override
    public void drawData(Canvas c) {

        for (DataRenderer renderer : mRenderers)
            renderer.drawData(c);
    }

    @Override
    public void drawValues(Canvas c) {

        for (DataRenderer renderer : mRenderers)
            renderer.drawValues(c);
    }

    @Override
    public void drawExtras(Canvas c) {

        for (DataRenderer renderer : mRenderers)
            renderer.drawExtras(c);
    }

    protected List<Highlight> mHighlightBuffer = new ArrayList<Highlight>();

    @Override
    public void drawHighlighted(Canvas c, Highlight[] indices) {

        Chart chart = mChart.get();
        if (chart == null) return;

        for (DataRenderer renderer : mRenderers) {
            ChartData data = null;

            if (renderer instanceof BarChartRenderer)
                data = ((BarChartRenderer)renderer).mChart.getBarData();
            else if (renderer instanceof RoundedBarChart.RoundedBarChartRenderer)
                data = ((RoundedBarChart.RoundedBarChartRenderer)renderer).mChart.getBarData();
            else if (renderer instanceof LineChartRenderer)
                data = ((LineChartRenderer)renderer).mChart.getLineData();
            else if (renderer instanceof CandleStickChartRenderer)
                data = ((CandleStickChartRenderer)renderer).mChart.getCandleData();
            else  if (renderer instanceof RoundedCandleStickChartRenderer)
                data = ((RoundedCandleStickChartRenderer)renderer).mChart.getCandleData();
            else if (renderer instanceof ScatterChartRenderer)
                data = ((ScatterChartRenderer)renderer).mChart.getScatterData();
            else if (renderer instanceof BubbleChartRenderer)
                data = ((BubbleChartRenderer)renderer).mChart.getBubbleData();

            int dataIndex = data == null ? -1
                    : ((CombinedData)chart.getData()).getAllData().indexOf(data);

            mHighlightBuffer.clear();

            for (Highlight h : indices) {
                if (h.getDataIndex() == dataIndex || h.getDataIndex() == -1)
                    mHighlightBuffer.add(h);
            }

            renderer.drawHighlighted(c, mHighlightBuffer.toArray(new Highlight[mHighlightBuffer.size()]));
        }
    }

    /**
     * Returns the sub-renderer object at the specified index.
     *
     * @param index
     * @return
     */
    public DataRenderer getSubRenderer(int index) {
        if (index >= mRenderers.size() || index < 0)
            return null;
        else
            return mRenderers.get(index);
    }

    /**
     * Returns all sub-renderers.
     *
     * @return
     */
    public List<DataRenderer> getSubRenderers() {
        return mRenderers;
    }

    public void setSubRenderers(List<DataRenderer> renderers) {
        this.mRenderers = renderers;
    }
}

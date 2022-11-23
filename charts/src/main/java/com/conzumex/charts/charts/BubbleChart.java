
package com.conzumex.charts.charts;

import android.content.Context;
import android.util.AttributeSet;

import com.conzumex.charts.renderer.BubbleChartRenderer;
import com.conzumex.charts.data.BubbleData;
import com.conzumex.charts.interfaces.dataprovider.BubbleDataProvider;

public class BubbleChart extends BarLineChartBase<BubbleData> implements BubbleDataProvider {

    public BubbleChart(Context context) {
        super(context);
    }

    public BubbleChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BubbleChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void init() {
        super.init();

        mRenderer = new BubbleChartRenderer(this, mAnimator, mViewPortHandler);
    }

    public BubbleData getBubbleData() {
        return mData;
    }
}

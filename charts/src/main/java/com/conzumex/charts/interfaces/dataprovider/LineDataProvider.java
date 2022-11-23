package com.conzumex.charts.interfaces.dataprovider;

import com.conzumex.charts.data.LineData;
import com.conzumex.charts.components.YAxis;

public interface LineDataProvider extends BarLineScatterCandleBubbleDataProvider {

    LineData getLineData();

    YAxis getAxis(YAxis.AxisDependency dependency);
}

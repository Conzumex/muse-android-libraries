package com.conzumex.charts.interfaces.dataprovider;

import com.conzumex.charts.data.BarLineScatterCandleBubbleData;
import com.conzumex.charts.utils.Transformer;
import com.conzumex.charts.components.YAxis.AxisDependency;

public interface BarLineScatterCandleBubbleDataProvider extends ChartInterface {

    Transformer getTransformer(AxisDependency axis);
    boolean isInverted(AxisDependency axis);
    
    float getLowestVisibleX();
    float getHighestVisibleX();

    BarLineScatterCandleBubbleData getData();
}

package com.conzumex.charts.interfaces.dataprovider;

import com.conzumex.charts.data.ScatterData;

public interface ScatterDataProvider extends BarLineScatterCandleBubbleDataProvider {

    ScatterData getScatterData();
}

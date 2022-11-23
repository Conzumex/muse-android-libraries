package com.conzumex.charts.interfaces.dataprovider;

import com.conzumex.charts.data.CandleData;

public interface CandleDataProvider extends BarLineScatterCandleBubbleDataProvider {

    CandleData getCandleData();
}

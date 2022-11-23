package com.conzumex.charts.interfaces.dataprovider;

import com.conzumex.charts.data.BubbleData;

public interface BubbleDataProvider extends BarLineScatterCandleBubbleDataProvider {

    BubbleData getBubbleData();
}

package com.conzumex.clockrangepicker

interface BitmapCachedClockRenderer: ClockRenderer {
    var isBitmapCacheEnabled: Boolean

    fun invalidateBitmapCache()
    fun recycleBitmapCache()
}
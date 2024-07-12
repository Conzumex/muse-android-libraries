package com.conzumex.sticky.provider.interfaces

import androidx.annotation.StyleableRes

internal interface IResourceProvider {
    fun getResourcesByIds(@StyleableRes vararg styleResId: Int): Array<Int>
}
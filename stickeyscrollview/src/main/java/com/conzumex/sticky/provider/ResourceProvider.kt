package com.conzumex.sticky.provider

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.StyleableRes
import androidx.core.content.res.use
import com.conzumex.sticky.R
import com.conzumex.sticky.provider.interfaces.IResourceProvider

internal class ResourceProvider(private val context: Context, private val attrs: AttributeSet?, @StyleableRes private val styleRes: IntArray) :
    IResourceProvider {
    override fun getResourcesByIds(@StyleableRes vararg styleResIds: Int): Array<Int> {
        val a = if (R.styleable.StickyScrollView == styleRes) 5 else 10
        var tpArray = context.obtainStyledAttributes(attrs,R.styleable.StickyScrollView)
        val header = tpArray.getResourceId(R.styleable.StickyScrollView_stickyHeader,0)
        val footer = tpArray.getResourceId(R.styleable.StickyScrollView_stickyFooter,0)
        context.obtainStyledAttributes(attrs, styleRes).use { typedArray ->
            return mutableListOf<Int>().apply {
                styleResIds.forEach { styleResId -> add(typedArray.getResourceId(styleResId, 0)) }
            }.toTypedArray()
        }
    }
}
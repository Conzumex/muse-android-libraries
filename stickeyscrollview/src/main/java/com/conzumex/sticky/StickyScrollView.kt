package com.conzumex.sticky

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.IdRes
import androidx.core.widget.NestedScrollView
import com.conzumex.sticky.provider.ResourceProvider
import com.conzumex.sticky.provider.ScreenInfoProvider
import com.conzumex.sticky.provider.interfaces.IResourceProvider
import com.conzumex.sticky.provider.interfaces.IScreenInfoProvider
import com.conzumex.sticky.ui.PropertySetter
import com.conzumex.sticky.ui.interfaces.IScrollViewListener
import com.conzumex.sticky.ui.presentation.IStickyScrollPresentation
import com.conzumex.sticky.ui.presenter.StickyScrollPresenter
import android.view.ViewTreeObserver.OnGlobalLayoutListener as OnGlobalLayoutListener1

class StickyScrollView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : NestedScrollView(context, attributeSet, defStyleAttr) {
    private var scrollViewListener: IScrollViewListener? = null
    private var stickyFooterView: View? = null
    private var stickyHeaderView: View? = null
    private var mStickyScrollPresenter: StickyScrollPresenter
    var mScrollable = true

    init {
        var tpArray = context.obtainStyledAttributes(attributeSet,R.styleable.StickyScrollView)
        val header = tpArray.getResourceId(R.styleable.StickyScrollView_stickyHeader,0)
        val footer = tpArray.getResourceId(R.styleable.StickyScrollView_stickyFooter,0)

        val screenInfoProvider: IScreenInfoProvider = ScreenInfoProvider(context)
        val resourceProvider: IResourceProvider =
            ResourceProvider(context, attributeSet, R.styleable.StickyScrollView)
        mStickyScrollPresenter = StickyScrollPresenter(StickyScrollPresentation(), screenInfoProvider, resourceProvider)
        onLayoutUpdate {
//            mStickyScrollPresenter.onGlobalLayoutChange(
//                R.styleable.StickyScrollView_stickyHeader,
//                R.styleable.StickyScrollView_stickyFooter
//            )
            mStickyScrollPresenter.onGlobalLayoutChangeDirect(header,footer)
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        if (!changed) {
            mStickyScrollPresenter.recomputeFooterLocation(getFooterTop())
        }
        stickyHeaderView?.let {
            mStickyScrollPresenter.recomputeHeaderLocation(it.top)
        }
    }

    override fun onScrollChanged(mScrollX: Int, mScrollY: Int, oldX: Int, oldY: Int) {
        super.onScrollChanged(mScrollX, mScrollY, oldX, oldY)
        mStickyScrollPresenter.onScroll(mScrollY)
        scrollViewListener?.onScrollChanged(mScrollX, mScrollY, oldX, oldY)
    }

    override fun onOverScrolled(scrollX: Int, scrollY: Int, clampedX: Boolean, clampedY: Boolean) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY)
        scrollViewListener?.onScrollStopped(clampedY)
    }

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putParcelable(STATE_SUPER, super.onSaveInstanceState())
        bundle.putBoolean(STATE_SCROLL, mStickyScrollPresenter.mScrolled)
        bundle.putInt(STATE_NAV_BAR_HEIGHT, mStickyScrollPresenter.mNavigationBarInitialHeight)
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state is Bundle) {
            mStickyScrollPresenter.mNavigationBarInitialHeight = state.getInt(STATE_NAV_BAR_HEIGHT)
            mStickyScrollPresenter.mScrolled = state.getBoolean(STATE_SCROLL)
            super.onRestoreInstanceState(state.getParcelable(STATE_SUPER))
            return
        }
        super.onRestoreInstanceState(state)
    }

    val isFooterSticky: Boolean
        get() = mStickyScrollPresenter.isFooterSticky
    val isHeaderSticky: Boolean
        get() = mStickyScrollPresenter.isHeaderSticky

    fun setHeaderView(@IdRes id: Int) {
        stickyHeaderView = findViewById(id)
        stickyHeaderView?.onLayoutUpdate { initialiseHeader() }
    }

    fun setFooterView(@IdRes id: Int) {
        stickyFooterView = findViewById(id)
        stickyFooterView?.onLayoutUpdate { initialiseFooter() }
    }

    fun setScrollViewListener(scrollViewListener: IScrollViewListener) {
        this.scrollViewListener = scrollViewListener
    }

    private fun initialiseHeader(){
        mStickyScrollPresenter.initStickyHeader(stickyHeaderView?.top)
    }

    private fun initialiseFooter(){
        mStickyScrollPresenter.initStickyFooter(
            stickyFooterView?.measuredHeight,
            getFooterTop()
        )
    }

    private fun getRelativeTop(myView: View): Int {
        return if (myView.parent === myView.rootView) {
            myView.top
        } else {
            myView.top + getRelativeTop(myView.parent as View)
        }
    }

    private fun getFooterTop(): Int {
        return stickyFooterView?.let {
            return getRelativeTop(it) - it.topCutOutHeight()
        } ?: 0
    }

    private inner class StickyScrollPresentation: IStickyScrollPresentation {
        override val currentScrollYPos: Int
            get() = scrollY

        override fun freeHeader() {
            stickyHeaderView?.let {
                it.translationY = 0f
                PropertySetter.setTranslationZ(it, 0f)
            }
        }

        override fun freeFooter() {
            stickyFooterView?.translationY = 0f
        }

        override fun stickHeader(translationY: Int) {
            stickyHeaderView?.let {
                it.translationY = translationY.toFloat()
                PropertySetter.setTranslationZ(it, 1f)
            }
        }

        override fun stickFooter(translationY: Int) {
            stickyFooterView?.translationY = translationY.toFloat()
        }

        override fun initHeaderView(@IdRes id: Int) {
            stickyHeaderView = findViewById(id)
            stickyHeaderView?.post { initialiseHeader() }
        }

        override fun initFooterView(@IdRes id: Int) {
            stickyFooterView = findViewById(id)
            stickyFooterView?.post { initialiseFooter() }
        }
    }

    private fun View.topCutOutHeight(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            rootWindowInsets.displayCutout?.safeInsetTop ?: 0
        } else {
            0
        }
    }

    private fun View.onLayoutUpdate(action: () -> Unit) {
        viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener1 {
            override fun onGlobalLayout() {
                action.invoke()
                viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }

    fun setScrollable(mScroll: Boolean){
        mScrollable = mScroll
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if(ev.action == MotionEvent.ACTION_DOWN){
            return mScrollable && super.onTouchEvent(ev)
        }
        return super.onTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return mScrollable && super.onInterceptTouchEvent(ev)
    }

    companion object {
        private const val STATE_SCROLL = "scroll_state"
        private const val STATE_SUPER = "super_state"
        private const val STATE_NAV_BAR_HEIGHT = "nav_bar_height_state"
    }
}
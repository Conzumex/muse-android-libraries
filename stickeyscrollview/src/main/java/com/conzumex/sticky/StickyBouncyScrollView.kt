package com.conzumex.sticky

import android.animation.ObjectAnimator
import android.animation.TimeInterpolator
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
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

class StickyBouncyScrollView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : NestedScrollView(context, attributeSet, defStyleAttr) {

    private var scrollViewListener: IScrollViewListener? = null
    private var stickyFooterView: View? = null
    private var stickyHeaderView: View? = null
    private var mStickyScrollPresenter: StickyScrollPresenter
    var mScrollable = true

    // --- Bounce config ---
    // Maximum pixels the content can be dragged beyond the edge
    private val BOUNCE_MAX_DRAG_PX = 300f
    // How much resistance is applied as the user drags further (0..1, lower = more resistance)
    private val BOUNCE_DAMPING = 0.35f
    // Duration of the snap-back spring animation in ms
    private val BOUNCE_RETURN_DURATION = 400L
    private val BOUNCE_RETURN_INTERPOLATOR: TimeInterpolator = DecelerateInterpolator(1.5f)

    private var bounceTouchStartY = 0f
    private var isBouncing = false
    private var bounceReturnAnimator: ObjectAnimator? = null

    init {
        val tpArray = context.obtainStyledAttributes(attributeSet, R.styleable.StickyScrollView)
        val header = tpArray.getResourceId(R.styleable.StickyScrollView_stickyHeader, 0)
        val footer = tpArray.getResourceId(R.styleable.StickyScrollView_stickyFooter, 0)
        tpArray.recycle()

        val screenInfoProvider: IScreenInfoProvider = ScreenInfoProvider(context)
        val resourceProvider: IResourceProvider =
            ResourceProvider(context, attributeSet, R.styleable.StickyScrollView)

        mStickyScrollPresenter = StickyScrollPresenter(
            StickyScrollPresentation(), screenInfoProvider, resourceProvider
        )

        onLayoutUpdate {
            mStickyScrollPresenter.onGlobalLayoutChangeDirect(header, footer)
        }
    }

    // region — Lifecycle overrides

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
        return Bundle().apply {
            putParcelable(STATE_SUPER, super.onSaveInstanceState())
            putBoolean(STATE_SCROLL, mStickyScrollPresenter.mScrolled)
            putInt(STATE_NAV_BAR_HEIGHT, mStickyScrollPresenter.mNavigationBarInitialHeight)
        }
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

    // endregion

    // region — Bounce touch handling

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (!mScrollable) {
            return if (ev.action == MotionEvent.ACTION_DOWN) false
            else super.onTouchEvent(ev)
        }

        val child = getChildAt(0) ?: return super.onTouchEvent(ev)

        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                bounceTouchStartY = ev.y
                isBouncing = false
                bounceReturnAnimator?.cancel()
            }

            MotionEvent.ACTION_MOVE -> {
                val atTop = scrollY == 0
                val atBottom = scrollY >= child.measuredHeight - height
                val dragDelta = ev.y - bounceTouchStartY

                if ((atTop && dragDelta > 0) || (atBottom && dragDelta < 0)) {
                    // Apply damped translation instead of scrolling
                    val damped = dragDelta * BOUNCE_DAMPING
                    val clamped = damped.coerceIn(-BOUNCE_MAX_DRAG_PX, BOUNCE_MAX_DRAG_PX)
                    child.translationY = clamped
                    isBouncing = true
                    return true // consume the event — don't pass to NestedScrollView
                } else if (isBouncing) {
                    // Finger moved back into normal scroll territory — snap back first
                    snapBack(child)
                    isBouncing = false
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isBouncing) {
                    snapBack(child)
                    isBouncing = false
                }
            }
        }

        return super.onTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return mScrollable && super.onInterceptTouchEvent(ev)
    }

    /** Animate the content back to its resting translationY of 0. */
    private fun snapBack(child: View) {
        bounceReturnAnimator?.cancel()
        bounceReturnAnimator = ObjectAnimator.ofFloat(child, View.TRANSLATION_Y, 0f).apply {
            duration = BOUNCE_RETURN_DURATION
            interpolator = BOUNCE_RETURN_INTERPOLATOR
            start()
        }
    }

    // endregion

    // region — Public API

    val isFooterSticky: Boolean get() = mStickyScrollPresenter.isFooterSticky
    val isHeaderSticky: Boolean get() = mStickyScrollPresenter.isHeaderSticky

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

    fun setScrollable(mScroll: Boolean) {
        mScrollable = mScroll
    }

    // endregion

    // region — Private helpers

    private fun initialiseHeader() {
        mStickyScrollPresenter.initStickyHeader(stickyHeaderView?.top)
    }

    private fun initialiseFooter() {
        mStickyScrollPresenter.initStickyFooter(
            stickyFooterView?.measuredHeight,
            getFooterTop()
        )
    }

    private fun getRelativeTop(myView: View): Int {
        return if (myView.parent === myView.rootView) myView.top
        else myView.top + getRelativeTop(myView.parent as View)
    }

    private fun getFooterTop(): Int {
        return stickyFooterView?.let {
            getRelativeTop(it) - it.topCutOutHeight()
        } ?: 0
    }

    private fun View.topCutOutHeight(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            rootWindowInsets.displayCutout?.safeInsetTop ?: 0
        } else 0
    }

    private fun View.onLayoutUpdate(action: () -> Unit) {
        viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener1 {
            override fun onGlobalLayout() {
                action.invoke()
                viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }

    // endregion

    // region — Presentation & state

    private inner class StickyScrollPresentation : IStickyScrollPresentation {
        override val currentScrollYPos: Int get() = scrollY

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

    companion object {
        private const val STATE_SCROLL = "scroll_state"
        private const val STATE_SUPER = "super_state"
        private const val STATE_NAV_BAR_HEIGHT = "nav_bar_height_state"
    }

    // endregion
}
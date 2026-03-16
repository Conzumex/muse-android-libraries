package com.conzumex.numberpicker

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.content.res.ResourcesCompat
import kotlin.math.roundToInt

/**
 * DrumRollPicker — infinite wrap-around
 *
 * Shows 3 items (prev · selected · next). Scrolling past the last item wraps
 * seamlessly back to the first, and vice-versa — like a slot machine drum.
 *
 * ── XML ──────────────────────────────────────────────────────────────────────
 *
 *   <com.example.drumrollpicker.DrumRollPicker
 *       android:id="@+id/picker"
 *       android:layout_width="160dp"
 *       android:layout_height="168dp"
 *       app:drp_minValue="1"
 *       app:drp_maxValue="30"
 *       app:drp_initialValue="5"
 *       app:drp_label="Days"
 *       app:drp_backgroundColor="#FF1A1A2E"
 *       app:drp_selectedTextColor="#FFF5F5F5"
 *       app:drp_adjacentTextColor="#66FFFFFF"
 *       app:drp_selectedTextSize="44"
 *       app:drp_adjacentTextSize="26"
 *       app:drp_labelTextSize="13"
 *       app:drp_typeface="sans-serif-light"
 *       app:drp_selectedTypefaceStyle="bold"
 *       app:drp_cornerRadius="20"
 *       app:drp_itemSpacing="12" />
 *
 * ── Kotlin ───────────────────────────────────────────────────────────────────
 *
 *   picker.minValue           = 1
 *   picker.maxValue           = 30
 *   picker.value              = 5
 *   picker.label              = "Days"
 *   picker.pickerBackgroundColor = Color.parseColor("#1A1A2E")
 *   picker.selectedTextColor  = Color.WHITE
 *   picker.adjacentTextColor  = 0x66FFFFFF
 *   picker.selectedTextSizeSp = 44f
 *   picker.adjacentTextSizeSp = 26f
 *   picker.labelTextSizeSp    = 13f
 *   picker.typeface           = Typeface.create("sans-serif-light", Typeface.NORMAL)
 *   picker.selectedTypeface   = Typeface.DEFAULT_BOLD
 *   picker.cornerRadiusDp     = 20f
 *   picker.itemSpacingDp      = 12f
 *   picker.onValueChanged     = { newValue -> }
 *
 * ── How infinite scrolling works ─────────────────────────────────────────────
 *
 *   currentOffset is never clamped — it can grow or shrink without limit.
 *   A "virtual index" is derived from the raw offset:
 *       virtualIndex = round(-currentOffset / itemHeight)
 *   The real item index is obtained with a proper positive modulo:
 *       realIndex = ((virtualIndex % size) + size) % size
 *   This maps any integer (positive or negative) onto [0, size-1], so the
 *   list appears to repeat infinitely in both directions.
 */
class DrumRollPicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // ── Public API ────────────────────────────────────────────────────────────

    var minValue: Int = 1
        set(v) { field = v; computeItems(); resetToValue(); invalidate() }

    var maxValue: Int = 30
        set(v) { field = v; computeItems(); resetToValue(); invalidate() }

    /**
     * Currently selected value. Updating this jumps the drum to that position.
     * Reading it always returns the item currently centred.
     */
    var value: Int = 5
        set(v) {
            field = v.coerceIn(minValue, maxValue)
            // Place the chosen item at virtual-index 0 so the offset is small
            // and can grow unboundedly in either direction from here.
            currentVirtualIndex = 0
            currentOffset       = 0f
            // Shift the items list so `value` sits at position 0
            rebuildItemsFromValue(field)
            invalidate()
        }

    /** Optional label drawn to the right of the selected item (e.g. "Days"). */
    var label: String = ""
        set(v) { field = v; invalidate() }

    /** Background fill color of the picker drum. */
    var pickerBackgroundColor: Int = 0xFF1A1A2E.toInt()
        set(v) { field = v; bgPaint.color = v; invalidate() }

    /** Text color of the selected (centre) item. */
    var selectedTextColor: Int = 0xFFF5F5F5.toInt()
        set(v) { field = v; selectedPaint.color = v; invalidate() }

    /** Text color of the adjacent (prev / next) items. */
    var adjacentTextColor: Int = 0x66FFFFFF
        set(v) { field = v; nearPaint.color = v; labelPaint.color = v; invalidate() }

    /** Text size of the selected item in SP. */
    var selectedTextSizeSp: Float = 44f
        set(v) { field = v; selectedPaint.textSize = spToPx(v); invalidate() }

    /** Text size of the adjacent items in SP. */
    var adjacentTextSizeSp: Float = 26f
        set(v) { field = v; nearPaint.textSize = spToPx(v); invalidate() }

    /** Text size of the label in SP. */
    var labelTextSizeSp: Float = 13f
        set(v) { field = v; labelPaint.textSize = spToPx(v); invalidate() }

    /** Typeface used for adjacent items and the label. */
    var typeface: Typeface = Typeface.DEFAULT
        set(v) { field = v; nearPaint.typeface = v; labelPaint.typeface = v; invalidate() }

    /** Typeface used for the selected item. */
    var selectedTypeface: Typeface = Typeface.DEFAULT_BOLD
        set(v) { field = v; selectedPaint.typeface = v; invalidate() }

    /** Corner radius of the background rounded rect in DP. */
    var cornerRadiusDp: Float = 20f
        set(v) { field = v; _cornerPx = dpToPx(v); invalidate() }

    /**
     * Extra spacing in DP added between the selected item and its neighbours.
     * 0 = items are evenly spaced (default). Increase to push prev/next further
     * away from centre. Also accepted via XML as app:drp_itemSpacing="12".
     */
    var itemSpacingDp: Float = 0f
        set(v) { field = v; _spacingPx = dpToPx(v); requestLayout(); invalidate() }

    /** Fired whenever the selected value changes during a drag or after a fling. */
    var onValueChanged: ((Int) -> Unit)? = null

    // ── Internal ──────────────────────────────────────────────────────────────

    private val density       = resources.displayMetrics.density
    private val scaledDensity = resources.displayMetrics.scaledDensity

    private fun dpToPx(dp: Float) = dp * density
    private fun spToPx(sp: Float) = sp * scaledDensity

    private val itemHeight: Float
    private var _cornerPx: Float
    private var _spacingPx: Float = 0f

    // ── Paints ────────────────────────────────────────────────────────────────

    private val bgPaint       = Paint(Paint.ANTI_ALIAS_FLAG)
    private val selectedPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val nearPaint     = Paint(Paint.ANTI_ALIAS_FLAG)
    private val labelPaint    = Paint(Paint.ANTI_ALIAS_FLAG)

    // ── Scroll state ──────────────────────────────────────────────────────────

    /**
     * The ordered list of display strings (e.g. "01".."30").
     * Rebuilt whenever minValue/maxValue changes. Order never changes —
     * wrap-around is achieved purely through modulo on the index.
     */
    private val items = mutableListOf<String>()

    /**
     * Raw unclamped scroll offset in px.
     * virtualIndex = round(-currentOffset / itemHeight)
     */
    private var currentOffset: Float = 0f

    /** Cached virtual index corresponding to the current offset. */
    private var currentVirtualIndex: Int = 0

    private var scrollAnimator: ValueAnimator? = null

    // ── Gesture ───────────────────────────────────────────────────────────────

    private val gestureDetector = GestureDetector(context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent) = true

            override fun onScroll(
                e1: MotionEvent?, e2: MotionEvent,
                dX: Float, dY: Float
            ): Boolean {
                scrollAnimator?.cancel()
                currentOffset -= dY           // no clamping — infinite!
                syncVirtualIndexInternal()
                invalidate()
                return true
            }

            override fun onFling(
                e1: MotionEvent?, e2: MotionEvent,
                vX: Float, vY: Float
            ): Boolean {
                fling(vY)
                return true
            }
        })

    // ── Init ──────────────────────────────────────────────────────────────────

    init {
        itemHeight = dpToPx(56f)
        _cornerPx  = dpToPx(20f)

        bgPaint.color = pickerBackgroundColor

        selectedPaint.color     = selectedTextColor
        selectedPaint.textAlign = Paint.Align.CENTER
        selectedPaint.textSize  = spToPx(selectedTextSizeSp)
        selectedPaint.typeface  = selectedTypeface

        nearPaint.color     = adjacentTextColor
        nearPaint.textAlign = Paint.Align.CENTER
        nearPaint.textSize  = spToPx(adjacentTextSizeSp)
        nearPaint.typeface  = typeface

        labelPaint.color     = adjacentTextColor
        labelPaint.textSize  = spToPx(labelTextSizeSp)
        labelPaint.textAlign = Paint.Align.LEFT
        labelPaint.typeface  = typeface

        attrs?.let { applyAttributes(it) }

        computeItems()
        resetToValue()
    }

    private fun applyAttributes(attrs: AttributeSet) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.DrumRollPicker)
        try {
            minValue = ta.getInt(R.styleable.DrumRollPicker_drp_minValue, minValue)
            maxValue = ta.getInt(R.styleable.DrumRollPicker_drp_maxValue, maxValue)
            value    = ta.getInt(R.styleable.DrumRollPicker_drp_initialValue, value)
            label    = ta.getString(R.styleable.DrumRollPicker_drp_label) ?: label

            if (ta.hasValue(R.styleable.DrumRollPicker_drp_backgroundColor))
                pickerBackgroundColor = ta.getColor(R.styleable.DrumRollPicker_drp_backgroundColor, pickerBackgroundColor)

            if (ta.hasValue(R.styleable.DrumRollPicker_drp_selectedTextColor))
                selectedTextColor = ta.getColor(R.styleable.DrumRollPicker_drp_selectedTextColor, selectedTextColor)

            if (ta.hasValue(R.styleable.DrumRollPicker_drp_adjacentTextColor))
                adjacentTextColor = ta.getColor(R.styleable.DrumRollPicker_drp_adjacentTextColor, adjacentTextColor)

            if (ta.hasValue(R.styleable.DrumRollPicker_drp_selectedTextSize))
                selectedTextSizeSp = ta.getFloat(R.styleable.DrumRollPicker_drp_selectedTextSize, selectedTextSizeSp)

            if (ta.hasValue(R.styleable.DrumRollPicker_drp_adjacentTextSize))
                adjacentTextSizeSp = ta.getFloat(R.styleable.DrumRollPicker_drp_adjacentTextSize, adjacentTextSizeSp)

            if (ta.hasValue(R.styleable.DrumRollPicker_drp_labelTextSize))
                labelTextSizeSp = ta.getFloat(R.styleable.DrumRollPicker_drp_labelTextSize, labelTextSizeSp)

            if (ta.hasValue(R.styleable.DrumRollPicker_drp_cornerRadius))
                cornerRadiusDp = ta.getFloat(R.styleable.DrumRollPicker_drp_cornerRadius, cornerRadiusDp)

            if (ta.hasValue(R.styleable.DrumRollPicker_drp_itemSpacing))
                itemSpacingDp = ta.getFloat(R.styleable.DrumRollPicker_drp_itemSpacing, itemSpacingDp)

            val fontFamily = ta.getString(R.styleable.DrumRollPicker_drp_typeface)
            if (!fontFamily.isNullOrEmpty()) {
                typeface = Typeface.create(fontFamily, Typeface.NORMAL)
            }

            val selStyle = ta.getInt(R.styleable.DrumRollPicker_drp_selectedTypefaceStyle, -1)
            if (selStyle >= 0) {
                val family = fontFamily ?: "sans-serif"
                selectedTypeface = Typeface.create(family, selStyle)
            }

            val fontResId = ta.getResourceId(R.styleable.DrumRollPicker_drp_fontFamily, 0)
            if (fontResId != 0) {
                val tf = ResourcesCompat.getFont(context, fontResId)
                if (tf != null) {
                    typeface         = tf
                    selectedTypeface = tf
                }
            }

        } finally {
            ta.recycle()
        }
    }

    // ── Items & index helpers ─────────────────────────────────────────────────

    private fun computeItems() {
        items.clear()
        for (n in minValue..maxValue) items.add(n.toString().padStart(2, '0'))
    }

    /**
     * Rebuild the items list so `value` appears at real-index 0, then reset
     * the scroll offset to 0.  This keeps currentOffset small regardless of
     * how far the user has scrolled before a programmatic value change.
     */
    private fun rebuildItemsFromValue(v: Int) {
        if (items.isEmpty()) return
        val startIdx = (v - minValue).coerceIn(0, items.size - 1)
        val rotated  = items.subList(startIdx, items.size) + items.subList(0, startIdx)
        items.clear()
        items.addAll(rotated)
        currentOffset       = 0f
        currentVirtualIndex = 0
    }

    private fun resetToValue() {
        if (items.isEmpty()) return
        rebuildItemsFromValue(value.coerceIn(minValue, maxValue))
    }

    /**
     * Positive modulo — always returns a value in [0, size-1] even for
     * negative inputs.  Kotlin's `%` can return negative results.
     */
    private fun Int.posMod(size: Int): Int = ((this % size) + size) % size

    /** Virtual index → real item index (wraps around). */
    private fun realIndex(virtualIdx: Int): Int =
        virtualIdx.posMod(items.size)

    /** Raw offset → nearest virtual index (no clamping). */
    private fun offsetToVirtual(offset: Float): Int =
        (-offset / itemHeight).roundToInt()

    /** Virtual index → the exact offset that centres it. */
    private fun virtualToOffset(virtualIdx: Int): Float =
        -virtualIdx * itemHeight

    // Shadow field — tracks displayed value without triggering the public setter
    // (which would reset the scroll offset).
    private var _currentValue: Int = 5

    /**
     * Called after every scroll delta. Updates currentVirtualIndex and fires
     * onValueChanged if the selected item has changed.
     */
    private fun syncVirtualIndexInternal() {
        val newVirtual = offsetToVirtual(currentOffset)
        if (newVirtual == currentVirtualIndex) return
        currentVirtualIndex = newVirtual
        val newValue = minValue + realIndex(newVirtual)
        if (newValue != _currentValue) {
            _currentValue = newValue
            onValueChanged?.invoke(newValue)
        }
    }

    // ── Fling + snap ──────────────────────────────────────────────────────────

    private fun fling(velocityY: Float) {
        val projected   = currentOffset + velocityY * 0.12f
        val targetVirtual = offsetToVirtual(projected)
        animateTo(targetVirtual)
    }

    private fun animateTo(targetVirtual: Int) {
        val targetOffset = virtualToOffset(targetVirtual)
        scrollAnimator?.cancel()
        scrollAnimator = ValueAnimator.ofFloat(currentOffset, targetOffset).apply {
            duration     = 280
            interpolator = DecelerateInterpolator(2f)
            addUpdateListener { anim ->
                currentOffset = anim.animatedValue as Float
                syncVirtualIndexInternal()
                invalidate()
            }
            start()
        }
    }

    // ── Touch ─────────────────────────────────────────────────────────────────

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                parent?.requestDisallowInterceptTouchEvent(true)
            }
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                parent?.requestDisallowInterceptTouchEvent(false)
                if (scrollAnimator?.isRunning != true) {
                    animateTo(offsetToVirtual(currentOffset))
                }
            }
        }
        return gestureDetector.onTouchEvent(event)
    }

    // ── Draw ──────────────────────────────────────────────────────────────────

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w  = width.toFloat()
        val h  = height.toFloat()
        val cx = w / 2f
        val cy = h / 2f

        // Background
        canvas.drawRoundRect(RectF(0f, 0f, w, h), _cornerPx, _cornerPx, bgPaint)
        canvas.clipRect(0f, 0f, w, h)

        if (items.isEmpty()) return

        // Draw prev, selected, next using modulo-wrapped real indices
        for (offset in -1..1) {
            val virtualIdx = currentVirtualIndex + offset
            val realIdx    = realIndex(virtualIdx)
            val text       = items[realIdx]

            // Y position: centre of the slot for this virtual index.
            // _spacingPx shifts adjacent items (offset ±1) away from centre —
            // positive spacing pushes prev up and next down.
            val spacing     = if (offset == 0) 0f else offset * _spacingPx
            val slotCentreY = cy + (virtualIdx * itemHeight) + currentOffset + spacing
            val paint       = if (offset == 0) selectedPaint else nearPaint

            canvas.drawText(text, cx, slotCentreY + paint.textSize * 0.35f, paint)
        }

        // Label to the right of the selected item
        if (label.isNotEmpty()) {
            val labelX = cx + selectedPaint.textSize * 0.72f
            val labelY = cy + labelPaint.textSize * 0.35f
            canvas.drawText(label, labelX, labelY, labelPaint)
        }
    }

    // ── Measure ───────────────────────────────────────────────────────────────

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Natural height = selected slot + prev slot + next slot + any extra spacing
        val naturalH = (itemHeight * 3 + _spacingPx * 2).toInt()
        setMeasuredDimension(
            resolveSize(dpToPx(160f).toInt(), widthMeasureSpec),
            resolveSize(naturalH, heightMeasureSpec)
        )
    }
}
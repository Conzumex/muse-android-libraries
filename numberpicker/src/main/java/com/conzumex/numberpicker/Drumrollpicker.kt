package com.conzumex.numberpicker

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
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
/** Controls where the label ("Days") is vertically aligned within the selected item slot. */
enum class LabelPosition { TOP, CENTER, BOTTOM }

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
        set(v) { field = v; bgPaint.color = v; buildFadeGradients(width.toFloat(), height.toFloat()); invalidate() }

    /**
     * The color the gradient fades TO at the outer edges of the prev/next items.
     * Defaults to the picker background color so it blends naturally.
     * The gradient runs from selectedTextColor (transparent, inner edge near selected slot)
     * to this color (outer edge at top/bottom of the view).
     */
    var gradientEndColor: Int = 0xFF1A1A2E.toInt()
        set(v) { field = v; buildFadeGradients(width.toFloat(), height.toFloat()); invalidate() }

    /** Text color of the selected (centre) item. */
    var selectedTextColor: Int = 0xFFF5F5F5.toInt()
        set(v) { field = v; selectedPaint.color = v; invalidate() }

    /** Text color of the adjacent (prev / next) items. */
    var adjacentTextColor: Int = 0x66FFFFFF
        set(v) { field = v; nearPaint.color = v; invalidate() }

    /** Text color of the label (e.g. "Days"). Defaults to adjacentTextColor. */
    var labelTextColor: Int = 0x66FFFFFF
        set(v) { field = v; labelPaint.color = v; invalidate() }

    /** Text size of the selected item in SP. */
    var selectedTextSizeSp: Float = 44f
        set(v) { field = v; selectedPaint.textSize = spToPx(v); invalidate() }

    /** Text size of the adjacent items in SP. */
    var adjacentTextSizeSp: Float = 26f
        set(v) { field = v; nearPaint.textSize = spToPx(v); invalidate() }

    /** Text size of the label in SP. */
    var labelTextSizeSp: Float = 13f
        set(v) { field = v; labelPaint.textSize = spToPx(v); invalidate() }

    /** Typeface used for adjacent (prev/next) number items. */
    var typeface: Typeface = Typeface.DEFAULT
        set(v) { field = v; nearPaint.typeface = v; invalidate() }

    /** Typeface used for the selected (centre) number item. */
    var selectedTypeface: Typeface = Typeface.DEFAULT_BOLD
        set(v) { field = v; selectedPaint.typeface = v; invalidate() }

    /** Typeface used for the label (e.g. "Days"). Defaults to system default. */
    var labelTypeface: Typeface = Typeface.DEFAULT
        set(v) { field = v; labelPaint.typeface = v; invalidate() }

    /**
     * Vertical alignment of the label relative to the selected item slot.
     *   LabelPosition.TOP    — label baseline sits at the top edge of the selected slot
     *   LabelPosition.CENTER — label is vertically centred in the selected slot (default)
     *   LabelPosition.BOTTOM — label baseline sits at the bottom edge of the selected slot
     * The label X position always stays fixed to the right of the widest possible number
     * (selectedTextSize) so it never shifts during the size animation.
     */
    var labelPosition: LabelPosition = LabelPosition.CENTER
        set(v) { field = v; invalidate() }

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
    /** Scratch paint reused every draw frame — avoids allocation inside onDraw. */
    private val drawPaint     = Paint(Paint.ANTI_ALIAS_FLAG)
    /** Gradient overlay for the top adjacent slot (fades downward toward selected). */
    private val fadeTopPaint    = Paint(Paint.ANTI_ALIAS_FLAG)
    /** Gradient overlay for the bottom adjacent slot (fades upward toward selected). */
    private val fadeBottomPaint = Paint(Paint.ANTI_ALIAS_FLAG)

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

            if (ta.hasValue(R.styleable.DrumRollPicker_drp_labelTextColor))
                labelTextColor = ta.getColor(R.styleable.DrumRollPicker_drp_labelTextColor, labelTextColor)

            if (ta.hasValue(R.styleable.DrumRollPicker_drp_gradientEndColor))
                gradientEndColor = ta.getColor(R.styleable.DrumRollPicker_drp_gradientEndColor, gradientEndColor)

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

            // Number font — try @font/ resource first, fall back to family name string
            val fontResId = ta.getResourceId(R.styleable.DrumRollPicker_drp_typeface, 0)
            if (fontResId != 0) {
                val tf = androidx.core.content.res.ResourcesCompat.getFont(context, fontResId)
                if (tf != null) typeface = tf
            } else {
                val fontFamily = ta.getString(R.styleable.DrumRollPicker_drp_typeface)
                if (!fontFamily.isNullOrEmpty()) {
                    typeface = Typeface.create(fontFamily, Typeface.NORMAL)
                }
            }

            // Selected number style (bold/italic etc.) applied on top of the number font
            val selStyle = ta.getInt(R.styleable.DrumRollPicker_drp_selectedTypefaceStyle, -1)
            if (selStyle >= 0) {
                selectedTypeface = Typeface.create(typeface, selStyle)
            }

            // Label font — try @font/ resource first, fall back to family name string
            val labelFontResId = ta.getResourceId(R.styleable.DrumRollPicker_drp_labelTypeface, 0)
            if (labelFontResId != 0) {
                val tf = androidx.core.content.res.ResourcesCompat.getFont(context, labelFontResId)
                if (tf != null) labelTypeface = tf
            } else {
                val labelFontFamily = ta.getString(R.styleable.DrumRollPicker_drp_labelTypeface)
                if (!labelFontFamily.isNullOrEmpty()) {
                    labelTypeface = Typeface.create(labelFontFamily, Typeface.NORMAL)
                }
            }

            // Label position
            val posOrdinal = ta.getInt(R.styleable.DrumRollPicker_drp_labelPosition, 1)
            labelPosition = enumValues<LabelPosition>().getOrElse(posOrdinal) { LabelPosition.CENTER }
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

    // ── Fade gradient ─────────────────────────────────────────────────────────

    /**
     * Builds two LinearGradient shaders — one for the top slot, one for the bottom.
     * Each covers exactly one itemHeight (the prev/next slot area).
     *
     * Direction:
     *   Top gradient    — gradientEndColor (outer/top edge) → transparent (inner/bottom edge)
     *   Bottom gradient — transparent (inner/top edge)      → gradientEndColor (outer/bottom edge)
     *
     * The "inner" transparent stop uses selectedTextColor with alpha=0 so the gradient
     * fades from the number text color family rather than an arbitrary transparent black.
     */
    private fun buildFadeGradients(w: Float, h: Float) {
        if (w <= 0f || h <= 0f) return

        val endColor   = gradientEndColor
        // Transparent version of gradientEndColor (preserves RGB, zeroes alpha)
        val endClear   = endColor and 0x00FFFFFF

        val slotH = itemHeight   // one slot = top edge to where selected slot starts

        // Top: outer (y=0) is solid endColor, inner (y=slotH) is transparent
        fadeTopPaint.shader = LinearGradient(
            0f, 0f, 0f, slotH,
            intArrayOf(endColor, endClear),
            null,
            Shader.TileMode.CLAMP
        )

        // Bottom: inner (y = h-slotH) is transparent, outer (y = h) is solid endColor
        fadeBottomPaint.shader = LinearGradient(
            0f, h - slotH, 0f, h,
            intArrayOf(endClear, endColor),
            null,
            Shader.TileMode.CLAMP
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        super.onSizeChanged(w, h, oldW, oldH)
        buildFadeGradients(w.toFloat(), h.toFloat())
    }

    // ── Draw ──────────────────────────────────────────────────────────────────

    /**
     * Returns a 0..1 progress value for how "selected" a slot is.
     *   1.0 = perfectly centred (fully selected)
     *   0.0 = one full itemHeight away (fully adjacent)
     * Computed from the fractional part of the raw scroll offset so it updates
     * continuously every pixel the user drags — no discrete jumps.
     */
    private fun selectionProgress(slotOffset: Int): Float {
        // How far (in itemHeight units) this slot is from the exact snap centre
        val exactVirtual = -currentOffset / itemHeight          // fractional virtual index
        val dist = kotlin.math.abs(exactVirtual - (currentVirtualIndex + slotOffset))
        // dist == 0  → item is dead-centre → progress 1.0
        // dist == 1  → item is one slot away → progress 0.0
        return (1f - dist.coerceIn(0f, 1f))
    }

    /** Linear interpolation between two floats. */
    private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t

    /** Linear interpolation between two ARGB colours (component-wise). */
    private fun lerpColor(from: Int, to: Int, t: Float): Int {
        val aA = (from ushr 24) and 0xFF
        val rA = (from ushr 16) and 0xFF
        val gA = (from ushr 8)  and 0xFF
        val bA =  from          and 0xFF
        val aB = (to   ushr 24) and 0xFF
        val rB = (to   ushr 16) and 0xFF
        val gB = (to   ushr 8)  and 0xFF
        val bB =  to            and 0xFF
        return ((lerp(aA.toFloat(), aB.toFloat(), t).toInt() shl 24) or
                (lerp(rA.toFloat(), rB.toFloat(), t).toInt() shl 16) or
                (lerp(gA.toFloat(), gB.toFloat(), t).toInt() shl 8)  or
                lerp(bA.toFloat(), bB.toFloat(), t).toInt())
    }

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

        val selectedSizePx = selectedPaint.textSize   // max text size (fully selected)
        val adjacentSizePx = nearPaint.textSize       // min text size (fully adjacent)

        // Draw prev (-1), selected (0), next (+1)
        for (slotOffset in -1..1) {
            val virtualIdx = currentVirtualIndex + slotOffset
            val realIdx    = realIndex(virtualIdx)
            val text       = items[realIdx]

            // 0.0 → adjacent size/alpha,  1.0 → selected size/alpha
            val progress = selectionProgress(slotOffset)

            // Interpolate text size continuously
            val textSize = lerp(adjacentSizePx, selectedSizePx, progress)

            // Interpolate colour (handles both colour and alpha together)
            val color = lerpColor(nearPaint.color, selectedPaint.color, progress)

            // Choose typeface: use selectedTypeface once past 50% into selection
            val tf = if (progress >= 0.5f) selectedPaint.typeface else nearPaint.typeface

            // Build a draw paint from the interpolated values
            drawPaint.set(if (progress >= 0.5f) selectedPaint else nearPaint)
            drawPaint.textSize = textSize
            drawPaint.color    = color
            drawPaint.typeface = tf

            // Y position with optional extra spacing for adjacent items
            val spacing     = if (slotOffset == 0) 0f else slotOffset * _spacingPx
            val slotCentreY = cy + (virtualIdx * itemHeight) + currentOffset + spacing

            canvas.drawText(text, cx, slotCentreY + drawPaint.textSize * 0.35f, drawPaint)
        }

        // ── Label — fully static position, never moves during animation ──────────
        // X: fixed to the right of the max possible selected text width so it
        //    never shifts as the number size animates.
        // Y: anchored to the selected slot's top/centre/bottom edge using
        //    itemHeight — completely independent of the animating text size.
        if (label.isNotEmpty()) {
            val labelX = cx + selectedPaint.textSize * 0.72f  // right of widest number
            val halfSlot = itemHeight / 2f
            val labelY = when (labelPosition) {
                LabelPosition.TOP    -> cy - halfSlot + labelPaint.textSize * 0.35f
                LabelPosition.CENTER -> cy              + labelPaint.textSize * 0.35f
                LabelPosition.BOTTOM -> cy + halfSlot  - labelPaint.textSize * 0.35f
            }
            canvas.drawText(label, labelX, labelY, labelPaint)
        }

        // ── Fade overlay — drawn last, covers only the prev/next slots ───────────
        // Top slot: y=0 to y=itemHeight
        canvas.drawRect(0f, 0f, w, itemHeight, fadeTopPaint)
        // Bottom slot: y=(h-itemHeight) to y=h
        canvas.drawRect(0f, h - itemHeight, w, h, fadeBottomPaint)
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
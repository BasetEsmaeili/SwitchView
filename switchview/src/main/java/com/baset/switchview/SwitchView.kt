package com.baset.switchview

import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import android.view.animation.DecelerateInterpolator
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import kotlin.math.min

class SwitchView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), View.OnClickListener {
    private var utils: Utils = Utils(context)
    private var isChecked = false
    private var defaultWidth = 0
    private var defaultHeight = 0
    private var backgroundPaint: Paint? = null
    private var backgroundRect: RectF? = null
    private var backgroundRadius = 2f
    private var switchRadius = 0f
    private var switchRect: RectF? = null
    private var switchPath: Path? = null
    private var switchPaint: Paint? = null
    private var checkedBackgroundColor = 0
    private var unCheckedBackgroundColor = 0
    private var switchToggleWidth = 0f
    private var switchToggleMarginLeft = 0f
    private var animatorSet: AnimatorSet? = null
    private val viewDefaultWidth = 39
    private val viewDefaultHeight = 27
    private var isInitialized = false
    private lateinit var onCheckedChangeListener: OnCheckedChangeListener

    init {
        animatorSet = AnimatorSet()
        defaultWidth = utils.convertDPtoPX(viewDefaultWidth.toFloat())
        defaultHeight = utils.convertDPtoPX(viewDefaultHeight.toFloat())
        backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        switchPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        backgroundRect = RectF()
        switchRect = RectF()
        switchPath = Path()
        attrs?.let {
            retrieveAttributes(attrs, defStyleAttr)
        }
        setOnClickListener(this)
    }

    private fun retrieveAttributes(attrs: AttributeSet, defStyleAttr: Int) {
        val typedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.SwitchView,
            defStyleAttr,
            R.style.SwitchView
        )

        isChecked = typedArray.getBoolean(R.styleable.SwitchView_android_checked, false)
        animatorSet?.duration =
            typedArray.getInt(R.styleable.SwitchView_android_animationDuration, 350)
                .toLong()
        checkedBackgroundColor = typedArray.getColor(
            R.styleable.SwitchView_switch_on_color,
            utils.getColorResource(R.color.color_background_enabled)
        )
        unCheckedBackgroundColor = typedArray.getColor(
            R.styleable.SwitchView_switch_off_color,
            utils.getColorResource(R.color.color_background_disabled)
        )
        switchPaint?.color =
            typedArray.getColor(R.styleable.SwitchView_switch_toggle_color, Color.WHITE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            elevation = (
                    typedArray.getDimension(R.styleable.SwitchView_switch_elevation, 0f)
                    )
        }
        typedArray.recycle()
        if (isChecked) backgroundPaint?.color =
            checkedBackgroundColor else backgroundPaint?.color = unCheckedBackgroundColor

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private fun initOutlineProvider(): ViewOutlineProvider {
        val provider: ViewOutlineProvider
        provider = object : ViewOutlineProvider() {
            override fun getOutline(
                view: View,
                outline: Outline
            ) {
                outline.setRoundRect(
                    backgroundRect!!.left.toInt(),
                    backgroundRect!!.top.toInt(),
                    backgroundRect!!.right.toInt(),
                    backgroundRect!!.bottom.toInt(),
                    height / backgroundRadius
                )
            }
        }
        return provider
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthFinal: Int
        val heightFinal: Int
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        widthFinal =
            if (widthMode == MeasureSpec.EXACTLY) width else if (widthMode == MeasureSpec.AT_MOST) defaultWidth else defaultWidth
        heightFinal =
            if (heightMode == MeasureSpec.EXACTLY) height else if (heightMode == MeasureSpec.AT_MOST) defaultHeight else defaultHeight
        setMeasuredDimension(widthFinal, heightFinal)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        backgroundRect!![0f, 0f, width.toFloat()] = height.toFloat()
        if (isChecked) {
            forceChecked()
        } else {
            forceUnCheck()
        }
    }

    override fun onDraw(canvas: Canvas) {
        switchRect!![switchToggleMarginLeft, backgroundRect!!.height() / 4f, switchToggleWidth] =
            height - backgroundRect!!.height() / 4f
        canvas.drawRoundRect(
            backgroundRect!!,
            height / backgroundRadius,
            height / backgroundRadius,
            backgroundPaint!!
        )
        switchPath!!.addRoundRect(
            switchRect as RectF,
            switchRadius,
            switchRadius,
            Path.Direction.CW
        )
        canvas.drawPath(switchPath!!, switchPaint!!)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            outlineProvider = initOutlineProvider()
        }
        isInitialized = true
    }

    override fun onClick(v: View) {
        isChecked = !isChecked
        onCheckedChangeListener.onCheckedChanged(this, isChecked)
        animateSwitch()
    }

    private fun animateSwitch() {
        val toggleRadius =
            if (isChecked) min(width, height) / 6f else min(
                width,
                height
            ) / 2f
        val toggleWidth =
            if (isChecked) width - backgroundRect!!.width() / 12f else backgroundRect!!.width() / 12f * 5
        val toggleMarginLeft =
            if (isChecked) width - backgroundRect!!.width() / 12f * 3 else backgroundRect!!.width() / 12f
        val backgroundColor = if (isChecked) checkedBackgroundColor else unCheckedBackgroundColor
        val switchRadiusAnimator = ValueAnimator.ofFloat(switchRadius, toggleRadius).apply {
            addUpdateListener { animation ->
                switchRadius = animation.animatedValue as Float
                switchPath?.reset()
                invalidate()
            }
        }
        val colorAnimator = ValueAnimator.ofObject(
            ArgbEvaluator(),
            backgroundPaint?.color,
            backgroundColor
        ).apply {
            addUpdateListener { animation ->
                backgroundPaint?.color = animation.animatedValue as Int
                invalidate()
            }
        }
        val switchToggleAnimator = ValueAnimator.ofFloat(switchToggleWidth, toggleWidth).apply {
            addUpdateListener { animation ->
                switchToggleWidth = animation.animatedValue as Float
                invalidate()
            }
        }
        val switchToggleMarginAnimator =
            ValueAnimator.ofFloat(switchToggleMarginLeft, toggleMarginLeft).apply {
                addUpdateListener { animation ->
                    switchToggleMarginLeft = animation.animatedValue as Float
                    invalidate()
                }
                interpolator = DecelerateInterpolator()
            }
        animatorSet?.playTogether(
            colorAnimator,
            switchRadiusAnimator,
            switchToggleAnimator,
            switchToggleMarginAnimator
        )
        animatorSet?.start()
    }

    fun setChecked(checked: Boolean) {
        if (checked != isChecked) {
            this.isChecked = checked
            if (isInitialized) {
                onCheckedChangeListener.onCheckedChanged(this, isChecked)
                animateSwitch()
            }
        }
    }


    private fun forceUnCheck() {
        switchRadius = min(width, height) / 2f
        switchToggleWidth = backgroundRect!!.width() / 12f * 5
        switchToggleMarginLeft = backgroundRect!!.width() / 12f
        backgroundPaint?.color = unCheckedBackgroundColor
    }

    private fun forceChecked() {
        switchRadius = min(width, height) / 6f
        switchToggleWidth = width - backgroundRect!!.width() / 12f
        switchToggleMarginLeft = width - backgroundRect!!.width() / 12f * 3
        backgroundPaint?.color = checkedBackgroundColor
    }

    fun isChecked() = isChecked

    fun setBackgroundColor(@ColorInt checkedColor: Int, @ColorInt uncheckedColor: Int) {
        checkedBackgroundColor = checkedColor
        unCheckedBackgroundColor = uncheckedColor
        invalidate()
    }

    fun setSwitchToggleColor(@ColorInt switchColor: Int) {
        switchPaint!!.color = switchColor
        invalidate()
    }

    fun getSwitchToggleColor() = switchPaint?.color

    fun setAnimationDuration(duration: Long) {
        animatorSet?.duration = duration
    }

    fun getAnimationDuration(): Long {
        return animatorSet?.duration!!
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    fun setSwitchElevation(elevation: Float) {
        this.elevation = elevation
        invalidate()
    }

    fun setOnCheckedChangeListener(onCheckedChangeListener: OnCheckedChangeListener) {
        this.onCheckedChangeListener = onCheckedChangeListener
    }

}
package de.tolunla.ghostotp.view.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.toColorInt
import de.tolunla.ghostotp.R

class CountdownIndicator(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var mColor: Int = "#3060c0".toColorInt() // Color used to paint the indicator's arc
    private val mPaint: Paint
    private var mPhase: Float = 0f
    private var mStartAngle: Int = 270 // Start angle for the indicator's arc
    private var mDrawingRect: RectF? = null

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.CountdownIndicator, 0, 0
        ).apply {
            try {
                mColor = getColor(R.styleable.CountdownIndicator_color, mColor)
                mStartAngle = getInt(R.styleable.CountdownIndicator_startAngle, mStartAngle)
                mPhase = getFloat(R.styleable.CountdownIndicator_phase, mPhase)
            } finally {
                recycle()
            }
        }

        mPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = mColor
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        val sweepAngle: Float = mPhase * 360
        val sweepStart: Float = mStartAngle.toFloat() - sweepAngle

        if (mDrawingRect == null) {
            val size = (width.coerceAtMost(height) - 1).toFloat()
            mDrawingRect = RectF(1f, 1f, size, size)
        }

        canvas?.let {
            it.apply {
                mDrawingRect?.let { rect ->
                    if (sweepAngle < 360) {
                        drawArc(rect, sweepStart, sweepAngle, true, mPaint)
                    } else {
                        drawOval(rect, mPaint)
                    }
                }
            }
        }
    }

    fun setPhase(phase: Float) {
        mPhase = phase
        invalidate()
    }

    fun setColor(color: Int) {
        mPaint.color = color
    }
}
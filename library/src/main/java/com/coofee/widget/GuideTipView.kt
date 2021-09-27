package com.coofee.widget

import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import java.lang.RuntimeException
import kotlin.math.sqrt

class GuideTipView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defaultStyle: Int = 0
) : FrameLayout(context, attrs, defaultStyle) {

    companion object {
        const val TOP = 0
        const val LEFT = 1
        const val BOTTOM = 2
        const val RIGHT = 3

        const val SRC_COLOR = "#80000000"
    }

    val srcDrawable: Drawable

    val radiusRound: Boolean
    val radius: Float
    val topLeftRadius: Float
    val topRightRadius: Float
    val bottomLeftRadius: Float
    val bottomRightRadius: Float

    val arrowRadius: Float
    val arrowSideLength: Float
    val arrowHeight: Float
    var arrowStartDistance: Float = 0f
        set(value) {
            if (field != value) {
                field = value
                generateBackgroundPath(width, height)
                invalidate()
            }
        }
    var arrowDirection: Int = Gravity.TOP
        set(value) {
            if (field != value) {
                field = value
                forceLayout()
                generateBackgroundPath(width, height)
                invalidate()
            }
        }


    private val backgroundPath = Path()

    init {
        if (attrs == null) {
            this.srcDrawable = ColorDrawable(Color.parseColor(SRC_COLOR))

            this.radiusRound = false
            this.radius = 0f
            this.topLeftRadius = 0f
            this.topRightRadius = 0f
            this.bottomLeftRadius = 0f
            this.bottomRightRadius = 0f

            this.arrowRadius = 0f
            this.arrowSideLength = 0f
        } else {
            val styledAttributes = context.obtainStyledAttributes(attrs, R.styleable.GuideTipView)
            this.srcDrawable = styledAttributes.getDrawable(R.styleable.GuideTipView_src)
                ?: ColorDrawable(Color.parseColor(SRC_COLOR))
            this.radiusRound = styledAttributes.getBoolean(R.styleable.GuideTipView_radiusRound, false)
            this.radius = styledAttributes.getDimensionPixelSize(R.styleable.GuideTipView_radius, 0).toFloat()
            this.topLeftRadius =
                styledAttributes.getDimensionPixelSize(R.styleable.GuideTipView_topLeftRadius, 0).toFloat()
            this.topRightRadius =
                styledAttributes.getDimensionPixelSize(R.styleable.GuideTipView_topRightRadius, 0).toFloat()
            this.bottomLeftRadius =
                styledAttributes.getDimensionPixelSize(R.styleable.GuideTipView_bottomLeftRadius, 0).toFloat()
            this.bottomRightRadius =
                styledAttributes.getDimensionPixelSize(R.styleable.GuideTipView_bottomRightRadius, 0).toFloat()

            this.arrowRadius = styledAttributes.getDimensionPixelSize(R.styleable.GuideTipView_arrowRadius, 0).toFloat()
            this.arrowSideLength =
                styledAttributes.getDimensionPixelSize(R.styleable.GuideTipView_arrowSideLength, 0).toFloat()
            this.arrowStartDistance =
                styledAttributes.getDimensionPixelSize(R.styleable.GuideTipView_arrowStartDistance, 0).toFloat()
            this.arrowDirection = styledAttributes.getInt(R.styleable.GuideTipView_arrowDirection, TOP)
            styledAttributes.recycle()
        }

        this.arrowHeight = sqrt(arrowSideLength * arrowSideLength / 2) - arrowRadius
        setWillNotDraw(false)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val gravity = when (arrowDirection) {
            TOP -> Gravity.BOTTOM
            BOTTOM -> Gravity.TOP
            LEFT -> Gravity.RIGHT
            RIGHT -> Gravity.LEFT
            else -> {
                throw RuntimeException("not support arrowDirection=${arrowDirection}")
            }
        }

        for (i in 0 until childCount) {
            val childAt = getChildAt(i)
            val childLayoutParams = childAt.layoutParams as LayoutParams
            childLayoutParams.gravity = gravity
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val arrowHeight = sqrt(arrowSideLength * arrowSideLength / 2) - arrowRadius

        if (arrowDirection == TOP || arrowDirection == BOTTOM) {
            val heightWithArrow = (measuredHeight + arrowHeight).toInt()
            setMeasuredDimension(measuredWidth, heightWithArrow)

        } else if (arrowDirection == LEFT || arrowDirection == RIGHT) {

            val widthWithArrow = (measuredWidth + arrowHeight).toInt()
            setMeasuredDimension(widthWithArrow, measuredHeight)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        generateBackgroundPath(w, h)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        val c = canvas ?: return
        c.save()
        canvas.clipPath(backgroundPath)
        srcDrawable.draw(canvas)
        c.restore()
    }

    private fun generateBackgroundPath(w: Int, h: Int) {
        backgroundPath.reset()

        val floatRadius = if (radiusRound) {
            val roundRadius = (h / 2.0).toFloat()
            floatArrayOf(
                roundRadius, roundRadius, roundRadius, roundRadius,
                roundRadius, roundRadius, roundRadius, roundRadius
            )
        } else if (radius > 0) {
            floatArrayOf(radius, radius, radius, radius, radius, radius, radius, radius)
        } else {
            floatArrayOf(
                topLeftRadius, topLeftRadius, topRightRadius, topRightRadius,
                bottomRightRadius, bottomRightRadius, bottomLeftRadius, bottomLeftRadius
            )
        }

        val arrowPath = Path().apply {
            addRoundRect(
                RectF(0f, 0f, arrowSideLength, arrowSideLength),
                floatArrayOf(
                    arrowRadius, arrowRadius, arrowRadius, arrowRadius,
                    arrowRadius, arrowRadius, arrowRadius, arrowRadius
                ),
                Path.Direction.CW
            )
        }
        val computeBounds = RectF()
        arrowPath.computeBounds(computeBounds, true)
        val matrix = Matrix()


        when (arrowDirection) {
            TOP -> {
                backgroundPath.addRoundRect(
                    0f, arrowHeight,
                    w.toFloat(), h.toFloat(), floatRadius, Path.Direction.CW
                )

                matrix.postRotate(45f, computeBounds.centerX(), computeBounds.centerY())

                val dx = if (arrowStartDistance < 1) {
                    (w - arrowSideLength) / 2.0f
                } else {
                    arrowStartDistance - arrowSideLength / 2
                }
                matrix.postTranslate(dx, arrowRadius)
            }

            BOTTOM -> {
                backgroundPath.addRoundRect(
                    0f, 0f,
                    w.toFloat(), h.toFloat() - arrowHeight, floatRadius, Path.Direction.CW
                )

                matrix.postRotate(45f, computeBounds.centerX(), computeBounds.centerY())
                val dx = if (arrowStartDistance < 1) {
                    (w - arrowSideLength) / 2.0f
                } else {
                    arrowStartDistance - arrowSideLength / 2
                }
                matrix.postTranslate(dx, h.toFloat() - arrowSideLength - arrowRadius)
            }

            LEFT -> {
                backgroundPath.addRoundRect(
                    arrowHeight, 0f,
                    w.toFloat(), h.toFloat(), floatRadius, Path.Direction.CW
                )

                matrix.postRotate(45f, computeBounds.centerX(), computeBounds.centerY())
                val dy = if (arrowStartDistance < 1) {
                    (h - arrowSideLength) / 2.0f
                } else {
                    arrowStartDistance - arrowSideLength / 2
                }
                matrix.postTranslate(arrowRadius, dy)
            }

            RIGHT -> {
                backgroundPath.addRoundRect(
                    0f, 0f,
                    w.toFloat() - arrowHeight, h.toFloat(), floatRadius, Path.Direction.CW
                )

                matrix.postRotate(45f, computeBounds.centerX(), computeBounds.centerY())
                val dy = if (arrowStartDistance < 1) {
                    (h - arrowSideLength) / 2.0f
                } else {
                    arrowStartDistance - arrowSideLength / 2
                }
                matrix.postTranslate(w.toFloat() - arrowSideLength - arrowRadius, dy)
            }
        }

        backgroundPath.addPath(arrowPath, matrix)
        srcDrawable.setBounds(0, 0, w, h)

//        Log.d(
//            "GuideTipView",
//            "w=${w}, h=${h} arrowRadius=${arrowRadius}, arrowHeight=${arrowHeight}, backgroundPath=${backgroundPath}"
//        )
    }
}
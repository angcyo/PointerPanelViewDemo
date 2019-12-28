package com.angcyo.pointerpanelviewdemo

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.toColorInt
import androidx.core.math.MathUtils
import kotlin.math.min

/**
 * 指针面板视图
 *
 * 注意:只有[paddingTop]属性有效
 *
 * 角度按照安卓坐标系的方向从0-360
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/27
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

class PointerPanelView(context: Context, attributeSet: AttributeSet? = null) :
    View(context, attributeSet) {

    val degreesGestureDetector: DegreesGestureDetector

    /**轨道背景颜色*/
    var trackBgColor: Int = "#E8E8E8".toColorInt()

    /**轨道进度*/
    var trackProgressStartColor: Int = "#FFC24B".toColorInt()
    var trackProgressEndColor: Int = "#FF8E24".toColorInt()

    /**轨道大小*/
    var trackSize: Int = 10.toDpi()

    /**
     * 轨道的半径
     * 用来推算原点坐标
     *
     * 只有将轨道绘制成圆,用户体验才好. 椭圆的体验不好.
     * */
    var trackRadius: Float = -1f

    /**轨道绘制开始的角度*/
    var trackStartAngle: Int = 200
    var trackEndAngle: Int = 340

    /**当前的进度*/
    var trackProgress: Float = 0f
        set(value) {
            field = value
            postInvalidate()
        }

    /**浮子*/
    var thumbDrawable: Drawable? = null
    /**浮子绘制在多少半径上, -1表示在轨道上*/
    var thumbRadius: Float = -1f
    /**浮子是否跟随进度,旋转*/
    var thumbEnableRotate: Boolean = true
    /**浮子额外旋转的角度*/
    var thumbRotateOffsetDegrees: Int = 90

    /**文本指示绘制回调*/
    var onPointerTextConfig: (degrees: Int, progress: Float, config: PointerTextConfig) -> Unit =
        { degrees, progress, config ->
            if (progress == 0.1f || progress == 0.9f || progress == 0.5f || progress == 0.3f || progress == 0.7f) {
                config.text = "$degrees°C"
            }
        }

    /**文本绘制时, 跨度多少*/
    var angleTextConfigStep = 1

    /**进度改变回调*/
    var onProgressChange: (progress: Float, fromUser: Boolean) -> Unit = { _, _ ->

    }

    val _pointerTextConfig = PointerTextConfig()

    val paint: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
        }
    }

    init {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.PointerPanelView)
        trackRadius =
            typedArray.getDimensionPixelOffset(R.styleable.PointerPanelView_p_track_radius, -1)
                .toFloat()

        trackSize =
            typedArray.getDimensionPixelOffset(R.styleable.PointerPanelView_p_track_size, trackSize)

        trackStartAngle =
            typedArray.getInt(R.styleable.PointerPanelView_p_track_start_angle, trackStartAngle)
        trackEndAngle =
            typedArray.getInt(R.styleable.PointerPanelView_p_track_end_angle, trackEndAngle)
        thumbDrawable = typedArray.getDrawable(R.styleable.PointerPanelView_p_thumb_drawable)
        angleTextConfigStep = typedArray.getInt(
            R.styleable.PointerPanelView_p_track_angle_text_step,
            angleTextConfigStep
        )

        setProgress(
            typedArray.getFloat(
                R.styleable.PointerPanelView_p_track_progress,
                trackProgress
            ), false
        )

        thumbRadius = typedArray.getDimensionPixelOffset(
            R.styleable.PointerPanelView_p_thumb_radius,
            thumbRadius.toInt()
        ).toFloat()

        typedArray.recycle()

        degreesGestureDetector = DegreesGestureDetector()

        degreesGestureDetector.onHandleEvent = { touchDegrees, rotateDegrees, touchDistance ->
            //i("handle:$touchDegrees $rotateDegrees")
            if (rotateDegrees <= 3) {
                val range = (trackStartAngle - 10)..(trackEndAngle + 10)
                touchDegrees in range ||
                        (touchDegrees + 360) in range
            } else {
                true
            }
        }

        degreesGestureDetector.onDegreesChange =
            { touchDegrees, touchDegreesQuadrant, rotateDegrees, touchDistance ->
                val range = trackStartAngle..trackEndAngle

                //i("$touchDegrees $touchDegreesQuadrant")

                var degress = if (touchDegreesQuadrant < 0) touchDegrees else touchDegreesQuadrant
                if (degreesGestureDetector._downQuadrant == 4 &&
                    degreesGestureDetector._downQuadrant == degreesGestureDetector._lastQuadrant
                ) {
                    //在第4象限按下, 并且未改变过象限
                    degress = touchDegrees + 360
                }

                if (degress in range) {

                    setProgress(
                        (degress - trackStartAngle.toFloat()) / (trackEndAngle.toFloat() - trackStartAngle.toFloat()),
                        true
                    )
                }

                //i("进度:$touchDegrees $touchDegreesQuadrant $trackProgress")
            }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        if (trackRadius < 0) {
            trackRadius = min(measuredWidth, measuredHeight) / 2f
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = trackSize.toFloat()
        paint.color = trackBgColor

        tempRectF.set(trackRectF)
        tempRectF.inset(paint.strokeWidth / 2, paint.strokeWidth / 2)

        //绘制背景
        paint.shader = null
        canvas.drawArc(
            tempRectF,
            trackStartAngle.toFloat(),
            trackEndAngle.toFloat() - trackStartAngle.toFloat(),
            false, paint
        )

        //绘制进度
        paint.shader = LinearGradient(
            tempRectF.left,
            0f,
            tempRectF.left + tempRectF.width() * trackProgress,
            0f,
            trackProgressStartColor,
            trackProgressEndColor,
            Shader.TileMode.CLAMP
        )

        canvas.drawArc(
            tempRectF,
            trackStartAngle.toFloat(),
            (trackEndAngle - trackStartAngle) * trackProgress,
            false,
            paint
        )

        paint.shader = null
        paint.style = Paint.Style.FILL

        //绘制文本
        for (angle in trackStartAngle..trackEndAngle step angleTextConfigStep) {
            _pointerTextConfig.reset()
            onPointerTextConfig(
                angle,
                (angle - trackStartAngle).toFloat() / (trackEndAngle - trackStartAngle),
                _pointerTextConfig
            )
            _pointerTextConfig.apply {
                if (text?.isNotEmpty() == true) {
                    paint.color = textColor
                    paint.textSize = textSize

                    val textWidth = paint.measureText(text)

                    val pointF = dotDegrees(
                        trackRadius - trackSize / 2 - textWidth, angle,
                        trackRectF.centerX().toInt(),
                        trackRectF.centerY().toInt()
                    )

                    canvas.drawText(
                        text!!,
                        pointF.x - textWidth / 2 + textOffsetX,
                        pointF.y + textOffsetY,
                        paint
                    )
                }
            }
        }

        val angle = trackStartAngle + (trackEndAngle - trackStartAngle) * trackProgress

        //绘制浮子
        thumbDrawable?.apply {
            val radius = if (thumbRadius >= 0) thumbRadius else trackRadius - trackSize / 2
            val pointF = dotDegrees(
                radius, angle.toInt(),
                trackRectF.centerX().toInt(),
                trackRectF.centerY().toInt()
            )

            val left = (pointF.x - minimumWidth / 2).toInt()
            val top = (pointF.y - minimumHeight / 2).toInt()

            setBounds(
                left,
                top,
                left + minimumWidth,
                top + minimumHeight
            )
            if (thumbEnableRotate) {
                canvas.save()
                canvas.rotate(angle + thumbRotateOffsetDegrees, pointF.x, pointF.y)
                draw(canvas)
                canvas.restore()
            } else {
                draw(canvas)
            }
        }

        //i("${(trackEndAngle - trackStartAngle) * trackProgress}")
    }

    val drawRectF = RectF()
        get() {
            field.set(
                paddingLeft.toFloat(),
                paddingTop.toFloat(),
                (measuredWidth - paddingRight).toFloat(),
                (measuredHeight - paddingBottom).toFloat()
            )
            return field
        }

    val trackRectF = RectF()
        get() {
            field.set(
                drawRectF.centerX() - trackRadius,
                drawRectF.top,
                drawRectF.centerX() + trackRadius,
                drawRectF.top + trackRadius * 2
            )
            return field
        }

    val tempRectF = RectF()

    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)

        if (!isEnabled) {
            return false
        }

        degreesGestureDetector.onTouchEvent(event, trackRectF.centerX(), trackRectF.centerY())
        return true

    }

    fun setProgress(progress: Float, fromUser: Boolean = false) {
        //i("进度:$progress")
        trackProgress = MathUtils.clamp(progress, 0f, 1f)
        onProgressChange(trackProgress, fromUser)
    }

    fun i(msg: String) {
        Log.i("angcyo", msg)
    }
}

data class PointerTextConfig(
    var text: String? = null,
    var textColor: Int = 0,
    var textSize: Float = 0f,
    var textOffsetX: Int = 0,
    var textOffsetY: Int = 0
)

fun PointerTextConfig.reset() {
    text = null
    textColor = "#333333".toColorInt()
    textSize = 10.toDp()
    textOffsetX = 0
    textOffsetY = 0
}

internal val dp: Float = Resources.getSystem()?.displayMetrics?.density ?: 0f
internal val dpi: Int = Resources.getSystem()?.displayMetrics?.density?.toInt() ?: 0

internal fun Int.toDp(): Float {
    return this * dp
}

internal fun Int.toDpi(): Int {
    return this * dpi
}
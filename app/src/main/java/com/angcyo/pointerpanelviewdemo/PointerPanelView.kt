package com.angcyo.pointerpanelviewdemo

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.toColorInt
import androidx.core.view.GestureDetectorCompat

/**
 * 指针面板视图
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/27
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

class PointerPanelView(context: Context, attributeSet: AttributeSet? = null) :
    View(context, attributeSet) {

    val gestureDetector: GestureDetectorCompat
    val rotateGestureDetector: RotateGestureDetector

    /**轨道背景颜色*/
    var trackBgColor: Int = "#E8E8E8".toColorInt()

    /**轨道进度*/
    var trackProgressStartColor: Int = "#FFC24B".toColorInt()
    var trackProgressEndColor: Int = "#FF8E24".toColorInt()

    /**轨道大小*/
    var trackSize = 20

    /**轨道绘制开始的角度*/
    var trackStartAngle: Float = 200f
    var trackEndAngle: Float = 340f

    /**当前的进度*/
    var trackProgress: Float = 0.0f

    val paint: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
        }
    }

    init {
        gestureDetector =
            GestureDetectorCompat(context, object : GestureDetector.SimpleOnGestureListener() {
                override fun onScroll(
                    e1: MotionEvent?,
                    e2: MotionEvent?,
                    distanceX: Float,
                    distanceY: Float
                ): Boolean {
                    parent.requestDisallowInterceptTouchEvent(true)
                    return super.onScroll(e1, e2, distanceX, distanceY)
                }

                override fun onDown(e: MotionEvent?): Boolean {
                    return super.onDown(e)
                }

                override fun onSingleTapUp(e: MotionEvent?): Boolean {
                    parent.requestDisallowInterceptTouchEvent(false)
                    return super.onSingleTapUp(e)
                }
            })

        rotateGestureDetector = RotateGestureDetector()
        rotateGestureDetector.onRotateListener = { angleStep, angle, _, _ ->
            //Log.i("angcyo", "旋转:$angleStep $angle")
            if (angle in trackStartAngle..trackEndAngle) {
                trackProgress = (angle - trackStartAngle) / (trackEndAngle - trackStartAngle)
                Log.i("angcyo", "旋转:$angleStep $angle $trackProgress")
                postInvalidate()
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor("#40000000".toColorInt())

        paint.strokeWidth = trackSize.toFloat()
        paint.color = trackBgColor

        tempRectF.set(drawRectF)
        tempRectF.inset(paint.strokeWidth / 2, paint.strokeWidth / 2)

        canvas.drawArc(
            tempRectF,
            trackStartAngle,
            trackEndAngle - trackStartAngle,
            false, paint
        )

        paint.color = trackProgressStartColor


//        paint.shader = LinearGradient(
//            0f,
//            0f,
//            measuredWidth.toFloat(),
//            0f,
//            trackProgressStartColor,
//            trackProgressEndColor, Shader.TileMode.CLAMP
//        )

        canvas.drawArc(
            tempRectF,
            trackStartAngle,
            (trackEndAngle - trackStartAngle) * trackProgress,
            false,
            paint
        )
    }


    val drawCenterX: Int
        get() = paddingLeft + (measuredWidth - paddingLeft - paddingRight) / 2


    val drawCenterY: Int
        get() = paddingTop + (measuredHeight - paddingTop - paddingBottom) / 2

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

    val tempRectF = RectF()

    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)

        rotateGestureDetector.startAngle = trackStartAngle
        rotateGestureDetector.endAngle = trackEndAngle
        rotateGestureDetector.onTouchEvent(event, drawCenterX, drawCenterY)
        return true
    }
}
package com.angcyo.pointerpanelviewdemo

import android.view.MotionEvent
import kotlin.math.atan2
import kotlin.math.roundToInt

/**
 * 旋转手势处理类
 */
class RotateGestureDetector {

    /**
     * 循环旋转(默认开启)
     */
    var isCycle = true
    /**
     * 当前旋转角度
     */
    var rotateAngle = 0f
    /**
     * 角度偏移值
     */
    var offsetAngle = 0f
    /**
     * 设置起始角,非循环旋转有效
     */
    var startAngle = 0f
    /**
     * 设置结束角,非循环旋转有效
     */
    var endAngle = 360f
    /**
     * 上次旋转角度
     */
    var _lastAngle = 0f
    /**
     * 是否正在旋转
     */
    var _isRotate = false

    /**
     * 旋转回调
     *
     * @param angleStep  旋转的角度
     * @param angle  当前手势对应的角度
     * @param pivotX 旋转中心点x坐标
     * @param pivotY 旋转中心点y坐标
     */
    var onRotateListener: (angleStep: Float, angle: Float, pivotX: Int, pivotY: Int) -> Unit =
        { _, _, _, _ ->

        }

    /**
     * 代理手势处理
     * @param pivotX 中心点坐标
     * @param pivotY 中心点坐标
     * */
    fun onTouchEvent(event: MotionEvent, pivotX: Int, pivotY: Int): Boolean {
        val pointerCount = event.pointerCount
        if (pointerCount == 1) {
            return doOnePointerRotate(event, pivotX, pivotY)
        } else if (pointerCount == 2) {
            return doTwoPointerRotate(event)
        }
        return false
    }

    /**
     * 一根手指绕中心点旋转
     */
    fun doOnePointerRotate(ev: MotionEvent, pivotX: Int, pivotY: Int): Boolean {
        val deltaX = ev.getX(0) - pivotX
        val deltaY = ev.getY(0) - pivotY
        val degrees = Math.toDegrees(
            atan2(
                deltaY.toDouble(),
                deltaX.toDouble()
            )
        ).roundToInt()
        doEvent(ev, pivotX, pivotY, degrees.toFloat())
        return true
    }

    /**
     * 两根手指绕中心点旋转
     */
    fun doTwoPointerRotate(ev: MotionEvent): Boolean {
        val pivotX = (ev.getX(0) + ev.getX(1)).toInt() / 2
        val pivotY = (ev.getY(0) + ev.getY(1)).toInt() / 2
        val deltaX = ev.getX(0) - ev.getX(1)
        val deltaY = ev.getY(0) - ev.getY(1)
        val degrees = Math.toDegrees(
            atan2(
                deltaY.toDouble(),
                deltaX.toDouble()
            )
        ).roundToInt()
        doEvent(ev, pivotX, pivotY, degrees.toFloat())
        return true
    }

    fun doEvent(ev: MotionEvent, pivotX: Int, pivotY: Int, degrees: Float) {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                _lastAngle = degrees
                _isRotate = false
            }
            MotionEvent.ACTION_UP -> _isRotate = false
            MotionEvent.ACTION_POINTER_DOWN -> {
                _lastAngle = degrees
                _isRotate = false
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_POINTER_UP -> {
                _isRotate = false
                upRotate(pivotX, pivotY)
                _lastAngle = degrees
            }
            MotionEvent.ACTION_MOVE -> {
                _isRotate = true
                val degreesValue = degrees - _lastAngle
                if (degreesValue > 45) {
                    rotate(-5f, degrees, pivotX, pivotY)
                } else if (degreesValue < -45) {
                    rotate(5f, degrees, pivotX, pivotY)
                } else {
                    rotate(degreesValue, degrees, pivotX, pivotY)
                }
                _lastAngle = degrees
            }
            else -> {
            }
        }
    }

    /**
     * 实时旋转回调
     */
    fun rotate(degree: Float, angle: Float, pivotX: Int, pivotY: Int) {
        rotateAngle += degree
        if (isCycle) {
            if (rotateAngle > 360) {
                rotateAngle -= 360
            } else if (rotateAngle < 0) {
                rotateAngle += 360
            }
        } else {
            if (rotateAngle < startAngle) {
                rotateAngle = startAngle
            } else if (rotateAngle > endAngle) {
                rotateAngle = endAngle
            }
        }
        onRotateListener(
            rotateAngle + offsetAngle,
            if (angle < 0) 360 + angle else angle,
            pivotX, pivotY
        )
    }

    /**
     * 手指抬起回调
     */
    fun upRotate(pivotX: Int, pivotY: Int) {

    }
}
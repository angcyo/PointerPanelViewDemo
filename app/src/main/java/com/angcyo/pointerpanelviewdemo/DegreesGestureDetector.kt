package com.angcyo.pointerpanelviewdemo

import android.graphics.Path
import android.view.MotionEvent

/**
 * 角度手势检查
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/28
 */

open class DegreesGestureDetector {

    /**是否要处理此次事件
     * [touchDegrees] 事件的角度[0-360]
     * [rotateDegrees] 事件距离上一次跨越旋转的角度
     * [touchDistance] 距离原点按下的距离
     * */
    var onHandleEvent: (touchDegrees: Int, rotateDegrees: Int, touchDistance: Double) -> Boolean =
        { _, _, _ ->
            true
        }

    /**
     * 角度改变的通知回调
     * [touchDegrees] 手势当前对应的角度
     * [touchDegreesQuadrant] 手势当前对应的角度, 根据1,4象限做了处理
     * [touchDistance] 手势当前与原点的距离
     * [rotateDegrees] touch down 开始, 累计旋转的角度. 逆时针为负值.
     * */
    var onDegreesChange: (
        touchDegrees: Int, touchDegreesQuadrant: Int,
        rotateDegrees: Int, touchDistance: Double
    ) -> Unit =
        { _, _, _, _ ->

        }

    /**
     * 旋转的方向, 顺时针 or 逆时针
     * */
    var _rotateDirection: Path.Direction = Path.Direction.CW

    /**从按下开始, 总共旋转的角度*/
    var _rotateDegrees: Int = 0

    //保存一些变量
    var _handleTouch = true
    var _downX: Float = 0f
    var _downY: Float = 0f
    var _downDegrees = 0
    var _downDistance = 0.0
    var _downQuadrant = 1

    var _moveX: Float = 0f
    var _moveY: Float = 0f
    var _lastDegrees = 0

    //上一次点, 所在的象限 (1,2,3,4)
    var _lastQuadrant = 1

    //move事件移动的距离
    var dx: Float = 0f
    var dy: Float = 0f

    //跨越象限的次数
    var _quadrantCount = 0

    /**接收事件 [pivotX] [pivotY] 中心点坐标位置*/
    fun onTouchEvent(event: MotionEvent, pivotX: Float, pivotY: Float): Boolean {
        val x = event.x
        val y = event.y
        val c = c(x, y, pivotX, pivotY)
        var d = degrees(x, y, pivotX, pivotY)
        val quadrant = _parseQuadrant(d)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                _rotateDegrees = 0
                _downX = x
                _downY = y
                _moveX = x
                _moveY = y
                _downDegrees = d
                _lastDegrees = d
                _downDistance = c
                _downQuadrant = quadrant
                _lastQuadrant = quadrant
                _quadrantCount = 0
                _handleTouch = onHandleEvent(d, 0, c)
            }
            MotionEvent.ACTION_MOVE -> {
                if (!_handleTouch) {
                    return false
                }

                val lastDegrees = _lastDegrees
                val mD = d

                dx = x - _moveX
                dy = y - _moveY

                _moveX = x
                _moveY = y

                _parseDirection()

                if (_lastQuadrant == 1 && quadrant == 4) {
                    _quadrantCount++
                } else if (_lastQuadrant == 4 && quadrant == 1) {
                    _quadrantCount--
                }
                d += 360 * _quadrantCount

                _lastQuadrant = quadrant

                val mDegrees = d - lastDegrees

                if (onHandleEvent(mD, mDegrees, c)) {
                    _lastDegrees = mD

                    _rotateDegrees += mDegrees

                    onDegreesChange(mD, d, _rotateDegrees, c)

                    //Log.i("angcyo", "角度:$lastDegrees -> $d $_rotateDegrees")
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                _handleTouch = true
            }
        }
        return true
    }

    fun _parseQuadrant(degrees: Int): Int {
        return when (degrees) {
            in 0..89 -> {
                //第四象限
                4
            }
            in 90..179 -> {
                //第三象限
                3
            }
            in 180..269 -> {
                //第二象限
                2
            }
            else -> {//in 270..359
                //第一象限
                1
            }
        }
    }

    fun _parseDirection() {
        when (_lastQuadrant) {
            4 -> {
                //第四象限
                _rotateDirection = if (dy > 0) Path.Direction.CW else Path.Direction.CCW
            }
            3 -> {
                //第三象限
                _rotateDirection = if (dy > 0) Path.Direction.CCW else Path.Direction.CW
            }
            2 -> {
                //第二象限
                _rotateDirection = if (dy > 0) Path.Direction.CCW else Path.Direction.CW
            }
            else -> {
                //第一象限
                _rotateDirection = if (dy > 0) Path.Direction.CW else Path.Direction.CCW
            }
        }
    }
}
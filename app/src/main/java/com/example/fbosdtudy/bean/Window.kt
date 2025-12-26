package com.example.fbosdtudy.bean

import kotlin.math.cos
import kotlin.math.sin

/**
 *@author Y³
 *@date 2025/5/8
 */
data class Window(
    var winType: Int = 0,
    var zIndex: Int = 0,
    var mask: String? = null,
    var isSelected: Boolean = false,

    var vLeft: Float,
    var vTop: Float,
    var vWidth: Float,
    var vHeight: Float,
    var vDegrees: Float,
    var vFlip: Int = 0,

    var tLeft: Float,
    var tTop: Float,
    var tWidth: Float,
    var tHeight: Float,
    var tDegrees: Float,


    //顶点坐标
    var vVerData: FloatArray = FloatArray(8),
    //纹理坐标
    var tVerData: FloatArray = FloatArray(4),
    //控制视图的点坐标
    var pVerData: FloatArray = FloatArray(16)
) {

    // 计算旋转后并归一化的顶点坐标
    fun generateRotatedVertexData(screenWidth: Float, screenHeight: Float): FloatArray {
        if (vDegrees == 0f) {
            val left = (vLeft / screenWidth) * 2f - 1f
            val right = ((vLeft + vWidth) / screenWidth) * 2f - 1f
            val top = 1f - (vTop / screenHeight) * 2f
            val bottom = 1f - ((vTop + vHeight) / screenHeight) * 2f

            // 顺时针顺序：左上、右上、右下、左下（用于射线检测）
            vVerData = floatArrayOf(
                left, top,     // 左上
                right, top,    // 右上
                right, bottom, // 右下
                left, bottom   // 左下
            )

            // 返回 GL_TRIANGLE_STRIP 顺序：左上、左下、右上、右下
            when (vFlip) {
                1 -> {
                    //左右翻转
                    return floatArrayOf(
                        right, top,    // 右上
                        right, bottom,  // 右下
                        left, top,     // 左上
                        left, bottom  // 左下
                    )
                }

                2 -> {
                    //上下翻转
                    return floatArrayOf(
                        left, bottom,  // 左下
                        left, top,     // 左上
                        right, bottom,  // 右下
                        right, top    // 右上
                    )
                }

                else -> {
                    return floatArrayOf(
                        left, top,     // 左上
                        left, bottom,  // 左下
                        right, top,    // 右上
                        right, bottom  // 右下
                    )
                }
            }

        } else {
            val centerX = vLeft + vWidth / 2f
            val centerY = vTop + vHeight / 2f
            val rad = vDegrees.degreesToRadians()
            val cosTheta = cos(rad)
            val sinTheta = sin(rad)
            val halfW = vWidth / 2f
            val halfH = vHeight / 2f

            // 顺时针顺序：左上、右上、右下、左下
            val clockwiseOffsets = arrayOf(
                -halfW to -halfH, // 左上
                halfW to -halfH, // 右上
                halfW to halfH, // 右下
                -halfW to halfH  // 左下
            )

            // 绘制顺序（GL_TRIANGLE_STRIP）：左上、左下、右上、右下
            val stripOffsets = arrayOf(
                -halfW to -halfH, // 左上
                -halfW to halfH, // 左下
                halfW to -halfH, // 右上
                halfW to halfH  // 右下
            )

            // 保存顺时针顶点（用于射线检测）
            vVerData = FloatArray(8).apply {
                clockwiseOffsets.forEachIndexed { i, (dx, dy) ->
                    val rotatedX = dx * cosTheta - dy * sinTheta
                    val rotatedY = dx * sinTheta + dy * cosTheta
                    val screenX = centerX + rotatedX
                    val screenY = centerY + rotatedY
                    this[i * 2] = (screenX / screenWidth) * 2f - 1f
                    this[i * 2 + 1] = 1f - (screenY / screenHeight) * 2f
                }
            }

            when (vFlip) {
                1 -> {
                    //左右翻转
                    return FloatArray(8).apply {
                        val flippedOffsets = arrayOf(
                            halfW to -halfH,
                            halfW to halfH,
                            -halfW to -halfH,
                            -halfW to halfH
                        )
                        flippedOffsets.forEachIndexed { i, (dx, dy) ->
                            val rotatedX = dx * cosTheta - dy * sinTheta
                            val rotatedY = dx * sinTheta + dy * cosTheta
                            val screenX = centerX + rotatedX
                            val screenY = centerY + rotatedY
                            this[i * 2] = (screenX / screenWidth) * 2f - 1f
                            this[i * 2 + 1] = 1f - (screenY / screenHeight) * 2f
                        }
                    }
                }

                2 -> {
                    //上下翻转
                    return FloatArray(8).apply {
                        val flippedOffsets = arrayOf(
                            -halfW to halfH,
                            -halfW to -halfH,
                            halfW to halfH,
                            halfW to -halfH
                        )
                        flippedOffsets.forEachIndexed { i, (dx, dy) ->
                            val rotatedX = dx * cosTheta - dy * sinTheta
                            val rotatedY = dx * sinTheta + dy * cosTheta
                            val screenX = centerX + rotatedX
                            val screenY = centerY + rotatedY
                            this[i * 2] = (screenX / screenWidth) * 2f - 1f
                            this[i * 2 + 1] = 1f - (screenY / screenHeight) * 2f
                        }
                    }
                }

                else -> {
                    // 返回绘制用顶点数据（GL_TRIANGLE_STRIP）
                    return FloatArray(8).apply {
                        stripOffsets.forEachIndexed { i, (dx, dy) ->
                            val rotatedX = dx * cosTheta - dy * sinTheta
                            val rotatedY = dx * sinTheta + dy * cosTheta
                            val screenX = centerX + rotatedX
                            val screenY = centerY + rotatedY
                            this[i * 2] = (screenX / screenWidth) * 2f - 1f
                            this[i * 2 + 1] = 1f - (screenY / screenHeight) * 2f
                        }
                    }
                }
            }

        }
    }

    // 计算旋转后的纹理坐标
    fun calculateRotatedTextureCoordinates(screenWidth: Float, screenHeight: Float): FloatArray {

        if (tDegrees == 0f) {
            val left = tLeft / screenWidth
            val right = (tLeft + tWidth) / screenWidth
            val top = tTop / screenHeight
            val bottom = (tTop + tHeight) / screenHeight
            tVerData = floatArrayOf(left, top, right, bottom)
            return floatArrayOf(
                left, bottom,  // 左下
                left, top,     // 左上
                right, bottom, // 右下
                right, top    // 右上

            )
        } else {
            val centerX = (tLeft + tWidth / 2f) / screenWidth
            val centerY = (tTop + tHeight / 2f) / screenHeight
            val rad = tDegrees.degreesToRadians()
            val cosTheta = cos(rad)
            val sinTheta = sin(rad)

            // 计算四个角的偏移量
            val halfW = tWidth / 2f
            val halfH = tHeight / 2f
            val offsets = arrayOf(
                -halfW to -halfH, // 左下
                -halfW to halfH,  // 左上
                halfW to -halfH,  // 右下
                halfW to halfH    // 右上
            )
            // 旋转后的纹理坐标
            return FloatArray(8).apply {
                offsets.forEachIndexed { i, (dx, dy) ->
                    val rotatedX = dx * cosTheta - dy * sinTheta
                    val rotatedY = dx * sinTheta + dy * cosTheta
                    val texX = centerX + rotatedX / screenWidth
                    val texY = 1f - (centerY + rotatedY / screenHeight) // <== Y 翻转
                    this[i * 2] = texX
                    this[i * 2 + 1] = texY
                }
            }
        }
    }

    //计算窗口的控制点坐标
    fun getRotatedControlVertexData(vertex: FloatArray): FloatArray {
        // 顺时针四角点
        val ltX = vertex[0]
        val ltY = vertex[1] // 左上

        val rtX = vertex[2]
        val rtY = vertex[3] // 右上

        val rbX = vertex[4]
        val rbY = vertex[5] // 右下

        val lbX = vertex[6]
        val lbY = vertex[7] // 左下

        // 4 条边中点（顺时针边的中点）
        val topX = (ltX + rtX) / 2f
        val topY = (ltY + rtY) / 2f

        val rightX = (rtX + rbX) / 2f
        val rightY = (rtY + rbY) / 2f

        val bottomX = (rbX + lbX) / 2f
        val bottomY = (rbY + lbY) / 2f

        val leftX = (lbX + ltX) / 2f
        val leftY = (lbY + ltY) / 2f

        // 返回顺序保持和输入顺时针一致，同时中点在相应边中间
        pVerData = floatArrayOf(
            ltX, ltY,        // 左上
            topX, topY,      // 上中
            rtX, rtY,        // 右上
            rightX, rightY,  // 右中
            rbX, rbY,        // 右下
            bottomX, bottomY,// 下中
            lbX, lbY,        // 左下
            leftX, leftY     // 左中
        )
        return pVerData
    }

    fun maskTexData(): FloatArray {
        return floatArrayOf(
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f
        )
    }


    private fun Float.degreesToRadians(): Float = this * (Math.PI.toFloat() / 180f)
}

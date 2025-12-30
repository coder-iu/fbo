package com.example.fbostudy.bean

import android.opengl.GLES30

/**
 *@author Y³
 *@date 2025/5/9
 */
data class VaoBean(
    val vaoType: Int,
    val vaoId: Int,
    val verVboId: Int,
    val texVboId: Int,
    val maskVboId: Int = 0,
    val maskTexId: Int = 0
) {
    fun release() {
        GLES30.glDeleteVertexArrays(1, intArrayOf(vaoId), 0)
        GLES30.glDeleteBuffers(1, intArrayOf(verVboId), 0)
        GLES30.glDeleteBuffers(1, intArrayOf(texVboId), 0)
        GLES30.glDeleteBuffers(1, intArrayOf(maskVboId), 0)
        if (maskTexId != 0) {
            GLES30.glDeleteTextures(1, intArrayOf(maskTexId), 0)
        }
    }
}

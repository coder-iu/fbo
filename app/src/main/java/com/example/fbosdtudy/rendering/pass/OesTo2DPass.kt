package com.example.fbosdtudy.rendering.pass

import android.content.Context
import android.opengl.GLES11Ext
import android.opengl.GLES30
import com.example.fbosdtudy.R
import com.example.fbosdtudy.rendering.OpenGLHelper
import com.example.fbosdtudy.rendering.pipeline.FboRenderPass

/**
 * @author Y³
 * @date 2025/12/27
 * 第一步.把OES转场2D纹理进入FBO管道传递
 */
class OesTo2DPass(
    private val context: Context,
    helper: OpenGLHelper
) : FboRenderPass(helper) {

    private var samplerLoc = 0
    private var stMatrixLoc = 0
    private val stMatrix = FloatArray(16)

    override fun createProgram(): Int {
        val v = helper.readShaderFromRaw(context, R.raw.vertex_shader)
        val f = helper.readShaderFromRaw(context, R.raw.oes_to_2d_fragment)
        return helper.createShaderProgram(v, f)
    }

    override fun onProgramCreated() {
        samplerLoc = GLES30.glGetUniformLocation(programId, "uTextureSampler")
        stMatrixLoc = GLES30.glGetUniformLocation(programId, "uSTMatrix")
    }

    fun updateSTMatrix(matrix: FloatArray) {
        System.arraycopy(matrix, 0, stMatrix, 0, 16)
    }

    override fun bindInputTexture() {
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            inputTextureId
        )
        GLES30.glUniform1i(samplerLoc, 0)
        GLES30.glUniformMatrix4fv(stMatrixLoc, 1, false, stMatrix, 0)
    }
}

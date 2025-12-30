package com.example.fbostudy.rendering.pass

import android.content.Context
import android.opengl.GLES30
import com.example.fbostudy.R
import com.example.fbostudy.rendering.OpenGLHelper
import com.example.fbostudy.rendering.pipeline.FboRenderPass

/**
 * @author Y³
 * @date 2025/12/27
 * 画中画
 */
class PipPass(private val context: Context, helper: OpenGLHelper) : FboRenderPass(helper) {

    private var samplerLoc = 0

    override fun createProgram(): Int {
        val v = helper.readShaderFromRaw(context, R.raw.vertex_shader)
        val f = helper.readShaderFromRaw(context, R.raw.pip_fragment)
        return helper.createShaderProgram(v, f)
    }

    override fun onProgramCreated() {
        samplerLoc = GLES30.glGetUniformLocation(programId, "uTextureSampler")
    }

    override fun bindInputTexture() {
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, inputTextureId)
        GLES30.glUniform1i(samplerLoc, 0)
    }
}
package com.example.fbosdtudy.rendering.pass

import android.content.Context
import android.opengl.GLES30
import com.example.fbosdtudy.R
import com.example.fbosdtudy.rendering.OpenGLHelper
import com.example.fbosdtudy.rendering.pipeline.FboRenderPass

/**
 * @author Y³
 * @date 2025/12/27
 * 色相控制
 */
class HuePass(
    private val context: Context,
    helper: OpenGLHelper
) : FboRenderPass(helper) {

    var hue: Float = 0f //[0,1]
    private var uHueLoc = 0
    private var uTextureSamplerLoc = 0
    override fun createProgram(): Int {
        val vertexShader = helper.readShaderFromRaw(context, R.raw.vertex_shader)
        val fragmentShader = helper.readShaderFromRaw(context, R.raw.color_hue_fragment)
        return helper.createShaderProgram(vertexShader, fragmentShader)
    }

    override fun onProgramCreated() {
        uHueLoc = GLES30.glGetUniformLocation(programId, "hue")
        uTextureSamplerLoc = GLES30.glGetUniformLocation(programId, "uTextureSampler")
    }

    override fun bindInputTexture() {
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, inputTextureId)
        GLES30.glUniform1i(uTextureSamplerLoc, 0)
        GLES30.glUniform1f(uHueLoc, hue)
    }

}
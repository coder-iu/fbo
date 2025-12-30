package com.example.fbostudy.rendering.pass

import android.content.Context
import android.opengl.GLES30
import com.example.fbostudy.R
import com.example.fbostudy.rendering.OpenGLHelper
import com.example.fbostudy.rendering.pipeline.RenderPass

/**
 * @author Y³
 * @date 2025/12/27
 * 上屏
 */
class OutputPass(
    private val context: Context,
    private val helper: OpenGLHelper
) : RenderPass() {

    private var programId = 0
    private var vaoId = 0
    private var samplerLoc = 0

    override fun onInit(width: Int, height: Int) {
        vaoId = helper.createFullScreenVao()
        val v = helper.readShaderFromRaw(context, R.raw.vertex_shader)
        val f = helper.readShaderFromRaw(context, R.raw.output_fragment)
        programId = helper.createShaderProgram(v, f)

        samplerLoc = GLES30.glGetUniformLocation(programId, "uTextureSampler")
    }

    override fun onDraw() {
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)

        GLES30.glUseProgram(programId)
        GLES30.glBindVertexArray(vaoId)

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, inputTextureId)
        GLES30.glUniform1i(samplerLoc, 0)

        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)
        GLES30.glBindVertexArray(0)
    }

    override fun onRelease() {
        helper.releaseVAO(vaoId)
        GLES30.glDeleteProgram(programId)
    }

}
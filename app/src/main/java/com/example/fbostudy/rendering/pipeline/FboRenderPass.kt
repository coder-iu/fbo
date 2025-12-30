package com.example.fbostudy.rendering.pipeline

import android.opengl.GLES30
import com.example.fbostudy.rendering.OpenGLHelper

/**
 * @author Y³
 * @date 2025/12/27
 */
abstract class FboRenderPass(
    protected val helper: OpenGLHelper
) : RenderPass() {
    protected var programId = 0
    protected var vaoId = 0
    protected var fboId = 0
    protected var glWidth = 0
    protected var glHeight = 0
    protected var fboTextureId = 0
    override fun onInit(width: Int, height: Int) {
        glWidth = width
        glHeight = height

        vaoId = helper.createFullScreenVao()
        val fbo = helper.createFBO(width, height)
        fboId = fbo.first
        fboTextureId = fbo.second
        outputTextureId = fboTextureId

        programId = createProgram()
        onProgramCreated()
    }

    override fun onDraw() {
        if (!enabled) {
            outputTextureId = inputTextureId
            return
        }

        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, fboId)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

        GLES30.glUseProgram(programId)
        GLES30.glBindVertexArray(vaoId)

        bindInputTexture()
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)

        GLES30.glBindVertexArray(0)
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
    }

    override fun onRelease() {
        helper.releaseVAO(vaoId)
        helper.releaseFBO(fboId, fboTextureId)
        GLES30.glDeleteProgram(programId)
    }

    protected abstract fun createProgram(): Int
    protected abstract fun onProgramCreated()
    protected abstract fun bindInputTexture()
}
package com.example.fbostudy.rendering.pass

import android.content.Context
import android.opengl.GLES30
import com.example.fbostudy.R
import com.example.fbostudy.bean.VaoBean
import com.example.fbostudy.bean.Window
import com.example.fbostudy.rendering.OpenGLHelper
import com.example.fbostudy.rendering.pipeline.FboRenderPass

/**
 * @author Y³
 * @date 2025/12/27
 * 上屏前最后一步.可能有多个窗口
 */
class MultiWindowPass(
    private val context: Context,
    helper: OpenGLHelper
) : FboRenderPass(helper) {

    var dataUpdated = false
    var drawWindowList: MutableList<Window> = arrayListOf()
    private var vaoData: MutableList<VaoBean> = arrayListOf()
    private var aPositionLocation = 0
    private var aTextureCoLocation = 0
    private var uTextureSamplerLocation = 0


    override fun createProgram(): Int {
        val vertexShader = helper.readShaderFromRaw(context, R.raw.vertex_shader)
        val fragmentShader = helper.readShaderFromRaw(context, R.raw.multi_window_fragment)
        return OpenGLHelper.createShaderProgram(vertexShader, fragmentShader)
    }

    override fun onProgramCreated() {
        aPositionLocation = GLES30.glGetAttribLocation(programId, "aPosition")
        aTextureCoLocation = GLES30.glGetAttribLocation(programId, "aTextureCo")
        uTextureSamplerLocation = GLES30.glGetUniformLocation(programId, "uTextureSampler")
    }

    override fun bindInputTexture() {
        if (dataUpdated) {
            vaoData.forEach { helper.releaseVAO(it.vaoId) }
            vaoData.clear()
            drawWindowList.sortedBy { it.zIndex }.forEach { win ->
                val vao = helper.generateWinVao(
                    glWidth, glHeight,
                    aPositionLocation, aTextureCoLocation,
                    win
                )
                vaoData.add(vao)
            }
            dataUpdated = false
        }

        vaoData.forEach { vao ->
            GLES30.glBindVertexArray(vao.vaoId)
            GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, inputTextureId)
            GLES30.glUniform1i(uTextureSamplerLocation, 0)
            helper.draw(GLES30.GL_TRIANGLE_STRIP, 4)
            GLES30.glBindVertexArray(0)
        }
    }
}
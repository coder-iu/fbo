package com.example.fbosdtudy.rendering

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.util.Log
import android.view.Surface
import com.example.fbosdtudy.R
import com.example.fbosdtudy.bean.VaoBean
import com.example.fbosdtudy.bean.Window
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 *@author Y³
 *@date 2025/12/26
 */
class GLRenderer(
    private val context: Context,
    private val mOpenGLHelper: OpenGLHelper = OpenGLHelper,
) : GLSurfaceView.Renderer {
    private lateinit var surfaceTexture: SurfaceTexture
    private var programId: Int = 0
    private var splitScreenProgramId: Int = 0
    private var splitTextureSamplerLocation: Int = 0
    private var splitSTMatrixLocation: Int = 0
    private var pipTextureSamplerLocation: Int = 0
    private var pipProgramId: Int = 0
    private var programFinalId: Int = 0
    private var onSurfaceCreatedCallback: ((Surface) -> Unit)? = null
    private var requestRenderCallback: (() -> Unit)? = null
    private var mScreenW = 0
    private var mScreenH = 0

    private var dataUpdated: Boolean = false
    private var vaoData: MutableList<VaoBean> = arrayListOf()
    private var aPositionLocation = 0
    private var aTextureCoLocation = 0
    private var uTextureSamplerLocation = 0
    private var uTextureSamplerFinalLocation = 0
    private var uSTMatrixLocation = 0
    private var textureId = 0
    private var drawWindowList: MutableList<Window> = arrayListOf()

    // FBO
    private var fbo1Id = 0
    private var fbo1TextureId = 0
    private var fbo2Id = 0
    private var fbo2TextureId = 0
    private var screenVaoId = 0

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        //创建program
        val vertexShader = mOpenGLHelper.readShaderFromRaw(context, R.raw.vertex_shader)
        val fragmentShader = mOpenGLHelper.readShaderFromRaw(context, R.raw.origin_shader)
        programId = OpenGLHelper.createShaderProgram(vertexShader, fragmentShader)
        aPositionLocation = GLES30.glGetAttribLocation(programId, "aPosition")
        aTextureCoLocation = GLES30.glGetAttribLocation(programId, "aTextureCo")
        uTextureSamplerLocation = GLES30.glGetUniformLocation(programId, "uTextureSampler")
        uSTMatrixLocation = GLES30.glGetUniformLocation(programId, "uSTMatrix")

        // 分屏 shader
        val splitFragment = mOpenGLHelper.readShaderFromRaw(context, R.raw.split_screen_fragment)
        splitScreenProgramId = OpenGLHelper.createShaderProgram(vertexShader, splitFragment)
        splitTextureSamplerLocation =
            GLES30.glGetUniformLocation(splitScreenProgramId, "uTextureSampler")
        splitSTMatrixLocation = GLES30.glGetUniformLocation(splitScreenProgramId, "uSTMatrix")

        // 画中画 shader
        val pipFragment = mOpenGLHelper.readShaderFromRaw(context, R.raw.pip_fragment)
        pipProgramId = OpenGLHelper.createShaderProgram(vertexShader, pipFragment)
        pipTextureSamplerLocation = GLES30.glGetUniformLocation(pipProgramId, "uTextureSampler")

        //最终上屏
        val inputFragment = mOpenGLHelper.readShaderFromRaw(context, R.raw.input_fragment)
        programFinalId = OpenGLHelper.createShaderProgram(vertexShader, inputFragment)
        uTextureSamplerFinalLocation =
            GLES30.glGetUniformLocation(programFinalId, "uTextureSampler")


        // 全屏 VAO
        screenVaoId = mOpenGLHelper.createFullScreenVao()


        //创建纹理.并回调
        textureId = mOpenGLHelper.createOESTexture()
        surfaceTexture = SurfaceTexture(textureId).apply {
            setOnFrameAvailableListener {
                requestRenderCallback?.invoke()
            }
        }
        val surface = Surface(surfaceTexture)
        onSurfaceCreatedCallback?.invoke(surface)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        mScreenW = width
        mScreenH = height

        val fbo1 = mOpenGLHelper.createFBO(width, height)
        fbo1Id = fbo1.first
        fbo1TextureId = fbo1.second

        val fbo2 = mOpenGLHelper.createFBO(width, height)
        fbo2Id = fbo2.first
        fbo2TextureId = fbo2.second
    }


    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClearColor(0f, 0f, 0f, 1f)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
        if (dataUpdated) {
            vaoData.forEach { vao ->
                mOpenGLHelper.releaseVAO(vao.vaoId)
                mOpenGLHelper.releaseVBO(vao.verVboId)
                mOpenGLHelper.releaseVBO(vao.texVboId)
            }
            vaoData.clear()
            drawWindowList.sortedBy { it.zIndex }.forEach { win ->
                val vao = mOpenGLHelper.generateWinVao(
                    mScreenW, mScreenH,
                    aPositionLocation, aTextureCoLocation,
                    win
                )
                vaoData.add(vao)
            }
            dataUpdated = false
        }
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, fbo1Id)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
        drawVideoToSplitScreenFBO()
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)

        // 2️⃣ FBO1 → 画中画处理 → FBO2
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, fbo2Id)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
        drawSplitScreenFBOToPiPFBO()
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
        if (vaoData.isNotEmpty()) {
            for (vao in vaoData) {
                when (vao.vaoType) {
                    0 -> {
                        drawMainVideo(vao)
                    }
                }

            }
        }
    }

    private fun drawMainVideo(vao: VaoBean) {
        GLES30.glUseProgram(programFinalId)
//        val stMatrix = FloatArray(16)
//        surfaceTexture.getTransformMatrix(stMatrix)
//        GLES30.glUniformMatrix4fv(uSTMatrixLocation, 1, false, stMatrix, 0)
        GLES30.glBindVertexArray(vao.vaoId)
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, fbo2TextureId)
        GLES30.glUniform1i(uTextureSamplerFinalLocation, 0)
        mOpenGLHelper.draw(GLES30.GL_TRIANGLE_STRIP, 4)
        GLES30.glBindVertexArray(0)
    }


    // 绘制原视频到分屏 FBO
    private fun drawVideoToSplitScreenFBO() {
        surfaceTexture.updateTexImage()
        val stMatrix = FloatArray(16)
        surfaceTexture.getTransformMatrix(stMatrix)

        GLES30.glUseProgram(splitScreenProgramId)
        GLES30.glBindVertexArray(screenVaoId)

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        GLES30.glUniform1i(splitTextureSamplerLocation, 0)
        GLES30.glUniformMatrix4fv(splitSTMatrixLocation, 1, false, stMatrix, 0)
        OpenGLHelper.draw(GLES30.GL_TRIANGLE_STRIP, 4)
        GLES30.glBindVertexArray(0)
    }


    // 绘制分屏 FBO -> 画中画 FBO
    private fun drawSplitScreenFBOToPiPFBO() {
        GLES30.glUseProgram(pipProgramId)
        GLES30.glBindVertexArray(screenVaoId)

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, fbo1TextureId)
        GLES30.glUniform1i(pipTextureSamplerLocation, 0)

        OpenGLHelper.draw(GLES30.GL_TRIANGLE_STRIP, 4)
        GLES30.glBindVertexArray(0)
    }

    fun setOnSurfaceReady(callback: (Surface) -> Unit) {
        onSurfaceCreatedCallback = callback
    }

    fun setOnFrameAvailable(callback: () -> Unit) {
        requestRenderCallback = callback
    }

    fun updateWindows(windows: MutableList<Window>?) {
        drawWindowList = windows ?: arrayListOf()
        dataUpdated = true
    }


    fun release() {
        for (vao in vaoData) {
            mOpenGLHelper.releaseVAO(vao.vaoId)
            mOpenGLHelper.releaseVBO(vao.verVboId)
            mOpenGLHelper.releaseVBO(vao.texVboId)
        }
        vaoData.clear()
        drawWindowList.clear()
    }
}
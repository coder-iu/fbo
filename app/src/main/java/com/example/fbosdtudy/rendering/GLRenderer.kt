package com.example.fbosdtudy.rendering

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.view.Surface
import com.example.fbosdtudy.bean.Window
import com.example.fbosdtudy.rendering.pass.BrightnessPass
import com.example.fbosdtudy.rendering.pass.HuePass
import com.example.fbosdtudy.rendering.pass.MultiWindowPass
import com.example.fbosdtudy.rendering.pass.OesTo2DPass
import com.example.fbosdtudy.rendering.pass.OutputPass
import com.example.fbosdtudy.rendering.pass.PipPass
import com.example.fbosdtudy.rendering.pass.SplitScreenPass
import com.example.fbosdtudy.rendering.pipeline.RenderPipeline
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 *@author Y³
 *@date 2025/12/26
 */
class GLRenderer(
    private val context: Context,
) : GLSurfaceView.Renderer {
    private lateinit var surfaceTexture: SurfaceTexture
    private var oesTextureId = 0
    private lateinit var pipeline: RenderPipeline
    private var onSurfaceCreatedCallback: ((Surface) -> Unit)? = null
    private var requestRenderCallback: (() -> Unit)? = null

    /**
     * 管道喧然
     * OES → OesTo2DPass → SplitPass → PipPass → huePass->BrightnessPass-> MultiWindowPass→ OutputPass
     */
    private lateinit var oesTo2DPass: OesTo2DPass
    private lateinit var multiWindowPass: MultiWindowPass
    private lateinit var splitPass: SplitScreenPass
    private lateinit var pipPass: PipPass
    private lateinit var huePass: HuePass
    private lateinit var brightnessPass: BrightnessPass
    private lateinit var outputPass: OutputPass


    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        //创建纹理.并回调
        oesTextureId = OpenGLHelper.createOESTexture()
        surfaceTexture = SurfaceTexture(oesTextureId).apply {
            setOnFrameAvailableListener {
                requestRenderCallback?.invoke()
            }
        }
        val surface = Surface(surfaceTexture)
        onSurfaceCreatedCallback?.invoke(surface)

        //把OES视频纹理转成2D纹理,必须add
        oesTo2DPass = OesTo2DPass(context, OpenGLHelper)
        //分屏效果
        splitPass = SplitScreenPass(context, OpenGLHelper)
        //画中画效果
        pipPass = PipPass(context, OpenGLHelper)
        //亮度
        brightnessPass = BrightnessPass(context, OpenGLHelper)
        //色相
        huePass = HuePass(context, OpenGLHelper)
        //多窗口
        multiWindowPass = MultiWindowPass(context, OpenGLHelper)
        //上屏,必须add
        outputPass = OutputPass(context, OpenGLHelper)
        //全部特显先添加进管道. 绘制时用pass.enable控制是否启用
        pipeline = RenderPipeline()
            .add(oesTo2DPass)
            .add(splitPass)
            .add(pipPass)
            .add(huePass)
            .add(brightnessPass)
            .add(multiWindowPass)
            .add(outputPass)

    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        pipeline.init(width, height)
    }


    override fun onDrawFrame(gl: GL10?) {
        surfaceTexture.updateTexImage()

        val stMatrix = FloatArray(16)
        surfaceTexture.getTransformMatrix(stMatrix)
        oesTo2DPass.updateSTMatrix(stMatrix)

        pipPass.enabled = false
        splitPass.enabled = false
        huePass.enabled = true
        brightnessPass.enabled = true
        multiWindowPass.enabled = true

        pipeline.draw(oesTextureId)
    }


    fun setOnSurfaceReady(callback: (Surface) -> Unit) {
        onSurfaceCreatedCallback = callback
    }

    fun setOnFrameAvailable(callback: () -> Unit) {
        requestRenderCallback = callback
    }

    fun updateWindows(windows: MutableList<Window>?) {
        multiWindowPass.drawWindowList = windows ?: arrayListOf()
        multiWindowPass.dataUpdated = true
    }

    fun release() {
        pipeline.release()
    }
}
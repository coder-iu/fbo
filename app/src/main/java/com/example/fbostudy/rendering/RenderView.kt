package com.example.fbostudy.rendering

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet

/**
 *@author Y³
 *@date 2025/12/26
 */
class RenderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : GLSurfaceView(context, attrs) {
    var renderer: GLRenderer? = null

    init {
        setEGLContextClientVersion(3)
        setEGLConfigChooser(8, 8, 8, 8, 16, 8)
        if (renderer == null) {
            renderer = GLRenderer(context)
        }
        setRenderer(renderer)
        renderMode = RENDERMODE_WHEN_DIRTY
    }
}
package com.example.fbostudy.rendering.pipeline

/**
 * @author Y³
 * @date 2025/12/27
 * RenderPass = 输入纹理 → 输出纹理
 */
abstract class RenderPass {

    var inputTextureId: Int = 0

    var outputTextureId: Int = 0
        protected set

    var enabled: Boolean = true

    abstract fun onInit(width: Int, height: Int)

    abstract fun onDraw()

    abstract fun onRelease()
}
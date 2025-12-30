package com.example.fbostudy.rendering.pipeline

/**
 * @author Y³
 * @date 2025/12/27
 */
class RenderPipeline {
    private val passes = mutableListOf<RenderPass>()

    fun add(pass: RenderPass): RenderPipeline {
        passes.add(pass)
        return this
    }

    fun init(width: Int, height: Int) {
        passes.forEach { it.onInit(width, height) }
    }

    fun draw(inputTexture: Int) {
        var currentTex = inputTexture

        for (pass in passes) {
            pass.inputTextureId = currentTex
            pass.onDraw()
            currentTex = pass.outputTextureId
        }
    }

    fun release() {
        passes.forEach { it.onRelease() }
        passes.clear()
    }
}
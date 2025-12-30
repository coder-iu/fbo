package com.example.fbostudy.bean

import android.graphics.SurfaceTexture
import android.view.Surface

/**
 *@author Y³
 *@date 2025/5/8
 */
data class TextureItem(
    //textureType 0->视频  1->图片
    val textureType: Int,
    val programId: Int,
    val textureId: Int,
    val surfaceTexture: SurfaceTexture?,
    val surface: Surface?,
    var isEmptyTex: Boolean = true
)



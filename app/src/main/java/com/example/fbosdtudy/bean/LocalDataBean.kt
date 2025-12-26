package com.example.fbosdtudy.bean

/**
 *@author Y³
 *@date 2025/5/15
 */
class LocalDataBean(
    var screenWidth: Int = 0,
    var screenHeight: Int = 0,
    var isLandscape: Boolean = false,
    var canvasColor: FloatArray = floatArrayOf(),
    var winInfo: MutableList<Window>? = null,
    var deviceLimit: Boolean = false
)

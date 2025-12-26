package com.example.fbosdtudy.bean

/**
 *@author Y³
 *@date 2025/4/21
 */
data class LaunchDataBean(
    val code: Int?,
    val message: String?,
    val result: Result?
) {
    data class Result(
        val deviceStatus: Int,
        val appStatus: Int
    )
}



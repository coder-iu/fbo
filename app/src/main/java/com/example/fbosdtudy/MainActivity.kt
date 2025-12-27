package com.example.fbosdtudy

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.fbosdtudy.base.BaseActivity
import com.example.fbosdtudy.bean.Window
import com.example.fbosdtudy.databinding.ActivityMainBinding
import com.example.fbosdtudy.rendering.RenderView

class MainActivity : BaseActivity<ActivityMainBinding>(), Player.Listener {

    private lateinit var renderView: RenderView

    override val bindingInflater: (LayoutInflater) -> ActivityMainBinding
        get() = ActivityMainBinding::inflate

    override fun initView() {
        renderView = binding.renderView
        initPlayer()
    }

    private fun initMediaWindows() {
        val windows: MutableList<Window>? = mutableListOf(
            Window(
                vLeft = 0f,
                vTop = 0f,
                vWidth = 960f,
                vHeight = 540f,
                winType = 0,
                vDegrees = 0f,
                vFlip = 0,
                mask = "",

                tDegrees = 0f,
                tLeft = 0f,
                tTop = 0f,
                tWidth = 1920f,
                tHeight = 1080f,
            ),  Window(
                vLeft = 960f,
                vTop = 540f,
                vWidth = 960f,
                vHeight = 540f,
                winType = 0,
                vDegrees = 0f,
                vFlip = 0,
                mask = "",

                tDegrees = 0f,
                tLeft = 0f,
                tTop = 0f,
                tWidth = 1920f,
                tHeight = 1080f,
            )
        )
        renderView.renderer?.updateWindows(windows)
    }

    private fun initPlayer() {
        val assetManager = assets
        val player = ExoPlayer.Builder(this).build().apply {
            repeatMode = Player.REPEAT_MODE_ALL
            volume = 0f
            addListener(this@MainActivity)
        }
        val videoFiles = assetManager.list("video") ?: return
        val mediaItems = videoFiles
            .filter { it.endsWith(".mp4") || it.endsWith(".mkv") || it.endsWith(".mov") }
            .map { fileName ->
                MediaItem.fromUri("asset:///video/$fileName")
            }
        if (mediaItems.isNotEmpty()) {
            player.setMediaItems(mediaItems)
            player.prepare()
        }
        renderView.renderer?.setOnSurfaceReady { surface ->
            Handler(Looper.getMainLooper()).post {
                initMediaWindows()
                player.setVideoSurface(surface)
                player.playWhenReady = true
            }
        }
        renderView.renderer?.setOnFrameAvailable {
            renderView.requestRender()
        }
    }
}
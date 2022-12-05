package com.hslxywyjw.musicplayer

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper

class MusicService : Service() {
    private var myBinder = MyBinder()
    var mediaPlayer: MediaPlayer? = null

    //    任务Runnable定义了一个可以独立运行的代码片段
    private lateinit var runnable: Runnable


    override fun onBind(p0: Intent?): IBinder? {
//        showNotification(R.drawable.pause_icon)
        return myBinder
    }

    inner class MyBinder : Binder() {
        fun currentService(): MusicService {
            return this@MusicService
        }

    }


    //播放
    fun createMudiaPlayer() {
        try {
            if (PlayerActivity.musicService!!.mediaPlayer == null) PlayerActivity.musicService!!.mediaPlayer =
                MediaPlayer()
//                !!相当于Java里的if()else()
            PlayerActivity.musicService!!.mediaPlayer!!.reset()
            PlayerActivity.musicService!!.mediaPlayer!!.setDataSource(PlayerActivity.musiclistPA[PlayerActivity.songPostion].path)
            PlayerActivity.musicService!!.mediaPlayer!!.prepare()
            PlayerActivity.binding.playPauserBtnPA.setIconResource(R.drawable.pause_icon)
            PlayerActivity.binding.tvSeekBarStart.text =
                formatDuration(mediaPlayer!!.currentPosition.toLong())
            PlayerActivity.binding.tvSeekBarEnd.text =
                formatDuration(mediaPlayer!!.duration.toLong())
            PlayerActivity.binding.seekBarPA.progress = 0
            PlayerActivity.binding.seekBarPA.max =
                mediaPlayer!!.duration
            PlayerActivity.nowPlayingId = PlayerActivity.musiclistPA[PlayerActivity.songPostion].id
        } catch (e: Exception) {
            return
        }
    }
    //进度条
    fun seekBarSetup() {
        runnable= Runnable {
            PlayerActivity.binding.tvSeekBarStart.text =
                formatDuration(mediaPlayer!!.currentPosition.toLong())
            PlayerActivity.binding.seekBarPA.progress = mediaPlayer!!.currentPosition
            Handler(Looper.getMainLooper()).postDelayed(runnable,200)
        }
        Handler(Looper.getMainLooper()).postDelayed(runnable,0)
    }
}
package com.hslxywyjw.musicplayer

import android.media.MediaMetadataRetriever
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

//歌曲类
data class Music(
    val id: String,
    val title: String,
//    专辑
    val album: String,
    val artist: String,
    val duration: Long = 0,
    val path: String,
    val artUri: String
)

//歌曲时长格式化
fun formatDuration(duration: Long): String {
    val minutes = TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)
    val seconds = (TimeUnit.SECONDS.convert(
        duration,
        TimeUnit.MILLISECONDS
    )) - minutes * TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES)
    //格式化格式
    return String.format("%02d:%02d", minutes, seconds)
}
//获取图片
fun getImgArt(path: String): ByteArray? {
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(path)
    return retriever.embeddedPicture
}

//当前歌曲是否在首或尾
fun setSongPostion(increment: Boolean) {
    //页面重复执行
    if (!PlayerActivity.repeat) {
        if (increment) {
            if (PlayerActivity.musiclistPA.size - 1 == PlayerActivity.songPostion)
                PlayerActivity.songPostion = 0
            else ++PlayerActivity.songPostion
        } else {
            if (PlayerActivity.songPostion == 0)
                PlayerActivity.songPostion = PlayerActivity.musiclistPA.size - 1
            else --PlayerActivity.songPostion
        }
    }
}

//退出应用
fun exitApplication() {
//    if (PlayerActivity.musicService != null) {
//        PlayerActivity.musicService!!.stopForeground(true)
//        PlayerActivity.musicService!!.mediaPlayer!!.release()
//        PlayerActivity.musicService = null
//        exitProcess(1)
//    }
    exitProcess(1)
}

//喜欢按钮
fun favouriteChecker(id: String): Int {
    PlayerActivity.isFavourite = false
    FavouriteActivity.favouriteSongs.forEachIndexed { index, music ->
        if (id == music.id) {
            PlayerActivity.isFavourite = true
            return index
        }
    }
    return -1
}

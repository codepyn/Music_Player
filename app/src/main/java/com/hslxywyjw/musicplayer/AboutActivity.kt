package com.hslxywyjw.musicplayer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
//关于页面
class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_MusicPlayer)
        setTitle("关于")
        setContentView(R.layout.activity_about)
    }
}
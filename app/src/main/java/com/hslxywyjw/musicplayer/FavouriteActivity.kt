package com.hslxywyjw.musicplayer

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.hslxywyjw.musicplayer.databinding.ActivityFavouriteBinding
//喜欢页面
class FavouriteActivity : AppCompatActivity() {

    //页面绑定
    private lateinit var binding: ActivityFavouriteBinding
    //适配器
    private lateinit var adapter: FavouriteAdapter

    companion object {
        //喜欢的歌曲列表
        var favouriteSongs: ArrayList<Music> = ArrayList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //设置主题和标题
        setTheme(R.style.Theme_MusicPlayer)
        setTitle("我喜欢的音乐")
        binding = ActivityFavouriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //返回按钮
        binding.backBtnFA.setOnClickListener { finish() }
        //recycleview设置适应尺寸
        binding.favouriteRV.setHasFixedSize(true)
        //缓存的item
        binding.favouriteRV.setItemViewCacheSize(13)
        //网格布局
        binding.favouriteRV.layoutManager = GridLayoutManager(this, 4)
        //向适配器传输喜欢的歌曲列表
        adapter = FavouriteAdapter(this, favouriteSongs)
        binding.favouriteRV.adapter = adapter
        //如果列表数量多于1,随机播放按钮就可以显示
        if (favouriteSongs.size<1) binding.shuffleBtnFA.visibility= View.INVISIBLE
        //随机播放按钮事件
        binding.shuffleBtnFA.setOnClickListener {
            val intent = Intent(this, PlayerActivity::class.java)
            //向PlayerActivity传值
            intent.putExtra("index", 0)
            intent.putExtra("class", "FavouriteShuffle")
            startActivity(intent)
        }
    }
}
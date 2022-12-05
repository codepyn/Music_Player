package com.hslxywyjw.musicplayer

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hslxywyjw.musicplayer.databinding.MusicViewBinding

class MusicAdpter(private val context: Context, private var musicList: ArrayList<Music>) :
    RecyclerView.Adapter<MusicAdpter.MyHolder>() {
    class MyHolder(binding: MusicViewBinding) : RecyclerView.ViewHolder(binding.root) {
        val title = binding.songNameMV
        val album = binding.songAlbumMV
        val image = binding.imageMV
        val duration = binding.songDuration
        val root = binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(MusicViewBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.title.text = musicList[position].title
        holder.album.text = musicList[position].album
//        时间格式化
        holder.duration.text = formatDuration(musicList[position].duration)
        Glide.with(context)
            .load(musicList[position].artUri)
//                设置歌曲默认封面
            .apply(RequestOptions().placeholder(R.drawable.shuffle_icon).centerCrop())
            .into(holder.image)
//        点击列表条目
        holder.root.setOnClickListener {
            when{
                MainActivity.search -> sendIntent(ref = "MusicAdapterSearch", pos = position)
                musicList[position].id==PlayerActivity.nowPlayingId->
                    sendIntent(ref = "NowPlaying", pos = PlayerActivity.songPostion)
                else-> sendIntent(ref = "MusicAdpter", pos = position)
            }
        }

    }

    override fun getItemCount(): Int {
        return musicList.size
    }
    //歌曲列表更新操作
    fun updateMusicList(searchList : ArrayList<Music>) {
        musicList = ArrayList()
        musicList.addAll(searchList)
        notifyDataSetChanged()
    }
    //封装页面传值
    private fun sendIntent(ref:String,pos:Int) {
            val intent= Intent(context,PlayerActivity::class.java)
            intent.putExtra("index",pos)
            intent.putExtra("class",ref)
            ContextCompat.startActivity(context,intent,null)
    }
}
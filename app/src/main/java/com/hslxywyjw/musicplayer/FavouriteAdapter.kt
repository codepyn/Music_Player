package com.hslxywyjw.musicplayer

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hslxywyjw.musicplayer.databinding.FavouriteViewBinding
//喜欢数据适配器
//context页面凭证
//musicList歌曲列表
class FavouriteAdapter(private val context: Context, private var musicList: ArrayList<Music>) :
    RecyclerView.Adapter<FavouriteAdapter.MyHolder>() {
    class MyHolder(binding: FavouriteViewBinding) : RecyclerView.ViewHolder(binding.root) {
        //喜欢歌曲图片
        val image=binding.songImgFV
        //喜欢歌曲名
        val name=binding.songNameFV
        val root=binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(FavouriteViewBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        //绑定时的歌曲
        holder.name.text=musicList[position].title
        Glide.with(context)
            .load(musicList[position].artUri)
            //设置歌曲默认封面
            .apply(RequestOptions().placeholder(R.drawable.shuffle_icon).centerCrop())
            .into(holder.image)
        //item点击事件
        holder.root.setOnClickListener{
            //向PlayerActivity传值
            val intent= Intent(context,PlayerActivity::class.java)
            intent.putExtra("index",position)
            intent.putExtra("class","FavouriteAdapter")
            ContextCompat.startActivity(context,intent,null)
        }
    }
    //item总数
    override fun getItemCount(): Int {
        return musicList.size
    }

}
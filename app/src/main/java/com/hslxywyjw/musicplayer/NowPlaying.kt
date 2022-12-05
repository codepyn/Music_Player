package com.hslxywyjw.musicplayer

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hslxywyjw.musicplayer.databinding.FragmentNowPlayingBinding

//底部播放
class NowPlaying : Fragment() {
    companion object {
        lateinit var binding: FragmentNowPlayingBinding
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_now_playing, container, false)
        //可见性
        binding = FragmentNowPlayingBinding.bind(view)
        binding.root.visibility = View.INVISIBLE
        //暂停按钮点击事件
        binding.playPauseBtnNP.setOnClickListener {
            if (PlayerActivity.isPlaying) {
                pauseMusic()
            } else {
                playMusic()
            }
        }
        //下一首点击事件
        binding.nextBtnNP.setOnClickListener {
            setSongPostion(increment = true)
            PlayerActivity.musicService!!.createMudiaPlayer()
            //控制生命周期
            Glide.with(this)
                .load(PlayerActivity.musiclistPA[PlayerActivity.songPostion].artUri)
                //设置歌曲默认封面
                .apply(RequestOptions().placeholder(R.drawable.shuffle_icon).centerCrop())
                .into(binding.songImgNP)
            binding.songNameNP.text = PlayerActivity.musiclistPA[PlayerActivity.songPostion].title
            playMusic()
        }
        //点击底部时
        binding.root.setOnClickListener {
            val intent= Intent(requireContext(),PlayerActivity::class.java)
            intent.putExtra("index",PlayerActivity.songPostion)
            intent.putExtra("class","NowPlaying")
            ContextCompat.startActivity(requireContext(),intent,null)
        }
        return view
    }
    //记录播放点
    override fun onResume() {
        super.onResume()
        if (PlayerActivity.musicService != null) {
            binding.root.visibility = View.VISIBLE
            binding.songNameNP.isSelected=true
            Glide.with(this)
                .load(PlayerActivity.musiclistPA[PlayerActivity.songPostion].artUri)
                //设置歌曲默认封面
                .apply(RequestOptions().placeholder(R.drawable.shuffle_icon).centerCrop())
                .into(binding.songImgNP)
            binding.songNameNP.text = PlayerActivity.musiclistPA[PlayerActivity.songPostion].title
            if (PlayerActivity.isPlaying) {
                binding.playPauseBtnNP.setIconResource(R.drawable.pause_icon)
            } else {
                binding.playPauseBtnNP.setIconResource(R.drawable.play_icon)
            }
        }
    }
    //播放音乐
    private fun playMusic() {
        PlayerActivity.musicService!!.mediaPlayer!!.start()
        binding.playPauseBtnNP.setIconResource(R.drawable.pause_icon)

        PlayerActivity.binding.nextBtnPA.setIconResource(R.drawable.pause_icon)
        PlayerActivity.isPlaying = true
    }
    //暂停音乐
    private fun pauseMusic() {
        PlayerActivity.musicService!!.mediaPlayer!!.pause()
        binding.playPauseBtnNP.setIconResource(R.drawable.play_icon)

        PlayerActivity.binding.nextBtnPA.setIconResource(R.drawable.play_icon)
        PlayerActivity.isPlaying = false
    }
}
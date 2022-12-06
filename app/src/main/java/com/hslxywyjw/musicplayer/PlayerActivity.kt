package com.hslxywyjw.musicplayer

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hslxywyjw.musicplayer.databinding.ActivityPlayerBinding

//MediaPlayer.OnCompletionListener开场动画
class PlayerActivity : AppCompatActivity(), ServiceConnection, MediaPlayer.OnCompletionListener {

    companion object {
        //播放页面歌曲列表
        lateinit var musiclistPA: ArrayList<Music>
        //当前歌曲位置
        var songPostion: Int = 0
        //是否在播放
        var isPlaying: Boolean = false
        //播放音乐服务是否在运行
        var musicService: MusicService? = null
        //视图绑定
        lateinit var binding: ActivityPlayerBinding
        //是否单曲循环
        var repeat: Boolean = false
        //定时时间，为了方便演示，实际值为几秒
        var min15: Boolean = false
        var min30: Boolean = false
        var min60: Boolean = false
        //当前播放音乐的id
        var nowPlayingId: String = ""
        //是否为喜欢的歌曲
        var isFavourite: Boolean = false
        //喜欢歌曲的索引
        var fIndex: Int = -1

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_MusicPlayer)

        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 返回按钮
        binding.backBtnPA.setOnClickListener {
            finish()
        }

        //初始化页面
        initializeLayout()
        //暂停按钮
        binding.playPauserBtnPA.setOnClickListener {
            //判断是否在播放
            if (isPlaying) pauseMusic()
            else playMuis()
        }
        //上一首
        binding.previousBtnPA.setOnClickListener { prevNextSong(increment = false) }
        //下一首
        binding.nextBtnPA.setOnClickListener { prevNextSong(increment = true) }
        //进度条事件，随此时播放时长而变化
        binding.seekBarPA.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            //每次变化时会触发此方法
            override fun onProgressChanged(seekBar: SeekBar?, process: Int, fromUser: Boolean) {
                if (fromUser) musicService!!.mediaPlayer!!.seekTo(process)
            }
            //开始滑动
            //kotlin中的Unit相当于Java中的Void即为空
            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            //滑动结束
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit

        })
        //单曲循环
        binding.repeatBtnAP.setOnClickListener {
            if (!repeat) {
                repeat = true
                binding.repeatBtnAP.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
                Toast.makeText(this@PlayerActivity, "单曲循环", Toast.LENGTH_SHORT).show()
            } else {
                repeat = false
                binding.repeatBtnAP.setColorFilter(ContextCompat.getColor(this, R.color.cool_pink))
                Toast.makeText(this@PlayerActivity, "取消单曲循环", Toast.LENGTH_SHORT).show()
            }
        }
        //调用手机自带的均衡器，有些手机系统已经阉割了
        binding.equalizerBtnPA.setOnClickListener {
            try {
                val eqIntent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
                eqIntent.putExtra(
                    AudioEffect.EXTRA_AUDIO_SESSION,
                    musicService!!.mediaPlayer!!.audioSessionId
                )
                eqIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, baseContext.packageName)
                eqIntent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                startActivityForResult(eqIntent, 13)
            } catch (e: Exception) {
                Toast.makeText(this, "不支持均衡器功能", Toast.LENGTH_SHORT).show()
            }

        }
        //定时器
        binding.timerBtnPA.setOnClickListener {
            val timer = min15 || min30 || min60
            if (!timer) {
                //显示底部定时选择
                showBottomSheetDialog()
            } else {
                //对话框
                val builder = MaterialAlertDialogBuilder(this)
                builder.setTitle("Exit").setMessage("确定要取消定时播放吗?")
                    .setPositiveButton("确定") { _, _ ->
                        min15 = false
                        min30 = false
                        min60 = false
                        binding.timerBtnPA.setColorFilter(
                            ContextCompat.getColor(
                                this,
                                R.color.cool_pink
                            )
                        )

                    }
                    .setNegativeButton("再想想") { dialog, _ ->
                        dialog.dismiss()
                    }
                val custiomDialog = builder.create()
                custiomDialog.show()

                custiomDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
                custiomDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
            }

        }
        //分享按钮，从缓存内存中共享
        binding.shareBtnPA.setOnClickListener {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type = "audio/*"
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(musiclistPA[songPostion].path))
            startActivity(Intent.createChooser(shareIntent, "分享音乐文件"))
        }
        //喜欢按钮
        binding.favouritesBtnPA.setOnClickListener {
            if (isFavourite) {
                isFavourite = false
                binding.favouritesBtnPA.setImageResource(R.drawable.favorite_empty_icon)
                //移出喜欢歌曲列表
                FavouriteActivity.favouriteSongs.removeAt(fIndex)
            } else {
                isFavourite = true
                binding.favouritesBtnPA.setImageResource(R.drawable.favorite_icon)
                //添加喜欢歌曲列表
                FavouriteActivity.favouriteSongs.add(musiclistPA[songPostion])
            }
        }
    }

    //初始化图片名字
    private fun setLayout() {
        fIndex = favouriteChecker(musiclistPA[songPostion].id)
        Glide.with(this)
            .load(musiclistPA[songPostion].artUri)
            //设置歌曲默认封面
            .apply(RequestOptions().placeholder(R.drawable.shuffle_icon).centerCrop())
            .into(binding.songImgPA)
        binding.songNamePA.text = musiclistPA[songPostion].title

        if (repeat) binding.repeatBtnAP.setColorFilter(
            ContextCompat.getColor(
                this,
                R.color.purple_500
            )
        )
        if (min15 || min30 || min60)
            binding.timerBtnPA.setColorFilter(
                ContextCompat.getColor(
                    this,
                    R.color.purple_500
                )
            )
        if (isFavourite) binding.favouritesBtnPA.setImageResource(R.drawable.favorite_icon)
        else binding.favouritesBtnPA.setImageResource(R.drawable.favorite_empty_icon)
    }

    //播放
    private fun createMudiaPlayer() {
        try {
            if (musicService!!.mediaPlayer == null) musicService!!.mediaPlayer = MediaPlayer()
            //!!相当于Java里的if()else()
            musicService!!.mediaPlayer!!.reset()
            musicService!!.mediaPlayer!!.setDataSource(musiclistPA[songPostion].path)
            musicService!!.mediaPlayer!!.prepare()
            musicService!!.mediaPlayer!!.start()
            isPlaying = true
            binding.playPauserBtnPA.setIconResource(R.drawable.pause_icon)
            //进度条开始
            binding.tvSeekBarStart.text =
                formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
            //进度条结束
            binding.tvSeekBarEnd.text =
                formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
            binding.seekBarPA.progress = 0
            binding.seekBarPA.max = musicService!!.mediaPlayer!!.duration
            musicService!!.mediaPlayer!!.setOnCompletionListener(this)
            //正在播放的歌曲id
            nowPlayingId = musiclistPA[songPostion].id
        } catch (e: Exception) {
            return
        }
    }

    //加载初始化
    private fun initializeLayout() {
        //取值
        songPostion = intent.getIntExtra("index", 0)
        //判断从哪个页面跳转过来的
        when (intent.getStringExtra("class")) {
            "FavouriteAdapter"->{
                //        用于启动服务
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)
                musiclistPA = ArrayList()
                musiclistPA.addAll(FavouriteActivity.favouriteSongs)
                setLayout()
            }
            "NowPlaying" -> {
                setLayout()
                binding.tvSeekBarStart.text =
                    formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
                binding.tvSeekBarEnd.text =
                    formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
                //进度条当前位置
                binding.seekBarPA.progress = musicService!!.mediaPlayer!!.currentPosition
                binding.seekBarPA.max = musicService!!.mediaPlayer!!.duration
                //判断是否在播放，切换暂停/开始图标
                if (isPlaying) binding.playPauserBtnPA.setIconResource(R.drawable.pause_icon)
                else binding.playPauserBtnPA.setIconResource(R.drawable.play_icon)
            }
            "MusicAdapterSearch" -> {
                // 用于启动服务
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)
                musiclistPA = ArrayList()
                musiclistPA.addAll(MainActivity.musicListSearch)
                setLayout()
            }
            "MusicAdpter" -> {
                //用于启动服务
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)
                musiclistPA = ArrayList()
                musiclistPA.addAll(MainActivity.MusicListMA)
                setLayout()
            }
            "MainActivity" -> {
                //用于启动服务
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)
                musiclistPA = ArrayList()
                musiclistPA.addAll(MainActivity.MusicListMA)
                musiclistPA.shuffle()
                setLayout()
            }
            "FavouriteShuffle"->{
                //用于启动服务
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)
                musiclistPA = ArrayList()
                musiclistPA.addAll(FavouriteActivity.favouriteSongs)
                musiclistPA.shuffle()
                setLayout()
            }
        }
    }

    //播放
    private fun playMuis() {
        binding.playPauserBtnPA.setIconResource(R.drawable.pause_icon)

        isPlaying = true
        musicService!!.mediaPlayer!!.start()
    }

    //暂停
    private fun pauseMusic() {
        binding.playPauserBtnPA.setIconResource(R.drawable.play_icon)

        isPlaying = false
        musicService!!.mediaPlayer!!.pause()
    }

    //切换
    private fun prevNextSong(increment: Boolean) {
        //true为下一首
        if (increment) {
            setSongPostion(increment = true)
            setLayout()
            createMudiaPlayer()
        } else {
            setSongPostion(increment = false)
            setLayout()
            createMudiaPlayer()
        }
    }


    //连接服务时
    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as MusicService.MyBinder
        //当定当前服务
        musicService = binder.currentService()
        createMudiaPlayer()
        musicService!!.seekBarSetup()

    }

    //断开服务时
    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null
    }
    //播放结束时
    override fun onCompletion(mp: MediaPlayer?) {
        setSongPostion(increment = true)
        createMudiaPlayer()
        try {
            setLayout()
        } catch (e: Exception) {
            true
        }
    }
    //传递数据
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 13 || resultCode == RESULT_OK)
            return
    }
    //显示定时底部
    private fun showBottomSheetDialog() {
        val dialog = BottomSheetDialog(this@PlayerActivity)
        dialog.setContentView(R.layout.bottom_sheet_dialog)
        dialog.show()
        dialog.findViewById<LinearLayout>(R.id.min_15)?.setOnClickListener {
            Toast.makeText(baseContext, "音乐将在15分钟后停止", Toast.LENGTH_SHORT).show()
            binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
            min15 = true
            //线程处理
            Thread {
                Thread.sleep(5500)
                if (min15) exitApplication()
            }.start()
            dialog.dismiss()
        }
        dialog.findViewById<LinearLayout>(R.id.min_30)?.setOnClickListener {
            Toast.makeText(baseContext, "音乐将在30分钟后停止", Toast.LENGTH_SHORT).show()
            binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
            min30 = true
            //线程处理
            Thread {
                Thread.sleep(3000)
                if (min30) exitApplication()
            }.start()
            dialog.dismiss()
        }
        dialog.findViewById<LinearLayout>(R.id.min_60)?.setOnClickListener {
            Toast.makeText(baseContext, "音乐将在60分钟后停止", Toast.LENGTH_SHORT).show()
            binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
            min60 = true
            //线程处理
            Thread {
                //60*60000
                Thread.sleep(6000)
                if (min60) exitApplication()
            }.start()
            dialog.dismiss()
        }
    }
}
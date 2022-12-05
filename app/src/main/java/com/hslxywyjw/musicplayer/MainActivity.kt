package com.hslxywyjw.musicplayer

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.hslxywyjw.musicplayer.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {

    //创建binding
    private lateinit var binding: ActivityMainBinding

    //侧边栏
    private lateinit var toggle: ActionBarDrawerToggle

    //音乐设配器
    private lateinit var musicAdpter: MusicAdpter

    //companion object是当包含它的类被加载时就初始化了,会在原类中生成一个内部类
    //伴生类与伴生对象可相互访问各自私有成员
    //伴生对象可为伴生类增加静态成员

    companion object {
        lateinit var MusicListMA: ArrayList<Music>

        lateinit var musicListSearch: ArrayList<Music>

        var search: Boolean = false
    }

    //页面创建时
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //设置主题
        setTheme(R.style.Theme_MusicPlayer)
        //初始化binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //侧边栏绑定 R.string.open, R.string.Close开启关闭
        toggle = ActionBarDrawerToggle(this, binding.root, R.string.open, R.string.Close)
        //添加绑定事件
        binding.root.addDrawerListener(toggle)
        toggle.syncState()
        //左上角按钮是否可以点击
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        //申请权限
        if (requestRuntimePermission()) {
            //通过权限对页面进行初始化
            initializeLayout()
            //喜欢列表的数据
            FavouriteActivity.favouriteSongs = ArrayList()
            //存储喜欢列表的数据
            val editor = getSharedPreferences("FAVOURITES", MODE_PRIVATE)
            val jsonString = editor.getString("FavouriteSongs", null)
            val typeToken = object : TypeToken<ArrayList<Music>>() {}.type
            //喜欢列表不为空时，使用Gson填充数据
            if (jsonString != null) {
                val data: ArrayList<Music> = GsonBuilder().create().fromJson(jsonString, typeToken)
                FavouriteActivity.favouriteSongs.addAll(data)
            }
        }


        //按钮点击事件
        //单曲循环按钮，向PlayerActivity传值
        binding.shuffleBtn.setOnClickListener {
            val intent = Intent(this@MainActivity, PlayerActivity::class.java)
            //传值
            intent.putExtra("index", 0)
            intent.putExtra("class", "MainActivity")
            startActivity(intent)

        }
        //喜欢按钮，向FavouriteActivity传值
        binding.favouritesBtn.setOnClickListener {
            val intent = Intent(this@MainActivity, FavouriteActivity::class.java)
            startActivity(intent)
        }

        //侧边栏点击事件
        binding.navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navFeedback -> Toast.makeText(baseContext, "此功能正在建设中。。。", Toast.LENGTH_SHORT)
                    .show()
                R.id.navSetting -> Toast.makeText(baseContext, "此功能正在建设中。。。", Toast.LENGTH_SHORT)
                    .show()
                R.id.navAbout -> {
                    startActivity(Intent(this,AboutActivity::class.java))
                }

                R.id.navExit -> {
                    val builder = MaterialAlertDialogBuilder(this)
                    builder.setTitle("退出程序").setMessage("确定要退出程序吗?")
                        .setPositiveButton("确定") { _, _ ->
                            //封装的退出程序函数
                            exitApplication()
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
            true
        }
    }
    //获取权限
    private fun requestRuntimePermission(): Boolean {
        //确定是否已授权特定的权限，允许应用写（非读）用户的外部存储器（android.Manifest.permission.WRITE_EXTERNAL_STORAGE）
        //有权限PERMISSION_GRANTED
        //无权限PackageManager.PERMISSION_DENIED
        if (ActivityCompat.checkSelfPermission(
                this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            //否则就再次申请权限
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                13

            )
            return false
        }
        return true
    }
    //重写请求权限时
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        ////有权限PERMISSION_GRANTED
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 13) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show()
                MusicListMA = getAllAudio()
                initializeLayout()
            } else
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    13
                )
        }
    }

    //侧边栏
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item))
            return true
        return super.onOptionsItemSelected(item)
    }

    //封装页面初始化
    private fun initializeLayout() {
        search = false
        //首页的音乐列表
        MusicListMA = getAllAudio()
        //设置RecyclerView的Item宽或者高不会变
        binding.musicRV.setHasFixedSize(true)
        //当滚动RecyclerView以使视图几乎完全脱离屏幕时，RecyclerView将保留该视图，以便可以将其滚动回视图，而无需重新执行onBindViewHolder（）。
        binding.musicRV.setItemViewCacheSize(13)
        //设置线性布局
        binding.musicRV.layoutManager = LinearLayoutManager(this@MainActivity)
        //设置适配器
        musicAdpter = MusicAdpter(this@MainActivity, MusicListMA)
        binding.musicRV.adapter = musicAdpter
        binding.totalSongs.text = "总歌曲数:" + musicAdpter.itemCount
    }


    //    获取音频
    @SuppressLint("Range")
    private fun getAllAudio(): ArrayList<Music> {
        //中间list
        val tempList = ArrayList<Music>()
        //使用Android系统提供的MediaStore接口
        val selection = MediaStore.Audio.Media.IS_MUSIC + "!=0"
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID
        )
        //扫描设备上的歌曲 content://media/external/audio/media
        //adbshell提供的SD卡的数据库文件--多媒体数据库
        //取所有歌曲的信息MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        //contentResolver内容解析器:
        //MediaStore.Audio.Media.EXTERNAL_CONTENT_URI解析标识
        //projection查询要返回的列
        //selection查询位置
        //selectionArgs查询条件属性值
        //MediaStore.Audio.Media.DATE_ADDED结果排序规则
        val cursor = this.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null,
            MediaStore.Audio.Media.DATE_ADDED, null
        )
        //如果获取到的解析内容不为空
        if (cursor != null) {
            //cursor初始位置是在-1,而数据是从0开始的
            if (cursor.moveToFirst()) {
                do {
                    val titleC =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                    val idC =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID))
                    val albumC =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))
                    val artistC =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                    val pathC =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                    val durationC =
                        cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
                    val albumIdC =
                        cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))
                            .toString()
                    //获取专辑
                    val uri = Uri.parse("content://media/external/audio/albumart")
                    //withAppendedPath将已编码的路径段附加到基本Uri来创建新的Uri。
                    val artUriC = Uri.withAppendedPath(uri, albumIdC).toString()
                    val music = Music(
                        id = idC,
                        title = titleC,
                        album = albumC,
                        artist = artistC,
                        path = pathC,
                        duration = durationC,
                        artUri = artUriC
                    )
                    val file = File(music.path)
                    //在文件中是否存在
                    if (file.exists())
                        tempList.add(music)
                } while (cursor.moveToNext())
                cursor.close()
            }
        }
        return tempList
    }

    //在活动被销毁之前执行最后的清理。
    override fun onDestroy() {
        super.onDestroy()
        if (!PlayerActivity.isPlaying && PlayerActivity.musicService != null) {
            exitApplication()
        }

    }
    //比如做一个音乐播放程序，在播放过程中，突然有电话打进来了，这时系统自动调出电话，而你的音乐播放程序置于后台，触发了onPause方法。当你电话结束后，关闭电话，又自动回到音乐播放程序，此时，触发onResume方法，这时，如果你之前在onPause的时候记录了播放点，就可以在onResume方法里来继续播放。
    //onResume方法是Activity第一次创建时 重新加载实例时调用 例如 我打开App第一个界面OnCreate完 就调用onResume 然后切换到下一个界面 第一个界面不finish 按Back键回来时 就调onResume 不调onCreate， 还有就是 App用到一半 有事Home键切出去了 在回来时调onResume
    override fun onResume() {
        super.onResume()
        //数据持久性,用于存放喜欢的音乐列表
        val editor = getSharedPreferences("FAVOURITES", MODE_PRIVATE).edit()
        val jsonString = GsonBuilder().create().toJson(FavouriteActivity.favouriteSongs)
        editor.putString("FavouriteSongs", jsonString)
        editor.apply()
    }

    //搜索栏
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_view_menu, menu)
        val searchView = menu?.findItem(R.id.searchView)?.actionView as SearchView
        //搜索框提交查询时
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true
            //搜索框内容改变时
            override fun onQueryTextChange(newText: String?): Boolean {
                //搜索到的音乐
                musicListSearch = ArrayList()
                if (newText != null) {
                    val userInput = newText.lowercase()
                    for (song in MusicListMA) {
                        if (song.title.lowercase().contains(userInput)) {
                            musicListSearch.add(song)
                        }
                    }
                    search = true
                    //updateMusicList自定义的方法,当歌曲列表更新时
                    musicAdpter.updateMusicList(searchList = musicListSearch)
                    binding.totalSongs.text = "总歌曲数:" + musicListSearch.size
                }
                return true
            }

        })
        //回调
        return super.onCreateOptionsMenu(menu)
    }
}
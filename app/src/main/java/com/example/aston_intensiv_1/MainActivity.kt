package com.example.aston_intensiv_1

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.Manifest
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.widget.ImageView
import androidx.annotation.RequiresApi
import com.example.aston_intensiv_1.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var isBound = false
    var imgRes = 0

    private var musicService: MusicService? = null
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == MusicService.ACTION_PLAY_PAUSE) {
                val isPlaying = intent.getBooleanExtra("isPlaying", false)
                updatePlayPauseIcon(isPlaying)
            }
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
            musicService = null
        }
    }

    companion object {
        const val IMG_RES_KEY = "imgRes"
        const val SHARED_PREF = "MusicPreferences"
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val filter = IntentFilter(MusicService.ACTION_PLAY_PAUSE)
        registerReceiver(receiver, filter)

        val sharedPref = getSharedPreferences("MusicPreferences", Context.MODE_PRIVATE)
        imgRes = sharedPref.getInt(IMG_RES_KEY, R.drawable.play_btn_ic)
        binding.playBtn.setImageResource(imgRes)

        val intent = Intent(this, MusicService::class.java)
        startForegroundService(intent)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

        if (musicService?.isPlaying() == true) {
            binding.playBtn.setImageResource(R.drawable.pause_btn_ic)

            imgRes = R.drawable.play_btn_ic
        } else {
            binding.playBtn.setImageResource(R.drawable.play_btn_ic)
            imgRes = R.drawable.pause_btn_ic

        }

        binding.playBtn.setOnClickListener {
            if (musicService!!.isPlaying() == true) {
                musicService!!.pauseTrack()
                binding.playBtn.setImageResource(R.drawable.play_btn_ic)
                imgRes = R.drawable.play_btn_ic
            } else {
                musicService!!.resumeTrack()
                binding.playBtn.setImageResource(R.drawable.pause_btn_ic)
                imgRes = R.drawable.pause_btn_ic


            }
        }

        binding.nextBtn.setOnClickListener {
            musicService?.nextTrack()
        }

        binding.prevBtn.setOnClickListener {
            musicService?.previousTrack()
        }

    }


    override fun onDestroy() {
        val sharedPref = getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putInt(IMG_RES_KEY, imgRes)
            apply()
        }
        super.onDestroy()

    }

    private fun updatePlayPauseIcon(isPlaying: Boolean) {
        val iconRes = if (isPlaying) R.drawable.pause_btn_ic else R.drawable.play_btn_ic
        binding.playBtn.setImageResource(iconRes)
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun checkPermission() {
        val res = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)

        if (res != PackageManager.PERMISSION_GRANTED) {
            requestPermission()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun requestPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_MEDIA_AUDIO),
                1
            )

        }

    }


}
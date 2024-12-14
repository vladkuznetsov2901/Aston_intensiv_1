package com.example.aston_intensiv_1

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat

class MusicService : Service() {

    private var mPlayer: MediaPlayer? = null
    private val binder = MusicBinder()
    private var trackList = listOf(
        R.raw.track2,
        R.raw.track1,
        R.raw.track3
    )

    private var currentTrackIndex = 0
    private lateinit var mediaSession: MediaSessionCompat


    companion object {
        const val CHANNEL_ID = "MusicServiceChannel"
        const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Music Player")
            .setContentText("Playing music...")
            .setSmallIcon(R.drawable.play_btn_ic)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        return START_STICKY
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Music Service Channel",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        mPlayer?.release()
        mPlayer = null
        stopForeground(STOP_FOREGROUND_DETACH)
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }


    fun playTrack(index: Int) {
        if (index in trackList.indices) {
            mPlayer?.stop()
            mPlayer?.release()

            currentTrackIndex = index
            mPlayer = MediaPlayer.create(this, trackList[index])
            mPlayer?.setOnCompletionListener {
                nextTrack()
            }
            mPlayer?.start()
        }
    }

    fun pauseTrack() {

        mPlayer?.pause()
    }

    fun resumeTrack() {
        if (mPlayer != null && !mPlayer!!.isPlaying) {
            mPlayer!!.start()
        } else {
            playTrack(currentTrackIndex)
        }
    }


    fun nextTrack() {
        val nextIndex = (currentTrackIndex + 1) % trackList.size
        playTrack(nextIndex)
    }

    fun previousTrack() {
        val prevIndex = if (currentTrackIndex - 1 < 0) trackList.size - 1 else currentTrackIndex - 1
        playTrack(prevIndex)
    }

    fun isPlaying(): Boolean {
        return mPlayer?.isPlaying == true
    }

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }
}

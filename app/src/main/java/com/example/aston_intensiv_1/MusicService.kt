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
        const val ACTION_PLAY_PAUSE = "action_play_pause"
        const val ACTION_NEXT = "action_next"
        const val ACTION_PREVIOUS = "action_previous"
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "MusicNotification"
    }


    override fun onCreate() {
        super.onCreate()

        mediaSession = MediaSessionCompat(this, "MusicService").apply {
            isActive = true
        }

        createNotificationChannel()
        mediaSession = MediaSessionCompat(this, "MusicService")

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        createNotification()

        when (action) {
            ACTION_PLAY_PAUSE -> {
                if (isPlaying()) pauseTrack() else resumeTrack()
                createNotification()
            }
            ACTION_NEXT -> {
                nextTrack()
            }
            ACTION_PREVIOUS -> {
                previousTrack()
            }
        }

        return START_STICKY
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Music Player Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }


    fun createNotification(action: String? = null) {
        val playPauseIcon = if (isPlaying()) R.drawable.pause_btn_ic else R.drawable.play_btn_ic
        val isPlaying = isPlaying()
        val playPauseIntent = PendingIntent.getService(
            this, 0,
            Intent(this, MusicService::class.java).setAction(ACTION_PLAY_PAUSE),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nextIntent = PendingIntent.getService(
            this, 0,
            Intent(this, MusicService::class.java).setAction(ACTION_NEXT),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val prevIntent = PendingIntent.getService(
            this, 0,
            Intent(this, MusicService::class.java).setAction(ACTION_PREVIOUS),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val broadcastIntent = Intent(ACTION_PLAY_PAUSE)
        broadcastIntent.putExtra("isPlaying", isPlaying)
        sendBroadcast(broadcastIntent)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Music Player")
            .setContentText("Playing your favorite music")
            .setSmallIcon(R.drawable.play_btn_ic)
            .addAction(R.drawable.prev_btn_ic, "Previous", prevIntent)
            .addAction(playPauseIcon, "Play/Pause", playPauseIntent)
            .addAction(R.drawable.next_btn_ic, "Next", nextIntent)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSession.sessionToken)
                .setShowActionsInCompactView(0, 1, 2))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(NOTIFICATION_ID, notification)
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
        createNotification()
    }

    fun pauseTrack() {

        mPlayer?.pause()
        createNotification()
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

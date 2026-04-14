package com.kameji.starpowerradiowv

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource

class RadioService : Service() {

    private lateinit var player: ExoPlayer
    private var isMuted = false
    private var isStarted = false
    private var statusText = "Service gestartet..."

    companion object {
        const val CHANNEL_ID = "radio_channel"
        const val ACTION_START = "action_start"
        const val ACTION_TOGGLE_MUTE = "toggle_mute"
        // Stream URL removed for security reasons
        const val STREAM_URL = "REDACTED"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("RADIO_DEBUG", "RadioService onCreate")

        createNotificationChannel()

        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("StarPowerRadioApp")
            .setAllowCrossProtocolRedirects(true)

        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(STREAM_URL))

        player = ExoPlayer.Builder(this).build().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true
            )
            setMediaSource(mediaSource)
            prepare()
        }

        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                statusText = when (state) {
                    Player.STATE_IDLE -> "Idle"
                    Player.STATE_BUFFERING -> "Lädt..."
                    Player.STATE_READY -> "Live läuft"
                    Player.STATE_ENDED -> "Beendet"
                    else -> "Unbekannt"
                }
                Log.d("RADIO_STATE", statusText)
                if (isStarted) updateNotification()
            }

            override fun onPlayerError(error: PlaybackException) {
                statusText = "Fehler: ${error.errorCodeName}"
                Log.e("RADIO_ERROR", "Code=${error.errorCodeName}, Msg=${error.message}", error)
                if (isStarted) updateNotification()
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                Log.d("RADIO_PLAYING", "isPlaying=$isPlaying")
            }
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("RADIO_DEBUG", "onStartCommand action=${intent?.action}")

        when (intent?.action) {
            ACTION_START -> startRadio()
            ACTION_TOGGLE_MUTE -> {
                if (!isStarted) startRadio()
                isMuted = !isMuted
                player.volume = if (isMuted) 0f else 1f
                updateNotification()
            }
            else -> startRadio()
        }

        return START_STICKY
    }

    private fun startRadio() {
        Log.d("RADIO_DEBUG", "startRadio called")

        if (!isStarted) {
            isStarted = true
            statusText = "Startet..."
            startForeground(1, createNotification())
            player.playWhenReady = true
        } else {
            player.playWhenReady = true
            updateNotification()
        }
    }

    private fun createNotification(): Notification {
        val muteIntent = Intent(this, RadioService::class.java).apply {
            action = ACTION_TOGGLE_MUTE
        }

        val mutePendingIntent = PendingIntent.getService(
            this,
            0,
            muteIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val openAppIntent = Intent(this, MainActivity::class.java)
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            1,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Star Power Radio")
            .setContentText(if (isMuted) "Stummgeschaltet • $statusText" else statusText)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(openAppPendingIntent)
            .addAction(
                0,
                if (isMuted) "Unmute" else "Mute",
                mutePendingIntent
            )
            .setOngoing(true)
            .build()
    }

    private fun updateNotification() {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1, createNotification())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Radio",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        Log.d("RADIO_DEBUG", "RadioService onDestroy")
        player.release()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
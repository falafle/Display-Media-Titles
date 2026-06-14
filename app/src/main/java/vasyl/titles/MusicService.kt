package vasyl.titles

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import vasyl.titles.widget.isMusicPlaying
import vasyl.titles.widget.updateWidgetPlayState

class MusicService : Service() {

    companion object {
        const val MUSICSERVICE = "com.fyt.launcher.music"
        const val MUSIC_PKG = "com.syu.music"
        const val NEXTMUSIC = "com.syu.music.next"
        const val PLAYPAUSEMUSIC = "com.syu.music.playpause"
        const val PLAY_ALBUM = "play_album"
        const val PLAY_ARTIST = "play_artist"
        const val PLAY_CURMINUTES = "play_cur"
        const val PLAY_PATH = "play_path"
        const val PLAY_STATE = "play_state"
        const val PLAY_TOTALMINUTES = "play_total"
        const val PREVMUSIC = "com.syu.music.prev"
        const val REMOVE_MUSIC = "com.fyt.systemui.remove"
        const val TITLE = "title"
        const val TITLES_RECEIVER = "titlesReceiver"
        const val PLAY_SOURCE = "source"
        const val SOURCE = "fyt"

        @JvmStatic var album_cover: ByteArray? = null
        @JvmStatic var music_name: String = ""
        @JvmStatic var author_name: String = ""
        @JvmStatic var music_path: String = ""
        @JvmStatic var state: Boolean = false
        @JvmStatic var album: String = ""
        @JvmStatic var TOTALMINUTES: Long = 0
        @JvmStatic var CURMINUTES: Long = 0
    }

    private var intentSent = false

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            return super.onStartCommand(intent, flags, startId)
        }

        val action = intent.action
        if (MUSICSERVICE == action) {
            val bundle = intent.extras ?: Bundle()

            TOTALMINUTES = bundle.getLong(PLAY_TOTALMINUTES, 0)
            CURMINUTES = bundle.getLong(PLAY_CURMINUTES, 0)

            music_name = bundle.getString(TITLE, "")
            author_name = bundle.getString(PLAY_ARTIST, "")
            music_path = bundle.getString(PLAY_PATH, "")
            state = bundle.getBoolean(PLAY_STATE, false)

            album = bundle.getString(PLAY_ALBUM) ?: ""

            if (!music_name.contains("Unknown")) {
                sendData()
            } else {
                CoroutineScope(Dispatchers.Main).launch {
                    if (isMusicPlaying(applicationContext) && !state) {
                        val handler = Handler(Looper.getMainLooper())
                        // First state sent by music app after turning the music on is "false" - double check it to avoid bugs
                        handler.postDelayed({
                            if (!state) {
                                val intent = Intent("removeReceiver")
                                sendBroadcast(intent)
                            }
                        }, 500L)
                    }
                }
            }
        }
        // --- K706 RADIO LOGIC ---
        else if (action == "com.qf.radio.update_action") {
            val bundle = intent.extras ?: Bundle()

            val freqString = bundle.getString("com.qf.radio.update_action_key", "")
            val bandInt = bundle.getInt("com.qf.radio.update_action_band_key", 0)

            val bandText = if (bandInt in 0..2) {
                "FM${bandInt + 1}" // FM1, FM2, FM3
            } else {
                "AM${(bandInt % 3) + 1}" // AM1, AM2
            }

            val unit = if (bandInt in 0..2) "MHz" else "KHz"
            
            music_name = "$freqString $unit"
            author_name = bandText
            state = true

            sendData()
        }
        // --- K706 MEDIA INFO LOGIC (Title & Artist) ---
        else if (action == "com.qf.action.UPDATE_MEDIA_INFO") {
            val bundle = intent.extras ?: Bundle()

            music_name = bundle.getString("media_title", "Unknown") ?: "Unknown"
            author_name = bundle.getString("media_artist", "Unknown") ?: "Unknown"
            TOTALMINUTES = bundle.getLong("media_duration", 0L)

            sendData()
        }
        // --- NEW K706 MEDIA STATE LOGIC (Play/Pause & Time) ---
        else if (action == "com.qf.action.UPDATE_MEDIA_STATE") {
            val bundle = intent.extras ?: Bundle()

            state = bundle.getBoolean("media_state", false)
            CURMINUTES = bundle.getLong("media_position", 0L)

            sendData()
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun sendData() {
        val intent = Intent(TITLES_RECEIVER)
        val bundle = Bundle().apply {
            putBoolean(PLAY_STATE, state)
            putString(TITLE, music_name)
            putString(PLAY_ARTIST, author_name)
            putString(PLAY_ALBUM, album)
            putString(PLAY_PATH, music_path)
            putString(PLAY_SOURCE, SOURCE)
            putLong(PLAY_TOTALMINUTES, TOTALMINUTES)
            putLong(PLAY_CURMINUTES, CURMINUTES)
        }

        intent.putExtras(bundle)
        sendBroadcast(intent)
    }
}

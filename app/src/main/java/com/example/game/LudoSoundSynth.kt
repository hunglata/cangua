package com.example.game

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.math.floor
import kotlin.math.abs

object LudoSoundSynth {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var musicJob: Job? = null
    private var appContext: Context? = null
    private var mediaPlayer: MediaPlayer? = null
    private var currentTrack: String? = null
    var isMusicEnabled = true
    var isSfxEnabled = true

    private const val SAMPLE_RATE = 22050

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private fun findBgmAsset(): String? {
        val context = appContext ?: return null
        return try {
            val files = context.assets.list("") ?: emptyArray()
            files.firstOrNull { name ->
                val lower = name.lowercase()
                lower.endsWith(".mp3") || lower.endsWith(".wav") || lower.endsWith(".ogg") || lower.endsWith(".m4a")
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun playTone(
        frequencyStart: Float,
        frequencyEnd: Float,
        durationMs: Int,
        amplitude: Float = 0.3f,
        waveType: String = "sine"
    ) {
        if (!isSfxEnabled) return
        scope.launch {
            try {
                val numSamples = (SAMPLE_RATE * (durationMs / 1000f)).toInt()
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val progress = i.toFloat() / numSamples
                    val currentFreq = frequencyStart + (frequencyEnd - frequencyStart) * progress
                    val t = i.toFloat() / SAMPLE_RATE
                    val angle = 2.0 * Math.PI * currentFreq * t
                    
                    val sampleVal = when (waveType) {
                        "square" -> {
                            if (sin(angle) >= 0) 1.0f else -1.0f
                        }
                        "triangle" -> {
                            val x = (angle / (2.0 * Math.PI)).toFloat()
                            2.0f * abs(2.0f * (x - floor(x + 0.5f))) - 1.0f
                        }
                        else -> { // "sine"
                            sin(angle).toFloat()
                        }
                    }
                    
                    buffer[i] = (sampleVal * amplitude * Short.MAX_VALUE).toInt().toShort()
                }

                val audioTrack = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    AudioTrack.Builder()
                        .setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_GAME)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build()
                        )
                        .setAudioFormat(
                            AudioFormat.Builder()
                                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                .setSampleRate(SAMPLE_RATE)
                                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                .build()
                        )
                        .setBufferSizeInBytes(numSamples * 2)
                        .setTransferMode(AudioTrack.MODE_STATIC)
                        .build()
                } else {
                    @Suppress("DEPRECATION")
                    AudioTrack(
                        AudioManager.STREAM_MUSIC,
                        SAMPLE_RATE,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        numSamples * 2,
                        AudioTrack.MODE_STATIC
                    )
                }

                audioTrack.write(buffer, 0, numSamples)
                audioTrack.play()
                
                // Keep audio track alive until finished playing
                delay(durationMs + 100L)
                audioTrack.stop()
                audioTrack.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun playClick() {
        playTone(600f, 600f, 20, 0.25f)
    }

    fun playDiceRoll() {
        scope.launch {
            if (!isSfxEnabled) return@launch
            // play several rapid bouncy clicks of shifting frequencies to simulate the rich rattle of a dice
            for (i in 0..6) {
                val rFreq = 180f + (i * 90f) + (Math.random().toFloat() * 100f)
                playTone(rFreq, rFreq - 50f, 45, 0.22f, "triangle")
                delay(65)
            }
            // final sweet landing click
            playTone(450f, 400f, 80, 0.25f, "sine")
        }
    }

    fun playYourTurnAlert() {
        scope.launch {
            if (!isSfxEnabled) return@launch
            // Super sweet cheerful double chime (E5 -> A5) for turn alert
            playTone(659.25f, 659.25f, 110, 0.28f, "sine")
            delay(130)
            playTone(880.00f, 880.00f, 260, 0.28f, "sine")
        }
    }

    fun playPawnHop() {
        // Frequency sweep low to high (pop sound!)
        playTone(180f, 620f, 120, 0.35f, "sine")
    }

    fun playBumpScream() {
        scope.launch {
            if (!isSfxEnabled) return@launch
            // Cute brief slide down tone
            playTone(400f, 200f, 150, 0.2f, "sine")
        }
    }

    fun playGoalCelebration() {
        scope.launch {
            if (!isSfxEnabled) return@launch
            // Cheerful dessert-themed chime fanfare
            playTone(523.25f, 523.25f, 100, 0.18f, "sine")
            delay(100)
            playTone(659.25f, 659.25f, 100, 0.18f, "sine")
            delay(100)
            playTone(783.99f, 783.99f, 250, 0.22f, "sine")
        }
    }

    fun playPortalTeleport() {
        scope.launch {
            if (!isSfxEnabled) return@launch
            // Multi-frequency cosmic sweep
            for (i in 0..8) {
                val freq = 400f + (i * 120f)
                playTone(freq, freq + 150f, 100, 0.2f, "triangle")
                delay(80)
            }
            // Retro laser zap landing
            playTone(1500f, 600f, 300, 0.25f, "sine")
        }
    }

    fun playEmoteSound(type: EmoteType) {
        scope.launch {
            if (!isSfxEnabled) return@launch
            when (type) {
                EmoteType.ANGRY -> playTone(220f, 180f, 200, 0.22f, "sine")
                EmoteType.CRY -> {
                    playTone(350f, 300f, 150, 0.18f, "sine")
                    delay(160)
                    playTone(320f, 280f, 200, 0.18f, "sine")
                }
                EmoteType.LOVE -> {
                    playTone(523.25f, 659.25f, 150, 0.22f, "sine")
                    delay(120)
                    playTone(783.99f, 880f, 150, 0.22f, "sine")
                }
                EmoteType.LAUGH -> {
                    playTone(600f, 750f, 80, 0.18f, "sine")
                    delay(100)
                    playTone(600f, 750f, 120, 0.18f, "sine")
                }
                EmoteType.SLEEPY -> playTone(300f, 300f, 350, 0.14f, "sine")
                else -> playTone(440f, 330f, 150, 0.18f, "sine")
            }
        }
    }

    fun startMusic(isMainMenu: Boolean = true) {
        if (!isMusicEnabled) return
        
        var targetTrack: String? = null
        if (appContext != null) {
            val trackCandidates = if (isMainMenu) {
                listOf("home.mp3", "music/home.mp3")
            } else {
                listOf("play.mp3", "music/play.mp3")
            }
            
            for (candidate in trackCandidates) {
                try {
                    appContext!!.assets.open(candidate).use { stream ->
                        if (stream.available() > 0) {
                            targetTrack = candidate
                        }
                    }
                } catch (e: Exception) {
                    // Ignore and try next
                }
                if (targetTrack != null) break
            }
        }
        
        // If we don't have a valid custom track, stop any active MediaPlayer and play synthesized music instead
        if (targetTrack == null) {
            stopMusic()
            startSynthesizedMusic()
            return
        }
        
        // If already playing the correct track, do nothing
        if (mediaPlayer != null && currentTrack == targetTrack && mediaPlayer!!.isPlaying) {
            return
        }
        
        // If a different track is playing, stop it first
        if (currentTrack != targetTrack) {
            stopMusic()
        }
        
        currentTrack = targetTrack
        if (appContext != null) {
            // Stop synthesized music if running
            stopSynthesizedMusic()
            
            // Start MediaPlayer
            if (mediaPlayer == null) {
                try {
                    mediaPlayer = MediaPlayer().apply {
                        try {
                            val afd = appContext!!.assets.openFd(targetTrack!!)
                            setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                            afd.close()
                        } catch (ex: Exception) {
                            // If openFd fails (e.g. compressed), copy to a temp cache file
                            val cacheFile = java.io.File(appContext!!.cacheDir, targetTrack!!.replace("/", "_"))
                            // Always recreate/check size just in case, or check if exists
                            if (!cacheFile.exists() || cacheFile.length() == 0L) {
                                appContext!!.assets.open(targetTrack!!).use { input ->
                                    java.io.FileOutputStream(cacheFile).use { output ->
                                        input.copyTo(output)
                                    }
                                }
                            }
                            if (cacheFile.exists() && cacheFile.length() > 0) {
                                setDataSource(cacheFile.absolutePath)
                            } else {
                                throw Exception("Cache file is empty")
                            }
                        }
                        isLooping = true
                        setVolume(0.22f, 0.22f)
                        prepare()
                        start()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Release the bad player to avoid any leak or crash
                    try {
                        mediaPlayer?.release()
                    } catch (ex: Exception) {}
                    mediaPlayer = null
                    // Fallback to synth melody
                    startSynthesizedMusic()
                }
            } else if (!mediaPlayer!!.isPlaying) {
                try {
                    mediaPlayer?.start()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            // No custom asset found, use our synthesized melody loop
            startSynthesizedMusic()
        }
    }

    private fun startSynthesizedMusic() {
        if (musicJob != null) return
        musicJob = scope.launch {
            // A simple cute, sweet dessert-themed loop: 4-bar sweet melody in C major pentatonic
            val melody = listOf(
                261.63f, 293.66f, 329.63f, 392.00f, 329.63f, 293.66f, 261.63f, 0f,
                329.63f, 392.00f, 440.00f, 523.25f, 440.00f, 392.00f, 329.63f, 0f
            )
            val durations = listOf(
                300, 300, 300, 300, 300, 300, 600, 300,
                300, 300, 300, 300, 300, 300, 600, 300
            )
            var index = 0

            while (isActive) {
                if (isMusicEnabled) {
                    val note = melody[index]
                    val duration = durations[index]
                    if (note > 0f) {
                        playTone(note, note, duration - 50, 0.08f, "sine")
                    }
                    delay(duration.toLong())
                } else {
                    delay(500)
                }
                index = (index + 1) % melody.size
            }
        }
    }

    private fun stopSynthesizedMusic() {
        musicJob?.cancel()
        musicJob = null
    }

    fun stopMusic() {
        // Stop custom media player
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaPlayer = null
        currentTrack = null
        
        // Stop synthesized music
        stopSynthesizedMusic()
    }
}

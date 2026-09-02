package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sin
import kotlin.random.Random

/**
 * Synthesizes high-quality ambient forest soundscapes (gentle wind in leaves,
 * distant rustling streams, and natural bird chirps) in real-time using PCM audio synthesis.
 * Completely offline, zero asset downloads, and zero frame drops (runs off UI thread).
 */
class ForestAmbientAudioPlayer {

    enum class SfxType {
        CHANT, CRAFT, LEVEL_UP, CLICK, CHOP
    }

    private var audioTrack: AudioTrack? = null
    private var synthesisJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    @Volatile
    private var isPlaying = false

    @Volatile
    private var volumeMultiplier = 0.85f

    private val _isAmbientEnabled = MutableStateFlow(true)
    val isAmbientEnabled: StateFlow<Boolean> = _isAmbientEnabled.asStateFlow()

    private val _isSfxEnabled = MutableStateFlow(true)
    val isSfxEnabled: StateFlow<Boolean> = _isSfxEnabled.asStateFlow()

    private val _ambientVolume = MutableStateFlow(0.85f)
    val ambientVolume: StateFlow<Float> = _ambientVolume.asStateFlow()

    private val _sfxVolume = MutableStateFlow(0.85f)
    val sfxVolume: StateFlow<Float> = _sfxVolume.asStateFlow()

    fun start() {
        if (isPlaying || !_isAmbientEnabled.value) return
        isPlaying = true

        synthesisJob = scope.launch {
            try {
                val sampleRate = 44100
                val minBufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                val bufferSize = (minBufferSize * 2).coerceAtLeast(8820)

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                if (track.state != AudioTrack.STATE_INITIALIZED) {
                    isPlaying = false
                    return@launch
                }

                audioTrack = track
                try {
                    track.play()
                } catch (e: Exception) {
                    isPlaying = false
                    stopTrackSafely()
                    return@launch
                }

                val buffer = ShortArray(1024)
                var phaseWind1 = 0.0
                var phaseWind2 = 0.0
                var lfoPhase = 0.0

                // Bird call synthesis state
                var birdActive = false
                var birdSamplesRemaining = 0
                var birdBaseFreq = 2200.0
                var birdFreqMod = 0.0
                var birdTime = 0.0
                var nextBirdInSamples = Random.nextInt(sampleRate, sampleRate * 3)

                val random = Random(System.currentTimeMillis())

                while (isPlaying && isActive) {
                    if (track.state != AudioTrack.STATE_INITIALIZED || track.playState != AudioTrack.PLAYSTATE_PLAYING) {
                        break
                    }

                    val currentVol = _ambientVolume.value
                    for (i in buffer.indices) {
                        // 1. Soft Wind & Stream Soundscape (Pink Noise + Dual Sine Modulation)
                        phaseWind1 += 0.012 + 0.005 * sin(lfoPhase)
                        phaseWind2 += 0.018 + 0.003 * sin(lfoPhase * 0.7)
                        lfoPhase += 0.0003

                        val noise = (random.nextFloat() * 2f - 1f) * 0.25f
                        val breezeWave = (sin(phaseWind1) * 0.35f + sin(phaseWind2) * 0.25f + noise * 0.4f)
                        val windSample = breezeWave * (0.5f + 0.3f * sin(lfoPhase * 0.5f).toFloat())

                        // 2. Bird Chirp Synthesis
                        var birdSample = 0f
                        if (!birdActive) {
                            nextBirdInSamples--
                            if (nextBirdInSamples <= 0) {
                                birdActive = true
                                birdSamplesRemaining = Random.nextInt(3500, 8000) // ~80ms to 180ms chirp
                                birdBaseFreq = Random.nextDouble(2000.0, 3600.0)
                                birdTime = 0.0
                                nextBirdInSamples = Random.nextInt(sampleRate, sampleRate * 4)
                            }
                        } else {
                            birdTime += 1.0 / sampleRate
                            birdFreqMod = sin(birdTime * 140.0) * 500.0
                            val env = sin((1.0 - birdSamplesRemaining.toDouble() / 8000.0) * Math.PI).toFloat().coerceIn(0f, 1f)
                            birdSample = (sin(2.0 * Math.PI * (birdBaseFreq + birdFreqMod) * birdTime) * 0.35 * env).toFloat()

                            birdSamplesRemaining--
                            if (birdSamplesRemaining <= 0) {
                                birdActive = false
                            }
                        }

                        // Combined Master Sample
                        val mixed = ((windSample + birdSample) * currentVol).toFloat()
                        val clamped = mixed.coerceIn(-1f, 1f)
                        buffer[i] = (clamped * 32767f).toInt().toShort()
                    }

                    if (!isPlaying || !isActive) break

                    val written = try {
                        track.write(buffer, 0, buffer.size)
                    } catch (e: Exception) {
                        Log.w("ForestAudioPlayer", "AudioTrack write failed: ${e.message}")
                        break
                    }

                    if (written < 0) {
                        break
                    }
                }

            } catch (e: Exception) {
                Log.e("ForestAudioPlayer", "Audio track error: ${e.message}")
            } finally {
                stopTrackSafely()
            }
        }
    }

    fun toggleAmbient() {
        val next = !_isAmbientEnabled.value
        _isAmbientEnabled.value = next
        if (next) {
            start()
        } else {
            stop()
        }
    }

    fun toggleSfx() {
        _isSfxEnabled.value = !_isSfxEnabled.value
    }

    fun setAmbientVolume(vol: Float) {
        val clamped = vol.coerceIn(0f, 1f)
        _ambientVolume.value = clamped
        volumeMultiplier = clamped
    }

    fun setSfxVolume(vol: Float) {
        val clamped = vol.coerceIn(0f, 1f)
        _sfxVolume.value = clamped
    }

    fun playSfx(type: SfxType) {
        if (!_isSfxEnabled.value) return
        val currentSfxVol = _sfxVolume.value.toDouble()
        scope.launch {
            try {
                val sampleRate = 44100
                val durationMs = when (type) {
                    SfxType.CHANT -> 350
                    SfxType.CRAFT -> 180
                    SfxType.LEVEL_UP -> 600
                    SfxType.CLICK -> 60
                    SfxType.CHOP -> 150
                }
                val sampleCount = (sampleRate * (durationMs / 1000.0)).toInt()
                val pcmData = ShortArray(sampleCount)

                for (i in pcmData.indices) {
                    val t = i.toDouble() / sampleRate
                    val progress = i.toDouble() / sampleCount
                    val env = (1.0 - progress).coerceIn(0.0, 1.0)

                    val sampleVal = when (type) {
                        SfxType.CHANT -> {
                            val freq1 = 440.0 // A4
                            val freq2 = 554.37 // C#5
                            val chord = (sin(2 * Math.PI * freq1 * t) + sin(2 * Math.PI * freq2 * t)) * 0.4
                            chord * env
                        }
                        SfxType.CRAFT -> {
                            val freq = 600.0 + sin(t * 80.0) * 150.0
                            sin(2 * Math.PI * freq * t) * 0.5 * env
                        }
                        SfxType.LEVEL_UP -> {
                            val freq = 523.25 + progress * 500.0 // C5 sliding up
                            sin(2 * Math.PI * freq * t) * 0.6 * env
                        }
                        SfxType.CLICK -> {
                            (Random.nextDouble(-0.5, 0.5)) * env
                        }
                        SfxType.CHOP -> {
                            val freq = 180.0 - progress * 80.0
                            sin(2 * Math.PI * freq * t) * 0.6 * env
                        }
                    }
                    val scaled = sampleVal * currentSfxVol
                    pcmData[i] = (scaled.coerceIn(-1.0, 1.0) * 32767.0).toInt().toShort()
                }

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(pcmData.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                if (track.state == AudioTrack.STATE_INITIALIZED) {
                    val written = try {
                        track.write(pcmData, 0, pcmData.size)
                    } catch (e: Exception) {
                        -1
                    }
                    if (written >= 0) {
                        try {
                            track.play()
                            delay(durationMs.toLong() + 50)
                            if (track.state == AudioTrack.STATE_INITIALIZED) {
                                track.stop()
                            }
                        } catch (e: Exception) {
                            // ignore sfx play errors
                        }
                    }
                    try {
                        track.release()
                    } catch (e: Exception) {
                        // ignore release error
                    }
                } else {
                    try {
                        track.release()
                    } catch (e: Exception) {}
                }
            } catch (e: Exception) {
                Log.w("ForestAudioPlayer", "SFX error: ${e.message}")
            }
        }
    }

    fun stop() {
        isPlaying = false
        synthesisJob?.cancel()
        synthesisJob = null
        stopTrackSafely()
    }

    fun setVolume(vol: Float) {
        volumeMultiplier = vol.coerceIn(0f, 1f)
    }

    fun isPlaying(): Boolean = isPlaying

    private fun stopTrackSafely() {
        val targetTrack = audioTrack
        audioTrack = null
        try {
            targetTrack?.let {
                if (it.state == AudioTrack.STATE_INITIALIZED) {
                    if (it.playState == AudioTrack.PLAYSTATE_PLAYING) {
                        it.stop()
                    }
                    it.release()
                }
            }
        } catch (e: Exception) {
            // Safe cleanup
        }
    }
}

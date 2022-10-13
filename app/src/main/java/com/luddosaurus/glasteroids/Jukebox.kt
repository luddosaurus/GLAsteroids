package com.luddosaurus.glasteroids

import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.util.Log
import java.io.IOException


private const val TAG = "Jukebox"
const val MAX_STREAMS = 5
private const val DEFAULT_SFX_VOLUME = 0.5f
private const val MAX_SFX_VOLUME = 1f
private const val DEFAULT_MUSIC_VOLUME = 1f
private const val SOUNDS_PREF_KEY = "sound"
private const val MUSIC_PREF_KEY = "music"



class Jukebox(private val engine: Game) {

    private lateinit var soundPool: SoundPool
    private val mSoundsMap = HashMap<GameEvent, Int>()

    // Music
    private lateinit var mBgPlayer : MediaPlayer
    private var mSoundEnabled = true
    private var mMusicEnabled = true

    init {
        engine.getActivity().volumeControlStream = AudioManager.STREAM_MUSIC
        val prefs = engine.getPreferences()
        mSoundEnabled = prefs.getBoolean(SOUNDS_PREF_KEY, true)
        mMusicEnabled = prefs.getBoolean(MUSIC_PREF_KEY, true)
        loadIfNeeded()
    }

    fun toggleSoundStatus() {
        mSoundEnabled = !mSoundEnabled
        if (mSoundEnabled) {
            loadSounds()
        } else {
            unloadSounds()
        }
        engine.savePreference(SOUNDS_PREF_KEY, mSoundEnabled)
    }

    fun toggleMusicStatus() {
        mMusicEnabled = !mMusicEnabled
        if (mMusicEnabled) {
            loadMusic()
        } else {
            unloadMusic()
        }
        engine.savePreference(MUSIC_PREF_KEY, mSoundEnabled)
    }

    private fun loadMusic() {
        try {
            mBgPlayer = MediaPlayer()
            val afd = engine.getAssets().openFd("bgm/song.mp3")
            mBgPlayer!!.setDataSource(
                afd.fileDescriptor,
                afd.startOffset,
                afd.length
            )
            mBgPlayer!!.isLooping = true
            mBgPlayer!!.setVolume(DEFAULT_MUSIC_VOLUME, DEFAULT_MUSIC_VOLUME)
            mBgPlayer!!.prepare()

        } catch (e: IOException) {
            Log.e(TAG, "Unable to create MediaPlayer.", e)
        }
    }

    fun pauseBgMusic() {
        if (!mMusicEnabled) {
            return
        }
        mBgPlayer!!.pause()
    }

    fun resumeBgMusic() {
        if (!mMusicEnabled) {
            return
        }
        mBgPlayer!!.start()
    }

    private fun unloadMusic() {
        if (mBgPlayer == null) {
            return
        }
        mBgPlayer!!.stop()
        mBgPlayer!!.release()
    }


    private fun loadIfNeeded() {
        if (mSoundEnabled) {
            loadSounds()
        }
        if (mMusicEnabled) {
            loadMusic()
        }
    }

    private fun loadSounds() {
        val attr = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setAudioAttributes(attr)
            .setMaxStreams(MAX_STREAMS)
            .build()

        mSoundsMap.clear()
        loadEventSound(GameEvent.Boost, "sfx/boost.wav")
        loadEventSound(GameEvent.DAMAGE, "sfx/hit.wav")
        loadEventSound(GameEvent.PEW, "sfx/laser.wav")
        loadEventSound(GameEvent.LevelGoal, "sfx/victory.wav")
        loadEventSound(GameEvent.LevelStart, "sfx/level_start.wav")
    }

    private fun loadEventSound(event: GameEvent, fileName: String) {
        try {
            val afd = engine.getAssets().openFd(fileName)
            val soundId = soundPool.load(afd, 1)
            mSoundsMap[event] = soundId
        } catch (e: IOException) {
            Log.e(TAG, "Error loading sound $e")
        }
    }

    private fun unloadSounds() {
        soundPool.release()
        mSoundsMap.clear()
    }

    fun playEventSound(event: GameEvent) {
        if (!mSoundEnabled) {
            return
        }

        val leftVolume = DEFAULT_SFX_VOLUME
        val rightVolume = DEFAULT_SFX_VOLUME
        val priority = 1
        val loop = 0 //-1 loop forever, 0 play once
        val rate = 1.0f
        val soundID = mSoundsMap[event]
        if(soundID == null){
            Log.e(TAG, "Attempting to play non-existent event sound: {event}")
            return
        }
        if (soundID > 0) { //if soundID is 0, the file failed to load. Make sure you catch this in the loading routine.
            soundPool.play(soundID, leftVolume, rightVolume, priority, loop, rate)
        }
    }
    
    fun destroy() {
        soundPool.release()
        //the sound-pool can no longer be used! you have to create a new sound-pool.
    }

}
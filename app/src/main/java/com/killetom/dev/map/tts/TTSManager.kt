package com.killetom.dev.map.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.*

class TTSManager {
    private var mTts: TextToSpeech? = null
    private var isLoaded = false
    fun init(context: Context?) {

        if (mTts == null){
            mTts = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val result = mTts!!.setLanguage(Locale.CHINA) // 设置中文
                    if (result == TextToSpeech.LANG_MISSING_DATA ||
                        result == TextToSpeech.LANG_NOT_SUPPORTED
                    ) {
                        Log.e("TTS", "语言不支持")
                    } else {
                        isLoaded = true
                    }
                } else {
                    Log.e("TTS", "初始化失败")
                }
        }


        }
    }

    fun speak(text: String?) {
        if (isLoaded && !text.isNullOrEmpty()) {
            // 设置音调，1.0为正常
            mTts?.setPitch(1.0f)
            // 设置语速，1.0为正常
            mTts?.setSpeechRate(1.0f)
            // 播放语音
            mTts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    fun shutdown() {
        if (mTts != null) {
            mTts?.stop()
            mTts?.shutdown()
            mTts = null
        }
    }
}
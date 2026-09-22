package com.example.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Locale

class VoiceSpeechManager(private val context: Context) {

    var isListening by mutableStateOf(false)
        private set

    var isSpeaking by mutableStateOf(false)
        private set

    var rmsLevel by mutableFloatStateOf(0f)
        private set

    var lastRecognizedText by mutableStateOf<String?>(null)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var ttsInitialized = false

    init {
        initTts()
    }

    private fun initTts() {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsInitialized = true
                // Try setting Urdu, fallback to English or device default
                val urduLocale = Locale("ur", "PK")
                val langResult = tts?.setLanguage(urduLocale)
                if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.setLanguage(Locale.US)
                }
                tts?.setSpeechRate(0.95f)
                tts?.setPitch(1.0f)
            }
        }
    }

    fun startListening(onResult: (String) -> Unit) {
        errorMessage = null
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            errorMessage = "Speech recognition not available on this device."
            return
        }

        stopSpeaking()
        stopListening()

        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        isListening = true
                    }

                    override fun onBeginningOfSpeech() {
                        isListening = true
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        rmsLevel = rmsdB
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        isListening = false
                    }

                    override fun onError(error: Int) {
                        isListening = false
                        // Error 7 is no match, handle gracefully
                        if (error != SpeechRecognizer.ERROR_NO_MATCH) {
                            errorMessage = "Voice error ($error). You can also type or use quick chips."
                        }
                    }

                    override fun onResults(results: Bundle?) {
                        isListening = false
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()
                        if (!text.isNullOrBlank()) {
                            lastRecognizedText = text
                            onResult(text)
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        matches?.firstOrNull()?.let {
                            lastRecognizedText = it
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ur-PK")
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ur-PK")
                putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }

            speechRecognizer?.startListening(intent)
            isListening = true
        } catch (e: Exception) {
            isListening = false
            errorMessage = e.message
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
        } catch (_: Exception) {}
        speechRecognizer = null
        isListening = false
    }

    fun speak(text: String) {
        if (!ttsInitialized || tts == null) return
        stopSpeaking()
        isSpeaking = true
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "UTTERANCE_ID_${System.currentTimeMillis()}")
    }

    fun stopSpeaking() {
        try {
            tts?.stop()
        } catch (_: Exception) {}
        isSpeaking = false
    }

    fun destroy() {
        stopListening()
        stopSpeaking()
        try {
            tts?.shutdown()
        } catch (_: Exception) {}
        tts = null
    }
}

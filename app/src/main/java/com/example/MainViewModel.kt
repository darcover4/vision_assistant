package com.example

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val appPreferences: AppPreferences,
    private val ttsHelper: TextToSpeechHelper
) : ViewModel() {

    private val _apiKey = MutableStateFlow<String?>(null)
    val apiKey: StateFlow<String?> = _apiKey.asStateFlow()

    private val _videoInterval = MutableStateFlow(2)
    val videoInterval: StateFlow<Int> = _videoInterval.asStateFlow()

    private val _detailLevel = MutableStateFlow("brief")
    val detailLevel: StateFlow<String> = _detailLevel.asStateFlow()

    private val _speechRate = MutableStateFlow(1.0f)
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

    private val _textSize = MutableStateFlow(24)
    val textSize: StateFlow<Int> = _textSize.asStateFlow()

    private val _statusText = MutableStateFlow("Готов к работе")
    val statusText: StateFlow<String> = _statusText.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()
    
    private val _isVideoMode = MutableStateFlow(false)
    val isVideoMode: StateFlow<Boolean> = _isVideoMode.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isWaitingForNextFrame = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            appPreferences.apiKeyFlow.collect { key ->
                if (!key.isNullOrBlank()) {
                    _apiKey.value = key
                } else {
                    _apiKey.value = null
                }
            }
        }
        viewModelScope.launch {
            appPreferences.videoIntervalFlow.collect { interval ->
                _videoInterval.value = interval
            }
        }
        viewModelScope.launch {
            appPreferences.detailLevelFlow.collect { level ->
                _detailLevel.value = level
            }
        }
        viewModelScope.launch {
            appPreferences.speechRateFlow.collect { rate ->
                _speechRate.value = rate
                ttsHelper.setSpeechRate(rate)
            }
        }
        viewModelScope.launch {
            appPreferences.textSizeFlow.collect { size ->
                _textSize.value = size
            }
        }
    }

    fun saveApiKey(key: String) {
        viewModelScope.launch {
            appPreferences.saveApiKey(key)
        }
    }

    fun saveVideoInterval(interval: Int) {
        viewModelScope.launch {
            appPreferences.saveVideoInterval(interval)
        }
    }

    fun saveDetailLevel(level: String) {
        viewModelScope.launch {
            appPreferences.saveDetailLevel(level)
        }
    }

    fun saveSpeechRate(rate: Float) {
        viewModelScope.launch {
            appPreferences.saveSpeechRate(rate)
        }
    }

    fun saveTextSize(size: Int) {
        viewModelScope.launch {
            appPreferences.saveTextSize(size)
        }
    }

    fun stopSpeaking() {
        ttsHelper.stop()
        _isSpeaking.value = false
        _statusText.value = "Готов к работе"
        _isWaitingForNextFrame.value = false
    }

    private fun cleanText(text: String): String {
        return text.replace(Regex("[*#\\[\\]()]"), "")
    }

    private fun getPromptText(): String {
        return when (_detailLevel.value) {
            "brief" -> "Опиши, что ты видишь на этой фотографии. Опиши коротко и в общих чертах, только самые главные объекты. Будь максимально краток."
            else -> "Опиши, что ты видишь на этой фотографии. Дай достаточно подробное описание, но не вдавайся в мельчайшие детали."
        }
    }

    fun processPhoto(bitmap: Bitmap) {
        if (_isProcessing.value) return
        val currentKey = _apiKey.value
        if (currentKey.isNullOrBlank()) {
            _statusText.value = "Пожалуйста, укажите API ключ в настройках."
            ttsHelper.speak("Пожалуйста, укажите АПИ ключ в настройках.")
            return
        }

        viewModelScope.launch {
            _isProcessing.value = true
            _statusText.value = "Обработка фото..."
            ttsHelper.speak("Делаю фото. Подождите.")
            
            val prompt = getPromptText() + " Это для слепого или слабовидящего человека."
            val result = askApiWithImage(currentKey, prompt, bitmap)
            val cleanRes = cleanText(result)
            
            _statusText.value = cleanRes
            _isSpeaking.value = true
            ttsHelper.speak(cleanRes) {
                _isSpeaking.value = false
            }
            _isProcessing.value = false
        }
    }

    fun toggleVideoMode() {
        _isVideoMode.value = !_isVideoMode.value
        if (_isVideoMode.value) {
            _isWaitingForNextFrame.value = false
            _statusText.value = "Видео режим включен"
            _isSpeaking.value = true
            ttsHelper.speak("Запуск видео режима. Буду описывать происходящее перед камерой.") {
                _isSpeaking.value = false
            }
        } else {
            _statusText.value = "Видео режим остановлен"
            _isSpeaking.value = true
            ttsHelper.speak("Видео режим остановлен.") {
                _isSpeaking.value = false
            }
        }
    }
    
    fun processVideoFrame(bitmap: Bitmap) {
        if (!_isVideoMode.value || _isProcessing.value || _isWaitingForNextFrame.value) return
        val currentKey = _apiKey.value
        if (currentKey.isNullOrBlank()) return

        viewModelScope.launch {
            _isProcessing.value = true
            _statusText.value = "Анализ видео..."
            
            val prompt = getPromptText() + " Опиши только то, что происходит прямо сейчас. Это для слепого или слабовидящего человека."
            val result = askApiWithImage(currentKey, prompt, bitmap)
            val cleanRes = cleanText(result)
            
            if (_isVideoMode.value) {
                _statusText.value = cleanRes
                _isWaitingForNextFrame.value = true
                _isSpeaking.value = true
                ttsHelper.speak(cleanRes) {
                    _isSpeaking.value = false
                    viewModelScope.launch {
                        delay(_videoInterval.value * 1000L)
                        _isWaitingForNextFrame.value = false
                    }
                }
            }
            _isProcessing.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsHelper.shutdown()
    }
}

class MainViewModelFactory(
    private val appPreferences: AppPreferences,
    private val ttsHelper: TextToSpeechHelper
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(appPreferences, ttsHelper) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

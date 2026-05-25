package com.example.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.api.Content
import com.example.data.api.GeminiRequest
import com.example.data.api.Part
import com.example.data.api.RetrofitClient
import com.example.data.database.BookmarkEntity
import com.example.data.database.LastSelectedCityEntity
import com.example.data.database.WeatherDatabase
import com.example.data.model.GeocodingResult
import com.example.data.model.WeatherResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

sealed interface WeatherUiState {
    object Loading : WeatherUiState
    data class Success(val weather: WeatherResponse) : WeatherUiState
    data class Error(val message: String) : WeatherUiState
}

class WeatherViewModel(application: Application) : AndroidViewModel(application) {

    private val db = WeatherDatabase.getDatabase(application)
    private val bookmarkDao = db.bookmarkDao()

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private val _selectedCity = MutableStateFlow(
        GeocodingResult(
            id = 1642911L,
            name = "Jakarta",
            latitude = -6.2088,
            longitude = 106.8456,
            country = "Indonesia",
            admin1 = "DKI Jakarta",
            countryCode = "ID",
            timezone = "Asia/Jakarta"
        )
    )
    val selectedCity: StateFlow<GeocodingResult> = _selectedCity.asStateFlow()

    val bookmarkedCitiesFlow: StateFlow<List<GeocodingResult>> = bookmarkDao.getBookmarkedCities()
        .map { list -> list.map { it.toGeocodingResult() } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val isCurrentCityBookmarked: StateFlow<Boolean> = combine(_selectedCity, bookmarkDao.getBookmarkedCities()) { city, bookmarks ->
        bookmarks.any { it.id == city.id }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _isAutoDark = MutableStateFlow(true)
    val isAutoDark: StateFlow<Boolean> = _isAutoDark.asStateFlow()

    private val _geminiAdvice = MutableStateFlow<String?>(null)
    val geminiAdvice: StateFlow<String?> = _geminiAdvice.asStateFlow()

    private val _isGeminiLoading = MutableStateFlow(false)
    val isGeminiLoading: StateFlow<Boolean> = _isGeminiLoading.asStateFlow()

    private val _searchResults = MutableStateFlow<List<GeocodingResult>>(emptyList())
    val searchResults: StateFlow<List<GeocodingResult>> = _searchResults.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    // Sample list of popular places
    val popularCities = listOf(
        GeocodingResult(1642911, "Jakarta", -6.2088, 106.8456, "Indonesia", "DKI Jakarta"),
        GeocodingResult(1650357, "Bandung", -6.9175, 107.6191, "Indonesia", "Jawa Barat"),
        GeocodingResult(1630784, "Surabaya", -7.2575, 112.7521, "Indonesia", "Jawa Timur"),
        GeocodingResult(1621177, "Yogyakarta", -7.7956, 110.3695, "Indonesia", "DI Yogyakarta"),
        GeocodingResult(1633070, "Medan", 3.5952, 98.6722, "Indonesia", "Sumatera Utara"),
        GeocodingResult(1622318, "Ubud, Bali", -8.5069, 115.2625, "Indonesia", "Bali"),
        GeocodingResult(1636544, "Makassar", -5.1477, 119.4327, "Indonesia", "Sulawesi Selatan")
    )

    init {
        checkLocalTimeAndSetDarkMode()
        viewModelScope.launch {
            try {
                val lastSaved = bookmarkDao.getLastSelectedCity()
                if (lastSaved != null) {
                    val initialCity = lastSaved.toGeocodingResult()
                    _selectedCity.value = initialCity
                    loadWeatherForCity(initialCity)
                } else {
                    loadWeatherForCity(_selectedCity.value)
                }
            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Failed to retrieve last saved city from Room database", e)
                loadWeatherForCity(_selectedCity.value)
            }
        }
    }

    fun checkLocalTimeAndSetDarkMode() {
        if (_isAutoDark.value) {
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            // Evening hours (6 PM to 6 AM) automatically set Dark Mode
            _isDarkMode.value = hour < 6 || hour >= 18
        }
    }

    fun setAutoDarkEnabled(enabled: Boolean) {
        _isAutoDark.value = enabled
        if (enabled) {
            checkLocalTimeAndSetDarkMode()
        }
    }

    fun toggleDarkModeManual() {
        _isAutoDark.value = false
        _isDarkMode.value = !_isDarkMode.value
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.length >= 2) {
            triggerCitySearch(query)
        } else {
            _searchResults.value = emptyList()
        }
    }

    private fun triggerCitySearch(query: String) {
        _isSearching.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitClient.weatherService.searchCity(
                    url = "https://geocoding-api.open-meteo.com/v1/search",
                    name = query,
                    count = 8
                )
                _searchResults.value = response.results ?: emptyList()
            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Search city error", e)
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun selectCity(city: GeocodingResult) {
        _selectedCity.value = city
        _searchQuery.value = ""
        _searchResults.value = emptyList()
        loadWeatherForCity(city)
        viewModelScope.launch {
            try {
                bookmarkDao.saveLastSelectedCity(LastSelectedCityEntity.fromGeocodingResult(city))
            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Failed to save last selected city", e)
            }
        }
    }

    fun toggleBookmarkCurrentCity() {
        val city = _selectedCity.value
        viewModelScope.launch {
            try {
                val isBookmarked = bookmarkDao.isCityBookmarked(city.id)
                if (isBookmarked) {
                    bookmarkDao.deleteBookmark(city.id)
                } else {
                    bookmarkDao.insertBookmark(BookmarkEntity.fromGeocodingResult(city))
                }
            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Failed to toggle bookmark", e)
            }
        }
    }

    fun loadWeatherForCity(city: GeocodingResult) {
        _uiState.value = WeatherUiState.Loading
        _geminiAdvice.value = null
        viewModelScope.launch {
            try {
                val response = RetrofitClient.weatherService.getWeather(
                    url = "https://api.open-meteo.com/v1/forecast",
                    latitude = city.latitude,
                    longitude = city.longitude
                )
                _uiState.value = WeatherUiState.Success(response)
                
                // Trigger Gemini integration
                fetchGeminiWeatherInsight(city.name, response)
            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Get weather error", e)
                _uiState.value = WeatherUiState.Error(e.localizedMessage ?: "Gagal memuat data cuaca")
            }
        }
    }

    private fun fetchGeminiWeatherInsight(cityName: String, weatherData: WeatherResponse) {
        val current = weatherData.current ?: return
        val apiKey = BuildConfig.GEMINI_API_KEY

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            // Local fallback generating dynamic Indonesian text if API Key is not set
            _geminiAdvice.value = getLocalFallbackAdvice(cityName, current.temperature, current.weatherCode)
            return
        }

        _isGeminiLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val textPrompt = """
                    Berikan analisis cuaca singkat, cerdas, kreatif, dan rekomendasi aktivitas yang menyenangkan dalam bahasa Indonesia yang ramah dan inspiratif (maksimal 2 kalimat santai). Anggap kamu asisten cuaca pribadi cerdas yang seru.
                    Detail cuaca saat ini:
                    Kota: $cityName
                    Suhu: ${current.temperature}°C
                    Terasa seperti: ${current.apparentTemperature}°C
                    Kelembaban: ${current.humidity}%
                    Kode cuaca (WMO): ${current.weatherCode}
                    Kecepatan angin: ${current.windSpeed} km/h
                """.trimIndent()

                val request = GeminiRequest(
                    contents = listOf(
                        Content(
                            parts = listOf(Part(text = textPrompt))
                        )
                    )
                )

                val response = RetrofitClient.geminiService.generateContent(apiKey, request)
                val adviceText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

                withContext(Dispatchers.Main) {
                    if (!adviceText.isNullOrBlank()) {
                        _geminiAdvice.value = adviceText.trim()
                    } else {
                        _geminiAdvice.value = getLocalFallbackAdvice(cityName, current.temperature, current.weatherCode)
                    }
                }
            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Gemini API failed", e)
                withContext(Dispatchers.Main) {
                    _geminiAdvice.value = getLocalFallbackAdvice(cityName, current.temperature, current.weatherCode)
                }
            } finally {
                withContext(Dispatchers.Main) {
                    _isGeminiLoading.value = false
                }
            }
        }
    }

    private fun getLocalFallbackAdvice(cityName: String, temp: Double, code: Int): String {
        return when (code) {
            0 -> "Langit di $cityName sangat cerah hari ini dengan suhu ${temp}°C! Sempurna untuk berjalan-jalan sore atau menikmati kopi susu dingin di luar."
            1, 2, 3 -> "Cuaca di $cityName cukup teduh dan berawan hari ini. Waktu yang luar biasa untuk menikmati waktu santai atau menyelesaikan pekerjaan di kafe estetis."
            45, 48 -> "Udara berkabut menyelimuti $cityName saat ini. Tetap hangat, berkendara dengan hati-hati jika bepergian, dan secangkir teh jahe hangat akan melengkapi harimu!"
            51, 53, 55, 61, 63, 65, 80, 81, 82 -> "Hujan menyegarkan sedang membasahi $cityName. Persiapkan payungmu sebelum bepergian, atau nikmati suasana syahdu dengan membaca buku favorit di dalam ruangan."
            95, 96, 99 -> "Gemuruh petir terdengar di langit $cityName. Sebaiknya hindari bepergian keluar, amankan peralatan elektronik rumah tangga, dan buat dirimu nyaman di dalam rumah!"
            else -> "Cuaca syahdu menyelimuti $cityName hari ini. Jangan lupa menjaga kesehatan tubuh dengan konsumsi cairan yang cukup sepanjang aktivitasmu."
        }
    }
}

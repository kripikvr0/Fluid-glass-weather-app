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
import com.example.data.model.CurrentAirQuality
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
            id = 1626083L,
            name = "Tangerang",
            latitude = -6.1702,
            longitude = 106.6403,
            country = "Indonesia",
            admin1 = "Banten",
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

    private val sharedPrefs = application.getSharedPreferences("weather_prefs", android.content.Context.MODE_PRIVATE)

    private val _tempUnit = MutableStateFlow(sharedPrefs.getString("temp_unit", "C") ?: "C")
    val tempUnit: StateFlow<String> = _tempUnit.asStateFlow()

    private val _windUnit = MutableStateFlow(sharedPrefs.getString("wind_unit", "km/h") ?: "km/h")
    val windUnit: StateFlow<String> = _windUnit.asStateFlow()

    private val _language = MutableStateFlow(sharedPrefs.getString("app_language", "ID") ?: "ID")
    val language: StateFlow<String> = _language.asStateFlow()

    private val _isOfflineMode = MutableStateFlow(false)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()

    private val _airQuality = MutableStateFlow<CurrentAirQuality?>(null)
    val airQuality: StateFlow<CurrentAirQuality?> = _airQuality.asStateFlow()

    private val moshi = com.squareup.moshi.Moshi.Builder()
        .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
        .build()
    private val weatherAdapter by lazy { moshi.adapter(WeatherResponse::class.java) }
    private val aqAdapter by lazy { moshi.adapter(CurrentAirQuality::class.java) }

    fun setTempUnit(unit: String) {
        _tempUnit.value = unit
        sharedPrefs.edit().putString("temp_unit", unit).apply()
    }

    fun setWindUnit(unit: String) {
        _windUnit.value = unit
        sharedPrefs.edit().putString("wind_unit", unit).apply()
    }

    fun setLanguage(lang: String) {
        _language.value = lang
        sharedPrefs.edit().putString("app_language", lang).apply()
        // Re-fetch Gemini insight in matching language
        val state = _uiState.value
        if (state is WeatherUiState.Success) {
            fetchGeminiWeatherInsight(_selectedCity.value.name, state.weather)
        }
    }


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
        GeocodingResult(1626083L, "Tangerang", -6.1702, 106.6403, "Indonesia", "Banten"),
        GeocodingResult(1642911, "Jakarta", -6.2088, 106.8456, "Indonesia", "DKI Jakarta"),
        GeocodingResult(1650357, "Bandung", -6.9175, 107.6191, "Indonesia", "Jawa Barat"),
        GeocodingResult(1630784, "Surabaya", -7.2575, 112.7521, "Indonesia", "Jawa Timur"),
        GeocodingResult(1621177, "Yogyakarta", -7.7956, 110.3695, "Indonesia", "DI Yogyakarta"),
        GeocodingResult(1633070, "Medan", 3.5952, 98.6722, "Indonesia", "Sumatera Utara"),
        GeocodingResult(1640084, "Jayapura", -2.5916, 140.6690, "Indonesia", "Papua"),
        GeocodingResult(1651532, "Balikpapan", -1.2650, 116.8312, "Indonesia", "Kalimantan Timur"),
        GeocodingResult(1648473, "Banda Aceh", 5.5577, 95.3222, "Indonesia", "Aceh"),
        GeocodingResult(1640972, "Kupang", -10.1656, 123.6014, "Indonesia", "Nusa Tenggara Timur"),
        GeocodingResult(1622318, "Ubud, Bali", -8.5069, 115.2625, "Indonesia", "Bali"),
        GeocodingResult(1636544, "Makassar", -5.1477, 119.4327, "Indonesia", "Sulawesi Selatan"),
        GeocodingResult(1650158, "Ambon", -3.6954, 128.1814, "Indonesia", "Maluku")
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
            } catch (e: Throwable) {
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
        _isOfflineMode.value = false
        _airQuality.value = null
        viewModelScope.launch {
            try {
                // Fetch Air Quality from Air Quality API
                try {
                    val aqResponse = RetrofitClient.weatherService.getAirQuality(
                        url = "https://air-quality-api.open-meteo.com/v1/air-quality",
                        latitude = city.latitude,
                        longitude = city.longitude
                    )
                    val aqCurrent = aqResponse.current
                    _airQuality.value = aqCurrent
                    if (aqCurrent != null) {
                        val aqJson = aqAdapter.toJson(aqCurrent)
                        sharedPrefs.edit().putString("cached_aq_${city.id}", aqJson).apply()
                    }
                } catch (aqe: Throwable) {
                    Log.e("WeatherViewModel", "Get air quality error", aqe)
                }

                val response = RetrofitClient.weatherService.getWeather(
                    url = "https://api.open-meteo.com/v1/forecast",
                    latitude = city.latitude,
                    longitude = city.longitude
                )
                
                // Cache successfully parsed response
                try {
                    val json = weatherAdapter.toJson(response)
                    sharedPrefs.edit().putString("cached_weather_${city.id}", json).apply()
                } catch (ce: Throwable) {
                    Log.e("WeatherViewModel", "Failed to cache response", ce)
                }

                _uiState.value = WeatherUiState.Success(response)
                
                // Trigger Gemini integration
                fetchGeminiWeatherInsight(city.name, response)
            } catch (e: Throwable) {
                Log.e("WeatherViewModel", "Get weather error", e)
                
                // Try to load cached weather from SharedPreferences
                val cachedJson = sharedPrefs.getString("cached_weather_${city.id}", null)
                val cachedAqJson = sharedPrefs.getString("cached_aq_${city.id}", null)
                
                if (cachedAqJson != null) {
                    try {
                        val aqCurrent = aqAdapter.fromJson(cachedAqJson)
                        _airQuality.value = aqCurrent
                    } catch (aqce: Throwable) {
                        Log.e("WeatherViewModel", "Failed to decode AQ cache", aqce)
                    }
                }

                if (cachedJson != null) {
                    try {
                        val cachedResponse = weatherAdapter.fromJson(cachedJson)
                        if (cachedResponse != null) {
                            _isOfflineMode.value = true
                            _uiState.value = WeatherUiState.Success(cachedResponse)
                            fetchGeminiWeatherInsight(city.name, cachedResponse)
                            return@launch
                        }
                    } catch (ce: Throwable) {
                        Log.e("WeatherViewModel", "Failed to decode cache", ce)
                    }
                }
                
                _uiState.value = WeatherUiState.Error(
                    if (_language.value == "ID") {
                        e.localizedMessage ?: "Gagal memuat data cuaca"
                    } else {
                        e.localizedMessage ?: "Failed to load weather data"
                    }
                )
            }
        }
    }

    private fun fetchGeminiWeatherInsight(cityName: String, weatherData: WeatherResponse) {
        val current = weatherData.current ?: return
        val apiKey = BuildConfig.GEMINI_API_KEY
        val isEng = _language.value == "EN"

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            _geminiAdvice.value = getLocalFallbackAdvice(cityName, current.temperature, current.weatherCode)
            return
        }

        _isGeminiLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val textPrompt = if (isEng) {
                    """
                        Provide a brief, intelligent, creative weather analysis and fun activity recommendation in English, with a friendly and inspiring tone (maximum 2 simple sentences). Imagine you are a cool intelligent personal weather companion.
                        Current weather details:
                        City: $cityName
                        Temp: ${current.temperature}°C
                        Apparent feels: ${current.apparentTemperature}°C
                        Humidity: ${current.humidity}%
                        WMO Weather code: ${current.weatherCode}
                        Wind speed: ${current.windSpeed} km/h
                    """.trimIndent()
                } else {
                    """
                        Berikan analisis cuaca singkat, cerdas, kreatif, dan rekomendasi aktivitas yang menyenangkan dalam bahasa Indonesia yang ramah dan inspiratif (maksimal 2 kalimat santai). Anggap kamu asisten cuaca pribadi cerdas yang seru.
                        Detail cuaca saat ini:
                        Kota: $cityName
                        Suhu: ${current.temperature}°C
                        Terasa seperti: ${current.apparentTemperature}°C
                        Kelembaban: ${current.humidity}%
                        Kode cuaca (WMO): ${current.weatherCode}
                        Kecepatan angin: ${current.windSpeed} km/h
                    """.trimIndent()
                }

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
        val isEng = _language.value == "EN"
        return if (isEng) {
            when (code) {
                0 -> "The sky in $cityName is beautifully clear today at ${temp}°C! Ideal for a pleasant outdoor walk or enjoying cold coffee."
                1, 2, 3 -> "The weather in $cityName is quite cloudy and pleasant today. A wonderful time to read books or finish up some tasks inside a cozy cafe."
                45, 48 -> "Foggy conditions are blanketing $cityName right now. Drive carefully, stay warm, and a cup of warm ginger tea will be perfect!"
                51, 53, 55, 61, 63, 65, 80, 81, 82 -> "Refreshing rain is falling in $cityName. Don't forget your umbrella, or enjoy the cozy rain sounds by reading your favorite book indoors."
                95, 96, 99 -> "Thunderstorms are roaring over $cityName. Best to stay indoors, secure electrical outlets, and get comfortable inside!"
                else -> "A serene day is hovering over $cityName. Remember to stay hydrated and take care of yourself throughout the day."
            }
        } else {
            when (code) {
                0 -> "Langit di $cityName sangat cerah hari ini dengan suhu ${temp}°C! Sempurna untuk berjalan-jalan sore atau menikmati kopi susu dingin di luar."
                1, 2, 3 -> "Cuaca di $cityName cukup teduh dan berawan hari ini. Waktu yang luar biasa untuk menikmati waktu santai atau menyelesaikan pekerjaan di kafe estetis."
                45, 48 -> "Udara berkabut menyelimuti $cityName saat ini. Tetap hangat, berkendara dengan hati-hati jika bepergian, dan secangkir teh jahe hangat akan melengkapi harimu!"
                51, 53, 55, 61, 63, 65, 80, 81, 82 -> "Hujan menyegarkan sedang membasahi $cityName. Persiapkan payungmu sebelum bepergian, atau nikmati suasana syahdu dengan membaca buku favorit di dalam ruangan."
                95, 96, 99 -> "Gemuruh petir terdengar di langit $cityName. Sebaiknya hindari bepergian keluar, amankan peralatan elektronik rumah tangga, dan buat dirimu nyaman di dalam rumah!"
                else -> "Cuaca syahdu menyelimuti $cityName hari ini. Jangan lupa menjaga kesehatan tubuh dengan konsumsi cairan yang cukup sepanjang aktivitasmu."
            }
        }
    }

    fun fetchWeatherFromLocation(latitude: Double, longitude: Double, customCityName: String? = null) {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            _geminiAdvice.value = null
            _isOfflineMode.value = false
            
            val locationId = (latitude.hashCode().toLong() shl 32) or (longitude.hashCode().toLong() and 0xFFFFFFFFL)
            
            val resolvedName = customCityName ?: withContext(Dispatchers.IO) {
                try {
                    val geocoder = android.location.Geocoder(getApplication(), java.util.Locale.getDefault())
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    val address = addresses?.firstOrNull()
                    address?.locality ?: address?.subAdminArea ?: address?.adminArea ?: "Lokasi Saya"
                } catch (e: Exception) {
                    Log.e("WeatherViewModel", "Geocoder exception", e)
                    "Lokasi Saya"
                }
            }

            val cityToSet = GeocodingResult(
                id = locationId,
                name = resolvedName,
                latitude = latitude,
                longitude = longitude,
                country = null,
                admin1 = null,
                countryCode = null,
                timezone = null
            )
            
            _selectedCity.value = cityToSet
            loadWeatherForCity(cityToSet)
        }
    }

    fun detectLocationAndFetchWeather(onPermissionRequired: () -> Unit) {
        val context = getApplication<Application>()
        val hasFine = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        
        val hasCoarse = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) {
            onPermissionRequired()
            return
        }

        val locationManager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as android.location.LocationManager
        
        var bestLocation: android.location.Location? = null
        try {
            val providers = locationManager.getProviders(true)
            for (provider in providers) {
                val loc = locationManager.getLastKnownLocation(provider) ?: continue
                if (bestLocation == null || loc.accuracy < bestLocation.accuracy) {
                    bestLocation = loc
                }
            }
        } catch (e: SecurityException) {
            Log.e("WeatherViewModel", "Permission issue while reading last known location", e)
        }

        if (bestLocation != null) {
            fetchWeatherFromLocation(bestLocation.latitude, bestLocation.longitude)
        } else {
            try {
                val provider = if (locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)) {
                    android.location.LocationManager.NETWORK_PROVIDER
                } else if (locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
                    android.location.LocationManager.GPS_PROVIDER
                } else {
                    null
                }

                if (provider != null) {
                    locationManager.requestSingleUpdate(
                        provider,
                        object : android.location.LocationListener {
                            override fun onLocationChanged(location: android.location.Location) {
                                fetchWeatherFromLocation(location.latitude, location.longitude)
                            }
                            @Suppress("DEPRECATION")
                            override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) {}
                            override fun onProviderEnabled(provider: String) {}
                            override fun onProviderDisabled(provider: String) {}
                        },
                        android.os.Looper.getMainLooper()
                    )
                } else {
                    _uiState.value = WeatherUiState.Error(
                        if (_language.value == "ID") {
                            "Layanan lokasi GPS/Jaringan dinonaktifkan. Silakan aktifkan GPS Anda."
                        } else {
                            "GPS/Network location services are disabled. Please enable GPS."
                        }
                    )
                }
            } catch (e: SecurityException) {
                Log.e("WeatherViewModel", "Permission issue requesting single location update", e)
            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Failed to get location update", e)
                _uiState.value = WeatherUiState.Error(
                    if (_language.value == "ID") {
                        "Gagal mendeteksi lokasi ponsel Anda."
                    } else {
                        "Failed to detect your device location."
                    }
                )
            }
        }
    }

}

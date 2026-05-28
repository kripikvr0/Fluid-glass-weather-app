package com.example.ui

import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.NetworkService
import com.example.data.model.CityWeather
import com.example.ui.widget.WeatherAppWidgetProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WeatherViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "WeatherViewModel"
    private val prefs: SharedPreferences = application.getSharedPreferences("weather_prefs", Context.MODE_PRIVATE)

    // Current screen weather state
    private val _selectedCity = MutableStateFlow(prefs.getString("last_city", "DKI Jakarta") ?: "DKI Jakarta")
    val selectedCity: StateFlow<String> = _selectedCity.asStateFlow()

    private val _weatherState = MutableStateFlow<WeatherUIState>(WeatherUIState.Loading)
    val weatherState: StateFlow<WeatherUIState> = _weatherState.asStateFlow()

    // Bookmarked cities list
    private val _bookmarks = MutableStateFlow<List<String>>(emptyList())
    val bookmarks: StateFlow<List<String>> = _bookmarks.asStateFlow()

    // Widget customizer states (Persisted in SharePreferences for quick appwidget retrieves)
    private val _widgetBgType = MutableStateFlow(prefs.getString("widget_bg_type", "dark") ?: "dark")
    val widgetBgType: StateFlow<String> = _widgetBgType.asStateFlow()

    private val _widgetAccentHex = MutableStateFlow(prefs.getString("widget_accent_hex", "#0288D1") ?: "#0288D1")
    val widgetAccentHex: StateFlow<String> = _widgetAccentHex.asStateFlow()

    private val _widgetTag1 = MutableStateFlow(prefs.getString("widget_tag1", "wind") ?: "wind")
    val widgetTag1: StateFlow<String> = _widgetTag1.asStateFlow()

    private val _widgetTag2 = MutableStateFlow(prefs.getString("widget_tag2", "humidity") ?: "humidity")
    val widgetTag2: StateFlow<String> = _widgetTag2.asStateFlow()

    private val _widgetLayout = MutableStateFlow(prefs.getString("widget_layout", "detailed") ?: "detailed")
    val widgetLayout: StateFlow<String> = _widgetLayout.asStateFlow()

    private val _widgetForecastMode = MutableStateFlow(prefs.getString("widget_forecast_mode", "daily") ?: "daily")
    val widgetForecastMode: StateFlow<String> = _widgetForecastMode.asStateFlow()

    init {
        // Load initial bookmarked cities (default to classic Indonesia locations)
        val savedBookmarks = prefs.getStringSet("bookmarks", setOf("DKI Jakarta", "Yogyakarta", "Denpasar")) ?: setOf("DKI Jakarta", "Yogyakarta", "Denpasar")
        _bookmarks.value = savedBookmarks.toList().sorted()
        
        // Initial fetch
        getWeatherData(_selectedCity.value)
    }

    // Load weather data for the city
    fun getWeatherData(city: String) {
        viewModelScope.launch {
            _weatherState.value = WeatherUIState.Loading
            _selectedCity.value = city
            prefs.edit().putString("last_city", city).apply()

            // Try loading real API weather, fall back to offline-realistic dynamic generator
            val realResult = NetworkService.fetchRealWeather(city)
            if (realResult != null) {
                _weatherState.value = WeatherUIState.Success(realResult)
                // Cache selected weather in SharedPreferences so the Widget can access it instantly in dark/light mode
                saveWeatherForWidget(realResult)
            } else {
                Log.d(TAG, "Network API failed or returned null, generating local simulated conditions.")
                val fakeResult = NetworkService.generateDynamicWeather(city)
                _weatherState.value = WeatherUIState.Success(fakeResult)
                saveWeatherForWidget(fakeResult)
            }
        }
    }

    private fun saveWeatherForWidget(weather: CityWeather) {
        prefs.edit().apply {
            putString("widget_cached_city", weather.cityName)
            putFloat("widget_cached_temp", weather.temp.toFloat())
            putString("widget_cached_cond", weather.condition)
            putString("widget_cached_emoji", weather.iconEmoji)
            putFloat("widget_cached_wind", weather.windSpeed.toFloat())
            putFloat("widget_cached_rain", weather.rainfall.toFloat())
            putInt("widget_cached_humidity", weather.humidity)
            putInt("widget_cached_aqi", weather.aqi)
            putFloat("widget_cached_pm25", weather.pm25.toFloat())
            apply()
        }
        updateHomeScreenWidgets()
    }

    // Toggle city bookmark state
    fun toggleBookmark(city: String) {
        val currentList = _bookmarks.value.toMutableList()
        val isBookmarked = currentList.contains(city)
        if (isBookmarked) {
            currentList.remove(city)
        } else {
            currentList.add(city)
        }
        val sortedList = currentList.sorted()
        _bookmarks.value = sortedList
        prefs.edit().putStringSet("bookmarks", sortedList.toSet()).apply()
    }

    // Save widget customizations and broadcast updates immediately
    fun updateWidgetCustomization(
        bgType: String,
        accentHex: String,
        tag1: String,
        tag2: String,
        layout: String,
        forecastMode: String
    ) {
        viewModelScope.launch {
            _widgetBgType.value = bgType
            _widgetAccentHex.value = accentHex
            _widgetTag1.value = tag1
            _widgetTag2.value = tag2
            _widgetLayout.value = layout
            _widgetForecastMode.value = forecastMode

            prefs.edit().apply {
                putString("widget_bg_type", bgType)
                putString("widget_accent_hex", accentHex)
                putString("widget_tag1", tag1)
                putString("widget_tag2", tag2)
                putString("widget_layout", layout)
                putString("widget_forecast_mode", forecastMode)
                apply()
            }

            Log.d(TAG, "Widget customization updated. Triggering broadcast widget updates.")
            updateHomeScreenWidgets()
        }
    }

    // Helper to trigger Widget update broadcast
    fun updateHomeScreenWidgets() {
        val context = getApplication<Application>()
        val intent = Intent(context, WeatherAppWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, WeatherAppWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
        }
        context.sendBroadcast(intent)
    }
}

sealed class WeatherUIState {
    object Loading : WeatherUIState()
    data class Success(val weather: CityWeather) : WeatherUIState()
    data class Error(val message: String) : WeatherUIState()
}

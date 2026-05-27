package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.CurrentWeather
import com.example.data.model.GeocodingResult
import com.example.data.model.WeatherResponse
import com.example.ui.WeatherInfoMapper
import com.example.ui.WeatherUiState
import com.example.ui.WeatherViewModel
import com.example.ui.components.AnimatedWeatherIcon
import com.example.ui.components.FluidBackground
import com.example.ui.components.GlassCard
import java.util.Calendar

// --- Localization and Unit Conversion Helpers ---

fun getTranslate(key: String, language: String): String {
    val isEng = language == "EN"
    return when (key) {
        "search_placeholder" -> if (isEng) "Search city around the world..." else "Telusuri kota seluruh dunia..."
        "display_settings" -> if (isEng) "Display Settings" else "Pengaturan Tampilan"
        "auto_dark" -> if (isEng) "Auto Dark Mode" else "Gelap Otomatis (Auto)"
        "auto_dark_sub" -> if (isEng) "Adjusts to local night time" else "Menyesuaikan waktu malam lokal"
        "force_dark" -> if (isEng) "Force Dark Mode" else "Paksa Mode Gelap"
        "force_dark_sub" -> if (isEng) "Disable auto to toggle theme manually" else "Matikan otomatis untuk mengatur manual"
        "apply" -> if (isEng) "Apply" else "Terapkan"
        "syncing" -> if (isEng) "Syncing Weather..." else "Menyelaraskan Cuaca..."
        "failed_load" -> if (isEng) "Oops! Failed to Load Data" else "Oops! Gagal Memuat Data"
        "try_again" -> if (isEng) "Try Again" else "Coba Lagi"
        "now" -> if (isEng) "NOW" else "SEKARANG"
        "apparent_prefix" -> if (isEng) "Feels like" else "Sensasi"
        "humidity_label" -> if (isEng) "HUMIDITY" else "KELEMBABAN"
        "humidity_sub" -> if (isEng) "Outdoor" else "Luar"
        "humidity_footer" -> if (isEng) "Comfortable Dew Point" else "Titik Embun Nyaman"
        "hourly_label" -> if (isEng) "HOURLY FORECAST (12H)" else "PRAKIRAAN PER JAM (12 JAM KE DEPAN)"
        "wind_label" -> if (isEng) "WIND" else "ANGIN"
        "wind_footer" -> if (isEng) "Wind blowing West" else "Hembusan ke Barat"
        "uv_label" -> if (isEng) "UV INDEX" else "INDEKS SINAR UV"
        "uv_status" -> if (isEng) "3 (Moderate)" else "3 (Sedang)"
        "uv_footer" -> if (isEng) "Safe for outdoor activities" else "Aman beraktivitas luar"
        "uv_sub" -> if (isEng) "Wear solar protection" else "Gunakan tabir surya"
        "gemini_title" -> if (isEng) "GEMINI AI SMART INSIGHT" else "REKOMENDASI CERDAS GEMINI AI"
        "gemini_loading" -> if (isEng) "Reading wind patterns to generate best advice..." else "Membaca dinamika angin untuk memberikan rekomendasi terbaik..."
        "daily_label" -> if (isEng) "5-DAY FORECAST" else "PRAKIRAAN 5 HARI KE DEPAN"
        "today" -> if (isEng) "Today" else "Hari ini"
        "temp_unit_label" -> if (isEng) "Temperature Unit" else "Satuan Suhu"
        "wind_unit_label" -> if (isEng) "Wind Speed Unit" else "Satuan Kecepatan Angin"
        "language_label" -> if (isEng) "Language / Bahasa" else "Bahasa / Language"
        "offline_banner" -> if (isEng) "⚠️ Offline Mode - Displaying Last Cached Weather" else "⚠️ Mode Offline - Menampilkan Cuaca Disimpan Terakhir"
        "close_details" -> if (isEng) "Close Details" else "Tutup Detail"
        "midday" -> if (isEng) "Midday" else "Tengah Hari"
        "detail_dashboard" -> if (isEng) "Dashboard Details" else "Detail Dashboard"
        "health_temp" -> if (isEng) "HEALTH & TEMPERATURE" else "KESEHATAN & SUHU"
        "condition" -> if (isEng) "Condition" else "Kondisi"
        "humidity_rain_headline" -> if (isEng) "RAIN & DEW ANALYSIS" else "INFORMASI EMBUN & HUJAN"
        "rain" -> if (isEng) "Rainfall" else "Curah Hujan"
        "humidity_info_desc" -> if (isEng) {
            "Current air humidity is at %d%%. This indicates a normal humidity level typically experienced in coastal or tropical climates."
        } else {
            "Kelembaban udara saat ini berada pada angka %d%%. Ini menandakan kelembaban yang cukup tinggi yang umum dialami oleh wilayah beriklim tropis seperti Indonesia."
        }
        "temp_chart_headline" -> if (isEng) "12-HOUR TEMPERATURE GRAPH" else "CHART SUHU 12 JAM KE DEPAN"
        "wind_analysis_headline" -> if (isEng) "WIND SPEED ANALYSIS" else "ANALISIS KECEPATAN ANGIN"
        "wind_info_desc" -> if (isEng) {
            "The current wind speed is %s. This breeze shows normal, refreshing air circulation in %s area."
        } else {
            "Kecepatan angin saat ini berkisar di %s. Aliran angin ini membuktikan adanya pergerakan sirkulasi udara yang normal dan menyegarkan di wilayah %s."
        }
        "uv_index_headline" -> if (isEng) "UV INDEX CLASSIFICATION" else "KLASIFIKASI INDEKS UV"
        "uv_info_desc" -> if (isEng) {
            "The tropical UV radiation index is Moderate. It is safe for outdoor work, but using a hat or UV sunscreen is advised."
        } else {
            "Indeks radiasi UV tropis berkategori Sedang. Aman digunakan untuk melakukan berbagai jenis kegiatan outdoor namun direkomendasikan untuk tetap menggunakan topi lebar atau tabir surya UV-filter."
        }
        "gemini_advisory_headline" -> if (isEng) "GEMINI ADVISORY" else "GEMINI ADVISORY"
        "reading_air" -> if (isEng) "Analyzing air parameters..." else "Membaca dinamika udara..."
        "apparent_desc_fmt" -> if (isEng) {
            "Currently in %s it feels like %s because of the relative humidity of around %d%%."
        } else {
            "Saat ini di %s terasa seperti %s karena kelembaban udara yang berkisar di sekitar %d%%."
        }
        "gps_disabled" -> if (isEng) "GPS/Network location services are disabled. Please enable GPS." else "Layanan lokasi GPS/Jaringan dinonaktifkan. Silakan aktifkan GPS Anda."
        "location_error" -> if (isEng) "Failed to detect your device location." else "Gagal mendeteksi lokasi ponsel Anda."
        "location_permission_denied" -> if (isEng) "Location permission denied. Please grant location permissions in App Settings." else "Izin lokasi ditolak. Silakan berikan izin lokasi di pengaturan aplikasi."
        "lokasi_saya" -> if (isEng) "My Location" else "Lokasi Saya"
        "sunrise_sunset_label" -> if (isEng) "SUNRISE & SUNSET" else "MATAHARI TERBIT & TENGGELAM"
        "precipitation_label" -> if (isEng) "PRECIPITATION" else "PRESIPITASI"
        "precipitation_sub" -> if (isEng) "Water volume today" else "Volume air hari ini"
        "precipitation_footer" -> if (isEng) "Expected water levels" else "Perkiraan akumulasi air"
        "sunrise" -> if (isEng) "Sunrise" else "Terbit"
        "sunset" -> if (isEng) "Sunset" else "Tenggelam"
        "aqi_title" -> if (isEng) "AIR QUALITY INDEX (AQI)" else "INDEKS KUALITAS UDARA (IKU)"
        "aqi_good" -> if (isEng) "Good (0-50)" else "Sangat Baik (0-50)"
        "aqi_moderate" -> if (isEng) "Moderate (51-100)" else "Sedang (51-100)"
        "aqi_sensitive" -> if (isEng) "Unhealthy for Sensitive" else "Tidak Sehat bagi Sensitif (101-150)"
        "aqi_unhealthy" -> if (isEng) "Unhealthy (151-200)" else "Tidak Sehat (151-200)"
        "aqi_very_unhealthy" -> if (isEng) "Very Unhealthy (201-300)" else "Sangat Tidak Sehat (201-300)"
        "aqi_hazardous" -> if (isEng) "Hazardous (300+)" else "Berbahaya (300+)"
        "aqi_pm25" -> if (isEng) "PM2.5 (Fine Particles)" else "PM2.5 (Partikel Halus)"
        "aqi_pm10" -> if (isEng) "PM10 (Inhalable Coarse)" else "PM10 (Partikel Kasar)"
        "aqi_co" -> if (isEng) "Carbon Monoxide" else "Karbon Monoksida (CO)"
        "aqi_no2" -> if (isEng) "Nitrogen Dioxide" else "Nitrogen Dioksida (NO2)"
        "aqi_so2" -> if (isEng) "Sulphur Dioxide" else "Sulfur Dioksida (SO2)"
        "aqi_ozone" -> if (isEng) "Ozone (O3)" else "Ozon (O3)"
        "aqi_desc_title" -> if (isEng) "AIR ENVIRONMENTAL METRICS" else "METRIK LINGKUNGAN UDARA"
        "aqi_desc_health_effect" -> if (isEng) "Health effects description" else "Dampak bagi Kesehatan"
        "aqi_tap_details" -> if (isEng) "Tap to view environmental metrics" else "Ketuk untuk melihat rincian gas emisi & partikel"
        "no_data" -> if (isEng) "No data available" else "Data tidak tersedia"
        "refresh" -> if (isEng) "Refresh Weather" else "Perbarui Cuaca"
        "no_cities_found" -> if (isEng) "No cities found" else "Kota tidak ditemukan"
        else -> key
    }
}

fun formatTemperature(celsius: Double, unit: String): String {
    return if (unit == "F") {
        val f = celsius * 9 / 5 + 32
        "${f.toInt()}°F"
    } else {
        "${celsius.toInt()}°C"
    }
}

fun formatTemperatureRaw(celsius: Double, unit: String): Int {
    return if (unit == "F") {
        (celsius * 9 / 5 + 32).toInt()
    } else {
        celsius.toInt()
    }
}

fun formatWindSpeed(kmh: Double, unit: String): String {
    return when (unit) {
        "m/s" -> {
            val ms = kmh / 3.6
            String.format("%.1f m/s", ms)
        }
        "mph" -> {
            val mph = kmh * 0.621371
            String.format("%.1f mph", mph)
        }
        else -> {
            String.format("%.1f km/h", kmh)
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun WeatherDashboard(
    viewModel: WeatherViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val isAutoDark by viewModel.isAutoDark.collectAsState()
    val selectedCity by viewModel.selectedCity.collectAsState()
    val geminiAdvice by viewModel.geminiAdvice.collectAsState()
    val isGeminiLoading by viewModel.isGeminiLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val isCurrentBookmarked by viewModel.isCurrentCityBookmarked.collectAsState()
    val bookmarkedCities by viewModel.bookmarkedCitiesFlow.collectAsState()

    // Configured states for customization
    val tempUnit by viewModel.tempUnit.collectAsState()
    val windUnit by viewModel.windUnit.collectAsState()
    val language by viewModel.language.collectAsState()
    val isOfflineMode by viewModel.isOfflineMode.collectAsState()
    val airQuality by viewModel.airQuality.collectAsState()

    // Active expanded widget state (for simulated iOS zoom transitions)
    var expandedWidget by remember { mutableStateOf<String?>(null) }
    var showSearchOverlay by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            viewModel.detectLocationAndFetchWeather {}
        } else {
            android.widget.Toast.makeText(
                context,
                getTranslate("location_permission_denied", language),
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }

    // Weather code representation for background coloring
    val currentWeatherCode = when (val state = uiState) {
        is WeatherUiState.Success -> state.weather.current?.weatherCode ?: 0
        else -> 0
    }
    val currentIsDay = when (val state = uiState) {
        is WeatherUiState.Success -> (state.weather.current?.isDay == 1)
        else -> true
    }

    val swipeCities = remember { viewModel.popularCities.take(6) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                var dragAccumulator = 0f
                detectHorizontalDragGestures(
                    onDragCancel = { dragAccumulator = 0f },
                    onDragEnd = {
                        if (dragAccumulator > 50f) { // Swipe Right (Prev)
                            val currentIndex = swipeCities.indexOfFirst { it.name.lowercase() == selectedCity.name.lowercase() }
                            val newIndex = if (currentIndex <= 0) swipeCities.size - 1 else currentIndex - 1
                            viewModel.selectCity(swipeCities[newIndex])
                        } else if (dragAccumulator < -50f) { // Swipe Left (Next)
                            val currentIndex = swipeCities.indexOfFirst { it.name.lowercase() == selectedCity.name.lowercase() }
                            val newIndex = if (currentIndex == -1 || currentIndex == swipeCities.size - 1) 0 else currentIndex + 1
                            viewModel.selectCity(swipeCities[newIndex])
                        }
                        dragAccumulator = 0f
                    }
                ) { _, dragAmount -> 
                    dragAccumulator += dragAmount
                }
            }
    ) {
        // Drifting animated colorful glass background
        FluidBackground(
            weatherCode = currentWeatherCode,
            isDay = if (isDarkMode) false else currentIsDay
        )

        // Dark dim overlay if darkmode is active
        if (isDarkMode) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f))
            )
        }

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {},
                bottomBar = {
                    val currentIndex = swipeCities.indexOfFirst { it.name.lowercase() == selectedCity.name.lowercase() }
                    if (currentIndex != -1) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(swipeCities.size) { iteration ->
                                val color = if (currentIndex == iteration) 
                                    (if (isDarkMode) Color.White else Color.Black) 
                                else 
                                    (if (isDarkMode) Color.White.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.3f))
                                Box(
                                    modifier = Modifier
                                        .padding(2.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .size(if (currentIndex == iteration) 8.dp else 6.dp)
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // Main Content Switching
                        AnimatedContent(
                            targetState = uiState,
                            transitionSpec = {
                                (fadeIn(animationSpec = tween(600, easing = EaseOutQuart)) +
                                    scaleIn(initialScale = 0.95f, animationSpec = tween(600, easing = EaseOutQuart)))
                                    .togetherWith(
                                        fadeOut(animationSpec = tween(400, easing = EaseInQuart)) +
                                            scaleOut(targetScale = 1.05f, animationSpec = tween(400, easing = EaseInQuart))
                                    )
                            },
                            label = "MainContentTransition"
                        ) { state ->
                    when (state) {
                        is WeatherUiState.Loading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                GlassCard(
                                    isDark = isDarkMode,
                                    cornerRadius = 20.dp,
                                    glassColor = if (isDarkMode) Color.Black.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.4f)
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        CircularProgressIndicator(color = if (isDarkMode) Color.White else Color(0xFF0288D1))
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            getTranslate("syncing", language),
                                            color = if (isDarkMode) Color.White.copy(alpha = 0.8f) else Color(0xFF1E293B),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        is WeatherUiState.Success -> {
                            SuccessContent(
                                cityName = selectedCity.name,
                                adminName = selectedCity.admin1 ?: selectedCity.country ?: "Indonesia",
                                weather = state.weather,
                                isDark = isDarkMode,
                                geminiAdvice = geminiAdvice,
                                isGeminiLoading = isGeminiLoading,
                                onWidgetTap = { expandedWidget = it },
                                popularCities = viewModel.popularCities,
                                onQuickCityTap = { viewModel.selectCity(it) },
                                isBookmarked = isCurrentBookmarked,
                                onBookmarkToggle = { viewModel.toggleBookmarkCurrentCity() },
                                onRefresh = { viewModel.loadWeatherForCity(selectedCity) },
                                bookmarkedCities = bookmarkedCities,
                                tempUnit = tempUnit,
                                windUnit = windUnit,
                                language = language,
                                isOfflineMode = isOfflineMode,
                                airQuality = airQuality,
                                onDetectLocation = {
                                    val hasFine = androidx.core.content.ContextCompat.checkSelfPermission(
                                        context,
                                        android.Manifest.permission.ACCESS_FINE_LOCATION
                                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                    
                                    val hasCoarse = androidx.core.content.ContextCompat.checkSelfPermission(
                                        context,
                                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                    
                                    if (hasFine || hasCoarse) {
                                        viewModel.detectLocationAndFetchWeather {}
                                    } else {
                                        permissionLauncher.launch(
                                            arrayOf(
                                                android.Manifest.permission.ACCESS_FINE_LOCATION,
                                                android.Manifest.permission.ACCESS_COARSE_LOCATION
                                            )
                                        )
                                    }
                                },
                                onOpenSearchOverlay = { showSearchOverlay = true }
                            )
                        }

                        is WeatherUiState.Error -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                GlassCard(
                                    isDark = isDarkMode,
                                    cornerRadius = 24.dp
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CloudOff,
                                            contentDescription = "Error",
                                            tint = Color.Red,
                                            modifier = Modifier.size(64.dp)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            getTranslate("failed_load", language),
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDarkMode) Color.White else Color(0xFF1F2937)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            state.message,
                                            fontSize = 14.sp,
                                            color = if (isDarkMode) Color.White.copy(alpha = 0.7f) else Color(0xFF6B7280),
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(
                                            onClick = { viewModel.loadWeatherForCity(selectedCity) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isDarkMode) Color.White else Color(0xFF0288D1),
                                                contentColor = if (isDarkMode) Color.Black else Color.White
                                            )
                                        ) {
                                            Text(getTranslate("try_again", language))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Smoothly detailed expand modal overlays (Simulated widgets expand)
                expandedWidget?.let { widgetKey ->
                    when (uiState) {
                        is WeatherUiState.Success -> {
                            WidgetExpandedDialog(
                                widgetKey = widgetKey,
                                isDark = isDarkMode,
                                weather = (uiState as WeatherUiState.Success).weather,
                                cityName = selectedCity.name,
                                geminiAdvice = geminiAdvice,
                                onDismiss = { expandedWidget = null },
                                tempUnit = tempUnit,
                                windUnit = windUnit,
                                language = language,
                                airQuality = airQuality
                            )
                        }
                        else -> {}
                    }
                }
            }
        }

        // SLIDING GLASS SEARCH OVERLAY (Screen 2: Savable locations, Search & settings list overview matching iOS)
        AnimatedVisibility(
            visible = showSearchOverlay,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (isDarkMode) Color.Black.copy(alpha = 0.82f) else Color(0xFFE0F2FE).copy(alpha = 0.94f))
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Header layout inside search overlay
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (language == "EN") "Weather Locations" else "Lokasi Cuaca",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                        )
                        IconButton(
                            onClick = { showSearchOverlay = false },
                            modifier = Modifier
                                .background(if (isDarkMode) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.05f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = if (isDarkMode) Color.White else Color.Black
                             )
                        }
                    }

                    // WeatherTopBar for live searches/settings
                    WeatherTopBar(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                        searchResults = searchResults,
                        isSearching = isSearching,
                        onCitySelected = { city ->
                            viewModel.selectCity(city)
                            showSearchOverlay = false
                        },
                        isDarkMode = isDarkMode,
                        isAutoDark = isAutoDark,
                        onToggleAutoDark = { viewModel.setAutoDarkEnabled(it) },
                        onToggleManualDark = { viewModel.toggleDarkModeManual() },
                        tempUnit = tempUnit,
                        onTempUnitChange = { viewModel.setTempUnit(it) },
                        windUnit = windUnit,
                        onWindUnitChange = { viewModel.setWindUnit(it) },
                        language = language,
                        onLanguageChange = { viewModel.setLanguage(it) },
                        onDetectLocation = {
                            val hasFine = androidx.core.content.ContextCompat.checkSelfPermission(
                                context,
                                android.Manifest.permission.ACCESS_FINE_LOCATION
                            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                            
                            val hasCoarse = androidx.core.content.ContextCompat.checkSelfPermission(
                                context,
                                android.Manifest.permission.ACCESS_COARSE_LOCATION
                            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                            
                            if (hasFine || hasCoarse) {
                                viewModel.detectLocationAndFetchWeather {
                                    showSearchOverlay = false
                                }
                            } else {
                                permissionLauncher.launch(
                                    arrayOf(
                                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Saved Cities / Bookmark list mirroring iOS stack cards
                    Text(
                        text = if (language == "EN") "SAVED LOCATIONS" else "LOKASI TERSIMPAN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isDarkMode) Color.White.copy(alpha = 0.5f) else Color(0xFF475569),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val bookmarkedList = bookmarkedCities
                        if (bookmarkedList.isEmpty()) {
                            item {
                                GlassCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    isDark = isDarkMode,
                                    cornerRadius = 16.dp,
                                    glassColor = if (isDarkMode) Color.White.copy(alpha = 0.04f) else Color.White.copy(alpha = 0.5f)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.StarBorder,
                                            contentDescription = "Empty",
                                            tint = if (isDarkMode) Color.White.copy(alpha = 0.3f) else Color.Gray,
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = if (language == "EN") "Search above to save your favorite cities!" else "Cari di atas dan simpan kota favorit Anda!",
                                            fontSize = 13.sp,
                                            color = if (isDarkMode) Color.White.copy(alpha = 0.6f) else Color(0xFF64748B),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        } else {
                            items(bookmarkedList) { city ->
                                val active = city.name.lowercase() == selectedCity.name.lowercase()
                                GlassCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    isDark = isDarkMode,
                                    cornerRadius = 20.dp,
                                    glassColor = if (active) {
                                        Color(0xFF0284C7).copy(alpha = if (isDarkMode) 0.5f else 0.3f)
                                    } else {
                                        if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.6f)
                                    },
                                    onClick = {
                                        viewModel.selectCity(city)
                                        showSearchOverlay = false
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = city.name,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                                            )
                                            Text(
                                                text = city.admin1 ?: city.country ?: "Location",
                                                fontSize = 12.sp,
                                                color = if (isDarkMode) Color.White.copy(alpha = 0.6f) else Color(0xFF475569)
                                            )
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = "Pinned",
                                                tint = Color(0xFFF59E0B),
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clickable {
                                                        viewModel.selectCity(city)
                                                        viewModel.toggleBookmarkCurrentCity()
                                                    }
                                                    .padding(4.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete Bookmark",
                                                tint = Color.Red.copy(alpha = 0.8f),
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clickable {
                                                        viewModel.selectCity(city)
                                                        viewModel.toggleBookmarkCurrentCity()
                                                    }
                                                    .padding(4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Top Bar Components ---

@Composable
fun WeatherTopBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    searchResults: List<GeocodingResult>,
    isSearching: Boolean,
    onCitySelected: (GeocodingResult) -> Unit,
    isDarkMode: Boolean,
    isAutoDark: Boolean,
    onToggleAutoDark: (Boolean) -> Unit,
    onToggleManualDark: () -> Unit,
    tempUnit: String,
    onTempUnitChange: (String) -> Unit,
    windUnit: String,
    onWindUnitChange: (String) -> Unit,
    language: String,
    onLanguageChange: (String) -> Unit,
    onDetectLocation: () -> Unit
) {
    var showSettings by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Sleek Search Input
            GlassCard(
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp),
                isDark = isDarkMode,
                cornerRadius = 18.dp,
                glassColor = if (isDarkMode) Color.Black.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.45f),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                onClick = { focusRequester.requestFocus() }
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = if (isDarkMode) Color.White.copy(alpha = 0.60f) else Color(0xFF64748B),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = getTranslate("search_placeholder", language),
                                color = if (isDarkMode) Color.White.copy(alpha = 0.45f) else Color(0xFF94A3B8),
                                fontSize = 14.sp,
                                maxLines = 1
                            )
                        }
                        // Use basic text field styled beautifully
                        androidx.compose.foundation.text.BasicTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester)
                                .testTag("city_search_input"),
                            textStyle = LocalTextStyle.current.copy(
                                color = if (isDarkMode) Color.White else Color(0xFF1E293B),
                                fontSize = 15.sp
                            ),
                            singleLine = true
                        )
                    }
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { onSearchQueryChange("") },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("clear_search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear Search",
                                tint = if (isDarkMode) Color.White.copy(alpha = 0.6f) else Color(0xFF64748B),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // GPS Location Trigger Button
            GlassCard(
                modifier = Modifier.size(54.dp),
                isDark = isDarkMode,
                cornerRadius = 18.dp,
                glassColor = if (isDarkMode) Color.Black.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.45f),
                contentPadding = PaddingValues(0.dp),
                onClick = onDetectLocation,
                testTag = "location_button"
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Detect Location",
                        tint = if (isDarkMode) Color.White else Color(0xFF475569)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Settings Capsule Trigger
            GlassCard(
                modifier = Modifier.size(54.dp),
                isDark = isDarkMode,
                cornerRadius = 18.dp,
                glassColor = if (isDarkMode) Color.Black.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.45f),
                contentPadding = PaddingValues(0.dp),
                onClick = { showSettings = true },
                testTag = "settings_button"
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = if (isDarkMode) Color.White else Color(0xFF475569)
                    )
                }
            }
        }

        // Dropdown matching results
        AnimatedVisibility(
            visible = searchQuery.length >= 2,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .heightIn(max = 280.dp),
                isDark = isDarkMode,
                cornerRadius = 20.dp
            ) {
                if (isSearching) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                } else if (searchResults.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = getTranslate("no_cities_found", language),
                            color = if (isDarkMode) Color.White.copy(alpha = 0.6f) else Color(0xFF64748B),
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(searchResults) { city ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onCitySelected(city) }
                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = city.name,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDarkMode) Color.White else Color(0xFF1E293B)
                                    )
                                    Text(
                                        text = "${city.admin1 ?: ""}, ${city.country ?: ""}",
                                        fontSize = 12.sp,
                                        color = if (isDarkMode) Color.White.copy(alpha = 0.6f) else Color(0xFF64748B)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Go",
                                    tint = if (isDarkMode) Color.White.copy(alpha = 0.4f) else Color(0xFF94A3B8),
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            HorizontalDivider(color = if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f))
                        }
                    }
                }
            }
        }
    }

    // Modal Settings Overlay Dialog
    if (showSettings) {
        Dialog(onDismissRequest = { showSettings = false }) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                isDark = isDarkMode,
                cornerRadius = 28.dp,
                glassColor = if (isDarkMode) Color(0xFF1E293B).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.85f)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = getTranslate("display_settings", language),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkMode) Color.White else Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Language Selector Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = getTranslate("language_label", language),
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDarkMode) Color.White else Color(0xFF334155)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("ID", "EN").forEach { lang ->
                                val active = language == lang
                                GlassCard(
                                    modifier = Modifier.size(width = 54.dp, height = 36.dp),
                                    isDark = isDarkMode,
                                    cornerRadius = 10.dp,
                                    glassColor = if (active) {
                                        if (isDarkMode) Color.White.copy(alpha = 0.25f) else Color(0xFF0288D1).copy(alpha = 0.35f)
                                    } else {
                                        if (isDarkMode) Color.Black.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.20f)
                                    },
                                    contentPadding = PaddingValues(0.dp),
                                    onClick = { onLanguageChange(lang) },
                                    borderWidth = if (active) 1.5.dp else 1.dp
                                ) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text(
                                            text = lang,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDarkMode) Color.White else if (active) Color(0xFF1D4ED8) else Color(0xFF475569)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f))

                    // Temp Unit Selector Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = getTranslate("temp_unit_label", language),
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDarkMode) Color.White else Color(0xFF334155)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("C", "F").forEach { unit ->
                                val active = tempUnit == unit
                                val label = if (unit == "C") "°C" else "°F"
                                GlassCard(
                                    modifier = Modifier.size(width = 54.dp, height = 36.dp),
                                    isDark = isDarkMode,
                                    cornerRadius = 10.dp,
                                    glassColor = if (active) {
                                        if (isDarkMode) Color.White.copy(alpha = 0.25f) else Color(0xFF0288D1).copy(alpha = 0.35f)
                                    } else {
                                        if (isDarkMode) Color.Black.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.20f)
                                    },
                                    contentPadding = PaddingValues(0.dp),
                                    onClick = { onTempUnitChange(unit) },
                                    borderWidth = if (active) 1.5.dp else 1.dp
                                ) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text(
                                            text = label,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDarkMode) Color.White else if (active) Color(0xFF1D4ED8) else Color(0xFF475569)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f))

                    // Wind Unit Selector Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = getTranslate("wind_unit_label", language),
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDarkMode) Color.White else Color(0xFF334155)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("km/h", "m/s", "mph").forEach { unit ->
                                val active = windUnit == unit
                                GlassCard(
                                    modifier = Modifier.size(width = 58.dp, height = 36.dp),
                                    isDark = isDarkMode,
                                    cornerRadius = 10.dp,
                                    glassColor = if (active) {
                                        if (isDarkMode) Color.White.copy(alpha = 0.25f) else Color(0xFF0288D1).copy(alpha = 0.35f)
                                    } else {
                                        if (isDarkMode) Color.Black.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.20f)
                                    },
                                    contentPadding = PaddingValues(0.dp),
                                    onClick = { onWindUnitChange(unit) },
                                    borderWidth = if (active) 1.5.dp else 1.dp
                                ) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text(
                                            text = unit,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDarkMode) Color.White else if (active) Color(0xFF1D4ED8) else Color(0xFF475569)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f))

                    // Auto Mode Settings row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = getTranslate("auto_dark", language),
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDarkMode) Color.White else Color(0xFF334155)
                            )
                            Text(
                                text = getTranslate("auto_dark_sub", language),
                                fontSize = 11.sp,
                                color = if (isDarkMode) Color.White.copy(alpha = 0.6f) else Color(0xFF64748B)
                            )
                        }
                        Switch(
                            checked = isAutoDark,
                            onCheckedChange = { onToggleAutoDark(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF0288D1),
                                checkedTrackColor = Color(0xFF0288D1).copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.testTag("auto_dark_switch")
                        )
                    }

                    HorizontalDivider(color = if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f))

                    // Manual Theme Force Row (Only active if AutoDark is off)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = getTranslate("force_dark", language),
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDarkMode) Color.White else Color(0xFF334155),
                                style = if (isAutoDark) LocalTextStyle.current.copy(color = Color.Gray) else LocalTextStyle.current
                            )
                            Text(
                                text = getTranslate("force_dark_sub", language),
                                fontSize = 11.sp,
                                color = if (isDarkMode) Color.White.copy(alpha = 0.6f) else Color(0xFF64748B)
                            )
                        }
                        IconButton(
                            onClick = { onToggleManualDark() },
                            enabled = !isAutoDark,
                            modifier = Modifier.testTag("manual_dark_toggle")
                        ) {
                            Icon(
                                imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = "Manual theme switch",
                                tint = if (isDarkMode) Color(0xFFFCD34D) else Color(0xFFF59E0B)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showSettings = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDarkMode) Color.White else Color(0xFF1E293B),
                            contentColor = if (isDarkMode) Color.Black else Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(getTranslate("apply", language))
                    }
                }
            }
        }
    }
}

// --- Dashboard Layout ---

@Composable
fun WeatherRangeBar(
    minTemp: Double,
    maxTemp: Double,
    globalMin: Double,
    globalMax: Double,
    currentTemp: Double?,
    isToday: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val totalDelta = (globalMax - globalMin).coerceAtLeast(1.0)
        
        // Draw background track
        drawRoundRect(
            color = Color.White.copy(alpha = 0.15f),
            size = size,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(height / 2, height / 2)
        )
        
        // Calculate bounds
        val startX = ((minTemp - globalMin) / totalDelta * width).toFloat().coerceIn(0f, width)
        val endX = ((maxTemp - globalMin) / totalDelta * width).toFloat().coerceIn(0f, width)
        val activeX = currentTemp?.let { ((it - globalMin) / totalDelta * width).toFloat().coerceIn(0f, width) } ?: startX
        
        // Draw active temperature segment gradient
        val brush = Brush.horizontalGradient(
            colors = listOf(
                Color(0xFF38BDF8), // Cool cyan
                Color(0xFFFBBF24), // Mild yellow
                Color(0xFFF97316)  // Warm orange
            ),
            startX = startX,
            endX = endX
        )
        
        drawRoundRect(
            brush = brush,
            topLeft = Offset(startX, 0f),
            size = androidx.compose.ui.geometry.Size((endX - startX).coerceAtLeast(3f), height),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(height / 2, height / 2)
        )
        
        // Draw current temperature dot (white circle with subtle border) if looking at today
        if (isToday) {
            drawCircle(
                color = Color.White,
                radius = 3.dp.toPx(),
                center = Offset(activeX, height / 2)
            )
            drawCircle(
                color = Color.Black.copy(alpha = 0.25f),
                radius = 3.dp.toPx(),
                center = Offset(activeX, height / 2),
                style = Stroke(width = 1.dp.toPx())
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SuccessContent(
    cityName: String,
    adminName: String,
    weather: WeatherResponse,
    isDark: Boolean,
    geminiAdvice: String?,
    isGeminiLoading: Boolean,
    onWidgetTap: (String) -> Unit,
    popularCities: List<GeocodingResult>,
    onQuickCityTap: (GeocodingResult) -> Unit,
    isBookmarked: Boolean,
    onBookmarkToggle: () -> Unit,
    onRefresh: () -> Unit,
    bookmarkedCities: List<GeocodingResult>,
    tempUnit: String,
    windUnit: String,
    language: String,
    isOfflineMode: Boolean,
    airQuality: com.example.data.model.CurrentAirQuality?,
    onDetectLocation: () -> Unit,
    onOpenSearchOverlay: () -> Unit
) {
    val scrollState = rememberScrollState()
    val current = weather.current ?: return
    val windDirection = 135.0
    val uvIndex = if (current.isDay == 1) 5.0 else 0.0

    val starScale by animateFloatAsState(
        targetValue = if (isBookmarked) 1.25f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "StarScale"
    )

    var refreshRotationAngle by remember { mutableStateOf(0f) }
    val refreshRotationAnim by animateFloatAsState(
        targetValue = refreshRotationAngle,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "RefreshRotation"
    )

    val combinedQuickCities = remember(bookmarkedCities, popularCities) {
        (bookmarkedCities + popularCities).distinctBy { it.id }
    }

    val todayMax = weather.daily?.temperaturesMax?.firstOrNull() ?: current.temperature
    val todayMin = weather.daily?.temperaturesMin?.firstOrNull() ?: current.temperature

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
        ) {
            // Offline Cache Indicator Banner
            if (isOfflineMode) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    isDark = isDark,
                    cornerRadius = 14.dp,
                    glassColor = if (isDark) Color(0xFF78350F).copy(alpha = 0.35f) else Color(0xFFFEF3C7).copy(alpha = 0.45f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = "Offline Mode",
                            tint = if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = getTranslate("offline_banner", language),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFFFDE68A) else Color(0xFF92400E)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Authentic iOS Weather Centered Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = adminName.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White.copy(alpha = 0.65f) else Color(0xFF475569),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = cityName,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Normal,
                        color = if (isDark) Color.White else Color(0xFF0F172A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = if (isDark) Color.White.copy(alpha = 0.8f) else Color(0xFF475569),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    text = "${formatTemperatureRaw(current.temperature, tempUnit)}°",
                    fontSize = 96.sp,
                    fontWeight = FontWeight.Thin,
                    color = if (isDark) Color.White else Color(0xFF0F172A)
                )
                Text(
                    text = WeatherInfoMapper.getDescription(current.weatherCode, language),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDark) Color.White.copy(alpha = 0.85f) else Color(0xFF475569)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (language == "EN") {
                        "H:${formatTemperatureRaw(todayMax, tempUnit)}°  L:${formatTemperatureRaw(todayMin, tempUnit)}°"
                    } else {
                        "T:${formatTemperatureRaw(todayMax, tempUnit)}°  R:${formatTemperatureRaw(todayMin, tempUnit)}°"
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDark) Color.White.copy(alpha = 0.75f) else Color(0xFF64748B)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

        // --- Simulated iOS Grid Dashboard and Stacks ---

        // Widget 3 (4x2 / Full Width) - Hourly Forecast matching iOS 15+ design style
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            isDark = isDark,
            onClick = { onWidgetTap("hourly") },
            testTag = "widget_hourly_chart"
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                // Explanatory advisory text header
                Text(
                    text = geminiAdvice ?: (getTranslate("condition", language) + ": " + WeatherInfoMapper.getDescription(current.weatherCode, language) + ". " + (if (language == "EN") "Conditions will remain steady." else "Kondisi stabil akan berlanjut.")),
                    fontSize = 13.sp,
                    color = if (isDark) Color.White else Color(0xFF1E293B),
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                HorizontalDivider(color = if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f))

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    val times = weather.hourly?.time?.take(12) ?: emptyList()
                    val temps = weather.hourly?.temperatures?.take(12) ?: emptyList()
                    val codes = weather.hourly?.weatherCodes?.take(12) ?: emptyList()

                    times.forEachIndexed { idx, isoTime ->
                        val temp = temps.getOrNull(idx) ?: 0.0
                        val code = codes.getOrNull(idx) ?: 0
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Text(
                                text = if (idx == 0) (if (language == "EN") "Now" else "Skarang") else formatTime(isoTime),
                                fontSize = 11.sp,
                                fontWeight = if (idx == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (isDark) Color.White.copy(alpha = 0.75f) else Color(0xFF475569)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            AnimatedWeatherIcon(
                                weatherCode = code,
                                isDay = current.isDay == 1,
                                size = 28.dp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${formatTemperatureRaw(temp, tempUnit)}°",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color(0xFF1E293B)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Widget 7 (4x4) - 5-Day forecast widget equipped with horizontal range bars (iOS style!)
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            isDark = isDark,
            testTag = "widget_weekly_list"
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Forecast",
                        tint = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF475569),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (language == "EN") "5-DAY FORECAST" else "PRAKIRAAN 5 HARI",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White.copy(alpha = 0.60f) else Color(0xFF475569),
                        letterSpacing = 1.sp
                    )
                }

                val daily = weather.daily ?: return@GlassCard
                val days = daily.time.take(5)
                val codes = daily.weatherCodes.take(5)
                val maxTemps = daily.temperaturesMax.take(5)
                val minTemps = daily.temperaturesMin.take(5)

                val globalMin = minTemps.minOrNull() ?: 10.0
                val globalMax = maxTemps.maxOrNull() ?: 35.0

                days.forEachIndexed { i, isoDate ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Text showing Day Name
                        Text(
                            text = if (i == 0) getTranslate("today", language) else formatDayOfWeek(isoDate, language),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color(0xFF1E293B),
                            modifier = Modifier.width(76.dp)
                        )

                        // Weather icon representing condition
                        Box(modifier = Modifier.width(36.dp), contentAlignment = Alignment.Center) {
                            AnimatedWeatherIcon(
                                weatherCode = codes.getOrNull(i) ?: 0,
                                isDay = true,
                                size = 24.dp
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Day's Minimum Temperature text
                        Text(
                            text = "${formatTemperatureRaw(minTemps.getOrNull(i) ?: 0.0, tempUnit)}°",
                            fontSize = 14.sp,
                            color = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF94A3B8),
                            modifier = Modifier.width(32.dp),
                            textAlign = TextAlign.End
                        )

                        // The Custom iOS dynamic range progress bar!
                        WeatherRangeBar(
                            minTemp = minTemps.getOrNull(i) ?: 0.0,
                            maxTemp = maxTemps.getOrNull(i) ?: 0.0,
                            globalMin = globalMin,
                            globalMax = globalMax,
                            currentTemp = if (i == 0) current.temperature else null,
                            isToday = (i == 0),
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .padding(horizontal = 8.dp)
                        )

                        // Day's Maximum Temperature text
                        Text(
                            text = "${formatTemperatureRaw(maxTemps.getOrNull(i) ?: 0.0, tempUnit)}°",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color(0xFF0F172A),
                            modifier = Modifier.width(32.dp),
                            textAlign = TextAlign.End
                        )
                    }
                    if (i < days.size - 1) {
                        HorizontalDivider(color = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Grid Layer 1: Simulated iOS 2x2 Widgets
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Widget 1 (2x2) - Feels Like
            GlassCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                isDark = isDark,
                onClick = { onWidgetTap("current") },
                testTag = "widget_weather_current"
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Thermostat,
                            contentDescription = "Feels Like",
                            tint = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF475569),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = getTranslate("apparent_prefix", language).uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF475569)
                        )
                    }

                    Column(modifier = Modifier.padding(top = 10.dp)) {
                        Text(
                            text = "${formatTemperatureRaw(current.apparentTemperature, tempUnit)}°",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Normal,
                            color = if (isDark) Color.White else Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = if (language == "EN") "Similar to the actual temperature." else "Terasa dekat dengan suhu asli.",
                            fontSize = 12.sp,
                            color = if (isDark) Color.White else Color(0xFF1E293B),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Widget 2 (2x2) - Air Quality or Humidity
            GlassCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                isDark = isDark,
                onClick = { onWidgetTap("humidity") },
                testTag = "widget_humidity"
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = "Humidity",
                            tint = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF475569),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = getTranslate("humidity_label", language).uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF475569)
                        )
                    }

                    Column(modifier = Modifier.padding(top = 10.dp)) {
                        Text(
                            text = "${current.humidity.toInt()}%",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Normal,
                            color = if (isDark) Color.White else Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = getTranslate("humidity_footer", language),
                            fontSize = 12.sp,
                            color = if (isDark) Color.White else Color(0xFF1E293B),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Grid Layer 2: Wind and UV Index
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Widget 4 (2x2) - Wind
            GlassCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                isDark = isDark,
                onClick = { onWidgetTap("wind") },
                testTag = "widget_wind"
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Air,
                            contentDescription = "Wind",
                            tint = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF475569),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = getTranslate("wind_label", language).uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF475569)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        // Compass Drawing
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color.Black.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "N",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color.Black,
                                modifier = Modifier.align(Alignment.TopCenter).padding(top = 4.dp)
                            )
                            Text(
                                "S",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color.Black,
                                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp)
                            )
                            Text(
                                "W",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color.Black,
                                modifier = Modifier.align(Alignment.CenterStart).padding(start = 4.dp)
                            )
                            Text(
                                "E",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color.Black,
                                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 4.dp)
                            )
                            
                            val windString = formatWindSpeed(current.windSpeed, windUnit)
                            val parts = windString.split(" ")
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = parts.getOrNull(0) ?: "${current.windSpeed.toInt()}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color.White else Color(0xFF0F172A)
                                )
                                Text(
                                    text = parts.getOrNull(1) ?: "km/h",
                                    fontSize = 9.sp,
                                    color = if (isDark) Color.White else Color(0xFF1E293B)
                                )
                            }
                            
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val radius = size.width / 2f - 4.dp.toPx()
                                
                                // Tick marks
                                for (i in 0 until 72) {
                                    val angle = i * 5f
                                    val tickLength = if (i % 18 == 0) 6.dp.toPx() else 3.dp.toPx()
                                    rotate(angle, center) {
                                        drawLine(
                                            color = if (isDark) Color.White.copy(0.3f) else Color.Black.copy(0.2f),
                                            start = Offset(center.x, center.y - radius),
                                            end = Offset(center.x, center.y - radius + tickLength),
                                            strokeWidth = if (i % 18 == 0) 1.5.dp.toPx() else 1.dp.toPx()
                                        )
                                    }
                                }
                                
                                rotate(windDirection.toFloat(), center) {
                                    val top = Offset(center.x, center.y - radius + 2.dp.toPx())
                                    val bottom = Offset(center.x, center.y + radius - 2.dp.toPx())
                                    
                                    val needlePathTop = Path().apply {
                                        moveTo(top.x, top.y)
                                        lineTo(center.x - 4.dp.toPx(), center.y)
                                        lineTo(center.x + 4.dp.toPx(), center.y)
                                        close()
                                    }
                                    drawPath(needlePathTop, color = Color(0xFFEF4444))
                                    
                                    val needlePathBottom = Path().apply {
                                        moveTo(bottom.x, bottom.y)
                                        lineTo(center.x - 4.dp.toPx(), center.y)
                                        lineTo(center.x + 4.dp.toPx(), center.y)
                                        close()
                                    }
                                    drawPath(needlePathBottom, color = if (isDark) Color.White else Color.Black)
                                }
                            }
                        }
                    }
                }
            }

            // Widget 5 (2x2) - UV Index
            GlassCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                isDark = isDark,
                onClick = { onWidgetTap("index") },
                testTag = "widget_uv"
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.WbSunny,
                            contentDescription = "UV",
                            tint = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF475569),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = getTranslate("uv_label", language).uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF475569)
                        )
                    }

                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        Text(
                            text = "${uvIndex.toInt()}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Normal,
                            color = if (isDark) Color.White else Color(0xFF1E293B)
                        )
                        val uvStatus = when (uvIndex.toInt()) {
                            in 0..2 -> if (language == "EN") "Low" else "Rendah"
                            in 3..5 -> if (language == "EN") "Moderate" else "Sedang"
                            in 6..7 -> if (language == "EN") "High" else "Tinggi"
                            in 8..10 -> if (language == "EN") "Very High" else "Sangat Tinggi"
                            else -> if (language == "EN") "Extreme" else "Ekstrem"
                        }
                        Text(
                            text = uvStatus,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Color.White else Color(0xFF1E293B)
                        )
                        
                        Spacer(modifier = Modifier.height(14.dp))
                        
                        // Progress gradient bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0xFF10B981), // Low
                                                Color(0xFFEAB308), // Mod
                                                Color(0xFFF97316), // High
                                                Color(0xFFEF4444), // V High
                                                Color(0xFFA855F7)  // Extreme
                                            )
                                        )
                                    )
                            )
                            // Indicator dot
                            val uvFraction = (uvIndex / 11f).coerceIn(0.01, 0.99).toFloat()
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(uvFraction)
                                        .align(Alignment.CenterStart)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.CenterEnd)
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = getTranslate("uv_sub", language),
                            fontSize = 12.sp,
                            color = if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF64748B),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Grid Layer 3: Sunrise/Sunset and Precipitation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Widget 6 (2x2) - Sunrise/Sunset
            GlassCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                isDark = isDark,
                onClick = { onWidgetTap("sunrise") },
                testTag = "widget_sunrise_sunset"
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LightMode,
                            contentDescription = "Sunrise",
                            tint = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF475569),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = getTranslate("sunrise_sunset_label", language).uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF475569)
                        )
                    }

                    Column(modifier = Modifier.padding(top = 10.dp)) {
                        val daily = weather.daily
                        val sRise = daily?.sunrise?.firstOrNull()?.let { formatTime(it) } ?: "06:00"
                        val sSet = daily?.sunset?.firstOrNull()?.let { formatTime(it) } ?: "18:00"

                        Text(
                            text = sRise,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Normal,
                            color = if (isDark) Color.White else Color(0xFF1E293B)
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Sine wave sun tracker
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height
                                
                                val path = Path().apply {
                                    moveTo(0f, h)
                                    quadraticBezierTo(w / 2f, -h / 2f, w, h)
                                }
                                drawPath(
                                    path = path,
                                    color = if (isDark) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.1f),
                                    style = Stroke(width = 2.dp.toPx())
                                )
                                
                                val cal = Calendar.getInstance()
                                val hour = cal.get(Calendar.HOUR_OF_DAY) + cal.get(Calendar.MINUTE) / 60f
                                val dayFraction = ((hour - 6f) / 12f).coerceIn(0f, 1f)
                                
                                val x = w * dayFraction
                                val y = h - (h * 1.5f * (1f - Math.pow((dayFraction * 2 - 1).toDouble(), 2.0).toFloat()))
                                
                                drawCircle(
                                    color = Color.Yellow,
                                    radius = 4.dp.toPx(),
                                    center = Offset(x, y.coerceAtLeast(0f).coerceAtMost(h))
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "${getTranslate("sunset", language)}: $sSet",
                            fontSize = 12.sp,
                            color = if (isDark) Color.White else Color(0xFF1E293B)
                        )
                    }
                }
            }

            // Widget 7 (2x2) - Precipitation
            GlassCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                isDark = isDark,
                onClick = { onWidgetTap("precipitation") },
                testTag = "widget_precipitation"
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Umbrella,
                            contentDescription = "Rain",
                            tint = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF475569),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = getTranslate("precipitation_label", language).uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF475569)
                        )
                    }

                    Column(modifier = Modifier.padding(top = 10.dp)) {
                        Text(
                            text = "${current.rain} mm",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Normal,
                            color = if (isDark) Color.White else Color(0xFF1E293B)
                        )
                        Text(
                            text = "in last 24h",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Color.White else Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = getTranslate("precipitation_sub", language),
                            fontSize = 12.sp,
                            color = if (isDark) Color.White else Color(0xFF1E293B),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Widget 8 (4x2 / Full Width) - Air Quality Index (US AQI Scale Card)
        airQuality?.let { aq ->
            val (aqiDesc, aqiColor, _) = getAqiDetails(aq.usAqi.toInt(), language)

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                isDark = isDark,
                onClick = { onWidgetTap("aqi") },
                testTag = "widget_air_quality"
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = "AQI",
                                tint = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF475569),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = getTranslate("aqi_title", language).uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF475569)
                            )
                        }
                        Text(
                            text = getTranslate("aqi_tap_details", language),
                            fontSize = 11.sp,
                            color = if (isDark) Color.White.copy(alpha = 0.5f) else Color(0xFF94A3B8)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "${aq.usAqi.toInt()} - $aqiDesc",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF0F172A)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Beautiful colored horizontal progress scale bar (Classic iOS AQI bar style!)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color.White.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        val aqiFraction = (aq.usAqi / 300f).coerceIn(0f, 1f)
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(aqiFraction)
                                .clip(RoundedCornerShape(3.dp))
                                .background(aqiColor)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Widget 9 (4x2 / Full Width) - Gemini Nasihat (AI Gemini weather guidelines)
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            isDark = isDark,
            testTag = "widget_gemini"
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Android,
                        contentDescription = "Gemini AI",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = getTranslate("gemini_title", language),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981),
                        letterSpacing = 1.sp
                    )
                }

                if (isGeminiLoading) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFF10B981))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = getTranslate("gemini_loading", language),
                            fontSize = 13.sp,
                            color = if (isDark) Color.White.copy(alpha = 0.70f) else Color(0xFF475569)
                        )
                    }
                } else {
                    Text(
                        text = geminiAdvice ?: getTranslate("gemini_loading", language),
                        fontSize = 13.sp,
                        color = if (isDark) Color.White.copy(alpha = 0.85f) else Color(0xFFBDC3C7), // elegant off-white text to stand out nicely
                        lineHeight = 20.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(110.dp)) // Extra scrollable padders for floating bar capsule space padding
    }

    // PERSISTENT TRANSLUCENT FLOATING CAPSULE BAR (iOS Weather bottom-anchored control bar layout!)
    GlassCard(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 24.dp, vertical = 20.dp)
            .fillMaxWidth()
            .height(60.dp),
        isDark = isDark,
        cornerRadius = 30.dp,
        glassColor = if (isDark) Color.Black.copy(alpha = 0.40f) else Color.White.copy(alpha = 0.55f),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Button 1: GPS location tracking (left)
            IconButton(
                onClick = onDetectLocation,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Map, // Outlined travel map/spot indicator
                    contentDescription = "Detect Location",
                    tint = if (isDark) Color.White else Color(0xFF1D4ED8),
                    modifier = Modifier.size(24.dp)
                )
            }

            // Page indicators / indicators dots tracking saved locations (center)
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val indicatorsCount = combinedQuickCities.size.coerceIn(1, 5)
                repeat(indicatorsCount) { idx ->
                    val active = idx == 0
                    Box(
                        modifier = Modifier
                            .size(if (active) 8.dp else 6.dp)
                            .background(
                                color = if (active) {
                                    if (isDark) Color.White else Color(0xFF1D4ED8)
                                } else {
                                    if (isDark) Color.White.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.2f)
                                },
                                shape = CircleShape
                            )
                    )
                }
            }

            // Button 2: Saved places overview sliding sliding glass drawer toggle (right)
            IconButton(
                onClick = onOpenSearchOverlay,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.List, // Three horizontal bars
                    contentDescription = "Saved Locations",
                    tint = if (isDark) Color.White else Color(0xFF1D4ED8),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
}

// --- Smooth transition detail expanding views ---

@Composable
fun WidgetExpandedDialog(
    widgetKey: String,
    isDark: Boolean,
    weather: WeatherResponse,
    cityName: String,
    geminiAdvice: String?,
    onDismiss: () -> Unit,
    tempUnit: String,
    windUnit: String,
    language: String,
    airQuality: com.example.data.model.CurrentAirQuality?
) {
    Dialog(onDismissRequest = onDismiss) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            isDark = isDark,
            cornerRadius = 28.dp,
            glassColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.90f) else Color.White.copy(alpha = 0.90f),
            testTag = "widget_expanded_modal"
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = getTranslate("detail_dashboard", language),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White.copy(alpha = 0.5f) else Color(0xFF64748B)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = if (isDark) Color.White else Color(0xFF475569)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                when (widgetKey) {
                    "current" -> {
                        val curr = weather.current ?: return@GlassCard
                        Text(getTranslate("health_temp", language), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0288D1))
                        Spacer(modifier = Modifier.height(12.dp))
                        AnimatedWeatherIcon(weatherCode = curr.weatherCode, size = 84.dp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(formatTemperature(curr.temperature, tempUnit), fontSize = 52.sp, fontWeight = FontWeight.Thin, color = if (isDark) Color.White else Color(0xFF0F172A))
                        Text(
                            text = getTranslate("condition", language) + ": " + WeatherInfoMapper.getDescription(curr.weatherCode, language),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = String.format(getTranslate("apparent_desc_fmt", language), cityName, formatTemperature(curr.apparentTemperature, tempUnit), curr.humidity.toInt()),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            color = if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF475569)
                        )
                    }

                    "humidity" -> {
                        val curr = weather.current ?: return@GlassCard
                        Text(getTranslate("humidity_rain_headline", language), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0288D1))
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = "Rain", tint = Color(0xFF0288D1), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(getTranslate("rain", language) + ": ${curr.rain} mm", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color(0xFF0F172A))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = String.format(getTranslate("humidity_info_desc", language), curr.humidity.toInt()),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            color = if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF475569)
                        )
                    }

                    "hourly" -> {
                        Text(getTranslate("temp_chart_headline", language), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0288D1))
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        val times = weather.hourly?.time?.take(12) ?: emptyList()
                        val temps = weather.hourly?.temperatures?.take(12) ?: emptyList()

                        // Custom drawn full-size dynamic line-chart converted to preferred temperature unit
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .padding(horizontal = 8.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                if (temps.isEmpty()) return@Canvas
                                val width = size.width
                                val height = size.height
                                val tempsConverted = temps.map { formatTemperatureRaw(it, tempUnit).toDouble() }
                                val maxVal = (tempsConverted.maxOrNull() ?: 35.0).toFloat()
                                val minVal = (tempsConverted.minOrNull() ?: 20.0).toFloat()
                                val diff = if (maxVal == minVal) 1f else maxVal - minVal

                                val points = tempsConverted.mapIndexed { idx, temp ->
                                    val x = (idx / 11f) * width
                                    val ratio = (temp.toFloat() - minVal) / diff
                                    val y = height - (ratio * (height * 0.7f) + height * 0.15f)
                                    Offset(x, y)
                                }

                                // Draw chart connection line
                                val chartPath = Path().apply {
                                    points.firstOrNull()?.let { moveTo(it.x, it.y) }
                                    for (i in 1 until points.size) {
                                        lineTo(points[i].x, points[i].y)
                                    }
                                }
                                drawPath(
                                    chartPath,
                                    color = Color(0xFF0288D1),
                                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                                )

                                // Draw points
                                points.forEachIndexed { i, pt ->
                                    drawCircle(
                                        color = if (isDark) Color.White else Color(0xFF0F172A),
                                        radius = 4.dp.toPx(),
                                        center = pt
                                    )
                                    drawCircle(
                                        color = Color(0xFF0288D1),
                                        radius = 2.dp.toPx(),
                                        center = pt
                                    )
                                }
                            }
                        }
                        
                        // X-axis timestamps
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(formatTime(times.firstOrNull() ?: ""), fontSize = 11.sp, color = if (isDark) Color.White.copy(alpha = 0.5f) else Color(0xFF64748B))
                            Text(getTranslate("midday", language), fontSize = 11.sp, color = if (isDark) Color.White.copy(alpha = 0.5f) else Color(0xFF64748B))
                            Text(formatTime(times.lastOrNull() ?: ""), fontSize = 11.sp, color = if (isDark) Color.White.copy(alpha = 0.5f) else Color(0xFF64748B))
                        }
                    }

                    "wind" -> {
                        val curr = weather.current ?: return@GlassCard
                        Text(getTranslate("wind_analysis_headline", language), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0288D1))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(formatWindSpeed(curr.windSpeed, windUnit), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color(0xFF0F172A))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = String.format(getTranslate("wind_info_desc", language), formatWindSpeed(curr.windSpeed, windUnit), cityName),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            color = if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF475569)
                        )
                    }

                    "index" -> {
                        Text(getTranslate("uv_index_headline", language), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0288D1))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(getTranslate("uv_status", language), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = getTranslate("uv_info_desc", language),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            color = if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF475569)
                        )
                    }

                    "sunrise" -> {
                        val sunriseRaw = weather.daily?.sunrise?.firstOrNull() ?: ""
                        val sunsetRaw = weather.daily?.sunset?.firstOrNull() ?: ""
                        val sunriseFormatted = if (sunriseRaw.isNotEmpty()) formatTime(sunriseRaw) else "06:00"
                        val sunsetFormatted = if (sunsetRaw.isNotEmpty()) formatTime(sunsetRaw) else "18:00"

                        Text(getTranslate("sunrise_sunset_label", language), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0288D1))
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.WbSunny, contentDescription = "Sunrise", tint = Color(0xFFF59E0B), modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(getTranslate("sunrise", language), fontSize = 12.sp, color = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF64748B))
                                Text(sunriseFormatted, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color(0xFF0F172A))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.WbSunny, contentDescription = "Sunset", tint = Color(0xFFE11D48), modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(getTranslate("sunset", language), fontSize = 12.sp, color = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF64748B))
                                Text(sunsetFormatted, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color(0xFF0F172A))
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = if (language == "EN") {
                                "The sun's journey across the sky drives our day. This curve represents the dynamic solar trajectory calculated according to the latitude and longitude of $cityName."
                            } else {
                                "Perjalanan matahari menentukan ritme hari kita. Grafik melengkung ini menggambarkan lintasan matahari secara dinamis berdasarkan koordinat garis lintang dan bujur setempat untuk wilayah $cityName."
                            },
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            color = if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF475569)
                        )
                    }

                    "precipitation" -> {
                        val curr = weather.current ?: return@GlassCard
                        Text(getTranslate("precipitation_label", language), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0288D1))
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = "Precipitation", tint = Color(0xFF3B82F6), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("${curr.precipitation} mm", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color(0xFF0F172A))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (language == "EN") {
                                "Precipitation measures the total depth of water reaching the ground surface, including rain, drizzle, and dew elements in $cityName. The current recorded precipitation value is ${curr.precipitation} mm."
                            } else {
                                "Presipitasi mengukur total kedalaman air yang mencapai permukaan bumi, termasuk elemen hujan, gerimis, dan embun di wilayah $cityName. Angka presipitasi saat ini terdeteksi sebesar ${curr.precipitation} mm."
                            },
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            color = if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF475569)
                        )
                    }

                    "aqi" -> {
                        val aq = airQuality
                        if (aq == null) {
                            Text(getTranslate("no_data", language), fontSize = 14.sp, color = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF64748B))
                        } else {
                            val (levelText, levelColor, description) = getAqiDetails(aq.usAqi.toInt(), language)
                            Text(getTranslate("aqi_title", language), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0288D1))
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "${aq.usAqi}",
                                fontSize = 64.sp,
                                fontWeight = FontWeight.Black,
                                color = levelColor
                            )
                            
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(levelColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = levelText,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = levelColor
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = description,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                color = if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF475569),
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Text(
                                text = getTranslate("aqi_desc_title", language),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White.copy(alpha = 0.5f) else Color(0xFF64748B),
                                modifier = Modifier.align(Alignment.Start).padding(start = 12.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                PollutantRow(getTranslate("aqi_pm25", language), "${String.format("%.1f", aq.pm25)} \u00B5g/m\u00B3", isDark)
                                PollutantRow(getTranslate("aqi_pm10", language), "${String.format("%.1f", aq.pm10)} \u00B5g/m\u00B3", isDark)
                                PollutantRow(getTranslate("aqi_ozone", language), "${String.format("%.1f", aq.ozone)} \u00B5g/m\u00B3", isDark)
                                PollutantRow(getTranslate("aqi_co", language), "${String.format("%.0f", aq.carbonMonoxide)} \u00B5g/m\u00B3", isDark)
                                PollutantRow(getTranslate("aqi_no2", language), "${String.format("%.1f", aq.nitrogenDioxide)} \u00B5g/m\u00B3", isDark)
                                PollutantRow(getTranslate("aqi_so2", language), "${String.format("%.1f", aq.sulphurDioxide)} \u00B5g/m\u00B3", isDark)
                            }
                        }
                    }

                    else -> {
                        Text(getTranslate("gemini_advisory_headline", language), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF10B981))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = geminiAdvice ?: getTranslate("reading_air", language),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            color = if (isDark) Color.White else Color(0xFF1E293B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) Color.White else Color(0xFF0F172A),
                        contentColor = if (isDark) Color.Black else Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(getTranslate("close_details", language))
                }
            }
        }
    }
}

// --- Date and Time Helpers ---

fun formatTime(isoTime: String): String {
    return try {
        val parts = isoTime.split("T")
        if (parts.size == 2) {
            parts[1].substring(0, 5)
        } else {
            isoTime
        }
    } catch (e: Exception) {
        isoTime
    }
}

fun formatDayOfWeek(isoDate: String, language: String = "ID"): String {
    return try {
        val parts = isoDate.split("-")
        if (parts.size == 3) {
            val year = parts[0].toInt()
            val month = parts[1].toInt() - 1
            val day = parts[2].toInt()
            val cal = Calendar.getInstance()
            cal.set(year, month, day)
            val isEng = language == "EN"
            when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.SUNDAY -> if (isEng) "Sunday" else "Minggu"
                Calendar.MONDAY -> if (isEng) "Monday" else "Senin"
                Calendar.TUESDAY -> if (isEng) "Tuesday" else "Selasa"
                Calendar.WEDNESDAY -> if (isEng) "Wednesday" else "Rabu"
                Calendar.THURSDAY -> if (isEng) "Thursday" else "Kamis"
                Calendar.FRIDAY -> if (isEng) "Friday" else "Jumat"
                Calendar.SATURDAY -> if (isEng) "Saturday" else "Sabtu"
                else -> isoDate
            }
        } else {
            isoDate
        }
    } catch (e: Exception) {
        isoDate
    }
}

// --- Air Quality Helpers ---

fun getAqiDetails(aqi: Int, language: String): Triple<String, Color, String> {
    val isEng = language == "EN"
    return when {
        aqi <= 50 -> Triple(
            getTranslate("aqi_good", language),
            Color(0xFF10B981), // Emerald Green
            if (isEng) "Air quality is satisfying, and air pollution poses little or no risk." else "Kualitas udara sangat memuaskan, dan risiko polusi udara berada pada tingkat minimal."
        )
        aqi <= 100 -> Triple(
            getTranslate("aqi_moderate", language),
            Color(0xFFEAB308), // Dynamic Amber
            if (isEng) "Air quality is acceptable; however, some pollutants may pose moderate health concern." else "Kualitas udara dapat diterima; namun, beberapa polutan mungkin menimbulkan risiko kesehatan ringan."
        )
        aqi <= 150 -> Triple(
            getTranslate("aqi_sensitive", language),
            Color(0xFFF97316), // Vivid Orange
            if (isEng) "Members of sensitive groups may experience health effects." else "Kelompok sensitif dapat mulai merasakan dampak polusi terhadap saluran pernapasan."
        )
        aqi <= 200 -> Triple(
            getTranslate("aqi_unhealthy", language),
            Color(0xFFEF4444), // Crimson Red
            if (isEng) "Everyone may begin to experience health effects; sensitive groups more seriously." else "Seluruh kelompok populasi dapat mulai mengalami gejala kesehatan akibat paparan kondisi udara buruk."
        )
        aqi <= 300 -> Triple(
            getTranslate("aqi_very_unhealthy", language),
            Color(0xFFA855F7), // Gorgeous Purple
            if (isEng) "Health warnings of emergency conditions. The entire population is more likely to be affected." else "Peringatan kesehatan kondisi darurat. Seluruh lapisan masyarakat rentan terdampak polusi parah."
        )
        else -> Triple(
            getTranslate("aqi_hazardous", language),
            Color(0xFF7F1D1D), // Dark Maroon
            if (isEng) "Health alarm: everyone may experience more serious health effects." else "Kondisi berbahaya ekstrim: semua orang berisiko mengalami efek pernapasan yang serius dan fatal."
        )
    }
}

@Composable
fun PollutantRow(label: String, value: String, isDark: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.04f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = if (isDark) Color.White.copy(alpha = 0.8f) else Color(0xFF475569))
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color(0xFF0F172A))
    }
}

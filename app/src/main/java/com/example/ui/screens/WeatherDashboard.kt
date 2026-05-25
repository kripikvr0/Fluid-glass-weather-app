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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
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

    // Active expanded widget state (for simulated iOS zoom transitions)
    var expandedWidget by remember { mutableStateOf<String?>(null) }

    // Weather code representation for background coloring
    val currentWeatherCode = when (val state = uiState) {
        is WeatherUiState.Success -> state.weather.current?.weatherCode ?: 0
        else -> 0
    }
    val currentIsDay = when (val state = uiState) {
        is WeatherUiState.Success -> (state.weather.current?.isDay == 1)
        else -> true
    }

    Box(modifier = modifier.fillMaxSize()) {
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
            topBar = {
                WeatherTopBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                    searchResults = searchResults,
                    isSearching = isSearching,
                    onCitySelected = { viewModel.selectCity(it) },
                    isDarkMode = isDarkMode,
                    isAutoDark = isAutoDark,
                    onToggleAutoDark = { viewModel.setAutoDarkEnabled(it) },
                    onToggleManualDark = { viewModel.toggleDarkModeManual() }
                )
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
                                            "Menyelaraskan Cuaca...",
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
                                bookmarkedCities = bookmarkedCities
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
                                            "Oops! Gagal Memuat Data",
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
                                            Text("Coba Lagi")
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
                                onDismiss = { expandedWidget = null }
                            )
                        }
                        else -> {}
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
    onToggleManualDark: () -> Unit
) {
    var showSettings by remember { mutableStateOf(false) }

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
                onClick = {}
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
                                text = "Telusuri kota seluruh dunia...",
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
                                .testTag("city_search_input"),
                            textStyle = LocalTextStyle.current.copy(
                                color = if (isDarkMode) Color.White else Color(0xFF1E293B),
                                fontSize = 15.sp
                            ),
                            singleLine = true
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Settings Capsule Trigger
            GlassCard(
                modifier = Modifier.size(54.dp),
                isDark = isDarkMode,
                cornerRadius = 18.dp,
                glassColor = if (isDarkMode) Color.Black.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.45f),
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
            visible = searchQuery.isNotEmpty() && (searchResults.isNotEmpty() || isSearching),
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
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Pengaturan Tampilan",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkMode) Color.White else Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

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
                                text = "Gelap Otomatis (Auto)",
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDarkMode) Color.White else Color(0xFF334155)
                            )
                            Text(
                                text = "Menyesuaikan waktu malam lokal",
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
                                text = "Paksa Mode Gelap",
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDarkMode) Color.White else Color(0xFF334155),
                                style = if (isAutoDark) LocalTextStyle.current.copy(color = Color.Gray) else LocalTextStyle.current
                            )
                            Text(
                                text = "Matikan otomatis untuk mengatur manual",
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

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { showSettings = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDarkMode) Color.White else Color(0xFF1E293B),
                            contentColor = if (isDarkMode) Color.Black else Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Terapkan")
                    }
                }
            }
        }
    }
}

// --- Dashboard Layout ---

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
    bookmarkedCities: List<GeocodingResult>
) {
    val scrollState = rememberScrollState()
    val current = weather.current ?: return

    val combinedQuickCities = remember(bookmarkedCities, popularCities) {
        (bookmarkedCities + popularCities).distinctBy { it.id }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
    ) {
        // Core City Display
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = if (isDark) Color.White else Color(0xFF2563EB),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = cityName,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDark) Color.White else Color(0xFF0F172A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(
                    onClick = onBookmarkToggle,
                    modifier = Modifier.testTag("bookmark_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Pin/Bookmark",
                        tint = if (isBookmarked) Color(0xFFF59E0B) else (if (isDark) Color.White.copy(alpha = 0.45f) else Color.Black.copy(alpha = 0.35f)),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Text(
                text = adminName,
                fontSize = 14.sp,
                color = if (isDark) Color.White.copy(alpha = 0.65f) else Color(0xFF475569),
                fontWeight = FontWeight.Medium
            )
        }

        // Horizontal Quick-Cities Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            combinedQuickCities.forEach { city ->
                val active = city.name.lowercase() == cityName.lowercase()
                GlassCard(
                    isDark = isDark,
                    cornerRadius = 14.dp,
                    glassColor = if (active) {
                        if (isDark) Color.White.copy(alpha = 0.25f) else Color(0xFF3B82F6).copy(alpha = 0.35f)
                    } else {
                        if (isDark) Color.Black.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.20f)
                    },
                    borderWidth = if (active) 1.5.dp else 1.dp,
                    onClick = { onQuickCityTap(city) },
                    modifier = Modifier.height(38.dp)
                ) {
                    Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
                        Text(
                            text = city.name,
                            fontSize = 12.sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                            color = if (isDark) Color.White else if (active) Color(0xFF1D4ED8) else Color(0xFF475569),
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- Simulated iOS Grid Dashboard and Stacks ---

        // Grid Layer 1: Simulated iOS 2x2 Widgets
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Widget 1 (2x2) - Current Weather Core
            GlassCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                isDark = isDark,
                onClick = { onWidgetTap("current") },
                testTag = "widget_weather_current"
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SEKARANG",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isDark) Color.White.copy(alpha = 0.5f) else Color(0xFF475569)
                        )
                        AnimatedWeatherIcon(
                            weatherCode = current.weatherCode,
                            isDay = current.isDay == 1,
                            size = 32.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "${current.temperature.toInt()}°",
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Thin,
                        color = if (isDark) Color.White else Color(0xFF0F172A)
                    )

                    Column {
                        Text(
                            text = WeatherInfoMapper.getDescription(current.weatherCode),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color(0xFF1E293B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Sensasi ${current.apparentTemperature.toInt()}°C",
                            fontSize = 11.sp,
                            color = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF64748B)
                        )
                    }
                }
            }

            // Widget 2 (2x2) - Dynamic Humidity Progress Ring
            GlassCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                isDark = isDark,
                onClick = { onWidgetTap("humidity") },
                testTag = "widget_humidity"
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "KELEMBABAN",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isDark) Color.White.copy(alpha = 0.5f) else Color(0xFF475569),
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(76.dp)
                    ) {
                        val percentage = (current.humidity.toFloat() / 100f).coerceIn(0f, 1f)
                        val animPercentage by animateFloatAsState(percentage, tween(1500, easing = EaseInOutQuad))

                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Back ring
                            drawCircle(
                                color = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f),
                                style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                            )
                            // Front progress ring
                            drawArc(
                                color = Color(0xFF0288D1),
                                startAngle = -90f,
                                sweepAngle = animPercentage * 360f,
                                useCenter = false,
                                style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${current.humidity.toInt()}%",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color(0xFF1E293B)
                            )
                            Text(
                                text = "Luar",
                                fontSize = 9.sp,
                                color = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF64748B)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Titik Embun Nyaman",
                        fontSize = 11.sp,
                        color = if (isDark) Color.White.copy(alpha = 0.60f) else Color(0xFF64748B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Widget 3 (4x2 / Full Width) - Hourly Forecast with custom Mini Temperature Line Chart
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            isDark = isDark,
            onClick = { onWidgetTap("hourly") },
            testTag = "widget_hourly_chart"
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "PRAKIRAAN PER JAM (12 JAM KE DEPAN)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDark) Color.White.copy(alpha = 0.5f) else Color(0xFF475569),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
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
                                text = formatTime(isoTime),
                                fontSize = 11.sp,
                                color = if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF475569)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            AnimatedWeatherIcon(
                                weatherCode = code,
                                size = 28.dp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${temp.toInt()}°",
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

        // Grid Layer 2: Simulated iOS 2x2 Widgets
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Widget 4 (2x2) - Wind Compass
            GlassCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                isDark = isDark,
                onClick = { onWidgetTap("wind") },
                testTag = "widget_wind"
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "ANGIN",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isDark) Color.White.copy(alpha = 0.5f) else Color(0xFF475569)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Drawing moving wind needle
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val center = Offset(size.width / 2f, size.height / 2f)
                                drawCircle(
                                    color = if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.05f),
                                    style = Stroke(width = 2.dp.toPx())
                                )
                                // Compass needle aligned to 135 degrees (North West direction draft)
                                this.rotate(45f, center) {
                                    // Pointer arrow
                                    val top = Offset(center.x, size.height * 0.15f)
                                    val left = Offset(center.x - size.width * 0.15f, size.height * 0.70f)
                                    val right = Offset(center.x + size.width * 0.15f, size.height * 0.70f)
                                    val needlePath = Path().apply {
                                        moveTo(top.x, top.y)
                                        lineTo(left.x, left.y)
                                        lineTo(center.x, center.y + 4f)
                                        lineTo(right.x, right.y)
                                        close()
                                    }
                                    drawPath(needlePath, color = Color(0xFFE11D48))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "${current.windSpeed.toInt()}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color(0xFF0F172A)
                            )
                            Text(
                                text = "km/jam",
                                fontSize = 11.sp,
                                color = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF64748B)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Hembusan ke Barat",
                        fontSize = 11.sp,
                        color = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF64748B)
                    )
                }
            }

            // Widget 5 (2x2) - Apparent Index Indices
            GlassCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                isDark = isDark,
                onClick = { onWidgetTap("index") }
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "INDEKS SINAR UV",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isDark) Color.White.copy(alpha = 0.5f) else Color(0xFF475569)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "3 (Sedang)",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF0F172A)
                    )

                    Column {
                        Text(
                            text = "Aman beraktivitas luar",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isDark) Color.White.copy(alpha = 0.8f) else Color(0xFF475569)
                        )
                        Text(
                            text = "Gunakan tabir surya",
                            fontSize = 9.sp,
                            color = if (isDark) Color.White.copy(alpha = 0.55f) else Color(0xFF64748B)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Widget 6 (4x2 / Full Width) - Intelligent Gemini Personal Advisor Card
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            isDark = isDark,
            cornerRadius = 24.dp,
            glassColor = if (isDark) Color(0xFF1E1E38).copy(alpha = 0.45f) else Color(0xFFF0FDF4).copy(alpha = 0.50f),
            testTag = "widget_gemini_info"
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // AI Rotating star using transition loop
                        val transition = rememberInfiniteTransition(label = "StarRotation")
                        val starRot by transition.animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec = infiniteRepeatable(tween(14000, easing = LinearEasing))
                        )
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "AI Star",
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier
                                .size(18.dp)
                                .rotate(starRot)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "REKOMENDASI CERDAS GEMINI AI",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isDark) Color(0xFFFBBF24) else Color(0xFF047857)
                        )
                    }

                    if (isGeminiLoading) {
                        CircularProgressIndicator(
                            color = if (isDark) Color(0xFFFBBF24) else Color(0xFF059669),
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = geminiAdvice ?: "Membaca dinamika angin untuk memberikan rekomendasi terbaik...",
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = if (isDark) Color.White.copy(alpha = 0.90f) else Color(0xFF064E3B),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Widget 7 (4x4) - 7-Day Long range forecast widget
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            isDark = isDark,
            testTag = "widget_weekly_list"
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "PRAKIRAAN 5 HARI KE DEPAN",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDark) Color.White.copy(alpha = 0.5f) else Color(0xFF475569),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                val daily = weather.daily ?: return@GlassCard
                val days = daily.time.take(5)
                val codes = daily.weatherCodes.take(5)
                val maxTemps = daily.temperaturesMax.take(5)
                val minTemps = daily.temperaturesMin.take(5)

                days.forEachIndexed { i, isoDate ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (i == 0) "Hari ini" else formatDayOfWeek(isoDate),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color(0xFF1E293B),
                            modifier = Modifier.width(76.dp)
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            AnimatedWeatherIcon(
                                weatherCode = codes.getOrNull(i) ?: 0,
                                size = 26.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = WeatherInfoMapper.getDescription(codes.getOrNull(i) ?: 0),
                                fontSize = 12.sp,
                                color = if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF64748B),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.width(84.dp)
                        ) {
                            Text(
                                text = "${minTemps.getOrNull(i)?.toInt()}°",
                                fontSize = 14.sp,
                                color = if (isDark) Color.White.copy(alpha = 0.5f) else Color(0xFF94A3B8),
                                fontWeight = FontWeight.Normal
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "${maxTemps.getOrNull(i)?.toInt()}°",
                                fontSize = 14.sp,
                                color = if (isDark) Color.White else Color(0xFF0F172A),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    if (i < days.size - 1) {
                        HorizontalDivider(color = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f))
                    }
                }
            }
        }
        Spacer(modifier = Modifier.navigationBarsPadding())
        Spacer(modifier = Modifier.height(16.dp))
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
    onDismiss: () -> Unit
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
                        text = "Detail Dashboard",
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
                        Text("KESEHATAN & SUHU", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0288D1))
                        Spacer(modifier = Modifier.height(12.dp))
                        AnimatedWeatherIcon(weatherCode = curr.weatherCode, size = 84.dp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("${curr.temperature}°C", fontSize = 52.sp, fontWeight = FontWeight.Thin, color = if (isDark) Color.White else Color(0xFF0F172A))
                        Text(
                            "Kondisi: ${WeatherInfoMapper.getDescription(curr.weatherCode)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Saat ini di $cityName terasa seperti ${curr.apparentTemperature}°C karena kelembaban udara yang berkisar di sekitar ${curr.humidity}%.",
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            color = if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF475569)
                        )
                    }

                    "humidity" -> {
                        val curr = weather.current ?: return@GlassCard
                        Text("INFORMASI EMBUN & HUJAN", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0288D1))
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = "Rain", tint = Color(0xFF0288D1), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Curah Hujan: ${curr.rain} mm", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color(0xFF0F172A))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Kelembaban udara saat ini berada pada angka ${curr.humidity.toInt()}%. Ini menandakan kelembaban yang cukup tinggi yang umum dialami oleh wilayah beriklim tropis seperti Indonesia.",
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            color = if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF475569)
                        )
                    }

                    "hourly" -> {
                        Text("CHART SUHU 12 JAM KE DEPAN", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0288D1))
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        val times = weather.hourly?.time?.take(12) ?: emptyList()
                        val temps = weather.hourly?.temperatures?.take(12) ?: emptyList()

                        // Custom drawn full-size dynamic line-chart
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
                                val maxVal = (temps.maxOrNull() ?: 35.0).toFloat()
                                val minVal = (temps.minOrNull() ?: 20.0).toFloat()
                                val diff = if (maxVal == minVal) 1f else maxVal - minVal

                                val points = temps.mapIndexed { idx, temp ->
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
                            Text("Tengah Hari", fontSize = 11.sp, color = if (isDark) Color.White.copy(alpha = 0.5f) else Color(0xFF64748B))
                            Text(formatTime(times.lastOrNull() ?: ""), fontSize = 11.sp, color = if (isDark) Color.White.copy(alpha = 0.5f) else Color(0xFF64748B))
                        }
                    }

                    "wind" -> {
                        val curr = weather.current ?: return@GlassCard
                        Text("ANALISIS KECEPATAN ANGIN", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0288D1))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("${curr.windSpeed} km/jam", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color(0xFF0F172A))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Kecepatan angin saat ini berkisar di ${curr.windSpeed} km/jam. Aliran angin ini membuktikan adanya pergerakan sirkulasi udara yang normal dan menyegarkan di wilayah $cityName.",
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            color = if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF475569)
                        )
                    }

                    "index" -> {
                        Text("KLASIFIKASI INDEKS UV", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0288D1))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("3 - Sedang", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Indeks radiasi UV tropis berkategori Sedang. Aman digunakan untuk melakukan berbagai jenis kegiatan outdoor namun direkomendasikan untuk tetap menggunakan topi lebar atau tabir surya UV-filter.",
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            color = if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF475569)
                        )
                    }

                    else -> {
                        Text("GEMINI ADVISORY", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF10B981))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = geminiAdvice ?: "Membaca dinamika udara...",
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
                    Text("Tutup Detail")
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

fun formatDayOfWeek(isoDate: String): String {
    return try {
        val parts = isoDate.split("-")
        if (parts.size == 3) {
            val year = parts[0].toInt()
            val month = parts[1].toInt() - 1
            val day = parts[2].toInt()
            val cal = Calendar.getInstance()
            cal.set(year, month, day)
            when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.SUNDAY -> "Minggu"
                Calendar.MONDAY -> "Senin"
                Calendar.TUESDAY -> "Selasa"
                Calendar.WEDNESDAY -> "Rabu"
                Calendar.THURSDAY -> "Kamis"
                Calendar.FRIDAY -> "Jumat"
                Calendar.SATURDAY -> "Sabtu"
                else -> isoDate
            }
        } else {
            isoDate
        }
    } catch (e: Exception) {
        isoDate
    }
}

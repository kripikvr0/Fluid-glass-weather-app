package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.NetworkService
import com.example.data.model.CityWeather
import com.example.ui.WeatherUIState
import com.example.ui.WeatherViewModel
import com.example.ui.components.WidgetCustomizerDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherDashboard(
    viewModel: WeatherViewModel
) {
    val selectedCity by viewModel.selectedCity.collectAsState()
    val weatherState by viewModel.weatherState.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showCustomizer by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    // Glassmorphic Space Slate gradient background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A), // Slate 900
                        Color(0xFF1E293B), // Slate 800
                        Color(0xFF020617)  // Slate 950
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // Header Row: Logo, Settings Customizer toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFF0288D1), Color(0xFF2DD4BF)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("K", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "weatherK",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Widget settings customize button
                IconButton(
                    onClick = { showCustomizer = true },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0x1AFFFFFF))
                        .testTag("dashboard_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Kostumisasi Widget",
                        tint = Color.White
                    )
                }
            }

            // Search Bar Component
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("city_search_bar"),
                placeholder = { Text("Cari kota...", color = Color(0x99FFFFFF)) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Ikon Cari", tint = Color.White) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Bersihkan teks", tint = Color.White)
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    if (searchQuery.isNotBlank()) {
                        viewModel.getWeatherData(searchQuery.trim())
                        focusManager.clearFocus()
                    }
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF0288D1),
                    unfocusedBorderColor = Color(0x33FFFFFF),
                    focusedContainerColor = Color(0x1AFFFFFF),
                    unfocusedContainerColor = Color(0x0DFFFFFF),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp)
            )

            // Bookmark Quick Switch Row
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // If query is vacant, display default quick searches + bookmarked cities!
                val allFilterPills = (bookmarks + NetworkService.defaultCities).distinct()
                items(allFilterPills) { city ->
                    val isSelected = selectedCity.lowercase() == city.lowercase()
                    val isBookmarked = bookmarks.contains(city)
                    
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) Color(0xFF0288D1) else Color(0x11FFFFFF))
                            .clickable {
                                viewModel.getWeatherData(city)
                                focusManager.clearFocus()
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("city_pill_$city"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isBookmarked) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "Disimpan",
                                    tint = if (isSelected) Color.White else Color(0xFFEF4444),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = city,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Weather State renderer
            when (val state = weatherState) {
                is WeatherUIState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF38BDF8))
                    }
                }
                is WeatherUIState.Success -> {
                    WeatherSuccessView(
                        weather = state.weather,
                        isBookmarked = bookmarks.contains(state.weather.cityName),
                        onToggleBookmark = { viewModel.toggleBookmark(state.weather.cityName) }
                    )
                }
                is WeatherUIState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Default.CloudQueue, contentDescription = "Kesalahan", tint = Color.Red, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(state.message, color = Color.White)
                        }
                    }
                }
            }
        }

        // Settings dialog customizer launcher
        if (showCustomizer) {
            WidgetCustomizerDialog(
                viewModel = viewModel,
                onDismiss = { showCustomizer = false }
            )
        }
    }
}

@Composable
fun WeatherSuccessView(
    weather: CityWeather,
    isBookmarked: Boolean,
    onToggleBookmark: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 20.dp)
    ) {
        // Core Glass Card Header: City, Bookmark trigger, Emoji, and Temp
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0x13FFFFFF))
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = weather.cityName,
                                style = MaterialTheme.typography.headlineLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = onToggleBookmark, modifier = Modifier.size(24.dp)) {
                                Icon(
                                    imageVector = if (isBookmarked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Simpan Kota",
                                    tint = if (isBookmarked) Color(0xFFEF4444) else Color.White
                                )
                            }
                        }
                        Text(
                            text = weather.condition,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(text = weather.iconEmoji, fontSize = 54.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = String.format(Locale.getDefault(), "%.1f", weather.temp),
                        style = MaterialTheme.typography.displayLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 58.sp
                    )
                    Text(
                        text = "°C",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color(0xFF38BDF8),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // HOURLY FORECAST SCROLL CHIPS
        Text(
            text = "ALMANAK DUA BELAS JAM KE DEPAN",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF94A3B8),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(weather.hourlyForecast) { slot ->
                Box(
                    modifier = Modifier
                        .width(75.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x0DFFFFFF))
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(slot.time, color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(slot.iconEmoji, fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("${Math.round(slot.temp)}°", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // METRICS GLASS GRID CONTAINER
        Text(
            text = "METRIK KESEHATAN CUACA & UDARA",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF94A3B8),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard("Kualitas Udara", "AQI ${weather.aqi}", getAqiMessage(weather.aqi), Icons.Default.Air)
                MetricCard("Lembap Udara", "${weather.humidity}%", "Kelembaban Relatif", Icons.Default.WaterDrop)
                MetricCard("Kecepatan Angin", "${weather.windSpeed} km/h", "Hembusan Angin", Icons.Default.Air)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard("Partikulat PM2.5", "${weather.pm25} µg/m³", "Mikropartikel Debu", Icons.Default.Air)
                MetricCard("Indeks Sinar UV", "${weather.uvIndex}", getUvLevel(weather.uvIndex), Icons.Default.WbSunny)
                MetricCard("Curah Hujan", "${weather.rainfall} mm", "Sore/Malam Hari", Icons.Default.InvertColors)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // DAILY FORECAST CARD (3 DAYS)
        Text(
            text = "ALMANAK TIGA HARI KE DEPAN",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF94A3B8),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0x0DFFFFFF))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            weather.dailyForecast.forEach { day ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1.5f)) {
                        Text(day.dayName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(day.dateLabel, color = Color(0xFF94A3B8), fontSize = 11.sp)
                    }
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(day.iconEmoji, fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(day.condition, color = Color(0xFF64748B), fontSize = 11.sp)
                    }
                    Text(
                        text = "${Math.round(day.tempMin)}° / ${Math.round(day.tempMax)}°",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Right
                    )
                }
                Divider(color = Color(0x13FFFFFF))
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x0DFFFFFF))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, color = Color(0xFF94A3B8), fontSize = 11.sp)
                Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(subtitle, color = Color(0xFF64748B), fontSize = 10.sp)
        }
    }
}

fun getAqiMessage(aqi: Int): String {
    return when {
        aqi <= 50 -> "Sangat Baik 🟢"
        aqi <= 100 -> "Sedang 🟡"
        else -> "Kurang Sehat 🔴"
    }
}

fun getUvLevel(uv: Double): String {
    return when {
        uv <= 2.9 -> "Rendah 🟢"
        uv <= 5.9 -> "Sedang 🟡"
        uv <= 7.9 -> "Tinggi 🟠"
        else -> "Sangat Tinggi 🔴"
    }
}

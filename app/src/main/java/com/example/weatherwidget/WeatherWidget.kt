package com.example.weatherwidget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.compose.ui.graphics.Color
import androidx.glance.appwidget.cornerRadius
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class WeatherWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val weatherData = WeatherApi.getJakartaWeather()
        
        provideContent {
            GlanceTheme {
                WeatherWidgetContent(weatherData)
            }
        }
    }
}

@Composable
fun WeatherWidgetContent(weather: OpenMeteoResponse?) {
    val backgroundColor = ColorProvider(Color(0xFF1E293B))
    val textColorPrimary = ColorProvider(Color.White)
    val textColorSecondary = ColorProvider(Color.White.copy(alpha = 0.8f))

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(backgroundColor)
            .cornerRadius(24.dp)
            .padding(16.dp)
    ) {
        if (weather == null || weather.current == null || weather.daily == null) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Loading or Error...", style = TextStyle(color = textColorPrimary))
            }
            return@Box
        }

        val currentTemp = weather.current.temperature_2m.toInt().toString()
        val desc = WeatherUtils.getWeatherDescription(weather.current.weather_code)
        val highTemp = weather.daily.temperature_2m_max.firstOrNull()?.toInt()?.toString() ?: "--"
        val lowTemp = weather.daily.temperature_2m_min.firstOrNull()?.toInt()?.toString() ?: "--"

        Column {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = "Jakarta",
                        style = TextStyle(color = textColorPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "$currentTemp°",
                        style = TextStyle(color = textColorPrimary, fontSize = 56.sp, fontWeight = FontWeight.Normal)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = desc,
                        style = TextStyle(color = textColorPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    )
                    Text(
                        text = "H:$highTemp° L:$lowTemp°",
                        style = TextStyle(color = textColorSecondary, fontSize = 14.sp)
                    )
                }
            }

            Spacer(modifier = GlanceModifier.height(16.dp))
            
            // Divider
            Box(
                modifier = GlanceModifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.2f))
            ) {}

            Spacer(modifier = GlanceModifier.height(16.dp))

            // Hourly forecast row
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (weather.hourly != null) {
                    val now = LocalDateTime.now()
                    var startIndex = weather.hourly.time.indexOfFirst { 
                        try {
                            LocalDateTime.parse(it).isAfter(now)
                        } catch (e: Exception) { false }
                    }
                    if (startIndex == -1) startIndex = 0

                    val maxItems = 5
                    for (i in 0 until maxItems) {
                        val index = startIndex + i
                        if (index < weather.hourly.time.size) {
                            val timeStr = weather.hourly.time[index]
                            val tempStr = weather.hourly.temperature_2m[index].toInt().toString() + "°"
                            val code = weather.hourly.weather_code[index]
                            
                            val isSunset = false // Simplify for now
                            
                            val timeFormatted = try {
                                val dt = LocalDateTime.parse(timeStr)
                                dt.format(DateTimeFormatter.ofPattern("h a"))
                            } catch (e: Exception) { "Time" }
                            
                            HourlyItem(
                                time = timeFormatted, 
                                temp = tempStr, 
                                icon = WeatherUtils.getWeatherIcon(code, true),
                                isEvent = isSunset
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HourlyItem(time: String, temp: String, icon: String, isEvent: Boolean) {
    val textColorPrimary = ColorProvider(Color.White)
    
    Column(
        modifier = GlanceModifier.padding(horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = time, style = TextStyle(color = textColorPrimary, fontSize = 12.sp, fontWeight = if (isEvent) FontWeight.Bold else FontWeight.Normal))
        Spacer(modifier = GlanceModifier.height(8.dp))
        Text(text = icon, style = TextStyle(fontSize = 18.sp))
        Spacer(modifier = GlanceModifier.height(8.dp))
        Text(text = temp, style = TextStyle(color = textColorPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold))
    }
}

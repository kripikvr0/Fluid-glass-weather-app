package com.example.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WeatherAppWidgetProvider : AppWidgetProvider() {
    private val TAG = "WeatherAppWidget"

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        Log.d(TAG, "onUpdate triggered for widgets size: ${appWidgetIds.size}")
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == "com.example.ACTION_WIDGET_REFRESH") {
            Log.d(TAG, "Manual refresh intent action received")
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                android.content.ComponentName(context, WeatherAppWidgetProvider::class.java)
            )
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_weather_layout)
        val prefs = context.getSharedPreferences("weather_prefs", Context.MODE_PRIVATE)

        // Load cached weather or set defaults
        val cityName = prefs.getString("widget_cached_city", "DKI Jakarta") ?: "DKI Jakarta"
        val temp = prefs.getFloat("widget_cached_temp", 28.5f)
        val condition = prefs.getString("widget_cached_cond", "Cerah Berawan") ?: "Cerah Berawan"
        val emoji = prefs.getString("widget_cached_emoji", "⛅") ?: "⛅"
        val wind = prefs.getFloat("widget_cached_wind", 12.0f)
        val rain = prefs.getFloat("widget_cached_rain", 0.0f)
        val humidity = prefs.getInt("widget_cached_humidity", 72)
        val aqi = prefs.getInt("widget_cached_aqi", 42)
        val pm25 = prefs.getFloat("widget_cached_pm25", 14.5f)

        // Load widget customizer parameters
        val bgType = prefs.getString("widget_bg_type", "dark") ?: "dark"
        val accentHex = prefs.getString("widget_accent_hex", "#0288D1") ?: "#0288D1"
        val tag1Choice = prefs.getString("widget_tag1", "wind") ?: "wind"
        val tag2Choice = prefs.getString("widget_tag2", "humidity") ?: "humidity"
        val layoutMode = prefs.getString("widget_layout", "detailed") ?: "detailed"

        // 1. Theme and Background Color application
        val parsedAccentColor = try {
            Color.parseColor(accentHex)
        } catch (e: Exception) {
            Color.parseColor("#0288D1")
        }

        val (backgroundColor, titleTextColor, descTextColor, subTextColor) = when (bgType) {
            "light" -> {
                Quadruple(Color.parseColor("#F8FAFC"), Color.parseColor("#0F172A"), Color.parseColor("#334155"), Color.parseColor("#64748B"))
            }
            "transparent" -> {
                Quadruple(Color.parseColor("#CC1E293B"), Color.parseColor("#FFFFFF"), Color.parseColor("#93C5FD"), Color.parseColor("#40FFFFFF"))
            }
            "accent" -> {
                Quadruple(parsedAccentColor, Color.parseColor("#FFFFFF"), Color.parseColor("#E0F2FE"), Color.parseColor("#CDFFFFFF"))
            }
            else -> { // "dark" mode (Space Slate theme)
                Quadruple(Color.parseColor("#0F172A"), Color.parseColor("#FFFFFF"), Color.parseColor("#93C5FD"), Color.parseColor("#80FFFFFF"))
            }
        }

        // Apply background tinting dynamically!
        views.setInt(R.id.widget_root, "setBackgroundColor", backgroundColor)

        // Apply Text Colors
        views.setTextColor(R.id.widget_city, titleTextColor)
        views.setTextColor(R.id.widget_condition, descTextColor)
        views.setTextColor(R.id.widget_temp, titleTextColor)
        views.setTextColor(R.id.widget_refreshed_time, subTextColor)

        // 2. Data Population
        views.setTextViewText(R.id.widget_city, cityName)
        views.setTextViewText(R.id.widget_condition, condition)
        views.setTextViewText(R.id.widget_temp, String.format(Locale.getDefault(), "%.1f°C", temp))
        views.setTextViewText(R.id.widget_emoji, emoji)

        // 3. Customize Slot 1
        val tag1Text = getMetricText(tag1Choice, wind, rain, humidity, aqi, pm25)
        if (tag1Text != null && layoutMode == "detailed") {
            views.setViewVisibility(R.id.widget_tag1, View.VISIBLE)
            views.setTextViewText(R.id.widget_tag1, tag1Text)
        } else {
            views.setViewVisibility(R.id.widget_tag1, View.GONE)
        }

        // Customize Slot 2
        val tag2Text = getMetricText(tag2Choice, wind, rain, humidity, aqi, pm25)
        if (tag2Text != null && layoutMode == "detailed") {
            views.setViewVisibility(R.id.widget_tag2, View.VISIBLE)
            views.setTextViewText(R.id.widget_tag2, tag2Text)
        } else {
            views.setViewVisibility(R.id.widget_tag2, View.GONE)
        }

        // 4. Compact Layout controls
        if (layoutMode == "compact") {
            views.setViewVisibility(R.id.metrics_layout, View.GONE)
        } else {
            views.setViewVisibility(R.id.metrics_layout, View.VISIBLE)
        }

        // 5. Build dynamic last refreshed time
        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        views.setTextViewText(R.id.widget_refreshed_time, "Aktif $currentTime")

        // 6. Set PendingIntent to launch primary MainActivity on click
        val appIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            appWidgetId,
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

        // Instruct WidgetManager to update widgets
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun getMetricText(choice: String, wind: Float, rain: Float, humidity: Int, aqi: Int, pm25: Float): String? {
        return when (choice) {
            "wind" -> String.format(Locale.getDefault(), "💨 %.1f km/h", wind)
            "rain" -> String.format(Locale.getDefault(), "🌧️ %.1f mm", rain)
            "humidity" -> String.format(Locale.getDefault(), "💧 %d%% RH", humidity)
            "aqi" -> String.format(Locale.getDefault(), "🍃 AQI %d", aqi)
            "pm25" -> String.format(Locale.getDefault(), "😷 PM2.5 %.1f", pm25)
            else -> null
        }
    }

    // Helper data structure
    data class Quadruple<out A, out B, out C, out D>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D
    )
}

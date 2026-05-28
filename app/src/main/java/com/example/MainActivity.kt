package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.WeatherViewModel
import com.example.ui.screens.WeatherDashboard
import com.example.ui.theme.WeatherKTheme

class MainActivity : ComponentActivity() {
    
    private val viewModel: WeatherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Enable premium modern full-bleed edge-to-edge support
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        setContent {
            WeatherKTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.ui.graphics.Color(0xFF0F172A)
                ) {
                    WeatherDashboard(viewModel = viewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh weather data on resume to keep the app and widgets fully updated!
        val currentCity = viewModel.selectedCity.value
        viewModel.getWeatherData(currentCity)
    }
}

package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.screens.WeatherDashboard
import com.example.ui.WeatherViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val viewModel = androidx.lifecycle.viewmodel.compose.viewModel<WeatherViewModel>()
      val isDarkMode by viewModel.isDarkMode.collectAsState()

      MyApplicationTheme(darkTheme = isDarkMode) {
        WeatherDashboard(
          viewModel = viewModel,
          modifier = Modifier.fillMaxSize()
        )
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
  MyApplicationTheme { Text("Cuaca Glass Dashboard Preview") }
}

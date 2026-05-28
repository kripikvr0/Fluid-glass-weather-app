package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SettingsBackupRestore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.WeatherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetCustomizerDialog(
    viewModel: WeatherViewModel,
    onDismiss: () -> Unit
) {
    // Read states from view model
    val bgType by viewModel.widgetBgType.collectAsState()
    val accentHex by viewModel.widgetAccentHex.collectAsState()
    val tag1 by viewModel.widgetTag1.collectAsState()
    val tag2 by viewModel.widgetTag2.collectAsState()
    val layoutMode by viewModel.widgetLayout.collectAsState()
    val forecastMode by viewModel.widgetForecastMode.collectAsState()

    // Dialog local draft states
    var draftBgType by remember { mutableStateOf(bgType) }
    var draftAccentHex by remember { mutableStateOf(accentHex) }
    var draftTag1 by remember { mutableStateOf(tag1) }
    var draftTag2 by remember { mutableStateOf(tag2) }
    var draftLayoutMode by remember { mutableStateOf(layoutMode) }
    var draftForecastMode by remember { mutableStateOf(forecastMode) }

    val accentColors = listOf(
        Pair("Teal", "#10B981"),
        Pair("Purple", "#8B5CF6"),
        Pair("Orange", "#F97316"),
        Pair("Blue", "#0288D1")
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .testTag("widget_customizer_dialog_surface"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.background,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Kostumisasi Widget",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("widget_dialog_close")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup Dialog")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Customizer Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // LIVE WIDGET PREVIEW PANEL
                    Text(
                        text = "PREVIEW WIDGET",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Widget Box container reflecting changes live!
                    val previewBgColor = when (draftBgType) {
                        "light" -> Color(0xFFF8FAFC)
                        "transparent" -> Color(0x33FFFFFF)
                        "accent" -> Color(android.graphics.Color.parseColor(draftAccentHex))
                        else -> Color(0xFF0F172A)
                    }

                    val previewTxtColor = if (draftBgType == "light") Color(0xFF0F172A) else Color.White
                    val previewSubTxtColor = if (draftBgType == "light") Color(0xFF64748B) else Color(0x93C5FD)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(previewBgColor)
                            .padding(14.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("DKI Jakarta", color = previewTxtColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text("Cerah Berawan", color = previewSubTxtColor, fontSize = 11.sp)
                                }
                                Text("⛅", fontSize = 32.sp)
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Text("28.5°C", color = previewTxtColor, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                                
                                if (draftLayoutMode == "detailed") {
                                    Row {
                                        if (draftTag1 != "none") {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(0x26FFFFFF))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                val t1Label = when (draftTag1) {
                                                    "wind" -> "💨 12 km/h"
                                                    "rain" -> "🌧️ 0 mm"
                                                    "humidity" -> "💧 72% RH"
                                                    "aqi" -> "🍃 AQI 42"
                                                    else -> "😷 PM2.5 14.5"
                                                }
                                                Text(t1Label, color = previewTxtColor, fontSize = 9.sp)
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                        }
                                        if (draftTag2 != "none") {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(0x26FFFFFF))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                val t2Label = when (draftTag2) {
                                                    "wind" -> "💨 12 km/h"
                                                    "rain" -> "🌧️ 0 mm"
                                                    "humidity" -> "💧 72% RH"
                                                    "aqi" -> "🍃 AQI 42"
                                                    else -> "😷 PM2.5 14.5"
                                                }
                                                Text(t2Label, color = previewTxtColor, fontSize = 9.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // SECTION 1: LATAR BELAKANG (THEME CHOSEN)
                    Text(
                        text = "TEMA LATAR BELAKANG",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "dark" to "Gelap",
                            "light" to "Terang",
                            "transparent" to "Glassmorphic",
                            "accent" to "Aksen"
                        ).forEach { (mode, label) ->
                            val isSelected = draftBgType == mode
                            Button(
                                onClick = { draftBgType = mode },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("bg_option_$mode")
                            ) {
                                Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // SECTION 2: ACCENT COLOR CHOOSER
                    if (draftBgType == "accent") {
                        Text(
                            text = "PILIHAN WARNA AKSEN",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            accentColors.forEach { (name, hex) ->
                                val isSelected = draftAccentHex == hex
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color(android.graphics.Color.parseColor(hex)))
                                        .clickable { draftAccentHex = hex }
                                        .testTag("accent_color_$name"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clip(CircleShape)
                                                .background(Color.White)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // SECTION 3: TATA LETAK & UKURAN WIDGET
                    Text(
                        text = "TATA LETAK WIDGET",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "detailed" to "Detail Lengkap",
                            "compact" to "Minimalis"
                        ).forEach { (layout, label) ->
                            val isSelected = draftLayoutMode == layout
                            Button(
                                onClick = { draftLayoutMode = layout },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("layout_option_$layout")
                            ) {
                                Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // SECTION 4: METRIK SLOT CHOOSER
                    if (draftLayoutMode == "detailed") {
                        Text(
                            text = "DATA METRIK (SLOT UTAMA & KEDUA)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text("Pilih data Slot 1:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf(
                                "wind" to "💨 Angin",
                                "rain" to "🌧️ Hujan",
                                "humidity" to "💧 Lembap",
                                "aqi" to "🍃 AQI",
                                "none" to "Sembunyi"
                            ).forEach { (tagVal, label) ->
                                FilterChip(
                                    selected = draftTag1 == tagVal,
                                    onClick = { draftTag1 = tagVal },
                                    label = { Text(label, fontSize = 10.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text("Pilih data Slot 2:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf(
                                "wind" to "💨 Angin",
                                "rain" to "🌧️ Hujan",
                                "humidity" to "💧 Lembap",
                                "aqi" to "🍃 AQI",
                                "none" to "Sembunyi"
                            ).forEach { (tagVal, label) ->
                                FilterChip(
                                    selected = draftTag2 == tagVal,
                                    onClick = { draftTag2 = tagVal },
                                    label = { Text(label, fontSize = 10.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // SECTION 5: ALMANAK MODE WIDGET (forecast mode)
                    Text(
                        text = "MODE PERKIRAAN WIDGET ALMANAK",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "hourly" to "Per Jam (Hourly)",
                            "daily" to "Per Hari (Daily)"
                        ).forEach { (forecast, label) ->
                            val isSelected = draftForecastMode == forecast
                            Button(
                                onClick = { draftForecastMode = forecast },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("forecast_option_$forecast")
                            ) {
                                Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Actions Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("widget_dialog_cancel"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Batal")
                    }

                    Button(
                        onClick = {
                            viewModel.updateWidgetCustomization(
                                bgType = draftBgType,
                                accentHex = draftAccentHex,
                                tag1 = draftTag1,
                                tag2 = draftTag2,
                                layout = draftLayoutMode,
                                forecastMode = draftForecastMode
                            )
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("widget_dialog_apply"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Terapkan ke Widget")
                    }
                }
            }
        }
    }
}

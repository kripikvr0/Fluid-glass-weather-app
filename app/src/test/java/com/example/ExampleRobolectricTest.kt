package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.WeatherViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Cuaca Glass", appName)
  }

  @Test
  fun `verify WeatherViewModel initialization`() {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = WeatherViewModel(application)
    assertNotNull(viewModel)
    assertNotNull(viewModel.selectedCity.value)
  }
}

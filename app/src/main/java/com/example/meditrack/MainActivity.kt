package com.example.meditrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.meditrack.ui.theme.MediTrackTheme
import com.example.meditrack.viewmodel.MainViewModel
import com.example.meditrack.viewmodel.MainViewModelFactory

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels {
        MainViewModelFactory(application, (application as MediTrackApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val themeMode by mainViewModel.themeMode.collectAsState()
            val textSize by mainViewModel.textSize.collectAsState()
            val themeColor by mainViewModel.themeColor.collectAsState()

            val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
            val isTimeWise = themeMode == "time"

            val darkTheme = if (isTimeWise) {
                currentHour !in 5..17
            } else {
                when (themeMode) {
                    "light" -> false
                    "dark" -> true
                    else -> isSystemInDarkTheme()
                }
            }

            val activeColor = if (isTimeWise) {
                when (currentHour) {
                    in 5..11 -> "teal"
                    in 12..17 -> "orange"
                    else -> "purple"
                }
            } else {
                themeColor
            }

            MediTrackTheme(
                darkTheme = darkTheme,
                textSize = textSize,
                themeColor = activeColor
            ) {
                AppNavigation(viewModel = mainViewModel)
            }
        }
    }
}

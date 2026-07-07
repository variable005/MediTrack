package com.example.meditrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.meditrack.ui.theme.MediTrackTheme
import com.example.meditrack.viewmodel.MainViewModel
import com.example.meditrack.viewmodel.MainViewModelFactory

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels {
        MainViewModelFactory((application as MediTrackApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            MediTrackTheme {
                AppNavigation(viewModel = mainViewModel)
            }
        }
    }
}

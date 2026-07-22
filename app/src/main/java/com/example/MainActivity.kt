package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.presentation.MainViewModel
import com.example.presentation.navigation.AppNavGraph
import com.example.ui.theme.PlayConsoleDashboardTheme

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PlayConsoleDashboardTheme {
                AppNavGraph(viewModel = mainViewModel)
            }
        }
    }
}

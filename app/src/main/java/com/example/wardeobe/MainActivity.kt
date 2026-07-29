package com.example.wardeobe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation.compose.rememberNavController
import com.example.wardeobe.ui.navigation.AppNavGraph
import com.example.wardeobe.ui.theme.WardrobeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // Lift the theme state
            val systemTheme = isSystemInDarkTheme()
            var isDark by rememberSaveable { mutableStateOf(systemTheme) }

            val toggleTheme: (Boolean) -> Unit = { shouldBeDark ->
                isDark = shouldBeDark
            }

            WardrobeTheme(darkTheme = isDark) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()

                    AppNavGraph(
                        navController = navController,
                        isDarkTheme = isDark,
                        onThemeToggled = toggleTheme
                    )
                }
            }
        }
    }
}
package com.example.wardeobe

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@HiltAndroidApp
class MyApplication : Application() {
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()
        
        // Clean up orphaned camera capture files
        GlobalScope.launch(Dispatchers.IO) {
            try {
                cacheDir.listFiles { file -> 
                    file.name.startsWith("capture_") && file.name.endsWith(".jpg") 
                }?.forEach { it.delete() }
            } catch (e: Exception) {
                // Ignore cleanup errors
            }
        }
    }
}

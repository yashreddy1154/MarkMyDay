package com.project.markmyday

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

class MarkMyDayApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Apply saved dark mode on app start
        val prefs = getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("dark_mode", false)
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}

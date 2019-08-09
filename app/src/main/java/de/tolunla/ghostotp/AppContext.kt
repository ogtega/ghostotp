package de.tolunla.ghostotp

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class AppContext : Application() {

    override fun onCreate() {
        super.onCreate()

        AppCompatDelegate.setDefaultNightMode(
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        )
    }
}
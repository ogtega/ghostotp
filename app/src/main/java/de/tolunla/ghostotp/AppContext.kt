package de.tolunla.ghostotp

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager

class AppContext : Application() {

    override fun onCreate() {
        super.onCreate()

        PreferenceManager.getDefaultSharedPreferences(this).apply {
            setNightMode(this.getBoolean(getString(R.string.preference_night_mode_key), false))
        }
    }
}

fun setNightMode(nightMode: Boolean) {
    AppCompatDelegate.setDefaultNightMode(
        if (nightMode)
            AppCompatDelegate.MODE_NIGHT_YES
        else
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    )
}

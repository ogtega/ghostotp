package de.tolunla.ghostotp

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager

/**
 * Class used as the applications entry point
 */
class AppContext : Application() {

    override fun onCreate() {
        super.onCreate()
        setNightMode(this)
    }
}

/**
 * Sets the application's preference for day/night mode
 */
fun setNightMode(context: Context) {
    PreferenceManager.getDefaultSharedPreferences(context).apply {
        AppCompatDelegate.setDefaultNightMode(
            if (this.getBoolean(context.getString(R.string.preference_night_mode_key), false)) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        )
    }
}

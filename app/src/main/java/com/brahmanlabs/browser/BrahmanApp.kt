package com.brahmanlabs.browser
import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
class BrahmanApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }
}

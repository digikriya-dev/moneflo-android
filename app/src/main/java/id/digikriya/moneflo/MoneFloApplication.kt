package id.digikriya.moneflo

import android.app.Application
import id.digikriya.moneflo.helper.ThemePrefs

class MoneFloApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ThemePrefs.applySavedTheme(this)
    }
}

package nu.brandrisk.kioskmode.domain.launcher

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import nu.brandrisk.kioskmode.data.model.LauncherSettings
import nu.brandrisk.kioskmode.data.model.IconSize
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LauncherSettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "launcher_settings_prefs", 
        Context.MODE_PRIVATE
    )
    
    fun saveLauncherSettings(settings: LauncherSettings) {
        prefs.edit()
            .putBoolean("show_app_labels", settings.showAppLabels)
            .putString("icon_size", settings.iconSize.name)
            .putInt("grid_columns", settings.gridColumns)
            .apply()
    }
    
    fun getLauncherSettings(): LauncherSettings {
        return LauncherSettings(
            showAppLabels = prefs.getBoolean("show_app_labels", true),
            iconSize = IconSize.valueOf(
                prefs.getString("icon_size", IconSize.MEDIUM.name) ?: IconSize.MEDIUM.name
            ),
            gridColumns = prefs.getInt("grid_columns", 4)
        )
    }
}

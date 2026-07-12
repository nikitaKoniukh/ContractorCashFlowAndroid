package com.yetzira.ContractorCashFlowAndroid.locale

import android.content.Context
import com.yetzira.ContractorCashFlowAndroid.data.preferences.ThemeModeOption

/**
 * Fast SharedPreferences mirror of theme mode so MainActivity can apply
 * night mode without blocking on DataStore.
 */
object ThemeHelper {
    private const val PREFS_NAME = "theme_prefs"
    private const val KEY_THEME_MODE = "theme_mode"

    fun getSavedThemeMode(context: Context): ThemeModeOption {
        val code = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_THEME_MODE, ThemeModeOption.SYSTEM.code)
        return ThemeModeOption.fromCode(code)
    }

    fun saveThemeMode(context: Context, themeMode: ThemeModeOption) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME_MODE, themeMode.code)
            .apply()
    }
}

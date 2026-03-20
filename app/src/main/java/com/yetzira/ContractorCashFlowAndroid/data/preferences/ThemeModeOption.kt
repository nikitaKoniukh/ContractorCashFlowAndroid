package com.yetzira.ContractorCashFlowAndroid.data.preferences

import androidx.appcompat.app.AppCompatDelegate

enum class ThemeModeOption(
    val code: String,
    val nightModeValue: Int
) {
    SYSTEM("system", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM),
    LIGHT("light", AppCompatDelegate.MODE_NIGHT_NO),
    DARK("dark", AppCompatDelegate.MODE_NIGHT_YES);

    companion object {
        fun fromCode(code: String?): ThemeModeOption =
            entries.find { it.code == code } ?: SYSTEM
    }
}


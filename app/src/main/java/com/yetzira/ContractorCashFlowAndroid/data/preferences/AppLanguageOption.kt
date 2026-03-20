package com.yetzira.ContractorCashFlowAndroid.data.preferences

enum class AppLanguageOption(
    val code: String,
    val displayName: String
) {
    ENGLISH("en", "English"),
    HEBREW("he", "עברית"),
    RUSSIAN("ru", "Русский");

    companion object {
        fun fromCode(code: String?): AppLanguageOption = values().find { it.code == code } ?: HEBREW

        fun fromDisplayName(displayName: String?): AppLanguageOption? =
            values().find { it.displayName == displayName }
    }
}


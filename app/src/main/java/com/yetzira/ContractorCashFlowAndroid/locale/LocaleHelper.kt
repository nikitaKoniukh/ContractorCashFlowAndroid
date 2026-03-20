package com.yetzira.ContractorCashFlowAndroid.locale

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.util.Log
import java.util.Locale

/**
 * Fast, synchronous locale storage backed by SharedPreferences.
 *
 * DataStore is async and cannot be read before super.attachBaseContext(),
 * so we keep a separate SharedPreferences copy that is written with commit()
 * (synchronous) whenever the user changes language.
 *
 * attachBaseContext in MainActivity reads from here to apply the locale
 * before any resource is touched.
 */
object LocaleHelper {

    private const val PREFS_NAME = "locale_prefs"
    private const val KEY_LANG = "lang_code"
    private const val DEFAULT_LANG = "he"

    /** Persist the selected language code synchronously. */
    @SuppressLint("ApplySharedPref")
    fun saveLanguage(context: Context, code: String) {
        context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANG, code)
            .commit()   // synchronous – must be committed before recreate()
        Log.d("KablanProLocale", "Saved locale code=$code")
    }

    /** Read the currently saved language code. */
    fun getSavedLanguage(context: Context): String =
        context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LANG, DEFAULT_LANG) ?: DEFAULT_LANG

    /**
     * Wrap [base] context with the saved locale configuration.
     * Call from Activity.attachBaseContext().
     */
    fun applyLocale(base: Context): Context {
        val code = getSavedLanguage(base)
        // Hebrew is stored as "he" (BCP 47) but Android's resource system uses the
        // old ISO 639 code "iw" internally. Locale("he").language == "iw".
        // We create the Locale using the old code explicitly to guarantee that
        // Android's AAPT resource matcher looks for values-iw/ (which exists).
        val locale = when (code) {
            "he" -> Locale("iw")   // Hebrew: new BCP47 "he" → old ISO "iw"
            "id" -> Locale("in")   // Indonesian: new "id" → old "in"
            else -> Locale.forLanguageTag(code)
        }
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        Log.d("KablanProLocale", "Applying locale code=$code internal=${locale.language} tag=${locale.toLanguageTag()}")
        return base.createConfigurationContext(config)
    }
}


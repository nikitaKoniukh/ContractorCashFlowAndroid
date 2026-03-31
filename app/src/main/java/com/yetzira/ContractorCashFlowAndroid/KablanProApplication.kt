package com.yetzira.ContractorCashFlowAndroid

import android.annotation.SuppressLint
import android.app.Application
import android.app.LocaleManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

class KablanProApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        ensureFirstLaunchDefaults()
        createNotificationChannels()
    }

    /**
     * Detects a true first launch (no init flag in a dedicated SharedPreferences)
     * and forces Hebrew + ILS as defaults.
     *
     * Why a dedicated "app_init" prefs file?
     *   • android:allowBackup="false" → nothing is restored on reinstall
     *   • So the "initialized" key only exists if THIS install has already run
     *
     * On Android 13+, the system per-app locale PERSISTS across uninstall/reinstall
     * at the OS level. If the user previously chose English, the OS still returns "en".
     * We MUST explicitly reset it to Hebrew on first launch.
     */
    @SuppressLint("ApplySharedPref")
    private fun ensureFirstLaunchDefaults() {
        val initPrefs = getSharedPreferences("app_init", MODE_PRIVATE)
        if (initPrefs.getBoolean("initialized", false)) {
            // Not a first launch — user's existing preferences are valid.
            return
        }

        Log.d(TAG, "First launch detected — writing Hebrew + ILS defaults")

        // 1. Write Hebrew to the fast SharedPreferences that attachBaseContext reads.
        getSharedPreferences("locale_prefs", MODE_PRIVATE)
            .edit()
            .putString("lang_code", "he")
            .commit()

        // 2. Seed the DataStore preferences file with Hebrew + ILS.
        val dsFile = java.io.File(filesDir, "datastore/kablan_pro_preferences.preferences_pb")
        if (!dsFile.exists()) {
            kotlinx.coroutines.runBlocking {
                val repo = com.yetzira.ContractorCashFlowAndroid.data.preferences
                    .UserPreferencesRepository(this@KablanProApplication)
                repo.setAppLanguage(
                    com.yetzira.ContractorCashFlowAndroid.data.preferences.AppLanguageOption.HEBREW
                )
                repo.setSelectedCurrency(
                    com.yetzira.ContractorCashFlowAndroid.data.preferences.CurrencyOption.ILS
                )
            }
        }

        // 3. On Android 13+, FORCE the system per-app locale to Hebrew.
        //    This overrides any stale "en" the OS remembers from a previous install.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                val localeManager = getSystemService(LocaleManager::class.java)
                localeManager.applicationLocales = LocaleList.forLanguageTags("he")
                Log.d(TAG, "System per-app locale set to 'he' via LocaleManager")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to set system per-app locale", e)
            }
        }
        // On Android < 13, AppCompat handles it; set via the compat API.
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("he"))

        // 4. Mark initialization as done.
        initPrefs.edit().putBoolean("initialized", true).commit()
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val invoiceReminders = NotificationChannel(
            CHANNEL_INVOICE_REMINDERS,
            "Invoice Reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Reminders for upcoming invoice due dates"
        }

        val invoiceOverdue = NotificationChannel(
            CHANNEL_INVOICE_OVERDUE,
            "Overdue Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alerts for overdue invoices"
        }

        val budgetWarnings = NotificationChannel(
            CHANNEL_BUDGET_WARNINGS,
            "Budget Warnings",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Warnings when project budget reaches 80% or 100%"
        }

        manager.createNotificationChannels(
            listOf(invoiceReminders, invoiceOverdue, budgetWarnings)
        )
    }

    companion object {
        private const val TAG = "KablanProApp"
        const val CHANNEL_INVOICE_REMINDERS = "kablanpro_invoice_notifications"
        const val CHANNEL_INVOICE_OVERDUE = "kablanpro_invoice_overdue"
        const val CHANNEL_BUDGET_WARNINGS = "kablanpro_budget_warnings"
    }
}

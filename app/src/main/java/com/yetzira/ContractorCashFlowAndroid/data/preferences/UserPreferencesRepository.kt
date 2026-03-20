package com.yetzira.ContractorCashFlowAndroid.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val PREFERENCES_NAME = "kablan_pro_preferences"

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PREFERENCES_NAME)

class UserPreferencesRepository(context: Context) {
    private val dataStore = context.dataStore

    // Preference Keys
    private companion object {
        val APP_LANGUAGE = stringPreferencesKey("app_language")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val SELECTED_CURRENCY_CODE = stringPreferencesKey("selected_currency_code")
        val INVOICE_REMINDERS_ENABLED = booleanPreferencesKey("invoice_reminders_enabled")
        val OVERDUE_ALERTS_ENABLED = booleanPreferencesKey("overdue_alerts_enabled")
        val BUDGET_WARNINGS_ENABLED = booleanPreferencesKey("budget_warnings_enabled")
        val SUBSCRIPTION_IS_PRO = booleanPreferencesKey("subscription_is_pro")
        val SUBSCRIPTION_PLAN_NAME = stringPreferencesKey("subscription_plan_name")
        val SUBSCRIPTION_RENEWAL_DATE = longPreferencesKey("subscription_renewal_date")
    }

    // Flow getters
    val appLanguage: Flow<AppLanguageOption> = dataStore.data.map { preferences ->
        val code = preferences[APP_LANGUAGE] ?: "he"
        AppLanguageOption.fromCode(code)
    }

    val themeMode: Flow<ThemeModeOption> = dataStore.data.map { preferences ->
        val code = preferences[THEME_MODE] ?: ThemeModeOption.SYSTEM.code
        ThemeModeOption.fromCode(code)
    }

    val selectedCurrencyCode: Flow<CurrencyOption> = dataStore.data.map { preferences ->
        val code = preferences[SELECTED_CURRENCY_CODE] ?: "ILS"
        CurrencyOption.fromCode(code)
    }

    val invoiceRemindersEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[INVOICE_REMINDERS_ENABLED] ?: true
    }

    val overdueAlertsEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[OVERDUE_ALERTS_ENABLED] ?: true
    }

    val budgetWarningsEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[BUDGET_WARNINGS_ENABLED] ?: true
    }

    val subscriptionIsPro: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[SUBSCRIPTION_IS_PRO] ?: false
    }

    val subscriptionPlanName: Flow<String?> = dataStore.data.map { preferences ->
        preferences[SUBSCRIPTION_PLAN_NAME]
    }

    val subscriptionRenewalDate: Flow<Long?> = dataStore.data.map { preferences ->
        preferences[SUBSCRIPTION_RENEWAL_DATE]
    }

    // Suspend setters
    suspend fun setAppLanguage(language: AppLanguageOption) {
        dataStore.edit { preferences ->
            preferences[APP_LANGUAGE] = language.code
        }
    }

    suspend fun setThemeMode(themeMode: ThemeModeOption) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE] = themeMode.code
        }
    }

    suspend fun setSelectedCurrency(currency: CurrencyOption) {
        dataStore.edit { preferences ->
            preferences[SELECTED_CURRENCY_CODE] = currency.code
        }
    }

    suspend fun setInvoiceRemindersEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[INVOICE_REMINDERS_ENABLED] = enabled
        }
    }

    suspend fun setOverdueAlertsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[OVERDUE_ALERTS_ENABLED] = enabled
        }
    }

    suspend fun setBudgetWarningsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[BUDGET_WARNINGS_ENABLED] = enabled
        }
    }

    suspend fun setSubscription(
        isPro: Boolean,
        planName: String? = null,
        renewalDate: Long? = null
    ) {
        dataStore.edit { preferences ->
            preferences[SUBSCRIPTION_IS_PRO] = isPro
            if (planName == null) {
                preferences.remove(SUBSCRIPTION_PLAN_NAME)
            } else {
                preferences[SUBSCRIPTION_PLAN_NAME] = planName
            }
            if (renewalDate == null) {
                preferences.remove(SUBSCRIPTION_RENEWAL_DATE)
            } else {
                preferences[SUBSCRIPTION_RENEWAL_DATE] = renewalDate
            }
        }
    }

    suspend fun clearSubscription() {
        setSubscription(isPro = false, planName = null, renewalDate = null)
    }
}


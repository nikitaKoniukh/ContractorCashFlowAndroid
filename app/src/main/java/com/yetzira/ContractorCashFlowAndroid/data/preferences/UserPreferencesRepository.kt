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

interface UserPreferencesRepositoryContract {
    val invoiceRemindersEnabled: Flow<Boolean>
    val overdueAlertsEnabled: Flow<Boolean>
}

interface SettingsPreferencesRepositoryContract {
    val appLanguage: Flow<AppLanguageOption>
    val themeMode: Flow<ThemeModeOption>
    val selectedCurrencyCode: Flow<CurrencyOption>
    val invoiceRemindersEnabled: Flow<Boolean>
    val overdueAlertsEnabled: Flow<Boolean>
    val budgetWarningsEnabled: Flow<Boolean>
    val subscriptionIsPro: Flow<Boolean>
    val subscriptionPlanName: Flow<String?>
    val subscriptionRenewalDate: Flow<Long?>

    suspend fun setAppLanguage(language: AppLanguageOption)
    suspend fun setThemeMode(themeMode: ThemeModeOption)
    suspend fun setSelectedCurrency(currency: CurrencyOption)
    suspend fun setInvoiceRemindersEnabled(enabled: Boolean)
    suspend fun setOverdueAlertsEnabled(enabled: Boolean)
    suspend fun setBudgetWarningsEnabled(enabled: Boolean)
}

interface SubscriptionPreferencesRepositoryContract {
    suspend fun setSubscription(
        isPro: Boolean,
        planName: String? = null,
        renewalDate: Long? = null
    )
}

class UserPreferencesRepository(context: Context) :
    UserPreferencesRepositoryContract,
    SettingsPreferencesRepositoryContract,
    SubscriptionPreferencesRepositoryContract {
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
        val SAVED_EXPENSE_COUNT = longPreferencesKey("saved_expense_count")
        val HAS_RATED = booleanPreferencesKey("has_rated")
        val DECLINED_REVIEW_AFTER_EXPENSES = booleanPreferencesKey("declined_review_after_expenses")
    }

    // Flow getters
    override val appLanguage: Flow<AppLanguageOption> = dataStore.data.map { preferences ->
        val code = preferences[APP_LANGUAGE] ?: "he"
        AppLanguageOption.fromCode(code)
    }

    override val themeMode: Flow<ThemeModeOption> = dataStore.data.map { preferences ->
        val code = preferences[THEME_MODE] ?: ThemeModeOption.SYSTEM.code
        ThemeModeOption.fromCode(code)
    }

    override val selectedCurrencyCode: Flow<CurrencyOption> = dataStore.data.map { preferences ->
        val code = preferences[SELECTED_CURRENCY_CODE] ?: "ILS"
        CurrencyOption.fromCode(code)
    }

    override val invoiceRemindersEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[INVOICE_REMINDERS_ENABLED] ?: false
    }

    override val overdueAlertsEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[OVERDUE_ALERTS_ENABLED] ?: false
    }

    override val budgetWarningsEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[BUDGET_WARNINGS_ENABLED] ?: false
    }

    override val subscriptionIsPro: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[SUBSCRIPTION_IS_PRO] ?: false
    }

    override val subscriptionPlanName: Flow<String?> = dataStore.data.map { preferences ->
        preferences[SUBSCRIPTION_PLAN_NAME]
    }

    override val subscriptionRenewalDate: Flow<Long?> = dataStore.data.map { preferences ->
        preferences[SUBSCRIPTION_RENEWAL_DATE]
    }

    val savedExpenseCount: Flow<Long?> = dataStore.data.map { preferences ->
        preferences[SAVED_EXPENSE_COUNT]
    }

    val hasRated: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[HAS_RATED] ?: false
    }

    val declinedReviewAfterExpenses: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[DECLINED_REVIEW_AFTER_EXPENSES] ?: false
    }

    // Suspend setters
    override suspend fun setAppLanguage(language: AppLanguageOption) {
        dataStore.edit { preferences ->
            preferences[APP_LANGUAGE] = language.code
        }
    }

    override suspend fun setThemeMode(themeMode: ThemeModeOption) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE] = themeMode.code
        }
    }

    override suspend fun setSelectedCurrency(currency: CurrencyOption) {
        dataStore.edit { preferences ->
            preferences[SELECTED_CURRENCY_CODE] = currency.code
        }
    }

    override suspend fun setInvoiceRemindersEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[INVOICE_REMINDERS_ENABLED] = enabled
        }
    }

    override suspend fun setOverdueAlertsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[OVERDUE_ALERTS_ENABLED] = enabled
        }
    }

    override suspend fun setBudgetWarningsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[BUDGET_WARNINGS_ENABLED] = enabled
        }
    }

    override suspend fun setSubscription(
        isPro: Boolean,
        planName: String?,
        renewalDate: Long?
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

    suspend fun setSavedExpenseCount(count: Long) {
        dataStore.edit { preferences ->
            preferences[SAVED_EXPENSE_COUNT] = count
        }
    }

    suspend fun setHasRated(hasRated: Boolean) {
        dataStore.edit { preferences ->
            preferences[HAS_RATED] = hasRated
        }
    }

    suspend fun setDeclinedReviewAfterExpenses(declined: Boolean) {
        dataStore.edit { preferences ->
            preferences[DECLINED_REVIEW_AFTER_EXPENSES] = declined
        }
    }
}


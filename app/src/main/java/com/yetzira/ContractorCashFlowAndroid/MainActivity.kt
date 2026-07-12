package com.yetzira.ContractorCashFlowAndroid

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.FirebaseApp
import com.yetzira.ContractorCashFlowAndroid.billing.PurchaseManagerProvider
import com.yetzira.ContractorCashFlowAndroid.data.preferences.UserPreferencesRepository
import com.yetzira.ContractorCashFlowAndroid.locale.LocaleHelper
import com.yetzira.ContractorCashFlowAndroid.locale.ThemeHelper
import com.yetzira.ContractorCashFlowAndroid.ui.navigation.KablanProNavigationShell
import com.yetzira.ContractorCashFlowAndroid.ui.navigation.TabDestination
import com.yetzira.ContractorCashFlowAndroid.ui.theme.KablanProTheme
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    /**
     * Called before onCreate().
     * Always inject the locale from SharedPreferences so the very first
     * frame renders in the correct language.  On Android 13+ the system
     * per-app locale is ALSO set (by KablanProApplication), but it may
     * not propagate in time for this first attachBaseContext call.
     * Both mechanisms agree on the same language, so there is no conflict.
     */
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Fast SharedPreferences mirrors — no runBlocking on the main thread.
        val themeMode = ThemeHelper.getSavedThemeMode(this)
        AppCompatDelegate.setDefaultNightMode(themeMode.nightModeValue)

        val desiredTag = LocaleHelper.getSavedLanguage(this)
        val currentTag = AppCompatDelegate.getApplicationLocales().toLanguageTags()
        if (currentTag.isNotBlank() && !currentTag.equals(desiredTag, ignoreCase = true)) {
            Log.d(TAG, "Updating AppCompat locale: '$currentTag' → '$desiredTag'")
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(desiredTag)
            )
            return
        }

        Log.d(TAG, "Locale correct: $desiredTag")

        lifecycleScope.launch {
            val preferencesRepo = UserPreferencesRepository(this@MainActivity)
            // One-shot: align SharedPreferences mirrors with DataStore if needed.
            val storedLanguage = preferencesRepo.appLanguage.first()
            if (storedLanguage.code != LocaleHelper.getSavedLanguage(this@MainActivity)) {
                LocaleHelper.saveLanguage(this@MainActivity, storedLanguage.code)
            }
            val storedTheme = preferencesRepo.themeMode.first()
            ThemeHelper.saveThemeMode(this@MainActivity, storedTheme)
            AppCompatDelegate.setDefaultNightMode(storedTheme.nightModeValue)

            preferencesRepo.themeMode.distinctUntilChanged().collect { mode ->
                ThemeHelper.saveThemeMode(this@MainActivity, mode)
            }
        }

        if (BuildConfig.DEBUG) {
            val apps = FirebaseApp.getApps(this)
            val projectIds = apps.mapNotNull { it.options.projectId }
            Log.d("KablanProFirebase", "Initialized=${apps.isNotEmpty()} projects=$projectIds")
        }

        enableEdgeToEdge()
        setContent {
            KablanProTheme {
                val selectedTab = rememberSaveable { mutableStateOf(TabDestination.PROJECTS) }
                KablanProNavigationShell(
                    selectedTab = selectedTab,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            PurchaseManagerProvider.getInstance(applicationContext).checkCurrentEntitlements()
        }
    }

    companion object {
        private const val TAG = "KablanProLocale"
    }
}

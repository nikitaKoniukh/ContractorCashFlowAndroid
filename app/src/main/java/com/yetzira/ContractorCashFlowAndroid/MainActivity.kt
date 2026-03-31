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
import com.yetzira.ContractorCashFlowAndroid.ui.navigation.KablanProNavigationShell
import com.yetzira.ContractorCashFlowAndroid.ui.navigation.TabDestination
import com.yetzira.ContractorCashFlowAndroid.ui.theme.KablanProTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

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

        val preferencesRepo = UserPreferencesRepository(this)
        val themeMode = runBlocking { preferencesRepo.themeMode.first() }
        AppCompatDelegate.setDefaultNightMode(themeMode.nightModeValue)

        val language = runBlocking { preferencesRepo.appLanguage.first() }

        // Keep SharedPreferences in sync with DataStore for attachBaseContext (pre-13).
        val savedCode = LocaleHelper.getSavedLanguage(this)
        if (language.code != savedCode) {
            Log.d(TAG, "Syncing locale DataStore=${language.code} prefs=$savedCode — recreating")
            LocaleHelper.saveLanguage(this, language.code)
            recreate()
            return
        }

        // Ensure AppCompat per-app locale matches our preference.
        // On first launch this was already set by KablanProApplication,
        // but on subsequent launches we still need to keep it in sync
        // (e.g. user changed language in Settings).
        val currentTag = AppCompatDelegate.getApplicationLocales().toLanguageTags()
        val desiredTag = language.code
        if (!currentTag.equals(desiredTag, ignoreCase = true)) {
            Log.d(TAG, "Updating AppCompat locale: '$currentTag' → '$desiredTag'")
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(desiredTag)
            )
            // This may trigger Activity recreation — the rest of onCreate
            // won't execute, but will on the next pass.
            return
        }

        Log.d(TAG, "Locale correct: $desiredTag")

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

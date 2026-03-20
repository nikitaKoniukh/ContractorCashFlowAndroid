package com.yetzira.ContractorCashFlowAndroid

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.core.os.LocaleListCompat
import com.google.firebase.FirebaseApp
import com.yetzira.ContractorCashFlowAndroid.data.preferences.UserPreferencesRepository
import com.yetzira.ContractorCashFlowAndroid.locale.LocaleHelper
import com.yetzira.ContractorCashFlowAndroid.ui.navigation.KablanProNavigationShell
import com.yetzira.ContractorCashFlowAndroid.ui.navigation.TabDestination
import com.yetzira.ContractorCashFlowAndroid.ui.theme.KablanProTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {

    /**
     * Called before onCreate() — the earliest point to apply locale so that
     * every resource lookup in this Activity uses the correct language.
     */
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Sync DataStore → SharedPreferences so attachBaseContext always
        // has the latest user preference on the next recreate / cold start.
        val preferencesRepo = UserPreferencesRepository(this)
        val language = runBlocking { preferencesRepo.appLanguage.first() }
        val savedCode = LocaleHelper.getSavedLanguage(this)
        if (language.code != savedCode) {
            // First launch after install/migration — sync and recreate once.
            Log.d("KablanProLocale", "Syncing locale DataStore=${language.code} prefs=$savedCode — recreating")
            LocaleHelper.saveLanguage(this, language.code)
            recreate()
            return
        }

        // Read and apply persisted locale during startup.
        val localeList = LocaleListCompat.forLanguageTags(language.code)
        AppCompatDelegate.setApplicationLocales(localeList)
        Log.d("KablanProLocale", "Restored locale code=${language.code}")


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
}

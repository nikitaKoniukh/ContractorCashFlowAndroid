package com.yetzira.ContractorCashFlowAndroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.core.os.LocaleListCompat
import com.yetzira.ContractorCashFlowAndroid.data.preferences.UserPreferencesRepository
import com.yetzira.ContractorCashFlowAndroid.ui.navigation.KablanProNavigationShell
import com.yetzira.ContractorCashFlowAndroid.ui.navigation.TabDestination
import com.yetzira.ContractorCashFlowAndroid.ui.theme.KablanProTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set locale from DataStore before first composition
        val preferencesRepo = UserPreferencesRepository(this)
        val language = runBlocking { preferencesRepo.appLanguage.first() }
        val localeList = LocaleListCompat.forLanguageTags(language.code)
        AppCompatDelegate.setApplicationLocales(localeList)
        
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


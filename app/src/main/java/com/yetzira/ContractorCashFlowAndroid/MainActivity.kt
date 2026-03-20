package com.yetzira.ContractorCashFlowAndroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.lifecycleScope
import com.yetzira.ContractorCashFlowAndroid.data.preferences.AppLanguageOption
import com.yetzira.ContractorCashFlowAndroid.data.preferences.UserPreferencesRepository
import com.yetzira.ContractorCashFlowAndroid.ui.navigation.KablanProNavigationShell
import com.yetzira.ContractorCashFlowAndroid.ui.navigation.TabDestination
import com.yetzira.ContractorCashFlowAndroid.ui.theme.KablanProTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set locale from DataStore before first composition
        val preferencesRepo = UserPreferencesRepository(this)
        lifecycleScope.launch {
            preferencesRepo.appLanguage.collect { language ->
                val localeList = LocaleListCompat.forLanguageTags(language.code)
                AppCompatDelegate.setApplicationLocales(localeList)
            }
        }
        
        enableEdgeToEdge()
        setContent {
            KablanProTheme {
                val selectedTab = remember { mutableStateOf(TabDestination.PROJECTS) }
                KablanProNavigationShell(
                    selectedTab = selectedTab,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}


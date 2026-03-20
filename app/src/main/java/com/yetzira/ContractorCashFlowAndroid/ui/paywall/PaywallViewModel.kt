package com.yetzira.ContractorCashFlowAndroid.ui.paywall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yetzira.ContractorCashFlowAndroid.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.launch

class PaywallViewModel(
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {
    fun activatePro(onDone: () -> Unit) {
        viewModelScope.launch {
            preferencesRepository.setSubscription(
                isPro = true,
                planName = "KablanPro Pro",
                renewalDate = System.currentTimeMillis() + THIRTY_DAYS_MS
            )
            onDone()
        }
    }

    private companion object {
        const val THIRTY_DAYS_MS = 30L * 24 * 60 * 60 * 1000
    }
}


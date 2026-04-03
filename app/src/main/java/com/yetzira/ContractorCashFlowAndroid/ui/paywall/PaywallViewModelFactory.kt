package com.yetzira.ContractorCashFlowAndroid.ui.paywall

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yetzira.ContractorCashFlowAndroid.data.preferences.UserPreferencesRepository

class PaywallViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PaywallViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PaywallViewModel(
                preferencesRepository = UserPreferencesRepository(context.applicationContext)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}


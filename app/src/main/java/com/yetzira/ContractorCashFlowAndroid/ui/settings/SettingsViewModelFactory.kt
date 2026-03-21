package com.yetzira.ContractorCashFlowAndroid.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.yetzira.ContractorCashFlowAndroid.data.local.AppDatabase
import com.yetzira.ContractorCashFlowAndroid.data.preferences.UserPreferencesRepository
import com.yetzira.ContractorCashFlowAndroid.export.DataExportService
import com.yetzira.ContractorCashFlowAndroid.network.NetworkConnectivityChecker
import com.yetzira.ContractorCashFlowAndroid.notification.InvoiceNotificationScheduler
import com.yetzira.ContractorCashFlowAndroid.notification.NotificationSettingsCoordinator
import com.yetzira.ContractorCashFlowAndroid.sync.FirestoreSyncService

class SettingsViewModelFactory(
    private val context: Context,
    private val database: AppDatabase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            val preferencesRepository = UserPreferencesRepository(context.applicationContext)
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(
                preferencesRepository = preferencesRepository,
                notificationSettingsCoordinator = NotificationSettingsCoordinator(
                    context = context.applicationContext,
                    invoiceDao = database.invoiceDao(),
                    invoiceNotificationScheduler = InvoiceNotificationScheduler(context.applicationContext)
                ),
                firestoreSyncService = FirestoreSyncService(database),
                exporter = DataExportService(
                    context = context.applicationContext,
                    database = database,
                    preferencesRepository = preferencesRepository
                ),
                firebaseAuth = FirebaseAuth.getInstance(),
                networkConnectivityChecker = NetworkConnectivityChecker(context.applicationContext)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}


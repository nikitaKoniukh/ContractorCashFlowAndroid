package com.yetzira.ContractorCashFlowAndroid.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yetzira.ContractorCashFlowAndroid.data.local.AppDatabase
import com.yetzira.ContractorCashFlowAndroid.data.preferences.UserPreferencesRepository
import com.yetzira.ContractorCashFlowAndroid.export.AppDataSnapshotExporter
import com.yetzira.ContractorCashFlowAndroid.notification.InvoiceNotificationScheduler
import com.yetzira.ContractorCashFlowAndroid.notification.NotificationSettingsCoordinator
import com.yetzira.ContractorCashFlowAndroid.sync.FirestoreSyncService

class SettingsViewModelFactory(
    private val context: Context,
    private val database: AppDatabase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(
                preferencesRepository = UserPreferencesRepository(context.applicationContext),
                notificationSettingsCoordinator = NotificationSettingsCoordinator(
                    context = context.applicationContext,
                    invoiceDao = database.invoiceDao(),
                    invoiceNotificationScheduler = InvoiceNotificationScheduler(context.applicationContext)
                ),
                firestoreSyncService = FirestoreSyncService(context.applicationContext),
                exporter = AppDataSnapshotExporter(
                    context = context.applicationContext,
                    database = database
                )
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}


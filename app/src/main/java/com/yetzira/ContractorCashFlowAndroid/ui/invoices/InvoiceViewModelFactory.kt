package com.yetzira.ContractorCashFlowAndroid.ui.invoices

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yetzira.ContractorCashFlowAndroid.data.local.AppDatabase
import com.yetzira.ContractorCashFlowAndroid.data.repository.InvoiceRepository
import com.yetzira.ContractorCashFlowAndroid.notification.InvoiceNotificationScheduler

class InvoiceViewModelFactory(
    private val context: Context,
    private val database: AppDatabase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InvoiceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InvoiceViewModel(
                repository = InvoiceRepository(
                    invoiceDao = database.invoiceDao(),
                    clientDao = database.clientDao(),
                    projectDao = database.projectDao()
                ),
                notificationScheduler = InvoiceNotificationScheduler(context)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}


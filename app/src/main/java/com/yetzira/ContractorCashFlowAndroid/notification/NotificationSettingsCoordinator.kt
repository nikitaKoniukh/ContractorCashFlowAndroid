package com.yetzira.ContractorCashFlowAndroid.notification

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import com.yetzira.ContractorCashFlowAndroid.data.local.dao.InvoiceDao
import kotlinx.coroutines.flow.first

interface NotificationSettingsCoordinatorContract {
    suspend fun rescheduleAll(
        invoiceRemindersEnabled: Boolean,
        overdueAlertsEnabled: Boolean,
        budgetWarningsEnabled: Boolean
    )
}

class NotificationSettingsCoordinator(
    private val context: Context,
    private val invoiceDao: InvoiceDao,
    private val invoiceNotificationScheduler: InvoiceNotificationScheduler
) : NotificationSettingsCoordinatorContract {
    override suspend fun rescheduleAll(
        invoiceRemindersEnabled: Boolean,
        overdueAlertsEnabled: Boolean,
        budgetWarningsEnabled: Boolean
    ) {
        val invoices = invoiceDao.getAll().first()
        invoices.forEach { invoice ->
            invoiceNotificationScheduler.schedule(
                invoiceId = invoice.id,
                clientName = invoice.clientName,
                dueDate = invoice.dueDate,
                isPaid = invoice.isPaid,
                invoiceRemindersEnabled = invoiceRemindersEnabled,
                overdueAlertsEnabled = overdueAlertsEnabled
            )
        }
        if (!budgetWarningsEnabled) {
            NotificationManagerCompat.from(context).cancel(2001)
        }
    }
}


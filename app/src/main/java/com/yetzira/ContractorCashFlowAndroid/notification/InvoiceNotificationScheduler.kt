package com.yetzira.ContractorCashFlowAndroid.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.InvoiceEntity

class InvoiceNotificationScheduler(
    private val context: Context
) {

    fun schedule(
        invoiceId: String,
        clientName: String,
        dueDate: Long,
        isPaid: Boolean,
        invoiceRemindersEnabled: Boolean = true,
        overdueAlertsEnabled: Boolean = true
    ) {
        cancel(invoiceId)
        if (isPaid) return

        if (invoiceRemindersEnabled) {
            scheduleWorker(
                requestCode = reminderRequestCode(invoiceId),
                triggerAtMillis = dueDate - THREE_DAYS_MS,
                title = "Invoice Reminder",
                message = "Invoice for $clientName is due in 3 days."
            )
        }

        if (overdueAlertsEnabled) {
            scheduleWorker(
                requestCode = overdueRequestCode(invoiceId),
                triggerAtMillis = dueDate + ONE_DAY_MS,
                title = "Invoice Overdue",
                message = "Invoice for $clientName is overdue."
            )
        }
    }

    fun rescheduleAll(
        invoices: List<InvoiceEntity>,
        invoiceRemindersEnabled: Boolean,
        overdueAlertsEnabled: Boolean
    ) {
        invoices.forEach { invoice ->
            schedule(
                invoiceId = invoice.id,
                clientName = invoice.clientName,
                dueDate = invoice.dueDate,
                isPaid = invoice.isPaid,
                invoiceRemindersEnabled = invoiceRemindersEnabled,
                overdueAlertsEnabled = overdueAlertsEnabled
            )
        }
    }

    fun cancel(invoiceId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(createPendingIntent(reminderRequestCode(invoiceId), "", ""))
        alarmManager.cancel(createPendingIntent(overdueRequestCode(invoiceId), "", ""))
    }

    private fun scheduleWorker(
        requestCode: Int,
        triggerAtMillis: Long,
        title: String,
        message: String
    ) {
        if (triggerAtMillis <= System.currentTimeMillis()) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = createPendingIntent(requestCode, title, message)
        try {
            // Requires SCHEDULE_EXACT_ALARM permission on API 31+.
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {
            // Permission not granted — fall back to inexact alarm so the app doesn't crash.
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    private fun createPendingIntent(requestCode: Int, title: String, message: String): PendingIntent {
        val intent = Intent(context, InvoiceReminderReceiver::class.java)
            .putExtra(InvoiceReminderReceiver.EXTRA_TITLE, title)
            .putExtra(InvoiceReminderReceiver.EXTRA_MESSAGE, message)
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun reminderRequestCode(invoiceId: String): Int = ("r_$invoiceId").hashCode()

    private fun overdueRequestCode(invoiceId: String): Int = ("o_$invoiceId").hashCode()

    private companion object {
        const val THREE_DAYS_MS = 3L * 24 * 60 * 60 * 1000
        const val ONE_DAY_MS = 24L * 60 * 60 * 1000
    }
}


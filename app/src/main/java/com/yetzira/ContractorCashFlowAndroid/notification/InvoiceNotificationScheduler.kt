package com.yetzira.ContractorCashFlowAndroid.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.yetzira.ContractorCashFlowAndroid.R
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.InvoiceEntity
import java.text.NumberFormat
import java.util.Locale

interface InvoiceNotificationSchedulerContract {
    fun schedule(
        invoiceId: String,
        clientName: String,
        amount: Double,
        dueDate: Long,
        isPaid: Boolean,
        invoiceRemindersEnabled: Boolean = false,
        overdueAlertsEnabled: Boolean = false
    )

    fun rescheduleAll(
        invoices: List<InvoiceEntity>,
        invoiceRemindersEnabled: Boolean,
        overdueAlertsEnabled: Boolean
    )

    fun cancel(invoiceId: String)
}

class InvoiceNotificationScheduler(
    private val context: Context
) : InvoiceNotificationSchedulerContract {

    override fun schedule(
        invoiceId: String,
        clientName: String,
        amount: Double,
        dueDate: Long,
        isPaid: Boolean,
        invoiceRemindersEnabled: Boolean,
        overdueAlertsEnabled: Boolean
    ) {
        cancel(invoiceId)
        if (isPaid) return

        val formattedAmount = NumberFormat.getCurrencyInstance(Locale.getDefault())
            .format(amount)

        if (invoiceRemindersEnabled) {
            scheduleWorker(
                requestCode = reminderRequestCode(invoiceId),
                triggerAtMillis = dueDate - THREE_DAYS_MS,
                title = context.getString(R.string.notif_invoice_due_soon_title),
                message = context.getString(R.string.notif_invoice_due_soon_body, clientName, formattedAmount)
            )
        }

        if (overdueAlertsEnabled) {
            scheduleWorker(
                requestCode = overdueRequestCode(invoiceId),
                triggerAtMillis = dueDate + ONE_DAY_MS,
                title = context.getString(R.string.notif_invoice_overdue_title),
                message = context.getString(R.string.notif_invoice_overdue_body, clientName, formattedAmount)
            )
        }
    }

    override fun rescheduleAll(
        invoices: List<InvoiceEntity>,
        invoiceRemindersEnabled: Boolean,
        overdueAlertsEnabled: Boolean
    ) {
        invoices.forEach { invoice ->
            schedule(
                invoiceId = invoice.id,
                clientName = invoice.clientName,
                amount = invoice.amount,
                dueDate = invoice.dueDate,
                isPaid = invoice.isPaid,
                invoiceRemindersEnabled = invoiceRemindersEnabled,
                overdueAlertsEnabled = overdueAlertsEnabled
            )
        }
    }

    override fun cancel(invoiceId: String) {
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
        if (canUseExactAlarms(alarmManager)) {
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
                return
            } catch (securityException: SecurityException) {
                Log.w(TAG, "Exact alarm permission unavailable, using fallback alarm", securityException)
            } catch (illegalStateException: IllegalStateException) {
                Log.w(TAG, "Exact alarm scheduling failed, using fallback alarm", illegalStateException)
            }
        }

        scheduleInexactAlarm(
            alarmManager = alarmManager,
            triggerAtMillis = triggerAtMillis,
            pendingIntent = pendingIntent
        )
    }

    private fun canUseExactAlarms(alarmManager: AlarmManager): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        return runCatching { alarmManager.canScheduleExactAlarms() }
            .getOrDefault(false)
    }

    private fun scheduleInexactAlarm(
        alarmManager: AlarmManager,
        triggerAtMillis: Long,
        pendingIntent: PendingIntent
    ) {
        try {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } catch (throwable: Throwable) {
            Log.e(TAG, "Failed to schedule fallback invoice alarm", throwable)
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
        const val TAG = "InvoiceNotifScheduler"
        const val THREE_DAYS_MS = 3L * 24 * 60 * 60 * 1000
        const val ONE_DAY_MS = 24L * 60 * 60 * 1000
    }
}

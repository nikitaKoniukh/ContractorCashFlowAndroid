package com.yetzira.ContractorCashFlowAndroid.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.yetzira.ContractorCashFlowAndroid.data.local.AppDatabase
import com.yetzira.ContractorCashFlowAndroid.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Re-schedules invoice reminder / overdue alarms after device reboot.
 */
class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED &&
            intent?.action != Intent.ACTION_LOCKED_BOOT_COMPLETED
        ) {
            return
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = UserPreferencesRepository(context)
                val reminders = prefs.invoiceRemindersEnabled.first()
                val overdue = prefs.overdueAlertsEnabled.first()
                if (!reminders && !overdue) return@launch

                val invoices = AppDatabase.getInstance(context).invoiceDao().getAll().first()
                InvoiceNotificationScheduler(context).rescheduleAll(
                    invoices = invoices,
                    invoiceRemindersEnabled = reminders,
                    overdueAlertsEnabled = overdue
                )
                Log.d(TAG, "Rescheduled ${invoices.size} invoice alarms after boot")
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to reschedule alarms after boot", t)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private companion object {
        const val TAG = "BootCompletedReceiver"
    }
}

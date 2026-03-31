package com.yetzira.ContractorCashFlowAndroid.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.yetzira.ContractorCashFlowAndroid.R
import java.text.NumberFormat
import java.util.Locale

class BudgetWarningNotifier(private val context: Context) {

    fun notify(
        utilizationPercent: Int,
        projectName: String = "",
        totalExpenses: Double = 0.0,
        budget: Double = 0.0
    ) {
        ensureChannel()

        val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
        val formattedExpenses = currencyFormat.format(totalExpenses)
        val formattedBudget = currencyFormat.format(budget)

        val title: String
        val body: String

        if (utilizationPercent >= 100) {
            title = context.getString(R.string.notif_budget_alert_100_title)
            body = if (projectName.isNotBlank()) {
                context.getString(R.string.notif_budget_alert_100_body, projectName, formattedExpenses, formattedBudget)
            } else {
                context.getString(R.string.expenses_budget_warning_critical)
            }
        } else {
            title = context.getString(R.string.notif_budget_warning_80_title)
            body = if (projectName.isNotBlank()) {
                context.getString(R.string.notif_budget_warning_80_body, projectName, formattedExpenses, formattedBudget)
            } else {
                context.getString(R.string.expenses_budget_warning_high)
            }
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    fun cancel() {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }

    private fun ensureChannel() {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val existing = manager.getNotificationChannel(CHANNEL_ID)
        if (existing != null) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Budget Warnings",
            NotificationManager.IMPORTANCE_HIGH
        )
        manager.createNotificationChannel(channel)
    }

    private companion object {
        const val CHANNEL_ID = "kablanpro_budget_warnings"
        const val NOTIFICATION_ID = 2001
    }
}

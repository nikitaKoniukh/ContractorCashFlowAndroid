package com.yetzira.ContractorCashFlowAndroid.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.yetzira.ContractorCashFlowAndroid.KablanProApplication
import com.yetzira.ContractorCashFlowAndroid.MainActivity
import com.yetzira.ContractorCashFlowAndroid.R
import java.text.NumberFormat
import java.util.Locale

class BudgetWarningNotifier(private val context: Context) {

    fun notify(
        utilizationPercent: Int,
        projectName: String = "",
        totalExpenses: Double = 0.0,
        budget: Double = 0.0,
        projectId: String? = null
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

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            if (!projectId.isNullOrBlank()) {
                putExtra(NotificationDeepLink.EXTRA_TYPE, NotificationDeepLink.TYPE_PROJECT)
                putExtra(NotificationDeepLink.EXTRA_PROJECT_ID, projectId)
            }
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            (projectId ?: "budget").hashCode(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, KablanProApplication.CHANNEL_BUDGET_WARNINGS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }
        val notificationId = projectId?.let { ("budget_$it").hashCode() } ?: NOTIFICATION_ID
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    fun cancel() {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }

    private fun ensureChannel() {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = KablanProApplication.CHANNEL_BUDGET_WARNINGS
        if (manager.getNotificationChannel(channelId) != null) return

        val channel = NotificationChannel(
            channelId,
            "Budget Warnings",
            NotificationManager.IMPORTANCE_HIGH
        )
        manager.createNotificationChannel(channel)
    }

    private companion object {
        const val NOTIFICATION_ID = 2001
    }
}

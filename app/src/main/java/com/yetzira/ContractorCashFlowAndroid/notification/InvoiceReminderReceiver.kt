package com.yetzira.ContractorCashFlowAndroid.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.yetzira.ContractorCashFlowAndroid.KablanProApplication
import com.yetzira.ContractorCashFlowAndroid.MainActivity
import com.yetzira.ContractorCashFlowAndroid.R

class InvoiceReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(EXTRA_TITLE)
            ?: context.getString(R.string.notification_invoice_reminder_title)
        val message = intent.getStringExtra(EXTRA_MESSAGE)
            ?: context.getString(R.string.notification_invoice_update_title)
        val invoiceId = intent.getStringExtra(NotificationDeepLink.EXTRA_INVOICE_ID)
        val kind = intent.getStringExtra(NotificationDeepLink.EXTRA_INVOICE_NOTIF_KIND)
            ?: NotificationDeepLink.TYPE_REMINDER

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val channelId = if (kind == NotificationDeepLink.TYPE_OVERDUE) {
            KablanProApplication.CHANNEL_INVOICE_OVERDUE
        } else {
            KablanProApplication.CHANNEL_INVOICE_REMINDERS
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        ensureChannel(manager, channelId, kind, context)

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            if (!invoiceId.isNullOrBlank()) {
                putExtra(NotificationDeepLink.EXTRA_TYPE, NotificationDeepLink.TYPE_INVOICE)
                putExtra(NotificationDeepLink.EXTRA_INVOICE_ID, invoiceId)
            }
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            (invoiceId ?: title).hashCode(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationId = invoiceId?.let {
            if (kind == NotificationDeepLink.TYPE_OVERDUE) {
                ("overdue_$it").hashCode()
            } else {
                ("reminder_$it").hashCode()
            }
        } ?: System.currentTimeMillis().toInt()

        manager.notify(notificationId, notification)
    }

    private fun ensureChannel(
        manager: NotificationManager,
        channelId: String,
        kind: String,
        context: Context
    ) {
        if (manager.getNotificationChannel(channelId) != null) return
        val (name, importance) = if (kind == NotificationDeepLink.TYPE_OVERDUE) {
            context.getString(R.string.notification_channel_overdue) to NotificationManager.IMPORTANCE_HIGH
        } else {
            context.getString(R.string.notification_channel_invoice_reminders) to NotificationManager.IMPORTANCE_DEFAULT
        }
        manager.createNotificationChannel(NotificationChannel(channelId, name, importance))
    }

    companion object {
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_MESSAGE = "extra_message"
    }
}

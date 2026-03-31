package com.yetzira.ContractorCashFlowAndroid

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

class KablanProApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val invoiceReminders = NotificationChannel(
            CHANNEL_INVOICE_REMINDERS,
            "Invoice Reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Reminders for upcoming invoice due dates"
        }

        val invoiceOverdue = NotificationChannel(
            CHANNEL_INVOICE_OVERDUE,
            "Overdue Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alerts for overdue invoices"
        }

        val budgetWarnings = NotificationChannel(
            CHANNEL_BUDGET_WARNINGS,
            "Budget Warnings",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Warnings when project budget reaches 80% or 100%"
        }

        manager.createNotificationChannels(
            listOf(invoiceReminders, invoiceOverdue, budgetWarnings)
        )
    }

    companion object {
        const val CHANNEL_INVOICE_REMINDERS = "kablanpro_invoice_notifications"
        const val CHANNEL_INVOICE_OVERDUE = "kablanpro_invoice_overdue"
        const val CHANNEL_BUDGET_WARNINGS = "kablanpro_budget_warnings"
    }
}


package com.yetzira.ContractorCashFlowAndroid.notification

/**
 * Shared extras / deep-link helpers for notifications that open MainActivity.
 */
object NotificationDeepLink {
    const val EXTRA_TYPE = "extra_deep_link_type"
    const val EXTRA_INVOICE_ID = "extra_invoice_id"
    const val EXTRA_PROJECT_ID = "extra_project_id"

    const val TYPE_INVOICE = "invoice"
    const val TYPE_PROJECT = "project"

    const val TYPE_OVERDUE = "overdue"
    const val TYPE_REMINDER = "reminder"
    const val EXTRA_INVOICE_NOTIF_KIND = "extra_invoice_notif_kind"
}

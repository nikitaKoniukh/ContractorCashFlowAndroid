package com.yetzira.ContractorCashFlowAndroid.billing

object BillingProduct {
    // Must match Google Play Console product IDs exactly.
    const val PRO_MONTHLY = "com.yetzira.contractorcashflow.pro.monthly"
    const val PRO_YEARLY = "com.yetzira.contractorcashflow.pro.yearly"

    val ALL_IDS = listOf(PRO_MONTHLY, PRO_YEARLY)

    const val MONTHLY_BASE_PLAN = "monthly"
    const val YEARLY_BASE_PLAN = "yearly"
}

object FreeTierLimit {
    const val MAX_PROJECTS = 1
    const val MAX_EXPENSES = 1
    const val MAX_INVOICES = 1
    const val MAX_WORKERS = 1
}


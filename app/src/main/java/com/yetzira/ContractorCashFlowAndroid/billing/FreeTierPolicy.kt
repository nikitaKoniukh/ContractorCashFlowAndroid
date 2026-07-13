package com.yetzira.ContractorCashFlowAndroid.billing

/**
 * Pure free-tier gate rules (testable without Play Billing).
 */
object FreeTierPolicy {
    fun canCreate(isProUser: Boolean, currentCount: Int, maxAllowed: Int): Boolean =
        isProUser || currentCount < maxAllowed

    fun canCreateProject(isProUser: Boolean, currentCount: Int): Boolean =
        canCreate(isProUser, currentCount, FreeTierLimit.MAX_PROJECTS)

    fun canCreateWorker(isProUser: Boolean, currentCount: Int): Boolean =
        canCreate(isProUser, currentCount, FreeTierLimit.MAX_WORKERS)
}

interface FreeTierGate {
    fun canCreateProject(currentCount: Int): Boolean
    fun canCreateWorker(currentCount: Int): Boolean
}

package com.yetzira.ContractorCashFlowAndroid.billing

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FreeTierPolicyTest {

    @Test
    fun `free user can create up to project limit`() {
        assertTrue(FreeTierPolicy.canCreateProject(isProUser = false, currentCount = 0))
        assertFalse(FreeTierPolicy.canCreateProject(isProUser = false, currentCount = FreeTierLimit.MAX_PROJECTS))
    }

    @Test
    fun `pro user can create beyond project limit`() {
        assertTrue(FreeTierPolicy.canCreateProject(isProUser = true, currentCount = 100))
    }

    @Test
    fun `free user can create up to worker limit`() {
        assertTrue(FreeTierPolicy.canCreateWorker(isProUser = false, currentCount = 0))
        assertTrue(FreeTierPolicy.canCreateWorker(isProUser = false, currentCount = 1))
        assertFalse(FreeTierPolicy.canCreateWorker(isProUser = false, currentCount = FreeTierLimit.MAX_WORKERS))
    }

    @Test
    fun `pro user can create beyond worker limit`() {
        assertTrue(FreeTierPolicy.canCreateWorker(isProUser = true, currentCount = 50))
    }
}

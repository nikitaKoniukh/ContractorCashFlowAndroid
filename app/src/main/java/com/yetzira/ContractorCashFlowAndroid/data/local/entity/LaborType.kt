package com.yetzira.ContractorCashFlowAndroid.data.local.entity

import androidx.annotation.StringRes
import com.yetzira.ContractorCashFlowAndroid.R

enum class LaborType(
    @StringRes val labelResId: Int,
    val usesQuantity: Boolean,
    val rateSuffix: String
) {
    HOURLY(
        labelResId = R.string.labor_type_hourly,
        usesQuantity = true,
        rateSuffix = "/hr"
    ),
    DAILY(
        labelResId = R.string.labor_type_daily,
        usesQuantity = true,
        rateSuffix = "/day"
    ),
    SUBCONTRACTOR(
        labelResId = R.string.labor_type_subcontractor,
        usesQuantity = false,
        rateSuffix = ""
    );

    companion object {
        fun fromString(value: String?): LaborType? =
            when (value) {
                "CONTRACT" -> SUBCONTRACTOR
                else -> entries.find { it.name == value }
            }
    }
}

package com.yetzira.ContractorCashFlowAndroid.data.local.entity

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes

enum class ExpenseCategory(
    val displayName: String,
    @ColorInt val chartColor: Int,
    @DrawableRes val iconResId: Int
) {
    MATERIALS(
        displayName = "חומרים",
        chartColor = 0xFF6C63FF.toInt(),
        iconResId = android.R.drawable.ic_dialog_info
    ),
    LABOR(
        displayName = "עבודה",
        chartColor = 0xFF00B4D8.toInt(),
        iconResId = android.R.drawable.ic_dialog_info
    ),
    EQUIPMENT(
        displayName = "ציוד",
        chartColor = 0xFFFFA500.toInt(),
        iconResId = android.R.drawable.ic_dialog_info
    ),
    MISC(
        displayName = "אחר",
        chartColor = 0xFF90EE90.toInt(),
        iconResId = android.R.drawable.ic_dialog_info
    );

    companion object {
        fun fromString(value: String?): ExpenseCategory? =
            values().find { it.name == value }
    }
}


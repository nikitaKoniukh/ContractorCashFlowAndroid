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
        chartColor = 0xFF007AFF.toInt(),   // MaterialsBlue
        iconResId = android.R.drawable.ic_dialog_info
    ),
    LABOR(
        displayName = "עבודה",
        chartColor = 0xFFFF9500.toInt(),   // LaborOrange
        iconResId = android.R.drawable.ic_dialog_info
    ),
    EQUIPMENT(
        displayName = "ציוד",
        chartColor = 0xFF8E8E93.toInt(),   // EquipmentGray
        iconResId = android.R.drawable.ic_dialog_info
    ),
    MISC(
        displayName = "אחר",
        chartColor = 0xFFAF52DE.toInt(),   // MiscPurple
        iconResId = android.R.drawable.ic_dialog_info
    );

    companion object {
        fun fromString(value: String?): ExpenseCategory? =
            values().find { it.name == value }
    }
}


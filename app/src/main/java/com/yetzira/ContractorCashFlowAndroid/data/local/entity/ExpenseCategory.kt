package com.yetzira.ContractorCashFlowAndroid.data.local.entity

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.yetzira.ContractorCashFlowAndroid.R

enum class ExpenseCategory(
    @StringRes val labelResId: Int,
    @ColorInt val chartColor: Int,
    @DrawableRes val iconResId: Int
) {
    MATERIALS(
        labelResId = R.string.expenses_category_materials,
        chartColor = 0xFF007AFF.toInt(),
        iconResId = android.R.drawable.ic_dialog_info
    ),
    LABOR(
        labelResId = R.string.expenses_category_labor,
        chartColor = 0xFFFF9500.toInt(),
        iconResId = android.R.drawable.ic_dialog_info
    ),
    EQUIPMENT(
        labelResId = R.string.expenses_category_equipment,
        chartColor = 0xFF8E8E93.toInt(),
        iconResId = android.R.drawable.ic_dialog_info
    ),
    MISC(
        labelResId = R.string.expenses_category_misc,
        chartColor = 0xFFAF52DE.toInt(),
        iconResId = android.R.drawable.ic_dialog_info
    );

    companion object {
        fun fromString(value: String?): ExpenseCategory? =
            entries.find { it.name == value }
    }
}

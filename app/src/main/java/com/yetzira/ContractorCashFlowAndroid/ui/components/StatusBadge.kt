package com.yetzira.ContractorCashFlowAndroid.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.yetzira.ContractorCashFlowAndroid.ui.theme.BadgeTextStyle

/**
 * StatusBadge — Capsule-shaped pill with 15% opacity background and colored text.
 * Matches ANDROID_UI_GUIDE §7: Status Badges.
 *
 * @param text  Display label (e.g. "Active", "Paid", "Overdue")
 * @param color Semantic color (e.g. IncomeGreen, ExpenseRed)
 * @param icon  Optional leading icon (e.g. checkmark for Paid)
 */
@Composable
fun StatusBadge(
    text: String,
    color: Color,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(color.copy(alpha = 0.15f), shape = CircleShape)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(12.dp)
            )
        }
        Text(
            text = text,
            style = BadgeTextStyle,
            color = color
        )
    }
}


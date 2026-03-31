package com.yetzira.ContractorCashFlowAndroid.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.yetzira.ContractorCashFlowAndroid.ui.theme.LabelSmallSemibold

/**
 * StatPill — Capsule metric display with icon + bold value + label.
 * Matches ANDROID_UI_GUIDE §7: Stat Pill Pattern.
 *
 * Example: StatPill("8", "hours", Icons.Default.Schedule, HourlyTeal)
 */
@Composable
fun StatPill(
    value: String,
    label: String,
    icon: ImageVector? = null,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(color.copy(alpha = 0.10f), shape = CircleShape)
            .padding(horizontal = 10.dp, vertical = 5.dp)
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
            text = value,
            style = LabelSmallSemibold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


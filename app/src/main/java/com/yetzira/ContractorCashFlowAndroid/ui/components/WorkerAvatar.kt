package com.yetzira.ContractorCashFlowAndroid.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.yetzira.ContractorCashFlowAndroid.ui.theme.KablanProColors

/**
 * WorkerAvatar — Circular avatar with first initial.
 * Matches ANDROID_UI_GUIDE §8.5: Worker Avatar.
 *
 * Default: 42dp, WorkerPurple(0.12) background, WorkerPurple text.
 * Use 64dp for detail headers.
 */
@Composable
fun WorkerAvatar(
    name: String,
    size: Dp = 42.dp,
    modifier: Modifier = Modifier
) {
    val initial = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .background(
                color = KablanProColors.WorkerPurple.copy(alpha = 0.12f),
                shape = CircleShape
            )
    ) {
        Text(
            text = initial,
            style = if (size >= 64.dp) {
                MaterialTheme.typography.headlineSmall
            } else {
                MaterialTheme.typography.titleMedium
            },
            color = KablanProColors.WorkerPurple
        )
    }
}


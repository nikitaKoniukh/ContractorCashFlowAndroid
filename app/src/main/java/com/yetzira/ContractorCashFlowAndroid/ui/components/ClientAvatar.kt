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

/**
 * ClientAvatar — Circular avatar with initials from client name.
 * Matches ANDROID_UI_GUIDE §8.6.
 *
 * Uses accentColor (primary) at 15% opacity for background.
 * Shows first letter of first two words.
 */
@Composable
fun ClientAvatar(
    name: String,
    size: Dp = 72.dp,
    modifier: Modifier = Modifier
) {
    val initials = name.trim()
        .split("\\s+".toRegex())
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                shape = CircleShape
            )
    ) {
        Text(
            text = initials.ifEmpty { "?" },
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}


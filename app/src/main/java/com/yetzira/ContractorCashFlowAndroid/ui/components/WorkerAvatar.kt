package com.yetzira.ContractorCashFlowAndroid.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yetzira.ContractorCashFlowAndroid.ui.theme.KablanProShapes

/**
 * WorkerAvatar - Circular avatar with initials for worker
 * Usage: WorkerAvatar(name = "John Doe", backgroundColor = Color.Blue)
 */
@Composable
fun WorkerAvatar(
    name: String,
    backgroundColor: Color,
    textColor: Color = Color.White,
    size: androidx.compose.ui.unit.Dp = 40.dp,
    modifier: Modifier = Modifier
) {
    val initials = name.split(" ")
        .mapNotNull { it.firstOrNull() }
        .take(2)
        .joinToString("")
        .uppercase()

    Box(
        modifier = modifier
            .size(size)
            .background(backgroundColor, shape = KablanProShapes.extraLarge),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = textColor,
            fontSize = 14.sp
        )
    }
}


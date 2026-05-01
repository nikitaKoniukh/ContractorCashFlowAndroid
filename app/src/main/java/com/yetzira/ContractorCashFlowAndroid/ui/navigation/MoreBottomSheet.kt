package com.yetzira.ContractorCashFlowAndroid.ui.navigation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val IosBlue = Color(0xFF007AFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreBottomSheet(
    selectedTab: TabDestination,
    onTabSelected: (TabDestination) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Theme-aware colors
    val isLightMode = !isSystemInDarkTheme()
    val groupedBgColor = if (isLightMode)
        Color(0xFFF2F2F7)
    else
        MaterialTheme.colorScheme.surfaceContainerLow
    val secondaryLabelColor = if (isLightMode)
        Color(0xFF8E8E93)
    else
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
        dragHandle = {
            // Minimal iOS-style drag handle
            Box(
                modifier = Modifier
                    .padding(top = 8.dp, bottom = 4.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            // Section header — iOS style small uppercase label
            Text(
                text = stringResource(id = com.yetzira.ContractorCashFlowAndroid.R.string.tab_more).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = secondaryLabelColor,
                letterSpacing = 0.8.sp,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
            )

            // iOS grouped list card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(groupedBgColor)
            ) {
                TabDestination.moreTabs.forEachIndexed { index, tab ->
                    val isSelected = selectedTab == tab

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                onTabSelected(tab)
                                onDismiss()
                            }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Icon in a colored rounded square (iOS app-icon style)
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) IosBlue else secondaryLabelColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else secondaryLabelColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Text(
                            text = stringResource(id = tab.label),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) IosBlue else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )

                        // iOS chevron
                        Text(
                            text = "›",
                            fontSize = 20.sp,
                            color = secondaryLabelColor,
                            fontWeight = FontWeight.Light
                        )
                    }

                    // Hairline divider between rows (iOS separator inset)
                    if (index < TabDestination.moreTabs.lastIndex) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(0.5.dp)
                                .padding(start = 62.dp)
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

package com.yetzira.ContractorCashFlowAndroid.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// iOS-style colors
private val IosBlue       = Color(0xFF007AFF)   // iOS active tint
private val IosGray       = Color(0xFF8E8E93)   // iOS inactive label / icon

@Composable
fun KablanProNavigationBar(
    selectedTab: TabDestination,
    onTabSelected: (TabDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val moreIsActive = selectedTab in TabDestination.moreTabs

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // ── Hairline top separator (iOS tab bar line) ─────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.33.dp)                       // single-pixel on most densities
                .background(Color(0xFFC6C6C8))         // iOS separator color
        )

        // ── Tab items ─────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(49.dp),                        // standard iOS tab bar height
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TabDestination.bottomBarTabs.forEach { tab ->
                val isSelected = if (tab == TabDestination.MORE) moreIsActive else selectedTab == tab
                val tint  = if (isSelected) IosBlue else IosGray
                val icon  = if (isSelected) tab.icon else tab.inactiveIcon   // filled vs outlined

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(49.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onTabSelected(tab) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = stringResource(id = tab.description),
                        tint = tint,
                        modifier = Modifier.size(25.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))   // iOS gap between icon and label
                    Text(
                        text = stringResource(id = tab.label),
                        color = tint,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        lineHeight = 12.sp,
                        letterSpacing = (-0.2).sp               // iOS system font tracking
                    )
                }
            }
        }

        // ── Consume home-indicator insets ─────────────────────────────────
        Box(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
    }
}

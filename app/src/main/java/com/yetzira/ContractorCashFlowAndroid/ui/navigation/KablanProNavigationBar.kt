package com.yetzira.ContractorCashFlowAndroid.ui.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource

@Composable
fun KablanProNavigationBar(
    selectedTab: TabDestination,
    onTabSelected: (TabDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier) {
        TabDestination.values().forEach { tab ->
            NavigationBarItem(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        painter = painterResource(id = tab.icon),
                        contentDescription = stringResource(id = tab.description),
                        modifier = Modifier
                    )
                },
                label = {
                    Text(text = stringResource(id = tab.label))
                }
            )
        }
    }
}


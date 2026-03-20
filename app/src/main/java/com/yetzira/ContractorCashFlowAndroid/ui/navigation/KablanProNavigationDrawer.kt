package com.yetzira.ContractorCashFlowAndroid.ui.navigation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun KablanProNavigationDrawer(
    selectedTab: TabDestination,
    onTabSelected: (TabDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(modifier = modifier) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(id = com.yetzira.ContractorCashFlowAndroid.R.string.app_name),
            modifier = Modifier.padding(horizontal = 16.dp),
            style = androidx.compose.material3.MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(24.dp))

        TabDestination.entries.forEach { tab ->
            NavigationDrawerItem(
                label = { Text(text = stringResource(id = tab.label)) },
                icon = {
                    Icon(
                        painter = painterResource(id = tab.icon),
                        contentDescription = stringResource(id = tab.description)
                    )
                },
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            )
        }
    }
}



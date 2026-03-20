package com.yetzira.ContractorCashFlowAndroid.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KablanProTopBar(
    onMenuClick: () -> Unit
) {
    TopAppBar(
        title = { Text(stringResource(id = com.yetzira.ContractorCashFlowAndroid.R.string.app_name)) },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Open Navigation Menu"
                )
            }
        }
    )
}


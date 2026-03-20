package com.yetzira.ContractorCashFlowAndroid.ui.clients

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yetzira.ContractorCashFlowAndroid.R

@Composable
fun ClientDetailScreen(
    clientName: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = stringResource(R.string.projects_client_detail_placeholder), style = MaterialTheme.typography.titleMedium)
        Text(text = clientName, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 10.dp))
        TextButton(onClick = onBack, modifier = Modifier.padding(top = 12.dp)) {
            Text(stringResource(R.string.common_back))
        }
    }
}


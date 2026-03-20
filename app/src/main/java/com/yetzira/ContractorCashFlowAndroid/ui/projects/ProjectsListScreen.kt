package com.yetzira.ContractorCashFlowAndroid.ui.projects

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsListScreen(
    viewModel: ProjectViewModel,
    onCreateProject: () -> Unit,
    onOpenProject: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.projectsUiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val deletedSuffix = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_deleted)
    val undoLabel = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.common_undo)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateProject) {
                Icon(
                    painter = painterResource(android.R.drawable.ic_input_add),
                    contentDescription = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_new_project)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            TextField(
                value = uiState.query,
                onValueChange = viewModel::setSearchQuery,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                placeholder = { Text(stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_search_placeholder)) },
                singleLine = true
            )

            if (uiState.projects.isEmpty()) {
                EmptyProjectsState(onCreateProject = onCreateProject)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.projects, key = { it.project.id }) { item ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { value ->
                                if (value != SwipeToDismissBoxValue.Settled) {
                                    viewModel.deleteProject(item.project)
                                }
                                true
                            }
                        )

                        LaunchedEffect(dismissState.currentValue) {
                            if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
                                val result = snackbarHostState.showSnackbar(
                                    message = "${item.project.name} $deletedSuffix",
                                    actionLabel = undoLabel
                                )
                                if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                                    viewModel.undoDeleteProject()
                                }
                            }
                        }

                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(MaterialTheme.shapes.medium)
                                        .background(MaterialTheme.colorScheme.errorContainer)
                                        .padding(horizontal = 16.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Text(text = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.common_delete))
                                }
                            },
                            content = {
                                ProjectRow(
                                    item = item,
                                    onClick = { onOpenProject(item.project.id) }
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyProjectsState(
    onCreateProject: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_empty_title),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_empty_body),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
        )
        FloatingActionButton(onClick = onCreateProject) {
            Icon(
                painter = painterResource(android.R.drawable.ic_input_add),
                contentDescription = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_new_project)
            )
        }
    }
}

@Composable
private fun ProjectRow(
    item: ProjectListItemUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.project.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (item.project.isActive) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(Color(0xFF34C759))
                    )
                }
            }
            Text(
                text = item.project.clientName,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_expenses)}: ${formatMoney(item.totalExpenses)}",
                    color = Color(0xFFFF3B30),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "${stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_income)}: ${formatMoney(item.totalIncome)}",
                    color = Color(0xFF34C759),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            val balanceColor = if (item.balance >= 0) Color(0xFF34C759) else Color(0xFFFF3B30)
            Text(
                text = "${stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_balance)}: ${formatMoney(item.balance)}",
                color = balanceColor,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

private fun formatMoney(amount: Double): String =
    String.format(Locale.US, "%.2f", amount)


package com.yetzira.ContractorCashFlowAndroid.ui.projects

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yetzira.ContractorCashFlowAndroid.data.preferences.CurrencyOption
import com.yetzira.ContractorCashFlowAndroid.data.preferences.UserPreferencesRepository
import com.yetzira.ContractorCashFlowAndroid.billing.FreeTierLimit
import com.yetzira.ContractorCashFlowAndroid.billing.PurchaseManagerProvider
import com.yetzira.ContractorCashFlowAndroid.billing.PurchaseViewModel
import com.yetzira.ContractorCashFlowAndroid.billing.PurchaseViewModelFactory
import com.yetzira.ContractorCashFlowAndroid.ui.components.ModernSearchBar
import com.yetzira.ContractorCashFlowAndroid.ui.components.formatCurrencyAmount
import com.yetzira.ContractorCashFlowAndroid.ui.paywall.PaywallSheet
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsListScreen(
    viewModel: ProjectViewModel,
    onCreateProject: () -> Unit,
    onOpenProject: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val preferencesRepository = remember(context) { UserPreferencesRepository(context.applicationContext) }
    val purchaseManager = remember(context) { PurchaseManagerProvider.getInstance(context.applicationContext) }
    val purchaseViewModel: PurchaseViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = remember { PurchaseViewModelFactory(context) }
    )
    val currency by preferencesRepository.selectedCurrencyCode.collectAsState(initial = CurrencyOption.ILS)
    val uiState by viewModel.projectsUiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var pendingDelete by remember { mutableStateOf<ProjectListItemUi?>(null) }
    var showPaywall by remember { mutableStateOf(false) }
    var paywallMessage by remember { mutableStateOf<String?>(null) }
    val deletedSuffix = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_deleted)
    val undoLabel = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.common_undo)

    val onCreateProjectAttempt = {
        if (purchaseManager.canCreateProject(uiState.projects.size)) {
            onCreateProject()
        } else {
            paywallMessage = context.getString(
                com.yetzira.ContractorCashFlowAndroid.R.string.paywall_limit_projects,
                FreeTierLimit.MAX_PROJECTS
            )
            showPaywall = true
        }
    }

    Scaffold(
    contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateProjectAttempt) {
                Icon(
                    imageVector = Icons.Default.Add,
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
            ModernSearchBar(
                value = uiState.query,
                onValueChange = viewModel::setSearchQuery,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                placeholder = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_search_placeholder)
            )

            if (uiState.projects.isEmpty()) {
                EmptyProjectsState(onCreateProject = onCreateProjectAttempt)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 14.dp, bottom = 92.dp)
                ) {
                    items(uiState.projects, key = { it.project.id }) { item ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { value ->
                                if (value != SwipeToDismissBoxValue.Settled) {
                                    pendingDelete = item
                                    return@rememberSwipeToDismissBoxState false
                                }
                                true
                            }
                        )

                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(MaterialTheme.colorScheme.errorContainer)
                                        .padding(horizontal = 16.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Text(text = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.common_delete))
                                }
                            },
                            content = {
                                ProjectCard(
                                    item = item,
                                    currency = currency,
                                    onClick = { onOpenProject(item.project.id) }
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    pendingDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(text = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.common_delete)) },
            text = { Text(text = item.project.name) },
            confirmButton = {
                TextButton(onClick = {
                    pendingDelete = null
                    viewModel.deleteProject(item.project)
                    scope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = "${item.project.name} $deletedSuffix",
                            actionLabel = undoLabel
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            viewModel.undoDeleteProject()
                        }
                    }
                }) {
                    Text(text = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.common_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(text = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.action_cancel))
                }
            }
        )
    }

    if (showPaywall) {
        PaywallSheet(
            viewModel = purchaseViewModel,
            onDismiss = { showPaywall = false },
            limitReachedMessage = paywallMessage
        )
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
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_new_project)
            )
        }
    }
}

@Composable
private fun ProjectCard(
    item: ProjectListItemUi,
    currency: CurrencyOption,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val IncomeGreen = Color(0xFF34C759)
    val ExpenseRed  = Color(0xFFFF3B30)
    val balanceColor = if (item.balance >= 0) IncomeGreen else ExpenseRed

    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Main content
            Column(modifier = Modifier.weight(1f)) {

                // ── Row 1: project name (bold) + status badge ──────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Text(
                        text = item.project.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (item.project.isActive) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(IncomeGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_active),
                                style = com.yetzira.ContractorCashFlowAndroid.ui.theme.BadgeTextStyle,
                                color = IncomeGreen,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFF8E8E93).copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_inactive),
                                style = com.yetzira.ContractorCashFlowAndroid.ui.theme.BadgeTextStyle,
                                color = Color(0xFF8E8E93),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // ── Row 2: client name with person icon ────────────────────
                Row(
                    modifier = Modifier
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.project.clientName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // ── Row 3: income | expenses ───────────────────────────────
                Row(
                    modifier = Modifier
                        .padding(top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Income (start/left)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(ExpenseRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))


                        Text(
                            text = formatCurrencyAmount(item.totalExpenses, currency),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.width(20.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(IncomeGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = formatCurrencyAmount(item.totalIncome, currency),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // ── Divider ────────────────────────────────────────────────
                HorizontalDivider(
                    modifier = Modifier.padding(top = 10.dp, bottom = 6.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    thickness = 0.5.dp
                )

                // ── Row 4: balance ─────────────────────────────────────────
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // "יתרה" / "Balance" label (end/right)
                    Text(
                        text = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_balance),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Balance amount (start/left, color-coded)
                    Text(
                        text = formatCurrencyAmount(item.balance, currency),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = balanceColor,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}



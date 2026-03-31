package com.yetzira.ContractorCashFlowAndroid.ui.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yetzira.ContractorCashFlowAndroid.R
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ExpenseCategory
import com.yetzira.ContractorCashFlowAndroid.data.preferences.CurrencyOption
import com.yetzira.ContractorCashFlowAndroid.data.preferences.UserPreferencesRepository
import com.yetzira.ContractorCashFlowAndroid.ui.components.formatCurrencyAmount
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDetailScreen(
    expenseId: String,
    viewModel: ExpenseViewModel,
    onEdit: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState by viewModel.listUiState.collectAsState()
    val item = listState.expenses.firstOrNull { it.expense.id == expenseId }
    val expense = item?.expense
    val projectName = item?.projectName

    val context = LocalContext.current
    val preferencesRepository = remember(context) { UserPreferencesRepository(context.applicationContext) }
    val currency by preferencesRepository.selectedCurrencyCode.collectAsState(initial = CurrencyOption.ILS)

    var showDeleteDialog by remember { mutableStateOf(false) }

    val bgColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val dividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.common_delete)) },
            text = { Text(stringResource(R.string.expenses_delete_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteExpenseById(expenseId, onDone = onBack)
                }) {
                    Text(stringResource(R.string.common_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier.fillMaxSize(),
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = expense?.descriptionText ?: "",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                },
                actions = {
                    if (expense != null) {
                        TextButton(onClick = { onEdit(expense.id) }) {
                            Text(
                                text = stringResource(R.string.common_edit),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (expense == null) {
            Text(
                text = "Expense not found",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp)
            )
            return@Scaffold
        }

        val category = ExpenseCategory.fromString(expense.category)
        val categoryName = category?.displayName ?: expense.category

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // -- Hero card: icon + amount + category badge --
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Text(
                        text = formatCurrencyAmount(expense.amount, currency),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF3B30)
                    )
                    Box(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = categoryName,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // -- Expense Details card --
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    SectionHeader(
                        icon = Icons.Default.Description,
                        title = stringResource(R.string.expenses_section_details)
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 16.dp), color = dividerColor)
                    DetailRow(
                        icon = Icons.AutoMirrored.Filled.List,
                        label = stringResource(R.string.expenses_detail_description),
                        value = expense.descriptionText
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 16.dp), color = dividerColor)
                    DetailRow(
                        icon = Icons.Default.CalendarToday,
                        label = stringResource(R.string.expenses_detail_date),
                        value = formatDate(expense.date)
                    )
                }
            }

            // -- Project card (if assigned) --
            if (projectName != null) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        SectionHeader(
                            icon = Icons.Default.FolderOpen,
                            title = stringResource(R.string.expenses_form_project_label)
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 16.dp), color = dividerColor)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 13.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = projectName,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // -- Created row (outside card) --
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.projects_created_date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatDateShort(expense.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // -- Delete card --
            Card(
                onClick = { showDeleteDialog = true },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.common_delete),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(icon: ImageVector, title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun formatDate(timestamp: Long): String =
    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(timestamp))

private fun formatDateShort(timestamp: Long): String =
    SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(timestamp))

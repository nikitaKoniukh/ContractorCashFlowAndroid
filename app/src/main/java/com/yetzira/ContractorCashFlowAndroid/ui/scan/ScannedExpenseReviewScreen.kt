package com.yetzira.ContractorCashFlowAndroid.ui.scan

import android.app.DatePickerDialog
import android.net.Uri
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.yetzira.ContractorCashFlowAndroid.R
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ExpenseCategory
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ExpenseEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ProjectEntity
import com.yetzira.ContractorCashFlowAndroid.data.preferences.CurrencyOption
import com.yetzira.ContractorCashFlowAndroid.data.preferences.UserPreferencesRepository
import com.yetzira.ContractorCashFlowAndroid.services.OcrService
import com.yetzira.ContractorCashFlowAndroid.ui.components.ModernDropdown
import com.yetzira.ContractorCashFlowAndroid.ui.components.ModernTextField
import com.yetzira.ContractorCashFlowAndroid.ui.components.formatAmountInput
import com.yetzira.ContractorCashFlowAndroid.ui.components.parseAmountInput
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannedExpenseReviewScreen(
    imageUri: Uri,
    activeProjects: List<ProjectEntity>,
    onSave: (ExpenseEntity) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val preferencesRepository = remember(context) { UserPreferencesRepository(context.applicationContext) }
    val currency by preferencesRepository.selectedCurrencyCode.collectAsState(initial = CurrencyOption.ILS)

    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(System.currentTimeMillis()) }
    var category by remember { mutableStateOf(ExpenseCategory.MISC) }
    var projectId by remember { mutableStateOf<String?>(null) }
    var notes by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(true) }

    // Run OCR on launch
    LaunchedEffect(imageUri) {
        isProcessing = true
        try {
            val result = OcrService.parseFromUri(context, imageUri)
            result.amount?.let { amount = formatAmountInput(it.toLong().toString()) }
            result.date?.let { date = it }
            if (result.description.isNotBlank()) {
                description = result.description
            }
            // Auto-suggest category
            val suggestedCategory = OcrService.suggestCategory(result.description)
            category = ExpenseCategory.fromString(suggestedCategory) ?: ExpenseCategory.MISC
        } catch (_: Exception) { }
        isProcessing = false
    }

    val canSave = description.isNotBlank() && (parseAmountInput(amount) ?: 0.0) > 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.scan_review_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val parsedAmount = parseAmountInput(amount) ?: 0.0
                            if (parsedAmount > 0.0 && description.isNotBlank()) {
                                val entity = ExpenseEntity(
                                    category = category.name,
                                    amount = parsedAmount,
                                    descriptionText = description,
                                    date = date,
                                    projectId = projectId,
                                    notes = notes.ifBlank { null },
                                    receiptImageUri = imageUri.toString()
                                )
                                onSave(entity)
                            }
                        },
                        enabled = canSave && !isProcessing
                    ) {
                        Icon(Icons.Default.Check, contentDescription = stringResource(R.string.common_save))
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        if (isProcessing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.scan_processing),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Receipt image thumbnail
                Card(
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = stringResource(R.string.scan_receipt_image),
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Column {
                            Text(
                                text = stringResource(R.string.scan_receipt_attached),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = stringResource(R.string.scan_receipt_hint),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Category
                Card(
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ModernDropdown(
                            label = stringResource(R.string.expenses_form_category_label),
                            options = ExpenseCategory.entries.map { it.name },
                            selected = category.name,
                            onSelected = { name ->
                                ExpenseCategory.fromString(name)?.let { category = it }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Amount, Description, Date
                Card(
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ModernTextField(
                            value = amount,
                            onValueChange = { amount = formatAmountInput(it) },
                            label = stringResource(R.string.expenses_form_amount_label),
                            modifier = Modifier.fillMaxWidth(),
                            suffix = { Text(currency.symbol) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )

                        ModernTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = stringResource(R.string.expenses_form_description_label),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        DatePickerFieldReview(
                            date = date,
                            onDateSelected = { date = it }
                        )
                    }
                }

                // Notes
                Card(
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ModernTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = stringResource(R.string.scan_notes_label),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = false,
                            minLines = 3
                        )
                    }
                }

                // Project picker
                Card(
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ModernDropdown(
                            label = stringResource(R.string.expenses_form_project_label),
                            options = listOf(stringResource(R.string.expenses_form_no_project)) + activeProjects.map { it.name },
                            selected = activeProjects.firstOrNull { it.id == projectId }?.name
                                ?: stringResource(R.string.expenses_form_no_project),
                            onSelected = { name ->
                                val selected = activeProjects.find { it.name == name }
                                projectId = selected?.id
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Save button
                Button(
                    onClick = {
                        val parsedAmount = parseAmountInput(amount) ?: 0.0
                        if (parsedAmount > 0.0 && description.isNotBlank()) {
                            val entity = ExpenseEntity(
                                category = category.name,
                                amount = parsedAmount,
                                descriptionText = description,
                                date = date,
                                projectId = projectId,
                                notes = notes.ifBlank { null },
                                receiptImageUri = imageUri.toString()
                            )
                            onSave(entity)
                        }
                    },
                    enabled = canSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        stringResource(R.string.common_save),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun DatePickerFieldReview(date: Long, onDateSelected: (Long) -> Unit) {
    val context = LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
            Text(
                text = stringResource(R.string.expenses_form_date_label),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
        TextButton(onClick = {
            val cal = Calendar.getInstance().apply { timeInMillis = date }
            DatePickerDialog(
                context,
                { _, year, month, day ->
                    val picked = Calendar.getInstance().apply {
                        set(year, month, day, 0, 0, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    onDateSelected(picked)
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }) {
            Text(
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(date)),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}


package com.yetzira.ContractorCashFlowAndroid.ui.scan

import android.Manifest
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.yetzira.ContractorCashFlowAndroid.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanExpenseScreen(
    onImageCaptured: (Uri) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember { mutableStateOf(false) }
    var showCamera by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        showCamera = granted
    }

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val savedUri = copyUriToInternalStorage(context, uri)
            if (savedUri != null) {
                onImageCaptured(savedUri)
            } else {
                Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // File picker launcher (images + PDFs)
    val filePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val savedUri = copyUriToInternalStorage(context, uri)
            if (savedUri != null) {
                onImageCaptured(savedUri)
            } else {
                Toast.makeText(context, "Failed to load file", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Camera setup
    val imageCapture = remember { ImageCapture.Builder().build() }

    LaunchedEffect(Unit) {
        val permission = Manifest.permission.CAMERA
        val granted = ContextCompat.checkSelfPermission(context, permission) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
        hasCameraPermission = granted
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.scan_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (showCamera && hasCameraPermission) {
                // Camera preview
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    AndroidView(
                        factory = { ctx ->
                            PreviewView(ctx).also { previewView ->
                                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                                cameraProviderFuture.addListener(
                                    {
                                        val cameraProvider = cameraProviderFuture.get()
                                        val preview = Preview.Builder().build().also {
                                            it.setSurfaceProvider(previewView.surfaceProvider)
                                        }
                                        try {
                                            cameraProvider.unbindAll()
                                            cameraProvider.bindToLifecycle(
                                                lifecycleOwner,
                                                CameraSelector.DEFAULT_BACK_CAMERA,
                                                preview,
                                                imageCapture
                                            )
                                        } catch (_: Exception) { }
                                    },
                                    ContextCompat.getMainExecutor(ctx)
                                )
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Capture button overlay
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        FilledIconButton(
                            onClick = {
                                capturePhoto(context, imageCapture) { uri ->
                                    onImageCaptured(uri)
                                }
                            },
                            modifier = Modifier.size(72.dp),
                            shape = CircleShape
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = stringResource(R.string.scan_capture),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            } else {
                // Selection screen
                Spacer(modifier = Modifier.height(48.dp))

                Text(
                    text = stringResource(R.string.scan_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
                Text(
                    text = stringResource(R.string.scan_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Scan Document (camera)
                    ScanOptionButton(
                        icon = Icons.Default.CameraAlt,
                        label = stringResource(R.string.scan_document),
                        subtitle = stringResource(R.string.scan_document_subtitle),
                        onClick = {
                            if (hasCameraPermission) {
                                showCamera = true
                            } else {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }
                    )

                    // Choose from Photos
                    ScanOptionButton(
                        icon = Icons.Default.Image,
                        label = stringResource(R.string.scan_photos),
                        subtitle = stringResource(R.string.scan_photos_subtitle),
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    )

                    // Choose from Files
                    ScanOptionButton(
                        icon = Icons.Default.Description,
                        label = stringResource(R.string.scan_files),
                        subtitle = stringResource(R.string.scan_files_subtitle),
                        onClick = {
                            filePickerLauncher.launch(arrayOf("image/*", "application/pdf"))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ScanOptionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun capturePhoto(
    context: Context,
    imageCapture: ImageCapture,
    onCaptured: (Uri) -> Unit
) {
    val receiptsDir = File(context.filesDir, "receipts").apply { mkdirs() }
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val photoFile = File(receiptsDir, "receipt_$timestamp.jpg")

    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                onCaptured(Uri.fromFile(photoFile))
            }

            override fun onError(exception: ImageCaptureException) {
                Toast.makeText(context, "Capture failed: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        }
    )
}

private fun copyUriToInternalStorage(context: Context, sourceUri: Uri): Uri? {
    return try {
        val receiptsDir = File(context.filesDir, "receipts").apply { mkdirs() }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val destFile = File(receiptsDir, "receipt_$timestamp.jpg")

        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            destFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        Uri.fromFile(destFile)
    } catch (e: Exception) {
        null
    }
}





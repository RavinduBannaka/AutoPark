@file:OptIn(ExperimentalGetImage::class)

package com.example.autopark.ui.screens.admin

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
//import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.autopark.data.model.ParkingTransaction
import com.example.autopark.ui.viewmodel.ParkingTransactionViewModel
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRScannerScreen(
    navController: NavController,
    viewModel: ParkingTransactionViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    var vehicleNumber by remember { mutableStateOf("") }
    var showResult by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var transactionResult by remember { mutableStateOf<Result<ParkingTransaction>?>(null) }
    var scannedCode by remember { mutableStateOf<String?>(null) }
    var isScanning by remember { mutableStateOf(true) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }
    
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    
    // Handle scanned QR code
    LaunchedEffect(scannedCode) {
        scannedCode?.let { code ->
            isScanning = false
            // Parse QR code format: "vehicleNumber|vehicleId"
            val parts = code.split("|")
            val extractedVehicleNumber = parts.getOrNull(0) ?: code
            val extractedVehicleId = parts.getOrNull(1) ?: code
            vehicleNumber = extractedVehicleNumber
            
            // Auto-process as entry if scanning
            if (extractedVehicleId.isNotBlank()) {
                isProcessing = true
                viewModel.processVehicleEntry(extractedVehicleId, extractedVehicleNumber) { result ->
                    transactionResult = result
                    showResult = true
                    isProcessing = false
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("QR Code Scanner") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (hasCameraPermission) {
                // Camera Preview with ML Kit
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isScanning) {
                            CameraPreviewWithQRScanner(
                                onQRCodeScanned = { code ->
                                    if (scannedCode == null) {
                                        scannedCode = code
                                    }
                                }
                            )
                            
                            // Scanning overlay
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(120.dp),
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                )
                            }
                        } else {
                            // Show scanned result preview
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(80.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "QR Code Scanned!",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Vehicle: $vehicleNumber",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }

                // Manual Entry Section
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Manual Entry",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Enter vehicle number manually:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = vehicleNumber,
                            onValueChange = { vehicleNumber = it.uppercase() },
                            label = { Text("Vehicle Number") },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("e.g., ABC123") }
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (vehicleNumber.isNotBlank()) {
                                        isProcessing = true
                                        viewModel.processVehicleEntryByNumber(vehicleNumber) { result ->
                                            transactionResult = result
                                            showResult = true
                                            isProcessing = false
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                enabled = vehicleNumber.isNotBlank() && !isProcessing
                            ) {
                                Text("Process Entry")
                            }
                            
                            Button(
                                onClick = {
                                    if (vehicleNumber.isNotBlank()) {
                                        isProcessing = true
                                        viewModel.processVehicleExitByNumber(vehicleNumber) { result ->
                                            transactionResult = result
                                            showResult = true
                                            isProcessing = false
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                enabled = vehicleNumber.isNotBlank() && !isProcessing
                            ) {
                                Text("Process Exit")
                            }
                        }
                        
                        if (!isScanning) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = {
                                    scannedCode = null
                                    vehicleNumber = ""
                                    isScanning = true
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Scan Another QR Code")
                            }
                        }
                    }
                }
                
                // Instructions
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Instructions:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "1. Point camera at QR code to scan automatically\n2. Or enter vehicle number manually\n3. Click 'Process Entry' for vehicle entry\n4. Click 'Process Exit' for vehicle exit",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            } else {
                // Permission Denied UI
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Camera permission required",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Please grant camera permission to scan QR codes",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }) {
                        Text("Grant Permission")
                    }
                }
            }
        }
    }
    
    // Result Dialog
    if (showResult) {
        ScanResultDialog(
            result = transactionResult,
            onDismiss = { 
                showResult = false
                transactionResult = null
            }
        )
    }
}

@Composable
fun CameraPreviewWithQRScanner(
    onQRCodeScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val executor = remember { Executors.newSingleThreadExecutor() }
    val barcodeScanner = remember { BarcodeScanning.getClient() }
    
    var preview by remember { mutableStateOf<Preview?>(null) }
    
    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }
            
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                
                preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                
                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { analysis ->
                        analysis.setAnalyzer(executor) { imageProxy ->
                            processImageProxy(
                                barcodeScanner,
                                imageProxy,
                                onQRCodeScanned
                            )
                        }
                    }
                
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    Log.e("QRScanner", "Camera binding failed", e)
                }
            }, ContextCompat.getMainExecutor(ctx))
            
            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

private fun processImageProxy(
    barcodeScanner: BarcodeScanner,
    imageProxy: ImageProxy,
    onQRCodeScanned: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )
        
        barcodeScanner.process(image)
            .addOnSuccessListener { barcodes ->
                if (barcodes.isNotEmpty()) {
                    val barcode = barcodes.first()
                    barcode.rawValue?.let { value ->
                        onQRCodeScanned(value)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("QRScanner", "Barcode scanning failed", e)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        imageProxy.close()
    }
}

@Composable
fun ScanResultDialog(
    result: Result<ParkingTransaction>?,
    onDismiss: () -> Unit
) {
    val transaction = result?.getOrNull()
    val error = result?.exceptionOrNull()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (transaction != null) "Scan Successful" else "Scan Failed",
                color = if (transaction != null) MaterialTheme.colorScheme.primary 
                       else MaterialTheme.colorScheme.error
            )
        },
        text = {
            Column {
                if (transaction != null) {
                    Text("Vehicle: ${transaction.vehicleNumber}")
                    Text("Status: ${transaction.status}")
                    Text("Entry Time: ${formatTimestamp(transaction.entryTime)}")
                    if (transaction.exitTime != null) {
                        Text("Exit Time: ${formatTimestamp(transaction.exitTime!!)}")
                        Text("Duration: ${transaction.duration} minutes")
                        Text("Charge: $${transaction.chargeAmount}")
                    }
                } else {
                    Text(error?.message ?: "Unknown error occurred")
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}

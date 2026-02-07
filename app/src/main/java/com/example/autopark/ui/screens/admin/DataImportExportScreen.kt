package com.example.autopark.ui.screens.admin

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.autopark.ui.viewmodel.DataImportExportViewModel
import com.example.autopark.util.JsonDataManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataImportExportScreen(
    navController: NavController,
    viewModel: DataImportExportViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val jsonDataManager = remember { JsonDataManager(context) }
    val scope = rememberCoroutineScope()
    
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val progress by viewModel.progress.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val successMessage by viewModel.successMessage.collectAsStateWithLifecycle()
    
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Show messages
    LaunchedEffect(errorMessage, successMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    // Export launcher with real Firebase data
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                scope.launch {
                    viewModel.exportAllData().onSuccess { exportData ->
                        jsonDataManager.exportAllData(
                            users = exportData.users,
                            vehicles = exportData.vehicles,
                            parkingLots = exportData.parkingLots,
                            parkingRates = exportData.parkingRates,
                            transactions = exportData.transactions,
                            invoices = exportData.invoices,
                            overdueCharges = exportData.overdueCharges,
                            outputUri = uri
                        ).onSuccess {
                            val totalRecords = exportData.users.size + 
                                exportData.vehicles.size + 
                                exportData.parkingLots.size +
                                exportData.parkingRates.size +
                                exportData.transactions.size +
                                exportData.invoices.size +
                                exportData.overdueCharges.size
                            viewModel.showSuccess("Exported $totalRecords records successfully!")
                        }.onFailure { error ->
                            viewModel.showError("Export failed: ${error.message}")
                        }
                    }.onFailure { error ->
                        viewModel.showError("Export failed: ${error.message}")
                    }
                }
            }
        }
    }

    // Import launcher
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                scope.launch {
                    try {
                        jsonDataManager.importData(uri).onSuccess { data ->
                            val importResult = viewModel.importAllData(
                                DataImportExportViewModel.ExportData(
                                    users = data.users,
                                    vehicles = data.vehicles,
                                    parkingLots = data.parkingLots,
                                    parkingRates = data.parkingRates,
                                    transactions = data.transactions,
                                    invoices = data.invoices,
                                    overdueCharges = data.overdueCharges
                                )
                            )
                            
                            val message = buildString {
                                append("Imported: ${importResult.successCount} records")
                                if (importResult.errorCount > 0) {
                                    append(" (${importResult.errorCount} errors)")
                                }
                            }
                            
                            if (importResult.errorCount > 0) {
                                viewModel.showError(message)
                            } else {
                                viewModel.showSuccess(message)
                            }
                        }.onFailure { error ->
                            viewModel.showError("Import failed: ${error.message}")
                        }
                    } catch (e: Exception) {
                        viewModel.showError("Import failed: ${e.message}")
                    }
                }
            }
        }
    }

    // Export sample data launcher
    val exportSampleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                jsonDataManager.exportSampleDataToFile(uri).onSuccess {
                    viewModel.showSuccess("Sample data exported successfully!")
                }.onFailure { error ->
                    viewModel.showError("Export failed: ${error.message}")
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Data Import/Export") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Export Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Export Data",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Export all parking data including users, vehicles, " +
                                "lots, transactions, and invoices to a JSON file.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (isLoading && progress > 0) {
                        Column {
                            LinearProgressIndicator(
                                progress = { progress / 100f },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Exporting... $progress%",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                                addCategory(Intent.CATEGORY_OPENABLE)
                                type = "application/json"
                                putExtra(Intent.EXTRA_TITLE, "autopark_data_${System.currentTimeMillis()}.json")
                            }
                            exportLauncher.launch(intent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export All Data")
                    }
                }
            }

            // Import Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Import Data",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Import parking data from a JSON file. " +
                                "This will add or update existing records.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (isLoading && progress > 0) {
                        Column {
                            LinearProgressIndicator(
                                progress = { progress / 100f },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Importing... $progress%",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                                addCategory(Intent.CATEGORY_OPENABLE)
                                type = "application/json"
                            }
                            importLauncher.launch(intent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Import Data")
                    }
                }
            }

            // Sample Data Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Sample Data",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Generate sample data for testing and demonstration purposes. " +
                                "Includes sample users, vehicles, parking lots, and transactions.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                                addCategory(Intent.CATEGORY_OPENABLE)
                                type = "application/json"
                                putExtra(Intent.EXTRA_TITLE, "autopark_sample_data.json")
                            }
                            exportSampleLauncher.launch(intent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    ) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export Sample Data")
                    }
                }
            }

            // Information Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Data Format Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "• Export/Import uses JSON format\n" +
                                "• Includes all entities: Users, Vehicles, Lots, Rates\n" +
                                "• Includes transactions, invoices, and overdue charges\n" +
                                "• Export timestamp is included for tracking",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

package com.example.autopark.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.autopark.data.model.ParkingSpot
import com.example.autopark.ui.viewmodel.ParkingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverDashboardScreen(
    navController: NavController,
    viewModel: ParkingViewModel = hiltViewModel()
) {
    val parkingSpots by viewModel.parkingSpots.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    var vehicleNumber by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadParkingSpots()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Driver Dashboard") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Available Parking Spots",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = vehicleNumber,
                onValueChange = { vehicleNumber = it },
                label = { Text("Your Vehicle Number") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g., ABC123") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                val availableSpots = parkingSpots.filter { !it.isOccupied }

                if (availableSpots.isEmpty()) {
                    Text(
                        text = "No available parking spots",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(availableSpots) { spot ->
                            ParkingSpotCard(spot = spot, isAdmin = false) {
                                if (vehicleNumber.isNotBlank()) {
                                    viewModel.occupyParkingSpot(spot.id, vehicleNumber)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.logout() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Logout")
            }
        }
    }
}

@Composable
fun DriverParkingSpotCard(
    spot: ParkingSpot,
    vehicleNumber: String,
    onPark: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Spot ${spot.spotNumber}",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "Floor: ${spot.floor}",
                style = MaterialTheme.typography.bodyMedium
            )

            Button(
                onClick = onPark,
                modifier = Modifier.fillMaxWidth(),
                enabled = vehicleNumber.isNotBlank()
            ) {
                Text("Park Here")
            }
        }
    }
}
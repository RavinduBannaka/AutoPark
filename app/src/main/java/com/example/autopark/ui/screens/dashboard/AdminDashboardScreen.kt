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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.autopark.data.model.ParkingSpot
import com.example.autopark.ui.viewmodel.ParkingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    navController: NavController,
    viewModel: ParkingViewModel = hiltViewModel()
) {
    val parkingSpots by viewModel.parkingSpots.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadParkingSpots()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard") },
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
                text = "Parking Spots Management",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(parkingSpots) { spot ->
                        ParkingSpotCard(spot = spot, isAdmin = true) {
                            // Toggle occupancy
                            viewModel.toggleParkingSpot(spot.id, !spot.isOccupied)
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
fun ParkingSpotCard(
    spot: ParkingSpot,
    isAdmin: Boolean = false,
    onToggleOccupancy: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Spot ${spot.spotNumber}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Floor: ${spot.floor}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = if (spot.isOccupied) "Status: Occupied" else "Status: Available",
                style = MaterialTheme.typography.bodyMedium,
                color = if (spot.isOccupied) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary
            )

            if (spot.isOccupied) {
                spot.vehicleNumber?.let {
                    Text(
                        text = "Vehicle: $it",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (isAdmin) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onToggleOccupancy,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (spot.isOccupied) "Mark as Available" else "Mark as Occupied")
                }
            }
        }
    }
}
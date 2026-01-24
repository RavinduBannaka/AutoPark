package com.example.autopark.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.autopark.ui.viewmodel.AuthUiState
import com.example.autopark.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("driver") }

    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthUiState.Authenticated -> {
                val user = (uiState as AuthUiState.Authenticated).user
                when (user.role) {
                    "admin" -> navController.navigate("admin_dashboard") {
                        popUpTo("register") { inclusive = true }
                    }
                    "driver" -> navController.navigate("driver_dashboard") {
                        popUpTo("register") { inclusive = true }
                    }
                }
            }
            is AuthUiState.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar((uiState as AuthUiState.Error).message)
                }
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Create Account",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Select Role:",
                style = MaterialTheme.typography.labelLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .selectable(
                            selected = selectedRole == "driver",
                            onClick = { selectedRole = "driver" },
                            role = Role.RadioButton
                        )
                ) {
                    RadioButton(
                        selected = selectedRole == "driver",
                        onClick = null
                    )
                    Text(
                        text = "Driver",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .selectable(
                            selected = selectedRole == "admin",
                            onClick = { selectedRole = "admin" },
                            role = Role.RadioButton
                        )
                ) {
                    RadioButton(
                        selected = selectedRole == "admin",
                        onClick = null
                    )
                    Text(
                        text = "Admin",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState == AuthUiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                Button(
                    onClick = {
                        if (password == confirmPassword) {
                            viewModel.register(email, password, name, phoneNumber, selectedRole)
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar("Passwords do not match")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = email.isNotBlank() &&
                            password.isNotBlank() &&
                            confirmPassword.isNotBlank() &&
                            name.isNotBlank() &&
                            phoneNumber.isNotBlank()
                ) {
                    Text("Sign Up")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { navController.navigateUp() },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState != AuthUiState.Loading
            ) {
                Text("Back to Login")
            }
        }
    }
}
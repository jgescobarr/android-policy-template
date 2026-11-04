package org.template.feature.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import org.template.core.designsystem.component.TemplateSnackbarVisuals
import org.template.feature.auth.R

/**
 * Pantalla de login e inspección interactiva para el ejercicio didáctico.
 */
@Composable
fun LoginScreen(
    snackbarHostState: SnackbarHostState,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is LoginEvent.ShowSnackbar -> snackbarHostState.showSnackbar(
                    TemplateSnackbarVisuals(message = event.message, severity = event.severity),
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (state.isLoggedIn) {
            HomeScreenContent(state = state, viewModel = viewModel)
        } else {
            LoginFormContent(state = state, viewModel = viewModel)
        }
    }
}

@Composable
private fun LoginFormContent(
    state: LoginUiState,
    viewModel: LoginViewModel,
) {
    Text(
        text = stringResource(R.string.login_title),
        style = MaterialTheme.typography.headlineMedium,
    )
    Text(
        text = stringResource(R.string.login_subtitle),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    Spacer(modifier = Modifier.height(24.dp))

    OutlinedTextField(
        value = state.email,
        onValueChange = viewModel::onEmailChanged,
        label = { Text(stringResource(R.string.email_label)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
        value = state.password,
        onValueChange = viewModel::onPasswordChanged,
        label = { Text(stringResource(R.string.password_label)) },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = Modifier.fillMaxWidth(),
    )

    Spacer(modifier = Modifier.height(24.dp))

    if (state.isLoading) {
        CircularProgressIndicator()
    } else {
        Button(
            onClick = viewModel::onLoginClicked,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.btn_login_normal))
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = viewModel::onSimulateNetworkErrorClicked,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.btn_simulate_network_error))
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = viewModel::onSimulateSecurityThreatClicked,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.btn_simulate_security_threat))
        }
    }
}

@Composable
private fun HomeScreenContent(
    state: LoginUiState,
    viewModel: LoginViewModel,
) {
    Text(
        text = stringResource(R.string.home_title),
        style = MaterialTheme.typography.headlineMedium,
    )
    Text(
        text = stringResource(R.string.home_welcome, state.userDisplayName ?: "Estudiante"),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
    )

    Spacer(modifier = Modifier.height(24.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.security_status_title),
                style = MaterialTheme.typography.titleSmall,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("• " + stringResource(R.string.status_storage))
            Text(
                if (state.isRooted) {
                    "• " + stringResource(R.string.status_root_threat)
                } else {
                    "• " + stringResource(R.string.status_root_secure)
                },
            )
            Text("• ADB / Depuración USB: " + if (state.isAdbActive) "Activa" else "Inactiva")
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    Button(
        onClick = viewModel::onTriggerSyncClicked,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(stringResource(R.string.btn_trigger_sync))
    }

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedButton(
        onClick = viewModel::onLogoutClicked,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(stringResource(R.string.btn_logout))
    }
}

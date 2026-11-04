package org.template.feature.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.template.core.designsystem.component.SnackbarSeverity
import org.template.core.logging.AppLogger
import org.template.core.security.DeveloperModeDetector
import org.template.core.security.RootDetector
import org.template.domain.model.DomainError
import org.template.domain.model.DomainResult
import org.template.domain.usecase.LoginUseCase
import org.template.domain.usecase.LogoutUseCase
import org.template.feature.sync.SyncWorkerScheduler
import javax.inject.Inject

/**
 * Estado de UI de la pantalla de login e inspección.
 */
data class LoginUiState(
    val email: String = "estudiante@ejemplo.com",
    val password: String = "password123",
    val isLoading: Boolean = false,
    val userUiModel: UserUiModel? = null,
    val isLoggedIn: Boolean = false,
    val isRooted: Boolean = false,
    val isAdbActive: Boolean = false,
    val secureStorageActive: Boolean = true,
) {
    val userDisplayName: String? get() = userUiModel?.displayName
    val userEmail: String? get() = userUiModel?.email
}

/**
 * Eventos únicos que la UI debe consumir (Side Effects).
 */
sealed class LoginEvent {
    data class ShowSnackbar(
        val message: String,
        val severity: SnackbarSeverity = SnackbarSeverity.Info,
    ) : LoginEvent()
}

/**
 * ViewModel interactivo para el ejercicio didáctico.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val rootDetector: RootDetector,
    private val developerModeDetector: DeveloperModeDetector,
    private val syncScheduler: SyncWorkerScheduler,
    private val appLogger: AppLogger,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<LoginEvent>()
    val events: SharedFlow<LoginEvent> = _events.asSharedFlow()

    init {
        checkSecurityEnvironment()
    }

    fun onEmailChanged(newEmail: String) {
        _uiState.update { it.copy(email = newEmail) }
    }

    fun onPasswordChanged(newPassword: String) {
        _uiState.update { it.copy(password = newPassword) }
    }

    fun onLoginClicked() {
        if (_uiState.value.email.isBlank() || !_uiState.value.email.contains("@")) {
            viewModelScope.launch {
                _events.emit(
                    LoginEvent.ShowSnackbar(
                        message = "Correo electrónico inválido (DomainError.Validation)",
                        severity = SnackbarSeverity.Warning,
                    ),
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            when (val result = loginUseCase()) {
                is DomainResult.Success -> {
                    appLogger.info("Login exitoso para userId=%s", result.data.id.value)
                    val uiModel = result.data.toUiModel()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            userUiModel = uiModel,
                        )
                    }
                    _events.emit(
                        LoginEvent.ShowSnackbar(
                            message = "¡Autenticación Círculo Completo Exitosa!",
                            severity = SnackbarSeverity.Info,
                        ),
                    )
                }
                is DomainResult.Failure -> {
                    appLogger.warn("Login falló: %s", result.error::class.simpleName ?: "desconocido")
                    _uiState.update { it.copy(isLoading = false) }

                    val severity = if (result.error is DomainError.SecurityViolation) {
                        SnackbarSeverity.Error
                    } else {
                        SnackbarSeverity.Warning
                    }

                    _events.emit(
                        LoginEvent.ShowSnackbar(
                            message = result.error.toUserMessage(),
                            severity = severity,
                        ),
                    )
                }
            }
        }
    }

    fun onSimulateNetworkErrorClicked() {
        viewModelScope.launch {
            val error = DomainError.Network(RuntimeException("Error 500 del Servidor"))
            _events.emit(
                LoginEvent.ShowSnackbar(
                    message = error.toUserMessage(),
                    severity = SnackbarSeverity.Warning,
                ),
            )
        }
    }

    fun onSimulateSecurityThreatClicked() {
        viewModelScope.launch {
            val error = DomainError.SecurityViolation(reasons = listOf("Dispositivo Root/Hooking Detectado"))
            _events.emit(
                LoginEvent.ShowSnackbar(
                    message = error.toUserMessage(),
                    severity = SnackbarSeverity.Error,
                ),
            )
        }
    }

    fun onTriggerSyncClicked() {
        syncScheduler.schedulePeriodicSync()
        viewModelScope.launch {
            _events.emit(
                LoginEvent.ShowSnackbar(
                    message = "Sincronización WorkManager programada exitosamente",
                    severity = SnackbarSeverity.Info,
                ),
            )
        }
    }

    fun onLogoutClicked() {
        viewModelScope.launch {
            logoutUseCase()
            _uiState.update {
                it.copy(
                    isLoggedIn = false,
                    userUiModel = null,
                )
            }
            _events.emit(
                LoginEvent.ShowSnackbar(
                    message = "Sesión cerrada de forma segura (LogoutUseCase)",
                    severity = SnackbarSeverity.Info,
                ),
            )
        }
    }

    private fun checkSecurityEnvironment() {
        val rootAssessment = rootDetector.assess()
        val adbActive = developerModeDetector.isAdbEnabled()
        _uiState.update {
            it.copy(
                isRooted = rootAssessment.isCompromised,
                isAdbActive = adbActive,
            )
        }
    }
}

/**
 * Traduce un [DomainError] a un mensaje presentable. En un proyecto real,
 * estos textos vendrían de `strings.xml` (Manual de Buenas Prácticas,
 * sección 2.6) — se dejan como literales aquí solo por ser un ejemplo de
 * una sola pantalla sin módulo de recursos propio todavía.
 */
private fun DomainError.toUserMessage(): String = when (this) {
    is DomainError.Network -> "Sin conexión. Intenta de nuevo."
    is DomainError.Unauthorized -> "No pudimos verificar tu identidad."
    is DomainError.SecurityViolation -> "Amenaza de seguridad detectada. La operación ha sido bloqueada."
    else -> "Ocurrió un error inesperado."
}

package org.template.feature.auth.ui

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.template.core.designsystem.component.SnackbarSeverity
import org.template.core.logging.AppLogger
import org.template.core.security.DeveloperModeDetector
import org.template.core.security.RootDetector
import org.template.core.security.RootRiskAssessment
import org.template.domain.model.DomainError
import org.template.domain.model.DomainResult
import org.template.domain.model.UserId
import org.template.domain.model.UserProfile
import org.template.domain.usecase.LoginUseCase
import org.template.feature.sync.SyncWorkerScheduler

/**
 * Pruebas unitarias para [LoginViewModel].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val loginUseCase: LoginUseCase = mockk()
    private val logoutUseCase: org.template.domain.usecase.LogoutUseCase = mockk(relaxed = true)
    private val rootDetector: RootDetector = mockk()
    private val developerModeDetector: DeveloperModeDetector = mockk()
    private val syncScheduler: SyncWorkerScheduler = mockk(relaxed = true)
    private val appLogger: AppLogger = mockk(relaxed = true)

    private lateinit var viewModel: LoginViewModel

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { rootDetector.assess() } returns RootRiskAssessment(isCompromised = false, reasons = emptyList())
        every { developerModeDetector.isAdbEnabled() } returns false

        viewModel = LoginViewModel(
            loginUseCase = loginUseCase,
            logoutUseCase = logoutUseCase,
            rootDetector = rootDetector,
            developerModeDetector = developerModeDetector,
            syncScheduler = syncScheduler,
            appLogger = appLogger,
        )
    }

    @AfterEach
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `login exitoso desactiva isLoading y expone el nombre del usuario`() = runTest {
        val profile = UserProfile(id = UserId("u1"), fullName = "Ada Lovelace", email = "ada@example.com")
        coEvery { loginUseCase() } returns DomainResult.Success(profile)

        viewModel.onLoginClicked()

        val finalState = viewModel.uiState.value
        assertFalse(finalState.isLoading)
        assertEquals("Ada Lovelace", finalState.userDisplayName)
    }

    @Test
    fun `login fallido por red emite un evento ShowSnackbar con severidad Warning`() = runTest {
        coEvery { loginUseCase() } returns DomainResult.Failure(DomainError.Network(RuntimeException()))

        val events = mutableListOf<LoginEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.collect { events.add(it) }
        }

        viewModel.onLoginClicked()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(1, events.size)
        val event = events.first() as LoginEvent.ShowSnackbar
        assertEquals("Sin conexión. Intenta de nuevo.", event.message)
        assertEquals(SnackbarSeverity.Warning, event.severity)
    }

    @Test
    fun `SecurityViolation emite un evento ShowSnackbar con severidad Error, no Warning`() = runTest {
        coEvery { loginUseCase() } returns DomainResult.Failure(
            DomainError.SecurityViolation(reasons = listOf("Binarios de root detectados")),
        )

        val events = mutableListOf<LoginEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.collect { events.add(it) }
        }

        viewModel.onLoginClicked()

        val event = events.first() as LoginEvent.ShowSnackbar
        assertEquals(SnackbarSeverity.Error, event.severity)
    }
}

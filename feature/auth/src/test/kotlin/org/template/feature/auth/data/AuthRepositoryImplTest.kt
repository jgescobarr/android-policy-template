package org.template.feature.auth.data

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialUnknownException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.template.core.common.DispatcherProvider
import org.template.core.security.SecureStorage
import org.template.domain.model.DomainError
import org.template.domain.model.DomainResult

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class AuthRepositoryImplTest {

    private val context: Context = mockk(relaxed = true)
    private val credentialManager: CredentialManager = mockk()
    private val secureStorage: SecureStorage = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    private val dispatchers = object : DispatcherProvider {
        override val io = testDispatcher
        override val main = testDispatcher
        override val default = testDispatcher
    }

    private lateinit var repository: AuthRepositoryImpl

    @Before
    fun setup() {
        repository = AuthRepositoryImpl(context, credentialManager, secureStorage, dispatchers)
    }

    @Test
    fun login_exitoso_guarda_token_y_retorna_perfil() = runTest {
        val mockResponse = mockk<GetCredentialResponse>()
        coEvery { credentialManager.getCredential(context, any<GetCredentialRequest>()) } returns mockResponse

        val result = repository.login()

        assertTrue(result is DomainResult.Success)
        coVerify { secureStorage.saveToken("access_token", any()) }
    }

    @Test
    fun login_fallido_traduce_GetCredentialException_a_DomainError_Unauthorized() = runTest {
        val exception = GetCredentialUnknownException("Fallo de credencial")
        coEvery { credentialManager.getCredential(context, any<GetCredentialRequest>()) } throws exception

        val result = repository.login()

        assertTrue(result is DomainResult.Failure)
        val failure = result as DomainResult.Failure
        assertTrue(failure.error is DomainError.Unauthorized)
    }

    @Test
    fun logout_elimina_token_del_almacenamiento_seguro() = runTest {
        repository.logout()

        coVerify { secureStorage.deleteToken("access_token") }
    }
}

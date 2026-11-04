package org.template.feature.auth.credentials

import android.content.Context
import androidx.credentials.CredentialManager
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.template.core.logging.AppLogger
import org.template.domain.model.DomainResult

class RestoreCredentialsManagerTest {

    private val context: Context = mockk(relaxed = true)
    private val credentialManager: CredentialManager = mockk(relaxed = true)
    private val appLogger: AppLogger = mockk(relaxed = true)

    private lateinit var restoreCredentialsManager: RestoreCredentialsManager

    @BeforeEach
    fun setup() {
        restoreCredentialsManager = RestoreCredentialsManager(context, credentialManager, appLogger)
    }

    @Test
    fun `getRestoreCredential retorna Success con clave de restauracion`() = runTest {
        val result = restoreCredentialsManager.getRestoreCredential()

        assertTrue(result is DomainResult.Success)
    }
}

package org.template.domain.usecase

import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.template.domain.repository.AuthRepository

class LogoutUseCaseTest {

    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val useCase = LogoutUseCase(authRepository)

    @Test
    fun `invoke llama a logout en el repositorio`() = runTest {
        useCase()

        coVerify { authRepository.logout() }
    }
}

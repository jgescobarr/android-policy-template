package org.template.domain.usecase

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.template.domain.model.DomainError
import org.template.domain.model.DomainResult
import org.template.domain.model.UserId
import org.template.domain.model.UserProfile
import org.template.domain.repository.UserRepository

/**
 * Manual de Buenas Prácticas de Código, sección 7.1 — Mockk para dependencias,
 * runTest de kotlinx-coroutines-test para ejecución determinista.
 *
 * Nótese que este UseCase se testea sin ningún componente de Android: es la
 * consecuencia directa de que :domain sea Kotlin puro (KotlinJvmConventionPlugin).
 */
class GetUserProfileUseCaseTest {

    private val userRepository: UserRepository = mockk()
    private val useCase = GetUserProfileUseCase(userRepository)

    @Test
    fun `invoke retorna el perfil cuando el repositorio responde Success`() = runTest {
        val userId = UserId("user-1")
        val expectedProfile = UserProfile(id = userId, fullName = "Ada Lovelace", email = "ada@example.com")
        coEvery { userRepository.getUserProfile(userId) } returns DomainResult.Success(expectedProfile)

        val result = useCase(userId)

        assertEquals(DomainResult.Success(expectedProfile), result)
    }

    @Test
    fun `invoke propaga el DomainError cuando el repositorio falla`() = runTest {
        val userId = UserId("user-404")
        val expectedError = DomainError.NotFound(entityId = userId.value)
        coEvery { userRepository.getUserProfile(userId) } returns DomainResult.Failure(expectedError)

        val result = useCase(userId)

        assertEquals(DomainResult.Failure(expectedError), result)
    }
}

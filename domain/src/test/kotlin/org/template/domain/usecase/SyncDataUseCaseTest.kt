package org.template.domain.usecase

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.template.domain.model.DomainResult

class SyncDataUseCaseTest {

    private val useCase = SyncDataUseCase()

    @Test
    fun `invoke ejecuta sincronizacion y retorna Success`() = runTest {
        val result = useCase()

        assertTrue(result is DomainResult.Success)
    }
}

package org.template.domain.usecase

import org.template.domain.model.DomainResult
import org.template.domain.model.UserProfile
import org.template.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Caso de uso: iniciar sesión.
 *
 * Referencia normativa: Manual de Arquitectura, sección 3.2 — depende
 * exclusivamente de la interfaz [AuthRepository], nunca de Credential
 * Manager, BiometricPrompt ni ninguna API de Android directamente.
 *
 * TRACEABILITY.md > :feature:auth > "Identidad y autenticación integradas
 * vía UseCase + Repository" (Bloqueante · PR)
 */
class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): DomainResult<UserProfile> = authRepository.login()
}

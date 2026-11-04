package org.template.domain.usecase

import org.template.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Caso de uso para cerrar la sesión activa.
 *
 * Referencia normativa: Manual de Arquitectura, sección 3.2 — "Toda acción de
 * negocio debe ser orquestada por un UseCase en la capa de Dominio."
 */
class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke() {
        authRepository.logout()
    }
}

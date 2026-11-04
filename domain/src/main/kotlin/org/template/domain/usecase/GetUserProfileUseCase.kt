package org.template.domain.usecase

import org.template.domain.model.DomainResult
import org.template.domain.model.UserId
import org.template.domain.model.UserProfile
import org.template.domain.repository.UserRepository
import javax.inject.Inject

/**
 * Caso de uso: obtener el perfil de un usuario.
 *
 * Referencia normativa: Manual de Arquitectura, sección 3.2 (Domain Layer) —
 * "Cada caso de uso (UseCase) encapsula una operación atómica de negocio ...
 * Los UseCases no tienen dependencias en la capa de UI ni en
 * implementaciones concretas de la capa de datos; solo dependen de
 * interfaces de repositorio."
 *
 * Nótese: este UseCase depende de la interfaz [UserRepository], nunca de una
 * implementación concreta (Retrofit, Room). Hilt (fuera de este módulo, ver
 * HiltConventionPlugin.kt) resuelve la implementación real en tiempo de
 * ejecución — :domain no sabe ni le importa cuál es.
 *
 * TRACEABILITY.md > :domain > "Coroutines con Structured Concurrency" y
 * "Manejo de errores centralizado mediante sealed classes"
 */
class GetUserProfileUseCase @Inject constructor(
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(userId: UserId): DomainResult<UserProfile> =
        userRepository.getUserProfile(userId)
}

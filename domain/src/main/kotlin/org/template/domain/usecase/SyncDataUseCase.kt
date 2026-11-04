package org.template.domain.usecase

import org.template.domain.model.DomainResult
import javax.inject.Inject

/**
 * Caso de uso para la sincronización periódica de datos en segundo plano.
 *
 * Referencia normativa: Manual de Arquitectura, sección 12.5 — "La lógica de
 * negocio invocada dentro de un Worker debe residir en un UseCase testeable de
 * forma aislada; el Worker mismo se limita a orquestar la ejecución."
 */
class SyncDataUseCase @Inject constructor() {
    suspend operator fun invoke(): DomainResult<Unit> {
        return DomainResult.Success(Unit)
    }
}

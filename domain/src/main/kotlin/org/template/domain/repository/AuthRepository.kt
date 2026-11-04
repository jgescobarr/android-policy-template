package org.template.domain.repository

import org.template.domain.model.DomainResult
import org.template.domain.model.UserProfile

/**
 * Contrato Gateway para autenticación.
 *
 * Referencia normativa: Manual de Arquitectura, sección 10.0 — "un
 * AuthRepository en la capa de Datos implementa la interacción con
 * Credential Manager, expone al dominio un contrato agnóstico del
 * mecanismo específico (passkey, contraseña, federado), y traduce
 * cualquier excepción de Credential Manager a un error de dominio antes de
 * que cruce hacia el UseCase."
 *
 * Nótese que esta interfaz NO tiene ningún método específico de passkeys,
 * contraseñas u OAuth — solo `login()`. La elección de mecanismo es un
 * detalle de la implementación concreta (en :feature:auth), invisible
 * desde aquí. Esto es lo que permite cambiar de Credential Manager a
 * cualquier otro proveedor sin tocar una sola línea de :domain.
 *
 * TRACEABILITY.md > :feature:auth > "Credential Manager integrado para
 * nuevos flujos de autenticación" (Crítico · PR)
 */
interface AuthRepository {
    suspend fun login(): DomainResult<UserProfile>
    suspend fun logout()
}

package org.template.domain.repository

import org.template.domain.model.DomainResult
import org.template.domain.model.UserId
import org.template.domain.model.UserProfile

/**
 * Contrato Gateway para acceso a datos de usuario.
 *
 * Referencia normativa: Manual de Arquitectura, sección 2.7 (Patrón Gateway
 * y Convención de Nombres) — "Toda interacción con un actor externo al
 * núcleo de negocio ... debe pasar por una interfaz declarada en la capa de
 * Dominio e implementada en la capa de Datos o Infraestructura."
 *
 * Convención de nombres: `Repository` (dato + Repository), NUNCA un sufijo
 * `Gateway` en el nombre de la clase — ver sección 2.7 para el porqué.
 *
 * Este archivo vive en :domain (Kotlin puro). Su implementación concreta —
 * que sí conocerá Retrofit, Room, etc. — vivirá en un módulo :data futuro,
 * marcado como Pendiente en TRACEABILITY.md.
 *
 * TRACEABILITY.md > :domain, :data > mappers explícitos en cada frontera
 */
interface UserRepository {
    suspend fun getUserProfile(userId: UserId): DomainResult<UserProfile>
}

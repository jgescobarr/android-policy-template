package org.template.domain.model

/**
 * Modelo de dominio de un perfil de usuario.
 *
 * Este modelo NUNCA debe reutilizarse directamente como UiState en la capa
 * de Presentación, ni como DTO en la capa de Datos — cada capa define su
 * propio modelo y el paso entre capas se hace con mappers explícitos
 * (Manual de Arquitectura, secciones 2.1 y 8.1).
 *
 * TRACEABILITY.md > :domain, :data > "Modelo de dominio separado de UiState
 * y DTO, con mappers explícitos en cada frontera" (Severidad: Crítico · Cuándo: PR)
 */
data class UserProfile(
    val id: UserId,
    val fullName: String,
    val email: String,
)

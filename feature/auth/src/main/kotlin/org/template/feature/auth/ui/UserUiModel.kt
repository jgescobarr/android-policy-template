package org.template.feature.auth.ui

import org.template.domain.model.UserProfile

/**
 * Modelo de presentación (UI) para el usuario autenticado.
 *
 * Referencia normativa: Manual de Arquitectura, sección 2.1 y 3.2 (Cambio 4) —
 * "Cada capa debe definir su propio modelo de datos, y el paso de información entre
 * capas debe realizarse siempre mediante mappers explícitos."
 */
data class UserUiModel(
    val displayName: String,
    val email: String,
)

fun UserProfile.toUiModel(): UserUiModel = UserUiModel(
    displayName = fullName,
    email = email,
)

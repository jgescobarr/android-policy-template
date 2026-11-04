package org.template.domain.model

/**
 * Identificador de dominio para un usuario.
 *
 * Referencia normativa: Manual de Buenas Prácticas de Código, sección 3.8
 * (Value Classes para Evitar Primitive Obsession) — "nada impide pasar un
 * userId donde se esperaba un accountId, ya que ambos son String para el
 * compilador."
 *
 * TRACEABILITY.md > :domain > "IDs y magnitudes de dominio representados
 * con value class donde aplica" (Severidad: Menor · Cuándo: PR)
 */
@JvmInline
value class UserId(val value: String)

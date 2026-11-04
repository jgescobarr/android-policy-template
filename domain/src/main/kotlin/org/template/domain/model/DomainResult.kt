package org.template.domain.model

/**
 * Desenlace de negocio de una operación invocada por un UseCase.
 *
 * Referencia normativa: Manual de Arquitectura, sección 4.3 — "Los
 * resultados de una operación de negocio invocada por un UseCase deben
 * modelarse con un sealed class que represente únicamente el desenlace de
 * negocio: éxito o falla. El estado transitorio de carga (Loading) es un
 * concepto de presentación y no debe formar parte del tipo de retorno de
 * un UseCase."
 *
 * Deliberadamente NO existe una variante `Loading` aquí. Ver
 * [org.template.domain.model.DomainError] para el tipo de error, y el
 * Manual de Buenas Prácticas, sección 5.1, que establece este tipo como el
 * único estándar del proyecto — nunca `kotlin.Result<T>`.
 *
 * TRACEABILITY.md > :domain > "Resultados de operaciones asíncronas
 * tipados con DomainResult (no kotlin.Result)" (Severidad: Crítico · Cuándo: PR)
 */
sealed class DomainResult<out T> {
    data class Success<T>(val data: T) : DomainResult<T>()
    data class Failure(val error: DomainError) : DomainResult<Nothing>()
}

/**
 * Ejecuta [onSuccess] u [onFailure] de forma exhaustiva — el compilador de
 * Kotlin obliga a manejar ambos casos (Manual de Arquitectura, sección 4.3).
 */
inline fun <T> DomainResult<T>.fold(
    onSuccess: (T) -> Unit,
    onFailure: (DomainError) -> Unit,
) {
    when (this) {
        is DomainResult.Success -> onSuccess(data)
        is DomainResult.Failure -> onFailure(error)
    }
}

/** Transforma el valor de éxito sin desempacar el DomainResult. */
inline fun <T, R> DomainResult<T>.map(transform: (T) -> R): DomainResult<R> =
    when (this) {
        is DomainResult.Success -> DomainResult.Success(transform(data))
        is DomainResult.Failure -> this
    }

/** Encadena otra operación que retorna un DomainResult. */
inline fun <T, R> DomainResult<T>.flatMap(transform: (T) -> DomainResult<R>): DomainResult<R> =
    when (this) {
        is DomainResult.Success -> transform(data)
        is DomainResult.Failure -> this
    }

/** Retorna el valor si es Success, o null si es Failure. */
fun <T> DomainResult<T>.getOrNull(): T? = when (this) {
    is DomainResult.Success -> data
    is DomainResult.Failure -> null
}

/** Ejecuta una acción de efecto secundario en caso de éxito. */
inline fun <T> DomainResult<T>.onSuccess(action: (T) -> Unit): DomainResult<T> {
    if (this is DomainResult.Success) action(data)
    return this
}

/** Ejecuta una acción de efecto secundario en caso de fallo. */
inline fun <T> DomainResult<T>.onFailure(action: (DomainError) -> Unit): DomainResult<T> {
    if (this is DomainResult.Failure) action(error)
    return this
}

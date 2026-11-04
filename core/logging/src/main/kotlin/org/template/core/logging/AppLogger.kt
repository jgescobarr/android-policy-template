package org.template.core.logging

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Abstracción única de logging del proyecto.
 *
 * Referencia normativa: Manual de Arquitectura, sección 15.1 (Abstracción
 * del Logger) — "Ninguna clase del proyecto debe importar directamente
 * android.util.Log ni Timber; el acceso al logger se realiza exclusivamente
 * mediante inyección de dependencias."
 *
 * TRACEABILITY.md > :core:logging > "Timber en uso; android.util.Log
 * ausente en código de producción" (Crítico · PR)
 *
 * Todo mensaje pasa por [Sanitizer] antes de llegar a Timber — ver
 * [AppLoggerImpl]. Esto hace que la sanitización sea imposible de omitir
 * por accidente: no es una responsabilidad del llamador, es estructural.
 */
interface AppLogger {
    fun debug(message: String, vararg args: Any?)
    fun info(message: String, vararg args: Any?)
    fun warn(message: String, vararg args: Any?)
    fun error(throwable: Throwable?, message: String, vararg args: Any?)
}

@Singleton
class AppLoggerImpl @Inject constructor(
    private val sanitizer: Sanitizer,
) : AppLogger {
    override fun debug(message: String, vararg args: Any?) {
        timber.log.Timber.d(sanitizer.sanitize(message), *args)
    }

    override fun info(message: String, vararg args: Any?) {
        timber.log.Timber.i(sanitizer.sanitize(message), *args)
    }

    override fun warn(message: String, vararg args: Any?) {
        timber.log.Timber.w(sanitizer.sanitize(message), *args)
    }

    override fun error(throwable: Throwable?, message: String, vararg args: Any?) {
        timber.log.Timber.e(throwable, sanitizer.sanitize(message), *args)
    }
}

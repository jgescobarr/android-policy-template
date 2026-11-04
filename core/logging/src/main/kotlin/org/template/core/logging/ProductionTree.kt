package org.template.core.logging

import android.util.Log
import timber.log.Timber

/**
 * Árbol de Timber para variantes de release.
 *
 * Referencia normativa: Manual de Seguridad, sección 12 (Logging Seguro) —
 * en producción solo se envían eventos WARN/ERROR a un sistema de
 * monitoreo remoto, nunca se escribe en logcat.
 *
 * TRACEABILITY.md > :core:logging > "Árbol de producción configurado:
 * solo errores al sistema de monitoreo, sin logcat en release" (Crítico · PR)
 *
 * La integración real con Crashlytics/Datadog queda como TODO explícito —
 * requiere las credenciales del proyecto de monitoreo real, fuera del
 * alcance de este esqueleto.
 */
class ProductionTree(private val sanitizer: Sanitizer) : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority < Log.WARN) return // silencia DEBUG/INFO en release

        val sanitizedMessage = sanitizer.sanitize(message)

        // TODO: enviar sanitizedMessage a Crashlytics/Datadog real.
        reportToMonitoring(priority, tag, sanitizedMessage, t)
    }

    private fun reportToMonitoring(priority: Int, tag: String?, message: String, t: Throwable?) {
        // Placeholder — pendiente de integración real.
    }
}

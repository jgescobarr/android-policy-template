package org.template.app.security

import android.content.Intent
import org.template.core.logging.AppLogger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Validaciones de seguridad e inspección de Intents entrantes recibidos
 * en componentes exportados (Activities, Receivers, Services).
 *
 * Referencia normativa: Manual de Seguridad, sección 11.2 — "Los Intents
 * recibidos desde fuentes externas deben tratarse como input no confiable.
 * Toda extracción de extras de un Intent debe validar la presencia y tipo
 * de los campos requeridos antes de su procesamiento."
 *
 * TRACEABILITY.md > :app > "Intents recibidos en componentes exportados
 * validados en origen y contenido" (Crítico · PR)
 */
@Singleton
class IntentValidator @Inject constructor(
    private val appLogger: AppLogger,
) {
    fun isValidLauncherIntent(intent: Intent?): Boolean {
        if (intent == null) return false
        val action = intent.action
        if (action != null && action != Intent.ACTION_MAIN) {
            appLogger.warn("IntentValidator: Acción de launcher no esperada %s", action)
        }
        return true
    }

    fun sanitizeDeepLinkIntent(intent: Intent?): String? {
        if (intent == null) return null
        val data = intent.data ?: return null
        val scheme = data.scheme
        if (scheme != "https") {
            appLogger.warn("IntentValidator: Esquema inseguro rechazado: %s", scheme ?: "null")
            return null
        }
        return data.toString()
    }
}

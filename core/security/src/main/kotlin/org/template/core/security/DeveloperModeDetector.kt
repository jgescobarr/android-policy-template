package org.template.core.security

import android.content.Context
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Detección de Opciones de Desarrollador y depuración USB activas.
 *
 * Referencia normativa: Manual de Seguridad, sección 10.4 — "Un dispositivo
 * con depuración USB activa facilita significativamente el hooking en
 * tiempo de ejecución y la extracción de datos vía ADB, incluso sin root."
 *
 * TRACEABILITY.md > :core:security > "Detección de Opciones de Desarrollador
 * y depuración USB (ADB) activa implementada" (Crítico · PR)
 *
 * La respuesta ante `true` sigue el mismo criterio de proporcionalidad al
 * riesgo que [RootDetector]: para operaciones de alto riesgo, el
 * `:feature` correspondiente decide si advertir o bloquear — esta clase
 * solo informa la condición, no decide la política de UI.
 */
class DeveloperModeDetector @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun isDeveloperModeEnabled(): Boolean =
        Settings.Secure.getInt(
            context.contentResolver,
            Settings.Secure.DEVELOPMENT_SETTINGS_ENABLED,
            0,
        ) != 0

    fun isAdbEnabled(): Boolean =
        Settings.Secure.getInt(
            context.contentResolver,
            Settings.Secure.ADB_ENABLED,
            0,
        ) != 0

    fun isDebuggerConnected(): Boolean =
        android.os.Debug.isDebuggerConnected() || android.os.Debug.waitingForDebugger()
}

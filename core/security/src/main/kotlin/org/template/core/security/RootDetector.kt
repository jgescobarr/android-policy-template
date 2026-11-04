package org.template.core.security

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.template.core.logging.AppLogger
import java.io.File
import javax.inject.Inject

/**
 * Detección de root, frameworks de hooking y ejecución en emuladores.
 *
 * Referencia normativa: Manual de Seguridad, sección 10.2 — "la aplicación
 * debe implementar detecciones en tiempo de ejecución de root, frameworks
 * de hooking (Frida, Xposed) y ejecución en emuladores. La respuesta debe
 * ser proporcional al riesgo."
 *
 * TRACEABILITY.md > :core:security > "Detección de root, hooking y
 * emuladores implementada para funcionalidades de alto riesgo" (Crítico · PR)
 *
 * Diseño: la recolección de señales (¿existen binarios de root? ¿hay
 * librerías de hooking cargadas?) está separada de la decisión de riesgo,
 * para que [RiskAssessor] sea testeable con JUnit sin necesitar un
 * dispositivo o emulador real.
 */
class RootDetector @Inject constructor(
    private val signals: RootSignalSource,
) {
    fun assess(): RootRiskAssessment = RiskAssessor.evaluate(
        hasRootBinaries = signals.hasKnownRootBinaries(),
        hasRootManagementApp = signals.hasRootManagementApp(),
        hasHookingFrameworkSignals = signals.hasHookingFrameworkSignals(),
        isLikelyEmulator = signals.isLikelyEmulator(),
    )
}

data class RootRiskAssessment(
    val isCompromised: Boolean,
    val reasons: List<String>,
)

/** Lógica de decisión pura — sin dependencias de Android, 100% testeable. */
internal object RiskAssessor {
    fun evaluate(
        hasRootBinaries: Boolean,
        hasRootManagementApp: Boolean,
        hasHookingFrameworkSignals: Boolean,
        isLikelyEmulator: Boolean,
    ): RootRiskAssessment {
        val reasons = buildList {
            if (hasRootBinaries) add("Binarios de root detectados")
            if (hasRootManagementApp) add("App de gestión de root instalada")
            if (hasHookingFrameworkSignals) add("Señales de framework de hooking")
            if (isLikelyEmulator) add("Dispositivo parece ser un emulador")
        }
        return RootRiskAssessment(isCompromised = reasons.isNotEmpty(), reasons = reasons)
    }
}

/** Fuente de señales — abstraída para permitir fakes en tests. */
interface RootSignalSource {
    fun hasKnownRootBinaries(): Boolean
    fun hasRootManagementApp(): Boolean
    fun hasHookingFrameworkSignals(): Boolean
    fun isLikelyEmulator(): Boolean
}

/**
 * Implementación de referencia. Las rutas y paquetes listados son un punto
 * de partida — no una lista exhaustiva. Proyectos con requisitos de
 * anti-tampering serios deben evaluar una librería especializada además de
 * (no en lugar de) esta verificación básica.
 *
 * Corrección de esta ronda: [hasRootManagementApp] requiere que cada
 * paquete consultado esté declarado en un bloque `<queries>` del
 * AndroidManifest de este módulo (ver src/main/AndroidManifest.xml) —
 * desde Android 11 (API 30, Package Visibility), consultar un paquete de
 * otra app sin esa declaración retorna "no instalado" incluso si sí lo
 * está, dando una falsa sensación de dispositivo limpio.
 */
class DefaultRootSignalSource(
    private val context: Context,
    private val appLogger: AppLogger,
) : RootSignalSource {
    private val knownRootPaths = listOf(
        "/system/app/Superuser.apk",
        "/sbin/su",
        "/system/bin/su",
        "/system/xbin/su",
        "/data/local/xbin/su",
        "/data/local/bin/su",
        "/system/sd/xbin/su",
        "/system/bin/failsafe/su",
        "/data/local/su",
    )

    private val knownRootManagementPackages = listOf(
        "com.topjohnwu.magisk",
        "eu.chainfire.supersu",
        "com.noshufou.android.su",
        "com.koushikdutta.superuser",
        "com.thirdparty.superuser",
        "com.yellowes.su",
        "com.formyhm.hideroot",
        "com.devadvance.rootcloak",
        "com.devadvance.rootcloakplus",
        "com.zachareew.systemuituner",
    )

    override fun hasKnownRootBinaries(): Boolean = knownRootPaths.any { File(it).exists() }

    override fun hasRootManagementApp(): Boolean {
        val packageManager = context.packageManager
        return knownRootManagementPackages.any { packageName ->
            try {
                packageManager.getPackageInfo(packageName, 0)
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    override fun hasHookingFrameworkSignals(): Boolean {
        return try {
            val mapsFile = File("/proc/self/maps")
            if (mapsFile.exists()) {
                val maps = mapsFile.readText()
                maps.contains("frida") || maps.contains("xposed")
            } else {
                false
            }
        } catch (e: Exception) {
            // Algunos fabricantes restringen el acceso a /proc/self/maps vía
            // SELinux — se registra la advertencia pero no se tumba la
            // ejecución (Manual de Arquitectura, sección 2.7: un Gateway
            // traduce fallos del entorno externo, nunca los propaga crudos).
            appLogger.warn("No se pudo leer /proc/self/maps: %s", e.message ?: "error desconocido")
            false
        }
    }

    override fun isLikelyEmulator(): Boolean {
        val fingerprint = android.os.Build.FINGERPRINT
        return fingerprint.startsWith("generic") ||
            fingerprint.startsWith("unknown") ||
            android.os.Build.MODEL.contains("google_sdk") ||
            android.os.Build.MODEL.contains("Emulator") ||
            android.os.Build.MANUFACTURER.contains("Genymotion")
    }
}

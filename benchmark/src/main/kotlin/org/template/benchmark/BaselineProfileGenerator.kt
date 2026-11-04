package org.template.benchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test

private const val TARGET_PACKAGE = "org.template.app.dev.debug"
private const val UI_TIMEOUT_MS = 5_000L

/**
 * Genera el Baseline Profile de :app.
 *
 * Referencia normativa: Manual de Arquitectura, sección 16.1 — "Todo
 * proyecto debe generar y mantener un Baseline Profile, que precompila
 * las rutas de código críticas (arranque en frío, navegación inicial,
 * pantallas de mayor tráfico) ... generarse mediante Macrobenchmark con
 * el módulo baselineprofile."
 *
 * TRACEABILITY.md > benchmark/ > "Baseline Profile generado, vigente y
 * regenerado en pipeline CI/CD" (Crítico · Release)
 *
 * Ejecutar con: `./gradlew :app:generateBaselineProfile`
 * (requiere un dispositivo administrado o conectado — ver build.gradle.kts).
 * El perfil resultante se copia automáticamente a
 * `app/src/main/baseline-prof.txt`.
 */
class BaselineProfileGenerator {

    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generateStartupAndLoginProfile() = baselineProfileRule.collect(
        packageName = TARGET_PACKAGE,
    ) {
        // Arranque en frío — la ruta crítica más importante del perfil.
        pressHome()
        startActivityAndWait()

        // Navegación inicial: esperar a que la pantalla de login renderice
        // su botón, cubriendo la ruta de LoginScreen + LoginViewModel.
        device.wait(Until.hasObject(By.text("Iniciar sesión")), UI_TIMEOUT_MS)
    }
}

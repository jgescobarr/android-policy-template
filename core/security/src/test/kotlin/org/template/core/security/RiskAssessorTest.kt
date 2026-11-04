package org.template.core.security

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Manual de Buenas Prácticas de Código, sección 7.1 — la lógica de negocio
 * se testea sin componentes de Android. [RiskAssessor] es la parte de
 * [RootDetector] deliberadamente separada de las APIs de Android
 * (File, Build.FINGERPRINT) para que este test no necesite Robolectric ni
 * un dispositivo real.
 */
class RiskAssessorTest {

    @Test
    fun `sin señales de riesgo retorna isCompromised false`() {
        val result = RiskAssessor.evaluate(
            hasRootBinaries = false,
            hasRootManagementApp = false,
            hasHookingFrameworkSignals = false,
            isLikelyEmulator = false,
        )

        assertFalse(result.isCompromised)
        assertTrue(result.reasons.isEmpty())
    }

    @Test
    fun `una sola señal es suficiente para marcar isCompromised true`() {
        val result = RiskAssessor.evaluate(
            hasRootBinaries = true,
            hasRootManagementApp = false,
            hasHookingFrameworkSignals = false,
            isLikelyEmulator = false,
        )

        assertTrue(result.isCompromised)
        assertEquals(1, result.reasons.size)
    }

    @Test
    fun `múltiples señales se acumulan todas en el reporte`() {
        val result = RiskAssessor.evaluate(
            hasRootBinaries = true,
            hasRootManagementApp = true,
            hasHookingFrameworkSignals = true,
            isLikelyEmulator = true,
        )

        assertTrue(result.isCompromised)
        assertEquals(4, result.reasons.size)
    }
}

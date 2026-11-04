package org.template.core.webview

import androidx.lifecycle.LifecycleOwner
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

/**
 * Tests unitarios para [WebBridge] y [WebBridgeMessage].
 *
 * Referencia normativa:
 * - Manual de Seguridad, sección 8.2: Verificación de prohibición del wildcard "*" en orígenes.
 * - Manual de Seguridad, sección 8.3: Sanitización y validación de mensajes entrantes.
 */
class WebBridgeTest {

    @Test
    fun `inicializacion lanza IllegalArgumentException si allowedOrigins contiene wildcard asterisk`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            WebBridge(
                allowedOrigins = setOf("*"),
                onValidMessage = {},
                onRejectedMessage = { _, _ -> },
            )
        }

        assertEquals(
            "WebBridge: lista blanca de orígenes no puede contener wildcard \"*\" (Seg. 8.2)",
            exception.message,
        )
    }

    @Test
    fun `inicializacion lanza IllegalArgumentException si wildcard asterisk esta acompanado de otros origenes`() {
        assertThrows(IllegalArgumentException::class.java) {
            WebBridge(
                allowedOrigins = setOf("https://trusted.com", "*"),
                onValidMessage = {},
                onRejectedMessage = { _, _ -> },
            )
        }
    }

    @Test
    fun `inicializacion exitosa con origenes explicitos bien formados`() {
        val bridge = WebBridge(
            allowedOrigins = setOf("https://trusted.com", "https://auth.trusted.com"),
            onValidMessage = {},
            onRejectedMessage = { _, _ -> },
        )

        assertNotNull(bridge)
    }

    @Test
    fun `WebBridgeMessage parseOrNull retorna null para mensaje en blanco o vacio`() {
        assertNull(WebBridgeMessage.parseOrNull(""))
        assertNull(WebBridgeMessage.parseOrNull("   "))
    }

    @Test
    fun `WebBridgeMessage parseOrNull retorna objeto WebBridgeMessage para mensaje no vacio`() {
        val result = WebBridgeMessage.parseOrNull("{\"action\":\"test\"}")

        assertNotNull(result)
        assertEquals("unknown", result?.type)
        assertEquals("{\"action\":\"test\"}", result?.payload)
    }

    @Test
    fun `eventos de ciclo de vida se ejecutan de forma segura sin WebView asignado`() {
        val bridge = WebBridge(
            allowedOrigins = setOf("https://trusted.com"),
            onValidMessage = {},
            onRejectedMessage = { _, _ -> },
        )
        val lifecycleOwner = mockk<LifecycleOwner>(relaxed = true)

        bridge.onResume(lifecycleOwner)
        bridge.onPause(lifecycleOwner)
        bridge.onDestroy(lifecycleOwner)

        assertNotNull(bridge)
    }
}

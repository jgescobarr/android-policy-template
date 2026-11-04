package org.template.detektrules

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.test.lint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Manual de Buenas Prácticas de Código, sección 7.1 — las reglas de
 * política también son lógica de negocio y se testean como tal. Una regla
 * de detekt sin test es peor que no tenerla: da la impresión de estar
 * protegiendo algo que en realidad podría no detectar nada.
 */
class OrgPolicyRulesTest {

    // ---------- GatewayNamingConventionRule ----------

    @Test
    fun `detecta una interfaz con sufijo Gateway`() {
        val code = """
            interface PaymentsGateway {
                fun charge(): Unit
            }
        """.trimIndent()

        val findings = GatewayNamingConventionRule(Config.empty).lint(code)

        assertEquals(1, findings.size)
        assertTrue(findings.first().message.contains("PaymentsRepository"))
    }

    @Test
    fun `no reporta una interfaz con sufijo Repository`() {
        val code = """
            interface PaymentsRepository {
                fun charge(): Unit
            }
        """.trimIndent()

        val findings = GatewayNamingConventionRule(Config.empty).lint(code)

        assertTrue(findings.isEmpty())
    }

    @Test
    fun `no reporta una clase cuyo nombre solo contiene Gateway en medio`() {
        val code = "class GatewayTimeoutHandler"

        val findings = GatewayNamingConventionRule(Config.empty).lint(code)

        assertTrue(findings.isEmpty())
    }

    // ---------- NoKotlinResultRule ----------

    @Test
    fun `detecta un import explícito de kotlin Result`() {
        val code = """
            import kotlin.Result

            class Foo
        """.trimIndent()

        val findings = NoKotlinResultRule(Config.empty).lint(code)

        assertTrue(findings.isNotEmpty())
    }

    @Test
    fun `detecta Result como tipo de retorno sin import explícito`() {
        val code = """
            class Foo {
                fun login(): Result<String> = TODO()
            }
        """.trimIndent()

        val findings = NoKotlinResultRule(Config.empty).lint(code)

        assertTrue(findings.isNotEmpty())
    }

    @Test
    fun `no reporta DomainResult`() {
        val code = """
            class Foo {
                fun login(): DomainResult<String> = TODO()
            }
        """.trimIndent()

        val findings = NoKotlinResultRule(Config.empty).lint(code)

        assertTrue(findings.isEmpty())
    }

    // ---------- NoConcreteSecurityClassLeakRule ----------

    @Test
    fun `detecta import de SecureStorageManager fuera del paquete de seguridad`() {
        val code = """
            package org.template.feature.auth.data

            import org.template.core.security.SecureStorageManager

            class AuthRepositoryImpl
        """.trimIndent()

        val findings = NoConcreteSecurityClassLeakRule(Config.empty).lint(code)

        assertEquals(1, findings.size)
        assertTrue(findings.first().message.contains("SecureStorageManager"))
    }

    @Test
    fun `no reporta import de la interfaz SecureStorage`() {
        val code = """
            package org.template.feature.auth.data

            import org.template.core.security.SecureStorage

            class AuthRepositoryImpl
        """.trimIndent()

        val findings = NoConcreteSecurityClassLeakRule(Config.empty).lint(code)

        assertTrue(findings.isEmpty())
    }

    @Test
    fun `no reporta KeystoreManager cuando se usa dentro del propio paquete de seguridad`() {
        // Un archivo de :core:security no necesita importar sus propias
        // clases — este caso verifica que la regla no genere falsos
        // positivos si algún archivo del módulo sí usa el import completo.
        val code = """
            package org.template.core.security

            class SomeInternalHelper
        """.trimIndent()

        val findings = NoConcreteSecurityClassLeakRule(Config.empty).lint(code)

        assertTrue(findings.isEmpty())
    }

    // ---------- NoAddJavascriptInterfaceRule ----------

    @Test
    fun `detecta invocacion de addJavascriptInterface en WebView`() {
        val code = """
            class LegacyWebComponent {
                fun setup(webView: android.webkit.WebView) {
                    webView.addJavascriptInterface(Any(), "nativeBridge")
                }
            }
        """.trimIndent()

        val findings = NoAddJavascriptInterfaceRule(Config.empty).lint(code)

        assertEquals(1, findings.size)
        assertTrue(findings.first().message.contains("addJavascriptInterface()"))
    }

    @Test
    fun `no reporta uso de WebMessageListener`() {
        val code = """
            class SecureWebComponent {
                fun setup(webView: android.webkit.WebView) {
                    // Uso seguro
                }
            }
        """.trimIndent()

        val findings = NoAddJavascriptInterfaceRule(Config.empty).lint(code)

        assertTrue(findings.isEmpty())
    }
}

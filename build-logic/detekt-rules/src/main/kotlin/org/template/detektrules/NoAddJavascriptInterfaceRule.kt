package org.template.detektrules

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtCallExpression

/**
 * Prohíbe invocar el método `addJavascriptInterface()` en cualquier [android.webkit.WebView].
 *
 * Referencia normativa:
 * - Manual de Seguridad, sección 8.1: "La comunicación entre código nativo y web
 *   debe realizarse exclusivamente mediante WebMessageListener ... Se prohíbe el uso
 *   del método addJavascriptInterface()."
 * - Manual de Arquitectura, sección 2.7 (Gateway / WebBridge).
 *
 * TRACEABILITY.md > :core:webview > "WebView con WebMessageListener; sin addJavascriptInterface expuesto"
 */
class NoAddJavascriptInterfaceRule(config: Config) : Rule(config) {

    override val issue = Issue(
        id = "NoAddJavascriptInterface",
        severity = Severity.Defect,
        description = "Prohibido el uso de 'addJavascriptInterface()' en WebViews (Manual de Seguridad, sección 8.1). " +
            "Inyectar objetos nativos en el contexto global de JavaScript expone la aplicación a ataques de " +
            "ejecución remota de código (RCE) y bypassing de orígenes. Utiliza WebBridge con WebMessageListener.",
        debt = Debt.TWENTY_MINS,
    )

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val calleeText = expression.calleeExpression?.text ?: return

        if (calleeText == "addJavascriptInterface") {
            report(
                CodeSmell(
                    issue,
                    Entity.from(expression),
                    "Uso inseguro detectado: 'addJavascriptInterface()'. Reemplázalo por WebBridge con " +
                        "WebMessageListener y lista blanca de orígenes (Manual de Seguridad, sección 8.1).",
                ),
            )
        }
    }
}

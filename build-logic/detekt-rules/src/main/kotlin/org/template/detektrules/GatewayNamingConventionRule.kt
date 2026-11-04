package org.template.detektrules

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtClassOrObject

/**
 * Prohíbe el sufijo `Gateway` en nombres de clase o interfaz.
 *
 * Referencia normativa: Manual de Arquitectura, sección 2.7 — "A nivel de
 * código, este manual usa exclusivamente Repository (convención: tipo de
 * dato + Repository) ... No se debe usar el sufijo Gateway en nombres de
 * clase o interfaz — 'Gateway' es únicamente el término conceptual de
 * este principio, no una convención de naming."
 *
 * TRACEABILITY.md — automatiza en CI la fila del checklist que hoy
 * depende de que un revisor humano recuerde esta convención en cada PR.
 */
class GatewayNamingConventionRule(config: Config) : Rule(config) {

    override val issue = Issue(
        id = "GatewayNamingConvention",
        severity = Severity.Defect,
        description = "No se debe usar el sufijo 'Gateway' en nombres de clase o interfaz. " +
            "Usa 'Repository' (dato + Repository) o el nombre específico del dominio que resuelve " +
            "(ver Manual de Arquitectura, sección 2.7).",
        debt = Debt.TEN_MINS,
    )

    override fun visitClassOrObject(classOrObject: KtClassOrObject) {
        super.visitClassOrObject(classOrObject)
        val name = classOrObject.name ?: return
        if (name.endsWith("Gateway")) {
            report(
                CodeSmell(
                    issue,
                    Entity.from(classOrObject),
                    "'$name' usa el sufijo prohibido 'Gateway'. Renómbralo con la convención " +
                        "Repository (sección 2.7 del Manual de Arquitectura), por ejemplo " +
                        "'${name.removeSuffix("Gateway")}Repository'.",
                ),
            )
        }
    }
}

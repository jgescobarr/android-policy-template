package org.template.detektrules

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider

/**
 * Agrupa las reglas normativas propias de esta organización bajo el
 * ruleset "org-policy-rules", configurable en `/detekt.yml` igual que
 * cualquier ruleset nativo de detekt.
 *
 * Cada regla de este archivo corresponde a una decisión tomada durante la
 * revisión de correspondencia de los tres manuales (Arquitectura,
 * Seguridad, Buenas Prácticas) — ver TRACEABILITY.md para el detalle de
 * qué fila de checklist automatiza cada una.
 */
class OrgPolicyRuleSetProvider : RuleSetProvider {
    override val ruleSetId: String = "org-policy-rules"

    override fun instance(config: Config): RuleSet = RuleSet(
        ruleSetId,
        listOf(
            GatewayNamingConventionRule(config),
            NoKotlinResultRule(config),
            NoConcreteSecurityClassLeakRule(config),
            NoAddJavascriptInterfaceRule(config),
        ),
    )
}

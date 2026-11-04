package org.template.detektrules

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtTypeReference

/**
 * Prohíbe el uso de `kotlin.Result<T>` en firmas de función y tipos.
 *
 * Referencia normativa: Manual de Buenas Prácticas de Código, sección 5.1
 * (corrección v2.0) — "Las operaciones que pueden fallar deben comunicar
 * su resultado mediante DomainResult<T> ... no mediante kotlin.Result<T>.
 * kotlin.Result<T> envuelve cualquier Throwable sin distinguir su
 * naturaleza semántica."
 *
 * Esta regla existe porque el propio lenguaje Kotlin ofrece `Result` en
 * su librería estándar sin ninguna importación explícita — es fácil que
 * el autocompletado de un IDE lo sugiera antes que `DomainResult`, y
 * ningún linter genérico de Kotlin detecta esta confusión porque, para
 * Kotlin, `kotlin.Result` es perfectamente válido.
 *
 * TRACEABILITY.md > :domain > "Resultados de operaciones asíncronas
 * tipados con DomainResult (no kotlin.Result)" (Crítico · PR) — pasa de
 * depender de la revisión de código humana a hacerse cumplir en CI.
 */
class NoKotlinResultRule(config: Config) : Rule(config) {

    override val issue = Issue(
        id = "NoKotlinResult",
        severity = Severity.Defect,
        description = "kotlin.Result<T> está prohibido. Usa DomainResult<T> (Manual de " +
            "Arquitectura, sección 4.3), que distingue el tipo de error semántico en vez de " +
            "envolver cualquier Throwable.",
        debt = Debt.TEN_MINS,
    )

    override fun visitImportDirective(importDirective: KtImportDirective) {
        super.visitImportDirective(importDirective)
        val path = importDirective.importedFqName?.asString() ?: return
        if (path == "kotlin.Result") {
            report(
                CodeSmell(
                    issue,
                    Entity.from(importDirective),
                    "Import explícito de kotlin.Result — reemplázalo por DomainResult.",
                ),
            )
        }
    }

    override fun visitTypeReference(typeReference: KtTypeReference) {
        super.visitTypeReference(typeReference)
        val typeText = typeReference.text ?: return
        // Cubre el uso sin import explícito (kotlin.* se resuelve implícitamente),
        // p. ej. "fun login(): Result<UserProfile>".
        if (Regex("""(^|[^.\w])Result<""").containsMatchIn(typeText)) {
            report(
                CodeSmell(
                    issue,
                    Entity.from(typeReference),
                    "Tipo 'Result<...>' detectado — si es kotlin.Result, reemplázalo por " +
                        "DomainResult. Si es un 'Result' propio de otro paquete, ignora esta " +
                        "advertencia o ajusta la configuración de la regla.",
                ),
            )
        }
    }
}

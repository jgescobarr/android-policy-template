package org.template.detektrules

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtImportDirective

private const val SECURITY_PACKAGE_PREFIX = "org.template.core.security"

/**
 * Prohíbe importar las implementaciones CONCRETAS de :core:security
 * (`SecureStorageManager`, `KeystoreManager`) desde código fuera de ese
 * mismo paquete. Solo las interfaces (`SecureStorage`, `RootSignalSource`)
 * pueden inyectarse desde otros módulos.
 *
 * Referencia normativa: Manual de Arquitectura, sección 2.7 (Patrón
 * Gateway) — "el dominio nunca conoce el tipo concreto que existe al otro
 * lado de la interfaz, solo su contrato." Manual de Seguridad, sección 4.1
 * — "Ningún componente de UI, ViewModel, UseCase ni repositorio distinto
 * al de seguridad puede acceder directamente a ... Android Keystore."
 *
 * Este es exactamente el tipo de regla que un code review humano puede
 * pasar por alto — el código compila igual de bien inyectando la interfaz
 * o la clase concreta, así que solo una regla automatizada garantiza que
 * la desviación se detecte en el primer PR, no años después en una
 * auditoría.
 *
 * TRACEABILITY.md > :core:security > "SecureStorageManager implementado
 * como único punto de acceso" (Bloqueante · PR)
 */
class NoConcreteSecurityClassLeakRule(config: Config) : Rule(config) {

    override val issue = Issue(
        id = "NoConcreteSecurityClassLeak",
        severity = Severity.Defect,
        description = "No se debe importar una implementación concreta de :core:security " +
            "(SecureStorageManager, KeystoreManager, DefaultRootSignalSource) fuera de ese " +
            "paquete. Inyecta la interfaz (SecureStorage, RootSignalSource) — ver Manual de " +
            "Arquitectura, sección 2.7.",
        debt = Debt.TWENTY_MINS,
    )

    private val forbiddenConcreteClasses = setOf(
        "SecureStorageManager",
        "KeystoreManager",
        "DefaultRootSignalSource",
    )

    override fun visitImportDirective(importDirective: KtImportDirective) {
        super.visitImportDirective(importDirective)
        val fqName = importDirective.importedFqName?.asString() ?: return
        val simpleName = fqName.substringAfterLast('.')

        val currentPackage = importDirective.containingKtFile.packageFqName.asString()
        val isForbiddenClass = simpleName in forbiddenConcreteClasses
        val isOutsideSecurityPackage = !currentPackage.startsWith(SECURITY_PACKAGE_PREFIX)

        if (isForbiddenClass && isOutsideSecurityPackage) {
            report(
                CodeSmell(
                    issue,
                    Entity.from(importDirective),
                    "Import de la implementación concreta '$simpleName' fuera de " +
                        "$SECURITY_PACKAGE_PREFIX. Inyecta la interfaz correspondiente en su " +
                        "lugar — el binding de Hilt ya resuelve la implementación real.",
                ),
            )
        }
    }
}

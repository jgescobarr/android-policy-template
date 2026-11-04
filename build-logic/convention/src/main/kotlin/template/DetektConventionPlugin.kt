package template

import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/**
 * Aplica detekt con la configuración centralizada de `/detekt.yml` a
 * cualquier módulo que lo incluya, más las reglas normativas propias de
 * la organización (ver build-logic/detekt-rules).
 *
 * Referencia normativa: Manual de Buenas Prácticas de Código, sección 8.2 —
 * "detekt realiza análisis estático de mayor profundidad semántica que
 * ktlint ... La configuración de detekt debe estar centralizada en un
 * archivo detekt.yml en la raíz del proyecto".
 *
 * Las reglas activas en /detekt.yml hacen cumplir automáticamente, sin
 * intervención humana, las siguientes filas del checklist (ver
 * TRACEABILITY.md > build-logic):
 *   - "Sin uso del operador !! en código de producción" (Crítico · PR)
 *   - "Funciones de menos de 30 líneas" (Menor · PR)
 *   - "Sin clases de más de 500 líneas" (Crítico · PR)
 *   - "Sin funciones con más de 5 parámetros" (Menor · PR)
 *   - "Valores técnicos en constantes UPPER_SNAKE_CASE, nunca mágicos" (Menor · PR)
 *   - "Sin uso directo de android.util.Log" (Menor · PR, regla custom)
 *
 * El ruleset "org-policy-rules" (build-logic/detekt-rules) agrega además:
 *   - "GatewayNamingConvention" (Arq. 2.7 — prohíbe el sufijo Gateway)
 *   - "NoKotlinResult" (BP 5.1 — exige DomainResult, no kotlin.Result)
 *   - "NoConcreteSecurityClassLeak" (Arq. 2.7 / Seg. 4.1 — Gateway real)
 */
class DetektConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "io.gitlab.arturbosch.detekt")

            configure<DetektExtension> {
                config.setFrom(files("${rootDir}/detekt.yml"))
                buildUponDefaultConfig = true
                allRules = false
                autoCorrect = true
            }

            tasks.withType(Detekt::class.java).configureEach {
                reports {
                    xml.required.set(true)
                    html.required.set(true)
                }
                jvmTarget = "17"
            }

            dependencies {
                add("detektPlugins", "org.template.buildlogic:detekt-rules")
            }
        }
    }
}

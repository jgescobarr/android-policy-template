package template

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

/**
 * Aplica Hilt de forma uniforme a cualquier módulo Android que lo requiera.
 *
 * Referencia normativa: Manual de Arquitectura, sección 6.1 — "Hilt ... es
 * el framework de inyección de dependencias oficial para Android y es de
 * uso obligatorio en todos los proyectos."
 *
 * NO se aplica a :domain ni a ningún módulo bajo template.kotlin.jvm — Hilt
 * requiere el plugin de Android, y :domain está deliberadamente configurado
 * para no tenerlo (ver KotlinJvmConventionPlugin.kt).
 *
 * Usa KSP (no Kapt) para el procesamiento de anotaciones — confirmado por
 * sincronización real que Kapt generaba fricción con AGP 9.4.0/Kotlin
 * 2.4.10. Cualquier otro módulo que también use KSP (p. ej. Room en
 * :core:database) reutiliza la misma configuración del plugin sin
 * necesidad de aplicarlo de nuevo — aplicar el mismo plugin dos veces al
 * mismo proyecto es un no-op seguro en Gradle.
 *
 * TRACEABILITY.md > build-logic > "Hilt configurado como único framework de
 * inyección de dependencias" (Severidad: Crítico · Cuándo: PR)
 */
class HiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "com.google.dagger.hilt.android")
            apply(plugin = "com.google.devtools.ksp")

            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
            dependencies {
                add("implementation", libs.findLibrary("hilt-android").get())
                add("ksp", libs.findLibrary("hilt-compiler").get())
            }
        }
    }
}

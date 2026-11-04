package template

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

/**
 * Aplica este plugin en cualquier módulo que deba compilar como Kotlin puro
 * (`kotlin("jvm")`), sin acceso al SDK de Android.
 *
 * Referencia normativa: Manual de Arquitectura, sección 5.2 — "El módulo
 * :domain de cada feature debe configurarse como módulo Kotlin puro ...
 * sin aplicar el plugin de Android. Esto no es una convención estilística:
 * es el mecanismo que hace cumplir en tiempo de compilación la independencia
 * de framework que se declara en la sección 3.2."
 *
 * TRACEABILITY.md > :domain > "Módulo :domain configurado como Kotlin puro,
 * sin plugin de Android" (Severidad: Crítico · Cuándo: Auditoría)
 *
 * Un módulo con este plugin NUNCA podrá importar `android.content.Context`,
 * `Room`, ni ninguna clase del SDK de Android — no porque una regla lo
 * prohíba, sino porque esas clases físicamente no están en el classpath.
 */
class KotlinJvmConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "org.jetbrains.kotlin.jvm")

            tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java).configureEach {
                compilerOptions {
                    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
                    // Manual de Buenas Prácticas, sección 2.3: sin warnings silenciados.
                    allWarningsAsErrors.set(false)
                }
            }

            extensions.configure(org.gradle.api.plugins.JavaPluginExtension::class.java) {
                sourceCompatibility = org.gradle.api.JavaVersion.VERSION_17
                targetCompatibility = org.gradle.api.JavaVersion.VERSION_17
            }

            tasks.withType(org.gradle.api.tasks.testing.Test::class.java).configureEach {
                useJUnitPlatform()
            }

            val libs = extensions.getByType(org.gradle.api.artifacts.VersionCatalogsExtension::class.java).named("libs")
            dependencies.add("testRuntimeOnly", libs.findLibrary("junitPlatformLauncher").get())
        }
    }
}

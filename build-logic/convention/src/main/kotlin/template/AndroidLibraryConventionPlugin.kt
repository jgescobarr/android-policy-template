package template

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

/**
 * Configuración base para cualquier módulo `com.android.library` del
 * template: compileSdk/minSdk, Java 17, y las banderas de la sección 17.5
 * del Manual de Arquitectura (edge-to-edge, compatibilidad de plataforma).
 *
 * Referencia normativa: Manual de Arquitectura, sección 17.5 (Compatibilidad
 * de Plataforma) — targetSdk mínimo vigente, compatibilidad de página de
 * 16 KB para librerías nativas.
 *
 * A diferencia de KotlinJvmConventionPlugin, este SÍ aplica el plugin de
 * Android — se usa en :core:security, :core:network, :feature:*, :app, pero
 * NUNCA en :domain (ver KotlinJvmConventionPlugin.kt).
 */
class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "com.android.library")
            // Nota (confirmado por sincronización real con AGP 9.4.0):
            // desde AGP 9.0+, el soporte de Kotlin es nativo ("Built-in
            // Kotlin") — ya no se aplica org.jetbrains.kotlin.android.

            configure<LibraryExtension> {
                compileSdk = 37

                defaultConfig {
                    minSdk = 26
                    // targetSdk se define solo en :app (no aplica a librerías).
                    // Revisar en cada cadencia de revisión semestral (manual, sección 1).
                }

                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }

                testOptions {
                    unitTests.all {
                        it.useJUnitPlatform()
                    }
                }
            }

            val libs = extensions.getByType(org.gradle.api.artifacts.VersionCatalogsExtension::class.java).named("libs")
            dependencies.add("testRuntimeOnly", libs.findLibrary("junitPlatformLauncher").get())
        }
    }
}

package template

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

/**
 * Configuración del único módulo `com.android.application` del template.
 *
 * Referencia normativa:
 * - Manual de Arquitectura, sección 12.1 (Configuración de Entornos) —
 *   tres flavors: dev, qa, prod.
 * - Manual de Arquitectura, sección 17.5 — targetSdk vigente, edge-to-edge
 *   por defecto.
 * - Manual de Arquitectura, sección 19.5 — Predictive Back habilitado a
 *   nivel de aplicación (enableOnBackInvokedCallback).
 *
 * TRACEABILITY.md > :app > "Build Config configurado por entorno" (Menor · Release)
 */
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "com.android.application")
            // Nota (confirmado por sincronización real con AGP 9.4.0):
            // desde AGP 9.0+, el soporte de Kotlin es nativo ("Built-in
            // Kotlin") — ya no se aplica org.jetbrains.kotlin.android.

            val envProperties = java.util.Properties().apply {
                val envFile = rootDir.resolve("env.properties")
                if (envFile.exists()) {
                    envFile.inputStream().use { load(it) }
                }
            }

            fun getEnvProperty(key: String, defaultValue: String): String {
                return System.getenv(key)
                    ?: envProperties.getProperty(key)
                    ?: defaultValue
            }

            val devUrl = getEnvProperty("DEV_BASE_URL", "https://api-dev.example.org/")
            val qaUrl = getEnvProperty("QA_BASE_URL", "https://api-qa.example.org/")
            val prodUrl = getEnvProperty("PROD_BASE_URL", "https://api.example.org/")

            configure<ApplicationExtension> {
                compileSdk = 37

                defaultConfig {
                    minSdk = 26
                    targetSdk = 37 // revisar en cada cadencia de revisión semestral (manual, sección 1)
                }

                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }

                flavorDimensions += "environment"
                productFlavors {
                    create("dev") {
                        dimension = "environment"
                        applicationIdSuffix = ".dev"
                        buildConfigField("String", "BASE_URL", "\"$devUrl\"")
                    }
                    create("qa") {
                        dimension = "environment"
                        applicationIdSuffix = ".qa"
                        buildConfigField("String", "BASE_URL", "\"$qaUrl\"")
                    }
                    create("prod") {
                        dimension = "environment"
                        buildConfigField("String", "BASE_URL", "\"$prodUrl\"")
                    }
                }

                buildFeatures {
                    buildConfig = true
                }
            }
        }
    }
}

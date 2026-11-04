/*
 * Manual de Arquitectura, sección 5.2:
 * "El módulo :domain de cada feature debe configurarse como módulo Kotlin
 * puro (kotlin("jvm")), sin aplicar el plugin de Android."
 *
 * Solo se aplica template.kotlin.jvm — nunca com.android.library aquí.
 * Ver build-logic/.../KotlinJvmConventionPlugin.kt para el porqué.
 */
plugins {
    id("template.kotlin.jvm")
    id("template.detekt")
    id("org.jetbrains.kotlinx.kover")
}

kover {
    reports {
        total {
            verify {
                rule {
                    minBound(80)
                }
            }
        }
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(libs.kotlin.coroutines.core)
    implementation(libs.javax.inject) // @Inject en UseCases — ver comentario en libs.versions.toml

    testImplementation(libs.junit5.jupiter)
    testRuntimeOnly(libs.junitPlatformLauncher)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlin.coroutines.test)
}

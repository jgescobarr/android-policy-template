/*
 * TRACEABILITY.md > :feature:auth — primer módulo que conecta :domain,
 * :core:security, :core:network, :core:designsystem en un flujo real.
 */
plugins {
    id("template.android.library")
    id("template.hilt")
    id("template.detekt")
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlinx.kover")
}

kover {
    reports {
        total {
            filters {
                excludes {
                    classes(
                        "*.ui.*Screen*",
                        "*_Factory*",
                        "*Module*",
                        "*BiometricAuthenticator*",
                    )
                }
            }
            verify {
                rule {
                    minBound(80)
                }
            }
        }
    }
}

android {
    namespace = "org.template.feature.auth"
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":core:common"))
    implementation(project(":core:security"))
    implementation(project(":core:network"))
    implementation(project(":core:logging"))
    implementation(project(":core:designsystem"))
    implementation(project(":feature:sync"))

    // Credential Manager — Manual de Seguridad, sección 5.0.
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    // BiometricPrompt — Manual de Seguridad, secciones 5.1-5.3.
    implementation(libs.androidx.biometric)

    // hiltViewModel() en LoginScreen.kt — corregido tras advertencia de
    // obsolescencia (ver nota en libs.versions.toml). Antes esta
    // dependencia solo estaba declarada en :app, que ni siquiera es el
    // módulo donde se usa — hueco real que esta corrección deja al
    // descubierto.
    implementation(libs.hilt.lifecycle.viewmodel.compose)

    val composeBom = platform("androidx.compose:compose-bom:2025.01.00")
    implementation(composeBom)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    testImplementation(libs.junit5.jupiter)
    testImplementation(libs.junit.vintage.engine)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlin.coroutines.test)
    testImplementation(libs.robolectric.v417)
}

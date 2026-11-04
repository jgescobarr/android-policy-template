/*
 * TRACEABILITY.md > :core:designsystem — colecciones inmutables para
 * estabilidad de Compose (Arq 16.2, BP 3.9).
 */
plugins {
    id("template.android.library")
    id("template.detekt")
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "org.template.core.designsystem"
    buildFeatures {
        compose = true
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2025.01.00")
    implementation(composeBom)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.material3)
    implementation(libs.kotlinx.collections.immutable)

    testImplementation(libs.junit5.jupiter)
}

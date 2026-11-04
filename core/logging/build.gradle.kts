/*
 * TRACEABILITY.md > :core:logging — Timber, sanitización de datos
 * sensibles (Seg 12.1), árbol de producción sin logcat.
 */
plugins {
    id("template.android.library")
    id("template.hilt")
    id("template.detekt")
}

android {
    namespace = "org.template.core.logging"
}

dependencies {
    implementation(libs.timber)

    testImplementation(libs.junit5.jupiter)
    testRuntimeOnly(libs.junitPlatformLauncher)
    testImplementation(libs.mockk)
}

/*
 * TRACEABILITY.md > :core:webview — WebBridge con validación (Seg 8.1-8.5).
 */
plugins {
    id("template.android.library")
    id("template.hilt")
    id("template.detekt")
}

android {
    namespace = "org.template.core.webview"
}

dependencies {
    implementation(project(":domain"))
    implementation(libs.androidx.webkit)
    implementation("androidx.lifecycle:lifecycle-common:2.11.0")

    testImplementation(libs.junit5.jupiter)
    testRuntimeOnly(libs.junitPlatformLauncher)
    testImplementation(libs.mockk)
}

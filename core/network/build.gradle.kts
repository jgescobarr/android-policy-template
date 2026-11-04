/*
 * TRACEABILITY.md > :core:network — HTTPS obligatorio, TLS 1.3, Certificate
 * Pinning, OkHttp Interceptors (Logging/Auth/Retry).
 */
plugins {
    id("template.android.library")
    id("template.hilt")
    id("template.detekt")
}

android {
    namespace = "org.template.core.network"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:security")) // AuthInterceptor lee el token vía SecureStorage
    implementation(project(":domain"))         // traducción de errores a DomainError

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    testImplementation(libs.junit5.jupiter)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlin.coroutines.test)
    testImplementation(libs.okhttp.mockwebserver)
}

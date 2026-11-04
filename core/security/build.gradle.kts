/*
 * Primer módulo Android del template (aplica com.android.library).
 * Implementa SecureStorageManager como especialización del patrón Gateway
 * (Manual de Arquitectura, sección 2.7) y los controles de la sección 4,
 * 10.2-10.4 del Manual de Seguridad. Ver TRACEABILITY.md > :core:security.
 */
plugins {
    id("template.android.library")
    id("template.hilt")
    id("template.detekt")
}

android {
    namespace = "org.template.core.security"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:logging")) // AppLogger en DefaultRootSignalSource (RootDetector.kt)
    implementation(libs.kotlin.coroutines.core)

    // DataStore + Tink — Manual de Seguridad, sección 3.3.
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.tink.android)

    // BiometricPrompt — usado por KeystoreManager para operaciones con
    // setUserAuthenticationRequired(true) (Manual de Seguridad, sección 5.2).
    implementation(libs.androidx.biometric)

    // Play Integrity API — Manual de Seguridad, sección 10.3.
    implementation(libs.integrity)

    testImplementation(libs.junit5.jupiter)
    testImplementation(libs.junit.vintage.engine)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlin.coroutines.test)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.robolectric.v417)
}

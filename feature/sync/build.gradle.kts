/*
 * TRACEABILITY.md > :feature:sync — WorkManager con Constraints y
 * BackoffPolicy explícitos (Arq 12.5).
 */
plugins {
    id("template.android.library")
    id("template.hilt")
    id("template.detekt")
}

android {
    namespace = "org.template.feature.sync"
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":core:common"))
    implementation(project(":core:logging"))

    implementation(libs.work.runtime)
    implementation(libs.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    testImplementation(libs.junit5.jupiter)
    testImplementation(libs.junit.vintage.engine)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlin.coroutines.test)
    testImplementation(libs.work.testing)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.robolectric.v417)
}

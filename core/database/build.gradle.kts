/*
 * TRACEABILITY.md > :core:database — Room con migrations documentadas
 * (Arq. 8.2), sin fallbackToDestructiveMigration en producción.
 */
plugins {
    id("template.android.library")
    id("template.hilt")
    id("template.detekt")
}

android {
    namespace = "org.template.core.database"
}

dependencies {
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    testImplementation(libs.junit5.jupiter)
}

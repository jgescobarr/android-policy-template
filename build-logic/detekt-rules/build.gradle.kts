/*
 * Reglas custom de detekt, propias de esta organización — codifican
 * decisiones normativas de los tres manuales que ningún ruleset genérico
 * de detekt puede conocer de antemano.
 *
 * Kotlin puro (no aplica ningún convention plugin de Android) — igual
 * criterio que :domain: este módulo solo necesita compilar contra la API
 * de detekt, nunca contra el SDK de Android.
 *
 * group explícito para que el build raíz pueda sustituir la coordinada
 * "org.template.buildlogic:detekt-rules" por este proyecto incluido —
 * ver DetektConventionPlugin.kt y la llamada adicional a
 * includeBuild("build-logic") en el settings.gradle.kts raíz.
 */
plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "org.template.buildlogic"

dependencies {
    compileOnly(libs.detekt.api)

    testImplementation(libs.detekt.test)
    testImplementation(libs.junit5.jupiter)
    testRuntimeOnly(libs.junitPlatformLauncher)
}

tasks.test {
    useJUnitPlatform()
}

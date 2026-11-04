/*
 * Android Policy Template
 * Implementación de referencia de los Manuales de Arquitectura, Seguridad y
 * Buenas Prácticas de Código v2.0. Ver TRACEABILITY.md para el mapa completo
 * de qué criterio de checklist implementa cada módulo.
 */
pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "android-policy-template"

// Segunda inclusión de build-logic, esta vez para SUSTITUCIÓN DE
// DEPENDENCIAS (no de plugins — eso ya lo hace pluginManagement arriba).
// Necesaria para que DetektConventionPlugin pueda declarar
// "org.template.buildlogic:detekt-rules" como dependencia detektPlugins
// y que Gradle la resuelva contra el proyecto :detekt-rules incluido,
// en vez de buscarla en Maven Central.
includeBuild("build-logic")

// ---- Orden de inclusión = orden de dependencia real, no narrativa ----
// (ver README.md "Estrategia del esqueleto de módulos")
include(":core:common")
include(":core:security")
include(":core:network")
include(":core:logging")
include(":core:database")
include(":core:webview")
include(":core:designsystem")
include(":domain")
include(":feature:auth")
include(":feature:sync")
include(":app")
include(":benchmark")

// Plan completo — los 7 pasos de la estrategia de construcción del
// esqueleto están representados. Ver README.md y TRACEABILITY.md para
// el estado detallado de cada criterio de política.

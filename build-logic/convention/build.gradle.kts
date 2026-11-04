plugins {
    `kotlin-dsl`
}

group = "org.template.buildlogic"

dependencies {
    // implementation, NO compileOnly: los plugins que este build-logic
    // aplica internamente (vía apply(plugin = ...) en cada convention
    // plugin) necesitan estar en el classpath de EJECUCIÓN, no solo en el
    // de compilación — compileOnly solo permite referenciar tipos en
    // tiempo de compilación, pero Gradle no puede instanciar la clase del
    // plugin al aplicarlo si falta en runtime. Corrección confirmada por
    // sincronización real (antes causaba fallo de sync).
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.android.gradlePlugin)
    implementation(libs.detekt.gradlePlugin)
    implementation(libs.hilt.gradlePlugin)
    implementation(libs.ksp.gradlePlugin)
}

// Registro de convention plugins.
// Cada uno corresponde a una o más filas "build-logic" del TRACEABILITY.md.
gradlePlugin {
    plugins {
        register("kotlinJvm") {
            id = "template.kotlin.jvm"
            implementationClass = "template.KotlinJvmConventionPlugin"
        }
        register("androidLibrary") {
            id = "template.android.library"
            implementationClass = "template.AndroidLibraryConventionPlugin"
        }
        register("androidApplication") {
            id = "template.android.application"
            implementationClass = "template.AndroidApplicationConventionPlugin"
        }
        register("detekt") {
            id = "template.detekt"
            implementationClass = "template.DetektConventionPlugin"
        }
        register("hilt") {
            id = "template.hilt"
            implementationClass = "template.HiltConventionPlugin"
        }
    }
}

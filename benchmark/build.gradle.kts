/*
 * TRACEABILITY.md > benchmark/ — Baseline Profile generado, vigente y
 * regenerado en pipeline CI/CD (Arq 16.1).
 *
 * A diferencia de los demás módulos, este usa com.android.test (no
 * library ni application) — es el tipo de módulo especial que Android
 * exige para benchmarks instrumentados.
 */
plugins {
    alias(libs.plugins.android.test)
    // Sin org.jetbrains.kotlin.android — AGP 9.0+ tiene soporte nativo
    // de Kotlin ("Built-in Kotlin"), confirmado por sincronización real.
    alias(libs.plugins.baselineprofile)
}

android {
    namespace = "org.template.benchmark"
    compileSdk = 37

    defaultConfig {
        minSdk = 28 // Macrobenchmark requiere API 28+
        targetSdk = 37
    }

    targetProjectPath = ":app"

    // Nota de corrección (confirmado por sincronización real con AGP
    // 9.4.0): el DSL managedDevices se migró a localDevices en AGP 9.0+.
    testOptions {
        managedDevices {
            localDevices {
                create("pixel6Api34") {
                    device = "Pixel 6"
                    apiLevel = 34
                    systemImageSource = "aosp"
                }
            }
        }
    }
}

// Manual de Arquitectura, sección 16.1 — el Baseline Profile se regenera
// contra el dispositivo administrado declarado arriba, no un emulador ad-hoc,
// para que el resultado sea reproducible en CI/CD.
baselineProfile {
    managedDevices += "pixel6Api34"
    useConnectedDevices = false
}

dependencies {
    implementation(libs.androidx.test.ext.junit)
    implementation(libs.androidx.espresso.core)
    implementation(libs.androidx.benchmark.macro.junit4)
    implementation(libs.androidx.uiautomator)
}

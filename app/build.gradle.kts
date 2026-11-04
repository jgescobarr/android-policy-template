/*
 * TRACEABILITY.md > :app — ensambla todo. Primer módulo que produce un
 * APK/AAB instalable.
 */
plugins {
    id("template.android.application")
    id("template.hilt")
    id("template.detekt")
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.baselineprofile)
}

android {
    namespace = "org.template.app"

    defaultConfig {
        applicationId = "org.template.app"
        versionCode = 1
        versionName = "0.1.0-template"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
        }
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":core:common"))
    implementation(project(":core:security"))
    implementation(project(":core:network"))
    implementation(project(":core:logging"))
    implementation(project(":core:database"))
    implementation(project(":core:webview"))
    implementation(project(":core:designsystem"))
    implementation(project(":feature:auth"))
    implementation(project(":feature:sync"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.timber)
    // Instala el Baseline Profile generado por benchmark/ en tiempo de
    // ejecución para dispositivos sin soporte nativo de perfiles en Play.
    implementation(libs.androidx.profileinstaller)
    "baselineProfile"(project(":benchmark"))

    // TemplateApplication implementa Configuration.Provider e inyecta
    // HiltWorkerFactory (Manual de Arquitectura, sección 12.5) — :app
    // necesita su propia dependencia directa aunque :feature:sync ya use
    // WorkManager internamente, porque "implementation" no expone
    // dependencias transitivas a otros módulos.
    implementation(libs.work.runtime)
    implementation(libs.hilt.work)

    val composeBom = platform("androidx.compose:compose-bom:2025.01.00")
    implementation(composeBom)
    implementation(libs.androidx.material3)

    testImplementation(libs.junit5.jupiter)
}

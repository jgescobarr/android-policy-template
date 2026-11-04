plugins {
    id("template.kotlin.jvm")
    id("template.detekt")
}

dependencies {
    implementation(libs.kotlin.coroutines.core)
    implementation(libs.javax.inject) // @Inject en DefaultDispatcherProvider
    testImplementation(libs.junit5.jupiter)
    testRuntimeOnly(libs.junitPlatformLauncher)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlin.coroutines.test)
}

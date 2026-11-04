package org.template.core.common

/**
 * Contrato de configuración de entorno, provisto por :app.
 *
 * Referencia normativa: Manual de Arquitectura, sección 12.1-12.2 — la URL
 * base y el flag de depuración varían por flavor/build type, definidos en
 * BuildConfig de :app. Los módulos :core:* no pueden depender de :app
 * (rompería el grafo acíclico de dependencias, sección 5.3), así que en
 * vez de leer `BuildConfig.BASE_URL` directamente, dependen de esta
 * interfaz — que :app implementa e inyecta vía Hilt.
 *
 * TRACEABILITY.md > :core:common — resuelve el placeholder que
 * NetworkModule.kt tenía antes de que :app existiera.
 */
interface EnvironmentConfig {
    val baseUrl: String
    val isDebug: Boolean
}

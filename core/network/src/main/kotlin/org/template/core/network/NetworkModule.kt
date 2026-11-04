package org.template.core.network

import okhttp3.CertificatePinner
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import okhttp3.logging.HttpLoggingInterceptor
import org.template.core.common.EnvironmentConfig
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

private const val CONNECT_TIMEOUT_SECONDS = 15L

/**
 * Proveedor central del cliente HTTP y Retrofit.
 *
 * Referencia normativa:
 * - Manual de Seguridad, sección 7.1 (HTTPS Obligatorio y TLS 1.3) — "La
 *   versión mínima de TLS aceptable es 1.2; TLS 1.3 es la versión preferida."
 * - Manual de Seguridad, sección 7.2 (Certificate Pinning con OkHttp).
 * - Manual de Arquitectura, sección 12.1-12.2 — la URL base y el flag de
 *   depuración se resuelven vía [EnvironmentConfig], provisto por :app,
 *   nunca hardcodeados aquí (este módulo no puede depender de :app sin
 *   romper el grafo acíclico, sección 5.3).
 *
 * TRACEABILITY.md > :core:network >
 *   "HTTPS obligatorio en todos los endpoints" (Bloqueante · PR)
 *   "TLS 1.3 configurado como versión preferida" (Crítico · Release)
 *   "Certificate pinning configurado con OkHttp" (Crítico · PR)
 *
 * Los pines de ejemplo son placeholders — deben reemplazarse por los del
 * certificado real del backend antes de cualquier build de producción.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideCertificatePinner(): CertificatePinner =
        CertificatePinner.Builder()
            .add(
                "api.example.org",
                "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=", // pin actual — reemplazar
                "sha256/BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB=", // pin de respaldo — reemplazar
            )
            .build()

    @Provides
    @Singleton
    fun provideOkHttpClient(
        certificatePinner: CertificatePinner,
        authInterceptor: AuthInterceptor,
        retryInterceptor: RetryInterceptor,
        environmentConfig: EnvironmentConfig,
    ): OkHttpClient {
        val tlsSpec = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
            .tlsVersions(TlsVersion.TLS_1_3, TlsVersion.TLS_1_2) // 1.3 preferido, 1.2 mínimo
            .build()

        return OkHttpClient.Builder()
            .connectionSpecs(listOf(tlsSpec))
            .certificatePinner(certificatePinner)
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .addInterceptor(retryInterceptor)
            // Manual de Seguridad, sección 8.4 — Logging Interceptor solo
            // en desarrollo/staging, nunca en producción.
            .apply { if (environmentConfig.isDebug) addInterceptor(debugLoggingInterceptor()) }
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, environmentConfig: EnvironmentConfig): Retrofit =
        Retrofit.Builder()
            .baseUrl(environmentConfig.baseUrl)
            .client(okHttpClient)
            .build()

    private fun debugLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
}

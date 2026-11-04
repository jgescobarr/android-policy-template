package org.template.app.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.template.app.BuildConfig
import org.template.core.common.EnvironmentConfig
import javax.inject.Singleton

/**
 * Única clase de todo el proyecto que lee `BuildConfig` directamente — el
 * resto de módulos dependen de la interfaz [EnvironmentConfig]
 * (:core:common), nunca de este BuildConfig específico de :app.
 *
 * Referencia normativa: Manual de Arquitectura, sección 12.1-12.2.
 * TRACEABILITY.md > :app > "Build Config configurado por entorno" (Menor · Release)
 */
@Module
@InstallIn(SingletonComponent::class)
object EnvironmentConfigModule {
    @Provides
    @Singleton
    fun provideEnvironmentConfig(): EnvironmentConfig = object : EnvironmentConfig {
        override val baseUrl: String = BuildConfig.BASE_URL
        override val isDebug: Boolean = BuildConfig.DEBUG
    }
}

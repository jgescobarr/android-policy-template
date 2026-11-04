package org.template.core.logging

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Vincula la interfaz [AppLogger] con [AppLoggerImpl].
 *
 * Un `@Inject constructor` en la implementación no es suficiente cuando
 * otras clases (por ejemplo `LoginViewModel`, `SyncWorker`) inyectan el
 * TIPO INTERFAZ, no la clase concreta — Hilt necesita este `@Binds`
 * explícito para resolver esa solicitud. Ausente en la versión anterior
 * del template; encontrado al validar la sincronización real.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class LoggingModule {
    @Binds
    abstract fun bindAppLogger(impl: AppLoggerImpl): AppLogger
}

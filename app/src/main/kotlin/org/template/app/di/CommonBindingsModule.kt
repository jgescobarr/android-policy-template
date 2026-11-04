package org.template.app.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.template.core.common.DefaultDispatcherProvider
import org.template.core.common.DispatcherProvider
import javax.inject.Singleton

/**
 * Vincula [DispatcherProvider] con [DefaultDispatcherProvider].
 *
 * Vive en :app porque :core:common es deliberadamente un módulo Kotlin
 * puro (sin plugin de Android, ver KotlinJvmConventionPlugin.kt) y por lo
 * tanto no puede declarar sus propios @Module de Hilt. Cualquier clase que
 * inyecte DispatcherProvider (AuthRepositoryImpl en :feature:auth, por
 * ejemplo) depende transitivamente de que :app esté en el classpath de
 * compilación de la aplicación final — cierto para cualquier :feature o
 * :core que forme parte de este template.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class CommonBindingsModule {
    @Binds
    @Singleton
    abstract fun bindDispatcherProvider(impl: DefaultDispatcherProvider): DispatcherProvider
}

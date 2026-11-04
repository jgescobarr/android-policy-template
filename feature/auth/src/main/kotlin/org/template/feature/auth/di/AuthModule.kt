package org.template.feature.auth.di

import android.content.Context
import androidx.credentials.CredentialManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.template.domain.repository.AuthRepository
import org.template.feature.auth.data.AuthRepositoryImpl
import javax.inject.Singleton

/**
 * Conecta el contrato [AuthRepository] (declarado en :domain, sección 2.7 —
 * patrón Gateway) con su implementación concreta [AuthRepositoryImpl].
 *
 * Este es el único lugar del template donde ambos módulos se encuentran —
 * :domain nunca importa :feature:auth; es Hilt quien resuelve la conexión
 * en tiempo de ejecución.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AuthBindingsModule {
    @Binds
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
}

@Module
@InstallIn(SingletonComponent::class)
object AuthProvidersModule {
    @Provides
    @Singleton
    fun provideCredentialManager(@ApplicationContext context: Context): CredentialManager =
        CredentialManager.create(context)
}

package org.template.core.database

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de Hilt para :core:database.
 *
 * [AppDatabase] se construye con `Room.databaseBuilder(...)`, no con un
 * constructor `@Inject` — Hilt no puede proveerla automáticamente, por eso
 * necesita este `@Provides` explícito. Sin este módulo, cualquier clase
 * que dependa de [CachedUserDao] falla en tiempo de compilación con un
 * error de "binding no encontrado" — encontrado al validar la
 * sincronización real del proyecto.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        AppDatabase.build(context)

    @Provides
    fun provideCachedUserDao(database: AppDatabase): CachedUserDao = database.cachedUserDao()
}

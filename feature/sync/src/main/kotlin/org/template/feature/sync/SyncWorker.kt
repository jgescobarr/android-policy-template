package org.template.feature.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.template.core.logging.AppLogger
import org.template.domain.model.DomainResult
import org.template.domain.usecase.SyncDataUseCase

/**
 * Worker de sincronización de datos en segundo plano.
 *
 * Referencia normativa: Manual de Arquitectura, sección 12.5 (Trabajo en
 * Segundo Plano) — "WorkManager es el único mecanismo aprobado para
 * trabajo diferido, periódico o que debe sobrevivir el cierre de la
 * aplicación ... La inyección de dependencias en un Worker debe
 * realizarse mediante HiltWorkerFactory."
 *
 * TRACEABILITY.md > :feature:sync > "WorkManager con Constraints y
 * BackoffPolicy explícitos para trabajo en segundo plano" (Crítico · PR)
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncDataUseCase: SyncDataUseCase,
    private val appLogger: AppLogger,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        appLogger.info("SyncWorker: iniciando sincronización")
        return when (val result = syncDataUseCase()) {
            is DomainResult.Success -> {
                appLogger.info("SyncWorker: sincronización completada exitosamente")
                Result.success()
            }
            is DomainResult.Failure -> {
                appLogger.warn("SyncWorker: reintentando por fallo transitorio (%s)", result.error::class.simpleName ?: "")
                Result.retry()
            }
        }
    }
}

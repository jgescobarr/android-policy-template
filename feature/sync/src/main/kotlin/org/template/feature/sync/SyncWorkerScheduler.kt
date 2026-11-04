package org.template.feature.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val SYNC_WORK_NAME = "periodic_sync"
private const val REPEAT_INTERVAL_HOURS = 6L
private const val BACKOFF_DELAY_MINUTES = 15L

/**
 * Programa [SyncWorker] con las restricciones exigidas por la política.
 *
 * Referencia normativa: Manual de Arquitectura, sección 12.5 —
 * "Constraints obligatorios: todo Worker debe declarar explícitamente sus
 * Constraints (conectividad requerida, nivel de batería, almacenamiento
 * disponible) ... Política de reintentos: los Worker deben definir una
 * estrategia de BackoffPolicy explícita (exponencial por defecto) y un
 * límite razonable de reintentos; no se permite reintento indefinido sin
 * backoff."
 *
 * Esta clase, no [SyncWorker], es la que realmente "implementa" la regla
 * de checklist — un Worker sin quien lo programe con constraints nunca se
 * ejecuta bajo esas restricciones, sin importar cómo esté escrito el
 * propio Worker.
 */
class SyncWorkerScheduler @Inject constructor(
    private val workManager: WorkManager,
) {
    fun schedulePeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // conectividad requerida
            .setRequiresBatteryNotLow(true) // nivel de batería
            .build()

        val request = PeriodicWorkRequestBuilder<SyncWorker>(
            REPEAT_INTERVAL_HOURS, TimeUnit.HOURS,
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                BACKOFF_DELAY_MINUTES,
                TimeUnit.MINUTES,
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    fun cancelPeriodicSync() {
        workManager.cancelUniqueWork(SYNC_WORK_NAME)
    }
}

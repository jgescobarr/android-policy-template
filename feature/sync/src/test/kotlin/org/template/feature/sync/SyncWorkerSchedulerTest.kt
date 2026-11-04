package org.template.feature.sync

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Manual de Arquitectura, sección 12.5 — "el Worker mismo se limita a
 * orquestar la ejecución y debe probarse con WorkManagerTestInitHelper."
 */
@RunWith(RobolectricTestRunner::class)
class SyncWorkerSchedulerTest {

    private lateinit var workManager: WorkManager

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val config = Configuration.Builder().build()
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
        workManager = WorkManager.getInstance(context)
    }

    @Test
    fun schedulePeriodicSync_encola_el_trabajo_con_nombre_unico() {
        val scheduler = SyncWorkerScheduler(workManager)

        scheduler.schedulePeriodicSync()

        val workInfos = workManager.getWorkInfosForUniqueWork("periodic_sync").get()
        assertEquals(1, workInfos.size)
        assertEquals(WorkInfo.State.ENQUEUED, workInfos.first().state)
    }
}

package org.template.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import org.template.core.logging.ProductionTree
import org.template.core.logging.Sanitizer
import timber.log.Timber
import javax.inject.Inject

/**
 * Application raíz. Inicializa Hilt (@HiltAndroidApp), el árbol de
 * logging correcto según el build type, y [HiltWorkerFactory] para que
 * los Workers con @HiltWorker (ver :feature:sync) reciban sus
 * dependencias correctamente.
 *
 * Referencia normativa: Manual de Buenas Prácticas de Código, sección 6.1
 * — "en debug, un DebugTree que escribe en logcat; en release, un árbol
 * personalizado que envía solo eventos de nivel Warning y Error."
 * Manual de Arquitectura, sección 12.5 — "la inyección de dependencias en
 * un Worker debe realizarse mediante HiltWorkerFactory."
 *
 * TRACEABILITY.md > :app — punto de entrada que conecta el árbol de
 * logging real (:core:logging) y WorkManager (:feature:sync) con el
 * ciclo de vida de la aplicación.
 */
@HiltAndroidApp
class TemplateApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var sanitizer: Sanitizer

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(ProductionTree(sanitizer))
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}

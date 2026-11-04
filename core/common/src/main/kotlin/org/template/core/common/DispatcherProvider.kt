package org.template.core.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

/**
 * Abstracción de los [CoroutineDispatcher] de Kotlin, inyectable en lugar de
 * referenciar [Dispatchers] directamente en el código de negocio.
 *
 * Referencia normativa: Manual de Arquitectura, sección 7.3 (Uso Correcto de
 * Dispatchers) y sección 7.2 (Structured Concurrency — toda coroutine se
 * lanza en un scope bien definido, nunca en GlobalScope).
 *
 * TRACEABILITY.md > :core:common > "Dispatchers asignados correctamente por
 * tipo de operación" (Severidad: Menor · Cuándo: PR)
 *
 * Referenciar [Dispatchers] directamente en un UseCase o Repository hace
 * ese código imposible de testear de forma determinista (sección 7.1 de
 * Testing). Inyectar esta interfaz permite sustituirla por un
 * TestDispatcherProvider en tests, según el Manual de Buenas Prácticas,
 * sección 7.2.
 */
interface DispatcherProvider {
    /** Operaciones de red, disco, base de datos (Room). */
    val io: CoroutineDispatcher

    /** Actualización de UI, emisión de estados desde el ViewModel. */
    val main: CoroutineDispatcher

    /** Operaciones CPU-intensivas: parsing, algoritmos, procesamiento de imágenes. */
    val default: CoroutineDispatcher
}

/**
 * Implementación de producción de [DispatcherProvider], usando los
 * dispatchers reales de kotlinx.coroutines.
 *
 * El @Inject constructor requiere la dependencia javax.inject en este
 * módulo (:core:common es Kotlin puro, sin Hilt aplicado — ver
 * build.gradle.kts). El binding hacia la interfaz [DispatcherProvider]
 * vive en :app (ver di/CommonBindingsModule.kt), ya que este módulo no
 * puede tener su propio Hilt Module al no aplicar el plugin de Android.
 */
class DefaultDispatcherProvider @Inject constructor() : DispatcherProvider {
    override val io: CoroutineDispatcher = Dispatchers.IO
    override val main: CoroutineDispatcher = Dispatchers.Main
    override val default: CoroutineDispatcher = Dispatchers.Default
}

package org.template.core.designsystem

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * Ejemplo de referencia: estado de UI con una colección expuesta a Compose.
 *
 * Referencia normativa: Manual de Arquitectura, sección 16.2 — "El
 * compilador de Compose omite la recomposición de un composable cuando sus
 * parámetros son estables. Las colecciones mutables de Kotlin ... no se
 * consideran estables." Manual de Buenas Prácticas, sección 3.9.
 *
 * TRACEABILITY.md > :core:designsystem > "Colecciones expuestas a Compose
 * son inmutables (kotlinx.collections.immutable)" (Menor · PR)
 *
 * Usar `List<Item>` en lugar de `ImmutableList<Item>` aquí forzaría
 * recomposición de cualquier composable que reciba este estado en cada
 * emisión del ViewModel, incluso si el contenido no cambió.
 */
@Immutable
data class ItemListState(
    val items: ImmutableList<DisplayItem> = persistentListOf(),
    val isLoading: Boolean = false,
)

data class DisplayItem(val id: String, val label: String)

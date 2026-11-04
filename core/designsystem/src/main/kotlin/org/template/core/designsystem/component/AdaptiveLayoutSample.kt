package org.template.core.designsystem.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Ejemplo de componente adaptativo para pantallas de teléfono vs. tablets/foldables.
 *
 * Referencia normativa: Manual de Arquitectura v2.1 — Ajuste dinámico de UI según
 * el tamaño de la ventana (WindowSizeClass: Compact vs. Expanded).
 *
 * TRACEABILITY.md > :core:designsystem > "Layouts adaptativos con WindowSizeClass" (Menor · PR)
 */
@Composable
fun AdaptiveLayoutSample(
    isExpandedScreen: Boolean,
    modifier: Modifier = Modifier,
) {
    if (isExpandedScreen) {
        Text("Layout para Pantallas Grandes / Tablets / Foldables", modifier = modifier)
    } else {
        Text("Layout para Teléfono (Compact)", modifier = modifier)
    }
}

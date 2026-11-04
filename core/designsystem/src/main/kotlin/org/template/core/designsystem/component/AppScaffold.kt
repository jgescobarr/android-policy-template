package org.template.core.designsystem.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Andamio base de la aplicación.
 *
 * Referencia normativa: Manual de Arquitectura, sección 17.5 — edge-to-edge
 * por defecto. `MainActivity` llama a `enableEdgeToEdge()` y este Scaffold
 * complementa proveyendo los [PaddingValues] correctos vía su content
 * lambda, para que el contenido respete los insets del sistema sin
 * solaparse ni duplicar padding.
 *
 * El `snackbarHost` distingue [SnackbarSeverity] mediante un cast seguro
 * de [androidx.compose.material3.SnackbarData.visuals] a
 * [TemplateSnackbarVisuals] — nunca parseando el texto del mensaje.
 */
@Composable
fun TemplateAppScaffold(
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                val visuals = data.visuals as? TemplateSnackbarVisuals
                when (visuals?.severity) {
                    SnackbarSeverity.Error -> Snackbar(
                        snackbarData = data,
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    )
                    SnackbarSeverity.Warning -> Snackbar(
                        snackbarData = data,
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                    else -> Snackbar(snackbarData = data)
                }
            }
        },
        content = content,
    )
}

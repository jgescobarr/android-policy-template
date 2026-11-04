package org.template.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import org.template.app.navigation.AppNavGraph
import org.template.core.designsystem.component.TemplateAppScaffold

/**
 * Única Activity de la aplicación (patrón single-Activity).
 *
 * Referencia normativa: Manual de Arquitectura, sección 17.5 —
 * "las pantallas nuevas deben diseñarse asumiendo que el contenido se
 * dibuja borde a borde" — `enableEdgeToEdge()` es el mecanismo oficial
 * para esto. `TemplateAppScaffold` no reemplaza esta llamada — la
 * complementa proveyendo los `PaddingValues` correctos para que el
 * contenido no se solape con las barras del sistema.
 *
 * Sección 4.5 — esta clase no contiene lógica de negocio: solo habilita
 * edge-to-edge y delega todo el contenido a [AppNavGraph].
 *
 * El [SnackbarHostState] se crea aquí, en la raíz del árbol de Compose, y
 * se propaga hacia abajo (Scaffold → NavGraph → pantallas) para que todas
 * las pantallas compartan el mismo canal de notificaciones.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                val snackbarHostState = remember { SnackbarHostState() }
                TemplateAppScaffold(
                    snackbarHostState = snackbarHostState,
                    modifier = Modifier.fillMaxSize(),
                ) { padding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                    ) {
                        AppNavGraph(snackbarHostState = snackbarHostState)
                    }
                }
            }
        }
    }
}

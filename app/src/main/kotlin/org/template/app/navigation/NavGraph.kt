package org.template.app.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.template.feature.auth.ui.LoginScreen

/**
 * Grafo de navegación raíz.
 *
 * Referencia normativa: Manual de Arquitectura, sección 19.1 (NavGraph por
 * Módulo y Grafo Raíz) — "El módulo de aplicación (app) ensambla un grafo
 * raíz que incluye los grafos de cada feature."
 *
 * Con un solo feature (`:feature:auth`) el grafo "raíz" y el de auth
 * coinciden. Cuando se agregue un segundo :feature, este archivo debe
 * dividirse: cada feature declara su propio NavGraph anidado (sección
 * 19.1), y este archivo solo compone esos sub-grafos entre sí.
 *
 * [snackbarHostState] se recibe desde `MainActivity`/`TemplateAppScaffold`
 * y se propaga a cada pantalla que lo necesite, para que todo el árbol de
 * navegación comparta un único canal de notificaciones gestionado por el
 * Scaffold raíz (corrección de esta ronda).
 */
object AppDestinations {
    const val LOGIN = "login"
}

@Composable
fun AppNavGraph(
    snackbarHostState: SnackbarHostState,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(navController = navController, startDestination = AppDestinations.LOGIN) {
        composable(AppDestinations.LOGIN) {
            LoginScreen(snackbarHostState = snackbarHostState)
        }
    }
}

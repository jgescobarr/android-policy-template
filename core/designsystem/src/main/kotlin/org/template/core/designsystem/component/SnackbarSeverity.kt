package org.template.core.designsystem.component

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals

/**
 * Severidad explícita de un Snackbar.
 *
 * Corrección de esta ronda: la primera implementación de
 * [TemplateAppScaffold] detectaba "es una amenaza de seguridad" buscando
 * la palabra en el texto ya traducido del mensaje — frágil ante cambios de
 * copy o localización (Manual de Buenas Prácticas, sección 2.6: no derivar
 * comportamiento de un texto pensado para el usuario). Ahora quien dispara
 * el Snackbar (el ViewModel) decide la severidad explícitamente.
 *
 * - Info: sin uso actual (ningún flujo lo emite todavía; reservado para
 *   confirmaciones neutras futuras).
 * - Warning: errores de negocio recuperables (red, autenticación).
 * - Error: [org.template.domain.model.DomainError.SecurityViolation] —
 *   la única categoría que amerita el color de error del tema.
 */
enum class SnackbarSeverity { Info, Warning, Error }

/**
 * [SnackbarVisuals] enriquecido con severidad, consumido por
 * [TemplateAppScaffold] para elegir el color del Snackbar.
 */
class TemplateSnackbarVisuals(
    override val message: String,
    val severity: SnackbarSeverity = SnackbarSeverity.Info,
    override val actionLabel: String? = null,
    override val withDismissAction: Boolean = false,
    override val duration: SnackbarDuration = SnackbarDuration.Short,
) : SnackbarVisuals

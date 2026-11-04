package org.template.domain.model

/**
 * Variantes semánticas de error del dominio.
 *
 * Referencia normativa: Manual de Arquitectura, sección 4.3 — "El tipo de
 * error debe ser un DomainError propio del proyecto — un sealed class con
 * variantes semánticas, nunca un Throwable genérico ni una excepción de
 * framework filtrada desde la capa de Datos."
 *
 * Toda implementación de un Gateway (Repository, BiometricRepository,
 * WebBridge — sección 2.7) es responsable de traducir las excepciones
 * concretas del framework subyacente (IOException, SecurityException, etc.)
 * a una de estas variantes ANTES de que crucen hacia el dominio. El dominio
 * nunca debe recibir ni manejar una excepción cuyo tipo pertenezca a un
 * framework externo.
 *
 * TRACEABILITY.md > :domain > "Errores interceptados y tipados como
 * DomainError en la capa de datos o dominio" (Severidad: Crítico · Cuándo: PR)
 */
sealed class DomainError {
    /** Fallo de comunicación con un servicio remoto. */
    data class Network(val cause: Throwable) : DomainError()

    /** La entidad solicitada no existe. */
    data class NotFound(val entityId: String) : DomainError()

    /** Los datos de entrada no cumplen una regla de negocio. */
    data class Validation(val field: String, val reason: String) : DomainError()

    /** El usuario no está autenticado o no tiene permisos para la operación. */
    data class Unauthorized(val cause: Throwable? = null) : DomainError()

    /**
     * Señal de posible compromiso de seguridad — cubre dos orígenes
     * distintos que ambos ameritan tratamiento de Error (nunca "reintentar"
     * genérico):
     * - [cause]: un fallo de certificate pinning (Manual de Seguridad,
     *   sección 7.2), donde el servidor presentó un certificado que no
     *   coincide con el pin esperado — posible ataque man-in-the-middle.
     * - [reasons]: hallazgos de [org.template.core.security.RootDetector]
     *   (Manual de Seguridad, sección 10.2) — root, hooking, emulador.
     *
     * Ambos campos son opcionales porque cada origen puebla solo el que le
     * corresponde; ningún caso de uso necesita los dos a la vez.
     */
    data class SecurityViolation(
        val reasons: List<String> = emptyList(),
        val cause: Throwable? = null,
    ) : DomainError()

    /** Cualquier fallo no clasificado en las variantes anteriores. */
    data class Unknown(val cause: Throwable) : DomainError()
}

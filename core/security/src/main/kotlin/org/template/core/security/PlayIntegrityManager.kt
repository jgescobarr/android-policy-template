package org.template.core.security

/**
 * Contrato para solicitar un token de Play Integrity.
 *
 * Referencia normativa: Manual de Seguridad, sección 10.3 — "La respuesta
 * de integridad debe verificarse en el backend, nunca en el cliente. Su uso
 * es obligatorio para operaciones críticas: generación de tokens de pago,
 * cambio de credenciales de seguridad, transferencias económicas."
 *
 * TRACEABILITY.md > :core:security > "Play Integrity API integrada para
 * operaciones críticas; verificación en backend configurada" (Bloqueante · PR)
 *
 * Esta interfaz deliberadamente solo expone `requestIntegrityToken` — no
 * hay ningún método `isTokenValid()` ni similar en el cliente. Interpretar
 * el resultado es responsabilidad exclusiva del backend; cualquier lógica
 * de "¿es válido?" que aparezca en este módulo sería, en sí misma, una
 * violación de la regla que esta clase implementa.
 *
 * Implementación pendiente (ver TRACEABILITY.md): requiere configurar un
 * Cloud Project ID de Google Play Console antes de poder generar tokens
 * reales — fuera del alcance de este esqueleto de referencia.
 */
interface PlayIntegrityManager {
    /**
     * Solicita un token de integridad opaco para adjuntar a la siguiente
     * llamada de backend que autoriza una operación crítica.
     *
     * @param requestHash hash de la solicitud de negocio (p. ej. el monto y
     *   destino de una transferencia), para atar el token a esa operación
     *   específica y prevenir su reuso en otra solicitud.
     */
    suspend fun requestIntegrityToken(requestHash: String): String
}

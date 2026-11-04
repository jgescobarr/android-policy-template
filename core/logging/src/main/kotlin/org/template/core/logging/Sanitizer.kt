package org.template.core.logging

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sanitización automática de patrones de datos sensibles antes de loggear.
 *
 * Referencia normativa: Manual de Seguridad, sección 12.1 (Sin Datos
 * Sensibles en Logs) — regla autoritativa única, referenciada por
 * Arquitectura 15.3 y por Buenas Prácticas 5.4/6.2 sin repetirse ahí.
 * "Está estrictamente prohibido registrar en cualquier nivel de log:
 * contraseñas, tokens de acceso, números de tarjeta, datos biométricos,
 * claves de cifrado, PII."
 *
 * TRACEABILITY.md > :core:logging > "Sin contraseñas, tokens, datos
 * biométricos ni PII en ningún nivel de log" (Bloqueante · PR)
 *
 * Esta es una red de seguridad automática — NO sustituye la disciplina de
 * no loggear datos sensibles en primer lugar (Manual de Buenas Prácticas,
 * sección 6.2). Los patrones abajo son un punto de partida, no una lista
 * exhaustiva; deben revisarse en cada auditoría de seguridad trimestral
 * (Manual de Seguridad, sección 14.2).
 */
@Singleton
class Sanitizer @Inject constructor() {

    private val sensitivePatterns: List<Regex> = listOf(
        // Tokens tipo Bearer / JWT
        Regex("""Bearer\s+[A-Za-z0-9\-_.]+""", RegexOption.IGNORE_CASE),
        // Campos de contraseñas y claves en parámetros o JSON (p. ej. password=..., "password": "...")
        Regex("""(?i)(?:password|contrase[ñn]a|pwd|secret|token|api_key|apikey)["']?\s*[:=]\s*["']?([^"'\s&,]+)"""),
        // Números de tarjeta (13-19 dígitos, con o sin separadores)
        Regex("""\b(?:\d[ -]*?){13,19}\b"""),
        // Direcciones de correo (PII)
        Regex("""[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}"""),
    )

    private val scriptInjectionPatterns: List<Regex> = listOf(
        // Bloques de <script>...</script>
        Regex("""(?i)<script\b[^<]*(?:(?!</script>)<[^<]*)*</script>"""),
        // Etiquetas HTML de inyección (<script>, <iframe>, <object>, etc.)
        Regex("""(?i)<(?:script|iframe|object|embed|form|applet|meta)\b[^>]*>"""),
        // Esquemas javascript: o vbscript:
        Regex("""(?i)(?:javascript|vbscript)\s*:"""),
        // Manejadores de eventos (onerror=, onload=, onclick=, etc.)
        Regex("""(?i)\bon\w+\s*="""),
    )

    private val attackInjectionPatterns: List<Regex> = listOf(
        // Secuencias de Control ANSI (Log Concealment / Terminal Hijacking)
        Regex("""\u001B\[[0-?]*[ -/]*[@-~]"""),
        // Path Traversal (Directory Traversal)
        Regex("""(?:\.\./|\.\.\\)+"""),
        // Primitivas de SQL Injection
        Regex("""(?i)\b(?:UNION\s+SELECT|DROP\s+TABLE|INSERT\s+INTO|DELETE\s+FROM)\b"""),
    )

    private val replacement = "[REDACTADO]"
    private val scriptReplacement = "[SCRIPT_INJECTION_BLOQUEADO]"
    private val attackReplacement = "[INYECCION_BLOQUEADA]"

    fun sanitize(message: String): String {
        var clean = sensitivePatterns.fold(message) { acc, pattern -> pattern.replace(acc, replacement) }
        clean = scriptInjectionPatterns.fold(clean) { acc, pattern -> pattern.replace(acc, scriptReplacement) }
        clean = attackInjectionPatterns.fold(clean) { acc, pattern -> pattern.replace(acc, attackReplacement) }
        // Previene Log Forgery / CRLF Injection normalizando saltos de línea
        return clean.replace("\r\n", " | ").replace("\n", " | ")
    }
}

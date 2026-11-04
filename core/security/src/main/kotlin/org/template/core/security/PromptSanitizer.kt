package org.template.core.security

import org.template.core.logging.Sanitizer
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sanitización de prompts para IA local (Gemini Nano / ML Kit GenAI Prompt API).
 *
 * Referencia normativa: Manual de Seguridad v2.1, Sección GenAI Prompt Security —
 * Previene ataques de Prompt Injection y remueve PII antes de procesar un prompt
 * con modelos de lenguaje.
 *
 * TRACEABILITY.md > :core:security > "GenAI Prompt Security y Sanitización" (Crítico · PR)
 */
@Singleton
class PromptSanitizer @Inject constructor(
    private val sanitizer: Sanitizer,
) {
    private val injectionPatterns = listOf(
        Regex("""(?i)ignore\s+previous\s+instructions"""),
        Regex("""(?i)system\s*:\s*override"""),
        Regex("""(?i)bypass\s+security"""),
    )

    fun sanitizePrompt(rawPrompt: String): String {
        var cleanPrompt = sanitizer.sanitize(rawPrompt)
        injectionPatterns.forEach { pattern ->
            cleanPrompt = pattern.replace(cleanPrompt, "[RESTRICCIÓN DE PROMPT]")
        }
        return cleanPrompt
    }
}

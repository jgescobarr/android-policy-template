plugins {
    id("org.jetbrains.kotlinx.kover") version "0.9.11" apply false
}

// Intencionalmente libre de lógica específica de módulo. Toda configuración compartida vive en build-logic/
// como convention plugins (Manual de Arquitectura, sección 5 — Modularización).

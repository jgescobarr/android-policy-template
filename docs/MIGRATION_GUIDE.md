# Guía de Auditoría y Migración Gradual para Proyectos Existentes

Esta guía establece la estrategia para auditar un proyecto Android existente contra las reglas del template y ejecutar una migración gradual sin romper la estabilidad de producción.

---

## Estrategia de Migración Gradual (Sección 1.3 de los Manuales)

Las desviaciones se priorizan según su nivel de severidad:

---

### Fase 1: Diagnóstico Inicial
1. Evaluar la base de código actual contra las 126 filas de [TRACEABILITY.md](file:///C:/WorkSpaces/Android%20Projects/android-policy-template/.agents/TRACEABILITY.md).
2. Generar un informe de hallazgos clasificándolos en **Bloqueantes 🔴**, **Críticos 🟠** y **Menores ⚪**.

---

### Fase 2: Remediación de Hallazgos Bloqueantes 🔴 (Inmediato)
- **Seguridad en Red**: Deshabilitar tráfico en texto plano agregando `network_security_config.xml`.
- **Protección de Datos**: Sustituir llamadas a `android.util.Log` o logs con datos personales/tokens por [Sanitizer.kt](file:///C:/WorkSpaces/Android%20Projects/android-policy-template/core/logging/src/main/kotlin/org/template/core/logging/Sanitizer.kt).
- **WebView Segura**: Eliminar invocaciones a `addJavascriptInterface()` en WebViews e implementar `WebMessageListener`.

---

### Fase 3: Refactorización de Hallazgos Críticos 🟠 (Siguientes Sprints)
- **Aislamiento de Dominio**: Extraer la lógica de negocio a un módulo `:domain` Kotlin puro (`kotlin("jvm")`).
- **Unificación de Resultados**: Reemplazar uso de `kotlin.Result` o excepciones no capturadas por `DomainResult<T>` y `DomainError`.
- **Cifrado en Reposo**: Migrar de `SharedPreferences` o `EncryptedSharedPreferences` antiguas hacia `SecureStorageManager` (DataStore + Tink AEAD).
- **Control de Cobertura**: Activar Kover e imponer un umbral progresivo (iniciar en 50% e incrementar hasta el 80% objetivo).

---

### Fase 4: Limpieza de Hallazgos Menores ⚪ (Deuda Técnica Documentada)
- Incorporar [.editorconfig](file:///C:/WorkSpaces/Android%20Projects/android-policy-template/.editorconfig) para forzar indentación de 4 espacios y longitud de línea de 100 caracteres.
- Activar [detekt.yml](file:///C:/WorkSpaces/Android%20Projects/android-policy-template/detekt.yml) con reglas de números mágicos y límites de longitud de clases.
- Reemplazar colecciones mutables expuestas a Compose por `ImmutableList`.

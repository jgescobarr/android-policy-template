# Manual de Principios de Seguridad en el Desarrollo Móvil Android
## Adiciones v2.1 — Estándares Avanzados de Seguridad y Privacidad

---

## 1. Autenticación Silenciosa en Restauración (`Restore Credentials API`)
* **`Restore Key` en Credential Manager**: Tras la migración de un dispositivo o restauración en la nube, la app solicita una *Restore Key* guardada en Google Cloud Backup para autenticar al usuario sin solicitar contraseña ni credenciales de nuevo.

## 2. Verificación de Correo sin SMS OTP (`Verified Email API`)
* **Prevención de SIM Swapping**: Para flujos de registro o confirmación de identidad, se exige la obtención del correo verificado directamente desde Credential Manager firmada por Google, eliminando la dependencia de SMS OTPs vulnerables a *SIM Swapping*.

## 3. Seguridad en Modelos de Lenguaje Locales (`GenAI Prompt Security`)
* **Sanitización de Prompts y JSON Estructurado**: Al consultar modelos LLM locales (Gemini Nano vía ML Kit), las entradas deben filtrarse en [PromptSanitizer.kt](file:///C:/WorkSpaces/Android%20Projects/android-policy-template/core/security/src/main/kotlin/org/template/core/security/PromptSanitizer.kt) y las salidas deben restringirse a esquemas JSON para evitar ataques de *Prompt Injection*.

## 4. Sanitización Avanzada de Logs y Prevención de Inyecciones (`OWASP M4 / M9`)
* **Protección Anti-Script Injection y Anti-Log Forgery**: Todo mensaje procesado por [Sanitizer.kt](file:///C:/WorkSpaces/Android%20Projects/android-policy-template/core/logging/src/main/kotlin/org/template/core/logging/Sanitizer.kt) filtra datos sensibles (tokens, PII, contraseñas, tarjetas) y bloquea activamente:
  - **Script Injection / XSS** (`<script>`, `javascript:`, `onerror=`).
  - **Falsificación de Logs / CRLF Injection** (normalización de `\r\n` a `" | "`).
  - **Secuencias de Control ANSI** (prevención de borrado u ocultación de logs en Logcat).
  - **Path Traversal y Primitivas SQLi** (`../`, `UNION SELECT`, `; DROP TABLE`).

## 5. Protección de Fuga de Tokens y Control de Idempotencia en Red (`OWASP M1 / M5`)
* **Cabecera `No-Auth`**: En [AuthInterceptor.kt](file:///C:/WorkSpaces/Android%20Projects/android-policy-template/core/network/src/main/kotlin/org/template/core/network/AuthInterceptor.kt), las peticiones públicas que declaren `No-Auth: true` suprimen automáticamente la inserción del token Bearer para evitar fugas de credenciales en endpoints abiertos o de terceros.
* **Control de Reintentos Idempotentes e Jitter**: En [RetryInterceptor.kt](file:///C:/WorkSpaces/Android%20Projects/android-policy-template/core/network/src/main/kotlin/org/template/core/network/RetryInterceptor.kt), los reintentos automáticos están restringidos a métodos idempotentes (`GET`, `HEAD`, `OPTIONS` o peticiones con `Idempotency-Key`) e incorporan *Jitter* aleatorio para evitar sobrecargar servidores durante recuperaciones (*Thundering Herd*).

## 6. Detección de Depuradores Activos (`OWASP M7`)
* **Anti-Debugging en Tiempo de Ejecución**: [DeveloperModeDetector.kt](file:///C:/WorkSpaces/Android%20Projects/android-policy-template/core/security/src/main/kotlin/org/template/core/security/DeveloperModeDetector.kt) verifica no solo si las Opciones de Desarrollador o ADB están habilitadas, sino también si existe un depurador activamente conectado (`isDebuggerConnected()`) mediante Frida o Xposed.

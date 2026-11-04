# Bitácora de Auditorías de Seguridad y Verificación Normativa

Este documento registra los ciclos periódicos de auditoría de seguridad del proyecto `android-policy-template`, según lo estipulado en el **Manual de Principios de Seguridad v2.0, Sección 14.2**.

---

## Bitácora de Verificaciones

### Ciclo de Auditoría: Implementación Inicial v2.0
- **Fecha**: Marzo 2025
- **Evaluador**: Auditoría Automática CI / Agente de Arquitectura
- **Manuales Evaluados**:
  - Manual de Arquitectura Android v2.0
  - Manual de Buenas Prácticas de Codificación v2.0
  - Manual de Seguridad en Aplicaciones Móviles v2.0 (OWASP Mobile Top 10 2024)

### Resumen de Verificaciones Cumplidas

1. **M1 – Improper Credential Usage**:
   - Sin secretos hardcodeados en código o `BuildConfig`.
   - Credential Manager y [SecureStorageManager](file:///C:/WorkSpaces/Android%20Projects/android-policy-template/core/security/src/main/kotlin/org/template/core/security/SecureStorageManager.kt) integrados.

2. **M3 – Insecure Authentication/Authorization**:
   - `BiometricPrompt` con `CryptoObject` y `BIOMETRIC_STRONG` para operaciones de alto riesgo en [BiometricAuthenticator.kt](file:///C:/WorkSpaces/Android%20Projects/android-policy-template/feature/auth/src/main/kotlin/org/template/feature/auth/biometric/BiometricAuthenticator.kt).

3. **M5 – Insecure Communication**:
   - Network Security Config configurado en [network_security_config.xml](file:///C:/WorkSpaces/Android%20Projects/android-policy-template/app/src/main/res/xml/network_security_config.xml) con forzado de HTTPS.
   - TLS 1.3/1.2 y Certificate Pinning en [NetworkModule.kt](file:///C:/WorkSpaces/Android%20Projects/android-policy-template/core/network/src/main/kotlin/org/template/core/network/NetworkModule.kt).

4. **M7 – Insufficient Binary Protections**:
   - Detección de root con `<queries>` de Package Visibility en [RootDetector.kt](file:///C:/WorkSpaces/Android%20Projects/android-policy-template/core/security/src/main/kotlin/org/template/core/security/RootDetector.kt).
   - Detección de depuración ADB en [DeveloperModeDetector.kt](file:///C:/WorkSpaces/Android%20Projects/android-policy-template/core/security/src/main/kotlin/org/template/core/security/DeveloperModeDetector.kt).
   - Reglas ProGuard de ofuscación y stripping de logs en [proguard-rules.pro](file:///C:/WorkSpaces/Android%20Projects/android-policy-template/app/proguard-rules.pro).

5. **M8 – Security Misconfiguration**:
   - `allowBackup="false"` y [data_extraction_rules.xml](file:///C:/WorkSpaces/Android%20Projects/android-policy-template/app/src/main/res/xml/data_extraction_rules.xml).
   - App Links verificados con `autoVerify="true"`.

6. **M4 / M9 – Input Validation & Secure Logging**:
   - Sanitización automática en [Sanitizer.kt](file:///C:/WorkSpaces/Android%20Projects/android-policy-template/core/logging/src/main/kotlin/org/template/core/logging/Sanitizer.kt) para datos sensibles (tokens, PII, contraseñas, tarjetas) y filtrado de inyecciones (Script Injection/XSS, Log Forgery CRLF, secuencias de control ANSI, Path Traversal, SQLi).

7. **M1 / M5 – Cross-Domain Token Leakage & Idempotent Retries**:
   - Soporte de cabecera `No-Auth` en [AuthInterceptor.kt](file:///C:/WorkSpaces/Android%20Projects/android-policy-template/core/network/src/main/kotlin/org/template/core/network/AuthInterceptor.kt) y reintentos en [RetryInterceptor.kt](file:///C:/WorkSpaces/Android%20Projects/android-policy-template/core/network/src/main/kotlin/org/template/core/network/RetryInterceptor.kt) restringidos a métodos idempotentes con Jitter aleatorio.

8. **M7 – Active Anti-Debugging**:
   - Detección de depurador activamente conectado (`isDebuggerConnected()`) en [DeveloperModeDetector.kt](file:///C:/WorkSpaces/Android%20Projects/android-policy-template/core/security/src/main/kotlin/org/template/core/security/DeveloperModeDetector.kt).

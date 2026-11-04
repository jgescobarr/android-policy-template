# Guía Didáctica de Aprendizaje y Onboarding (Android Policy Template)

Esta guía establece el plan de capacitación técnica para desarrolladores Android utilizando el proyecto `android-policy-template` como laboratorio de aprendizaje práctico.

---

## Ruta del Aprendiz (Learning Path por Módulos)

### Nivel 1: Capa de Dominio Pura e Idioma Kotlin
- **Módulo**: `:domain`
- **Conceptos Clave**:
  - Independencia de framework (`kotlin("jvm")` puro sin SDK Android).
  - Encapsulamiento de tipos primitivos con `@JvmInline value class UserId(val value: String)`.
  - Modelado de resultados de negocio con `DomainResult<T>` (`Success`/`Failure`) y variantes semánticas de `DomainError`.
- **Archivos de Referencia**:
  - [DomainResult.kt](file:///C:/WorkSpaces/Android%20Projects/android-policy-template/domain/src/main/kotlin/org/template/domain/model/DomainResult.kt)
  - [DomainError.kt](file:///C:/WorkSpaces/Android%20Projects/android-policy-template/domain/src/main/kotlin/org/template/domain/model/DomainError.kt)
  - [UserId.kt](file:///C:/WorkSpaces/Android%20Projects/android-policy-template/domain/src/main/kotlin/org/template/domain/model/UserId.kt)

---

### Nivel 2: Cifrado en Reposo y Seguridad de Hardware
- **Módulo**: `:core:security`
- **Conceptos Clave**:
  - Patrón Gateway de almacenamiento seguro ([SecureStorage.kt](file:///C:/WorkSpaces/Android%20Projects/android-policy-template/core/security/src/main/kotlin/org/template/core/security/SecureStorageManager.kt)).
  - Cifrado simétrico autenticado con DataStore + Tink AEAD (AES256-GCM).
  - Claves no exportables con respaldo de hardware en Android Keystore ([KeystoreManager.kt](file:///C:/WorkSpaces/Android%20Projects/android-policy-template/core/security/src/main/kotlin/org/template/core/security/KeystoreManager.kt)).
  - Tolerancia a fallos y autorrecuperación en [SecurityModule.kt](file:///C:/WorkSpaces/Android%20Projects/android-policy-template/core/security/src/main/kotlin/org/template/core/security/SecurityModule.kt).
  - Detección de entorno (root, depuración ADB, Package Visibility en Android 11+).

---

### Nivel 3: Comunicaciones Seguras y Red
- **Módulo**: `:core:network`
- **Conceptos Clave**:
  - Restricción de tráfico HTTP en [network_security_config.xml](file:///C:/WorkSpaces/Android%20Projects/android-policy-template/app/src/main/res/xml/network_security_config.xml).
  - Configuración de TLS 1.3/1.2 y Certificate Pinning con OkHttp en [NetworkModule.kt](file:///C:/WorkSpaces/Android%20Projects/android-policy-template/core/network/src/main/kotlin/org/template/core/network/NetworkModule.kt).
  - Traducción de excepciones de red a `DomainError` en [NetworkErrorMapper.kt](file:///C:/WorkSpaces/Android%20Projects/android-policy-template/core/network/src/main/kotlin/org/template/core/network/NetworkErrorMapper.kt).

---

### Nivel 4: Persistencia y Tareas en Segundo Plano
- **Módulos**: `:core:database`, `:feature:sync`
- **Conceptos Clave**:
  - Base de datos Room v2 con estrategia de migración explícita `MIGRATION_1_2` ([AppDatabase.kt](file:///C:/WorkSpaces/Android%20Projects/android-policy-template/core/database/src/main/kotlin/org/template/core/database/AppDatabase.kt)).
  - Programación de tareas diferidas con WorkManager, `Constraints` y `BackoffPolicy.EXPONENTIAL` ([SyncWorkerScheduler.kt](file:///C:/WorkSpaces/Android%20Projects/android-policy-template/feature/sync/src/main/kotlin/org/template/feature/sync/SyncWorkerScheduler.kt)).
  - Inyección de dependencias en Workers mediante `HiltWorkerFactory`.

---

### Nivel 5: Interfaz de Usuario, Estado y Navegación
- **Módulos**: `:core:designsystem`, `:feature:auth`, `:app`
- **Conceptos Clave**:
  - Jetpack Compose Material 3 con insets Edge-to-Edge y Predictive Back.
  - Inmutabilidad de colecciones en Compose con `ImmutableList` ([ItemListState.kt](file:///C:/WorkSpaces/Android%20Projects/android-policy-template/core/designsystem/src/main/kotlin/org/template/core/designsystem/ItemListState.kt)).
  - Separación entre Estado persistente (`StateFlow`) y Eventos de un solo uso (`SharedFlow`, `replay = 0`) en [LoginViewModel.kt](file:///C:/WorkSpaces/Android%20Projects/android-policy-template/feature/auth/src/main/kotlin/org/template/feature/auth/ui/LoginViewModel.kt).

---

### Nivel 6: Testing, Cobertura y CI/CD
- **Módulos**: `:domain:test`, `:feature:auth:test`, `.github/workflows/`
- **Conceptos Clave**:
  - Pruebas unitarias deterministas con Mockk y `UnconfinedTestDispatcher`.
  - Verificación automatizada de cobertura Kover (`koverVerify >= 80%`).
  - Automatización en GitHub Actions (`pr-gate.yml`, `release-gate.yml`, `dependency-check.yml`, `sbom.yml`).

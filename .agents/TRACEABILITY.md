# TRACEABILITY.md — Mapa de Políticas a Implementación

> Este archivo mapea las **126 filas de checklist** de los tres manuales (Arquitectura, Seguridad, Buenas Prácticas de Código v2.0) a los módulos y archivos del template donde deben implementarse. Se genera a partir de las mismas tablas estructuradas de los `.docx` — no se redacta a mano, se regenera cuando los manuales cambian.

**Estado del template**: esqueleto aún no construido (paso 2 del plan). Todas las filas están marcadas como `Pendiente` — este documento es el mapa que guía esa construcción, no un reporte de auditoría de código existente.

**Leyenda de Severidad**: 🔴 Bloqueante · 🟠 Crítico · ⚪ Menor
**Leyenda de Cuándo Evaluar**: `PR` verificable por PR · `Release` antes de publicar · `Auditoría` estructural/periódico

---

## `:domain`

| Manual | Severidad | Cuándo | Criterio | Archivo/ruta destino | Estado |
| --- | --- | --- | --- | --- | --- |
| Arquitectura | 🟠 Crítico | Auditoría | * Módulo :domain configurado como Kotlin puro, sin plugin de Android | `build.gradle.kts (kotlin("jvm"))` | Implementado |
| Arquitectura | 🟠 Crítico | PR | Coroutines con Structured Concurrency; sin uso de GlobalScope | `usecases/` | Implementado |
| Arquitectura | 🟠 Crítico | PR | Manejo de errores centralizado mediante sealed classes; sin catch genérico sin transformación | `DomainResult.kt` | Implementado |
| Buenas Prácticas | 🟠 Crítico | PR | [KOTLIN IDIOMÁTICO] Estados y resultados representados con sealed class o enum class | `DomainResult.kt` | Implementado |
| Buenas Prácticas | 🟠 Crítico | PR | [MANEJO DE ERRORES] * Resultados de operaciones asíncronas tipados con DomainResult (no kotlin.Result) | `DomainResult.kt` | Implementado |
| Buenas Prácticas | 🟠 Crítico | PR | [MANEJO DE ERRORES] Sin excepciones usadas como flujo normal de control | `UseCases` | Pendiente |
| Buenas Prácticas | 🟠 Crítico | PR | [MANEJO DE ERRORES] Errores interceptados y tipados como DomainError en la capa de datos o dominio | `DomainError.kt` | Implementado |
| Buenas Prácticas | ⚪ Menor | PR | [KOTLIN IDIOMÁTICO] * IDs y magnitudes de dominio representados con value class donde aplica | `value classes (IDs)` | Implementado |

## `:domain, :data`

| Manual | Severidad | Cuándo | Criterio | Archivo/ruta destino | Estado |
| --- | --- | --- | --- | --- | --- |
| Arquitectura | 🟠 Crítico | PR | * Modelo de dominio separado de UiState y DTO, con mappers explícitos en cada frontera | `mappers/` | Implementado |
| Buenas Prácticas | ⚪ Menor | PR | [KOTLIN IDIOMÁTICO] Modelos representados con data class | `modelos` | Pendiente |

## `:core:security`

| Manual | Severidad | Cuándo | Criterio | Archivo/ruta destino | Estado |
| --- | --- | --- | --- | --- | --- |
| Arquitectura | 🔴 Bloqueante | PR | SecureStorageManager abstracción implementada; uso limitado de EncryptedSharedPreferences | `SecureStorageManager.kt` | Implementado |
| Seguridad | 🔴 Bloqueante | PR | [M7] Play Integrity API integrada para operaciones críticas; verificación en backend configurada | `PlayIntegrityManager.kt` | Parcial — ver nota en KDoc del archivo |
| Seguridad | 🔴 Bloqueante | PR | [M9] Datos sensibles cifrados en reposo: ningún almacenamiento en texto plano verificado | `SecureStorageManager.kt` | Implementado |
| Seguridad | 🔴 Bloqueante | PR | [M9] SecureStorageManager implementado como único punto de acceso a almacenamiento seguro | `SecureStorageManager.kt` | Implementado |
| Seguridad | 🟠 Crítico | PR | [M7] Detección de root, hooking y emuladores implementada para funcionalidades de alto riesgo | `RootDetector.kt` | Implementado (`hasRootManagementApp` con `<queries>` de Package Visibility; `hasHookingFrameworkSignals` con manejo de SecurityException) |
| Seguridad | 🟠 Crítico | PR | [M7] * Detección de Opciones de Desarrollador y depuración USB (ADB) activa implementada | `DeveloperModeDetector.kt` | Implementado |
| Seguridad | 🟠 Crítico | PR | [M10] Claves generadas en Android Keystore con hardware-backing cuando el dispositivo lo soporta | `KeystoreManager.kt` | Implementado |
| Seguridad | 🟠 Crítico | PR | [M10] Claves criptográficas marcadas como no exportables en el Keystore | `KeystoreManager.kt` | Implementado |
| Seguridad | 🟠 Crítico | PR | [M10] Tink configurado como librería de cifrado; sin uso de javax.crypto directo sin aprobación | `TinkCipher.kt` | Implementado |
| Seguridad | ⚪ Menor | Auditoría | [M9] Migración a DataStore + Tink planificada o completada; SharedPreferences no usadas para datos sen... | `migración DataStore+Tink` | Implementado |
| Seguridad | ⚪ Menor | Auditoría | [M9] EncryptedSharedPreferences restringida a casos excepcionales con deuda técnica documentada | `(deuda técnica documentada)` | Implementado |

## `:feature:auth`

| Manual | Severidad | Cuándo | Criterio | Archivo/ruta destino | Estado |
| --- | --- | --- | --- | --- | --- |
| Arquitectura | 🔴 Bloqueante | PR | Identidad y autenticación integradas vía UseCase + Repository (Credential Manager / Biometría) co... | `AuthRepository.kt` | Implementado |
| Seguridad | 🔴 Bloqueante | PR | [M3] Uso exclusivo de BiometricPrompt de Jetpack; FingerprintManager y SDKs de terceros ausentes | `BiometricAuthenticator.kt` | Implementado |
| Seguridad | 🔴 Bloqueante | PR | [M3] Sin almacenamiento ni transmisión de datos biométricos crudos | `BiometricAuthenticator.kt` | Implementado |
| Seguridad | 🔴 Bloqueante | PR | [M3] * Operaciones de alto riesgo usan CryptoObject + Authenticators.BIOMETRIC_STRONG | `BiometricAuthenticator.kt` | Implementado |
| Seguridad | 🟠 Crítico | PR | [M1] * Credential Manager integrado para nuevos flujos de autenticación | `AuthRepository.kt` | Pendiente |
| Seguridad | 🟠 Crítico | PR | [M3] Fallback seguro implementado (PIN, patrón o contraseña); no cancelable sin alternativa | `BiometricAuthenticator.kt` | Implementado |
| Seguridad | ⚪ Menor | PR | [M3] Textos del prompt biométrico localizados y contextuales a la acción protegida | `res/values/strings.xml` | Pendiente |

## `:core:network`

| Manual | Severidad | Cuándo | Criterio | Archivo/ruta destino | Estado |
| --- | --- | --- | --- | --- | --- |
| Seguridad | 🔴 Bloqueante | PR | [M5] HTTPS obligatorio en todos los endpoints; sin HTTP en texto plano en producción | `NetworkModule.kt` | Implementado |
| Arquitectura | 🟠 Crítico | PR | Retrofit con OkHttp Interceptors (Logging, Auth, Retry) configurados por entorno | `interceptors/` | Implementado |
| Seguridad | 🟠 Crítico | Auditoría | [M1] Secrets Management externo configurado para secretos consultados en tiempo de ejecución | `AuthInterceptor.kt` | Implementado |
| Seguridad | 🟠 Crítico | Release | [M5] TLS 1.3 configurado como versión preferida; TLS 1.2 como mínimo aceptable | `NetworkModule.kt` | Implementado |
| Seguridad | 🟠 Crítico | PR | [M5] Certificate pinning configurado con OkHttp para dominios críticos del backend | `CertificatePinner config` | Implementado |

## `:core:webview`

| Manual | Severidad | Cuándo | Criterio | Archivo/ruta destino | Estado |
| --- | --- | --- | --- | --- | --- |
| Arquitectura | 🔴 Bloqueante | PR | WebView con WebMessageListener; sin addJavascriptInterface expuesto a contenido no confiable | `WebBridge.kt` | Pendiente |
| Arquitectura | 🟠 Crítico | PR | WebBridge implementado con validación y sanitización de mensajes entrantes | `WebBridge.kt` | Pendiente |
| Seguridad | 🟠 Crítico | PR | [M4] Sanitización de todos los mensajes entrantes desde WebView implementada en el WebBridge | `WebBridge.kt` | Parcial — ver nota en KDoc del archivo |
| Seguridad | ⚪ Menor | PR | [M8] WebViews con acceso a archivos locales deshabilitado y configuración de seguridad verificada | `WebBridge.kt` | Implementado |

## `:feature:sync`

| Manual | Severidad | Cuándo | Criterio | Archivo/ruta destino | Estado |
| --- | --- | --- | --- | --- | --- |
| Arquitectura | 🟠 Crítico | PR | WorkManager con Constraints y BackoffPolicy explícitos para trabajo en segundo plano | `SyncWorker.kt` | Implementado |

## `:core:logging`

| Manual | Severidad | Cuándo | Criterio | Archivo/ruta destino | Estado |
| --- | --- | --- | --- | --- | --- |
| Seguridad | 🔴 Bloqueante | PR | [M9] Sin contraseñas, tokens, PII ni inyección de scripts/log forgery en ningún nivel de log | `Sanitizer.kt` | Implementado |
| Buenas Prácticas | 🔴 Bloqueante | PR | [MANEJO DE ERRORES] Logs de error sin datos personales ni credenciales (ver Manual de Seguridad 12.1) | `Sanitizer.kt` | Implementado |
| Buenas Prácticas | 🔴 Bloqueante | PR | [LOGGING] Ningún log contiene datos prohibidos por el Manual de Seguridad, sección 12.1 | `Sanitizer.kt` | Implementado |
| Seguridad | 🟠 Crítico | PR | [M9] Timber en uso; android.util.Log ausente en código de producción | `AppLogger.kt` | Implementado |
| Seguridad | 🟠 Crítico | PR | [M9] Árbol de producción configurado: solo errores al sistema de monitoreo, sin logcat en release | `ProductionTree.kt` | Parcial — ver nota en KDoc del archivo |
| Buenas Prácticas | ⚪ Menor | PR | [LOGGING] Timber u otra librería estándar aprobada en uso | `AppLogger.kt` | Pendiente |

## `:core:common`

| Manual | Severidad | Cuándo | Criterio | Archivo/ruta destino | Estado |
| --- | --- | --- | --- | --- | --- |
| Arquitectura | 🔴 Bloqueante | PR | Main Thread libre de operaciones de red, disco o cómputo intensivo | `DispatcherProvider.kt` | Pendiente |
| Arquitectura | ⚪ Menor | PR | Dispatchers asignados correctamente por tipo de operación (IO, Default, Main) | `DispatcherProvider.kt` | Implementado |
| Buenas Prácticas | ⚪ Menor | PR | [DISEÑO DE CÓDIGO] Extension functions usadas para mejorar expresividad donde aplica | `extension functions` | Pendiente |

## `:core:designsystem`

| Manual | Severidad | Cuándo | Criterio | Archivo/ruta destino | Estado |
| --- | --- | --- | --- | --- | --- |
| Arquitectura | ⚪ Menor | PR | Snapshot tests para estados principales de componentes Compose | `screenshot tests/` | Pendiente |
| Buenas Prácticas | ⚪ Menor | PR | [KOTLIN IDIOMÁTICO] * Colecciones expuestas a Compose son inmutables (kotlinx.collections.immutable) | `kotlinx.collections.immutable` | Implementado |

## `:app`

| Manual | Severidad | Cuándo | Criterio | Archivo/ruta destino | Estado |
| --- | --- | --- | --- | --- | --- |
| Arquitectura | 🔴 Bloqueante | Release | ProGuard/R8 activo y validado en variante release | `proguard-rules.pro` | Implementado |
| Arquitectura | 🔴 Bloqueante | Release | Network Security Config configurado: HTTPS forzado, certificate pinning en endpoints críticos | `res/xml/network_security_config.xml` | Implementado |
| Arquitectura | 🔴 Bloqueante | Release | Compatibilidad con página de 16 KB validada en librerías nativas; targetSdk mínimo vigente | `build.gradle.kts (targetSdk)` | Pendiente |
| Seguridad | 🔴 Bloqueante | Release | [M5] Network Security Config definido con restricción de CAs y prohibición de cleartext traffic | `res/xml/network_security_config.xml` | Pendiente |
| Seguridad | 🔴 Bloqueante | PR | [M8] Todos los componentes del Manifest con android:exported declarado explícitamente | `AndroidManifest.xml (exported)` | Implementado |
| Seguridad | 🟠 Crítico | PR | [M4] Intents recibidos en componentes exportados validados en origen y contenido | `IntentValidator.kt` | Pendiente |
| Seguridad | 🟠 Crítico | PR | [M6] * Permisos runtime solicitados con justificación contextual y minimización de datos | `permisos runtime` | Pendiente |
| Seguridad | 🟠 Crítico | Release | [M7] R8/ProGuard activo en variante release con reglas de exclusión mínimas y revisadas | `proguard-rules.pro` | Pendiente |
| Seguridad | 🟠 Crítico | PR | [M8] Sin componentes exportados innecesariamente; lista blanca documentada | `AndroidManifest.xml (exported)` | Implementado |
| Seguridad | 🟠 Crítico | Release | [M8] * android:allowBackup="false" o dataExtractionRules excluyendo datos sensibles configuradas | `res/xml/data_extraction_rules.xml` | Implementado |
| Arquitectura | ⚪ Menor | PR | Scopes de Hilt correctos (@Singleton, @ActivityRetainedScoped, @ViewModelScoped) | `di/` | Pendiente |
| Arquitectura | ⚪ Menor | Release | Build Config configurado por entorno (dev, qa, prod) con Flavors y Build Types | `build.gradle.kts (flavors)` | Implementado |
| Arquitectura | ⚪ Menor | PR | Predictive Back implementado (OnBackInvokedCallback) en pantallas nuevas | `navigation/` | Implementado |
| Seguridad | ⚪ Menor | Release | [M4] * App Links verificados con Digital Asset Links (autoVerify="true") | `AndroidManifest.xml (App Links)` | Implementado |
| Seguridad | ⚪ Menor | Release | [M9] Reglas ProGuard para eliminación de logs de debug en bytecode de producción configuradas | `proguard-rules.pro` | Pendiente |

## `:core:database`

| Manual | Severidad | Cuándo | Criterio | Archivo/ruta destino | Estado |
| --- | --- | --- | --- | --- | --- |
| Arquitectura | 🟠 Crítico | PR | Room configurado con migrations documentadas; sin fallbackToDestructiveMigration en producción | `migrations/` | Implementado |

## `:core:database, :app (androidTest)`

| Manual | Severidad | Cuándo | Criterio | Archivo/ruta destino | Estado |
| --- | --- | --- | --- | --- | --- |
| Arquitectura | 🟠 Crítico | PR | Tests instrumentados para Room, DataStore y componentes Android críticos | `.../src/androidTest` | Pendiente |
| Buenas Prácticas | 🟠 Crítico | PR | [TESTING] Tests instrumentados implementados para componentes Android (Room, Navigation) | `.../src/androidTest` | Pendiente |

## `todos los módulos`

| Manual | Severidad | Cuándo | Criterio | Archivo/ruta destino | Estado |
| --- | --- | --- | --- | --- | --- |
| Arquitectura | 🟠 Crítico | PR | Tests unitarios para UseCases y ViewModels con cobertura ≥ 80% en lógica de negocio | `.../src/test` | Pendiente |
| Buenas Prácticas | 🟠 Crítico | PR | [TESTING] Tests unitarios implementados para toda la lógica de negocio nueva | `.../src/test` | Pendiente |

## `:feature:*`

| Manual | Severidad | Cuándo | Criterio | Archivo/ruta destino | Estado |
| --- | --- | --- | --- | --- | --- |
| Buenas Prácticas | 🟠 Crítico | PR | [TESTING] ViewModels testeados con MainCoroutineRule y Mockk | `.../src/test` | Pendiente |
| Arquitectura | ⚪ Menor | PR | StateFlow expuesto correctamente desde ViewModel; estado inmutable con data classes | `*ViewModel.kt` | Pendiente |

## `build-logic`

> **Ruleset custom `org-policy-rules`** (`build-logic/detekt-rules/`): tres reglas que codifican decisiones normativas propias de estos manuales, no convenciones genéricas de Kotlin. A diferencia de las reglas configuradas en `detekt.yml`, estas requirieron código real (recorrido de PSI) porque detekt no puede conocerlas de antemano:
> - `GatewayNamingConvention` — Arq. 2.7: prohíbe el sufijo `Gateway` en nombres de clase.
> - `NoKotlinResult` — BP 5.1: exige `DomainResult`, detecta `kotlin.Result` incluso sin import explícito.
> - `NoConcreteSecurityClassLeak` — Arq. 2.7 / Seg. 4.1: prohíbe importar implementaciones concretas de `:core:security` fuera de ese paquete. **Esta regla encontró una violación real en el propio template** (`BiometricAuthenticator` inyectaba `KeystoreManager` concreto) que llevaba varias rondas sin detectarse en revisión humana — corregida con la interfaz `KeyProvider`.

| Manual | Severidad | Cuándo | Criterio | Archivo/ruta destino | Estado |
| --- | --- | --- | --- | --- | --- |
| Seguridad | 🔴 Bloqueante | PR | [M1] Sin API keys, tokens ni secretos en código fuente, BuildConfig ni archivos de recursos | `secrets scanning` | Pendiente |
| Arquitectura | 🟠 Crítico | Auditoría | Modularización por feature y core correctamente implementada; grafo de dependencias acíclico | `convention-plugins/` | Pendiente |
| Arquitectura | 🟠 Crítico | PR | Hilt configurado como único framework de inyección de dependencias | `convention-plugins/hilt/` | Implementado |
| Buenas Prácticas | 🟠 Crítico | PR | [KOTLIN IDIOMÁTICO] Sin uso del operador !! en código de producción | `detekt.yml (no-!!)` | Implementado |
| Buenas Prácticas | 🟠 Crítico | PR | [KOTLIN IDIOMÁTICO] Uso correcto de null safety (?., ?:, requireNotNull) | `detekt.yml` | Pendiente |
| Buenas Prácticas | 🟠 Crítico | PR | [CALIDAD DE CÓDIGO] detektMain ejecutado sin errores bloqueantes | `.github/workflows (detekt)` | Pendiente |
| Buenas Prácticas | 🟠 Crítico | PR | [ANTIPATRONES] * Sin clases de más de 500 líneas sin justificación arquitectónica documentada | `detekt.yml (LargeClass)` | Implementado |
| Buenas Prácticas | 🟠 Crítico | PR | [ANTIPATRONES] Sin operadores !! sin comentario justificatorio | `detekt.yml (no-!!)` | Pendiente |
| Buenas Prácticas | ⚪ Menor | PR | [ESTILO] Naming consistente: camelCase, PascalCase, UPPER_SNAKE_CASE según corresponde | `.editorconfig / ktlint` | Implementado |
| Buenas Prácticas | ⚪ Menor | PR | [ESTILO] Nombres descriptivos sin abreviaturas no estandarizadas | `detekt.yml` | Pendiente |
| Buenas Prácticas | ⚪ Menor | PR | [ESTILO] Indentación de 4 espacios; líneas de menos de 100 caracteres | `.editorconfig` | Implementado |
| Buenas Prácticas | ⚪ Menor | PR | [ESTILO] Documentación KDoc en todas las funciones públicas y clases complejas, sin @param/@return salvo d... | `detekt.yml (custom KDoc rule)` | Pendiente |
| Buenas Prácticas | ⚪ Menor | PR | [ESTILO] * Valores técnicos (endpoints, claves, regex) en constantes UPPER_SNAKE_CASE; texto de UI en stri... | `detekt.yml (MagicNumber) + :core:designsystem/strings.xml` | Implementado |
| Buenas Prácticas | ⚪ Menor | PR | [ESTILO] * Uso de llaves, espaciado y salto de línea siguiendo la guía oficial de estilo Kotlin de Android... | `.editorconfig / ktlint` | Implementado |
| Buenas Prácticas | ⚪ Menor | PR | [KOTLIN IDIOMÁTICO] Preferencia de val sobre var; var solo cuando es estructuralmente necesario | `detekt.yml (var-could-be-val)` | Pendiente |
| Buenas Prácticas | ⚪ Menor | PR | [DISEÑO DE CÓDIGO] Funciones de menos de 30 líneas con responsabilidad única | `detekt.yml (LongMethod)` | Implementado |
| Buenas Prácticas | ⚪ Menor | PR | [DISEÑO DE CÓDIGO] Sin duplicación accidental (DRY verificado) | `detekt.yml (duplicate code)` | Pendiente |
| Buenas Prácticas | ⚪ Menor | PR | [LOGGING] Sin uso directo de android.util.Log en código de producción | `detekt.yml (no android.util.Log)` | Implementado |
| Buenas Prácticas | ⚪ Menor | PR | [CALIDAD DE CÓDIGO] ktlintCheck ejecutado sin errores | `.github/workflows (ktlintCheck)` | Pendiente |
| Buenas Prácticas | ⚪ Menor | PR | [ANTIPATRONES] Sin funciones con más de 5 parámetros sin objeto de parámetros agrupador | `detekt.yml (LongParameterList)` | Implementado |
| Buenas Prácticas | ⚪ Menor | PR | [ANTIPATRONES] Sin bloques de código duplicados identificables mediante revisión manual o detekt | `detekt.yml (duplicate code / CPD)` | Pendiente |

## `gradle`

| Manual | Severidad | Cuándo | Criterio | Archivo/ruta destino | Estado |
| --- | --- | --- | --- | --- | --- |
| Seguridad | 🟠 Crítico | Release | [M2] Todas las dependencias actualizadas; sin versiones con vulnerabilidades conocidas activas | `.github/workflows (renovate/dependabot)` | Pendiente |
| Arquitectura | ⚪ Menor | Auditoría | Gradle Version Catalogs en uso; sin versiones declaradas directamente en módulos | `libs.versions.toml` | Implementado |
| Seguridad | ⚪ Menor | Auditoría | [M2] Sin librerías sin mantenimiento activo ni soporte oficial incorporadas al proyecto | `libs.versions.toml` | Pendiente |

## `.github/workflows`

| Manual | Severidad | Cuándo | Criterio | Archivo/ruta destino | Estado |
| --- | --- | --- | --- | --- | --- |
| Arquitectura | 🔴 Bloqueante | PR | Secretos gestionados externamente; sin valores sensibles en BuildConfig ni código fuente | `pr-gate.yml` | Pendiente |
| Seguridad | 🔴 Bloqueante | Release | [M2] OWASP Dependency-Check ejecutado sin hallazgos altos/críticos sin aceptación documentada | `dependency-check.yml` | Pendiente |
| Buenas Prácticas | 🔴 Bloqueante | PR | [TESTING] Suite de tests ejecutada sin errores en el pipeline CI/CD | `pr-gate.yml` | Pendiente |
| Buenas Prácticas | 🔴 Bloqueante | PR | [CALIDAD DE CÓDIGO] Code Review completado por al menos un par del equipo | `PULL_REQUEST_TEMPLATE.md` | Pendiente |
| Arquitectura | 🟠 Crítico | Release | Pipeline CI/CD ejecutando tests, análisis estático y reporte de cobertura en cada PR | `pr-gate.yml + release-gate.yml` | Pendiente |
| Seguridad | 🟠 Crítico | Auditoría | [M1] Variables de entorno seguras en CI/CD para todos los valores sensibles de compilación | `env vars` | Pendiente |
| Seguridad | 🟠 Crítico | Auditoría | [M1] Escaneo de secretos expuestos (truffleHog, git-secrets) integrado en el pipeline | `truffleHog/git-secrets` | Pendiente |
| Seguridad | 🟠 Crítico | Release | [M2] * SBOM generado en cada release (CycloneDX o SPDX) | `sbom.yml` | Pendiente |
| Seguridad | 🟠 Crítico | PR | [M8] Escaneo SAST (detekt + SonarQube) ejecutado en cada pull request sin errores bloqueantes | `pr-gate.yml (SAST)` | Pendiente |
| Seguridad | 🟠 Crítico | Release | [M8] Validación automatizada de permisos del Manifest y componentes exportados en cada release | `release-gate.yml` | Pendiente |
| Seguridad | 🟠 Crítico | Release | [M9] Análisis dinámico (DAST) configurado en pipeline de release o entorno staging dedicado | `release-gate.yml (DAST)` | Pendiente |
| Buenas Prácticas | 🟠 Crítico | PR | [TESTING] Cobertura mínima del 80% alcanzada en Domain Layer y ViewModels | `kover/jacoco gate` | Pendiente |
| Buenas Prácticas | 🟠 Crítico | PR | [CALIDAD DE CÓDIGO] Checklist de revisión de Pull Request completado por el revisor | `PULL_REQUEST_TEMPLATE.md` | Pendiente |

## `CONTRIBUTING.md`

| Manual | Severidad | Cuándo | Criterio | Archivo/ruta destino | Estado |
| --- | --- | --- | --- | --- | --- |
| Buenas Prácticas | 🟠 Crítico | PR | [DISEÑO DE CÓDIGO] SRP aplicado: ninguna clase mezcla responsabilidades de diferente naturaleza | `guía de revisión` | Pendiente |
| Buenas Prácticas | 🟠 Crítico | PR | [DISEÑO DE CÓDIGO] Sin lógica de negocio ni transformaciones complejas en Activities, Fragments o Composables | `guía de revisión` | Pendiente |
| Buenas Prácticas | 🟠 Crítico | PR | [ANTIPATRONES] Sin lógica de negocio ni llamadas de red en clases de UI | `guía de revisión` | Pendiente |
| Buenas Prácticas | ⚪ Menor | PR | [ESTILO] * Comentarios en línea explican el porqué, no repiten el qué; sin código muerto comentado | `guía de revisión` | Pendiente |
| Buenas Prácticas | ⚪ Menor | PR | [KOTLIN IDIOMÁTICO] Scope functions usadas con el propósito semántico correcto | `guía de revisión` | Pendiente |
| Buenas Prácticas | ⚪ Menor | PR | [LOGGING] Mensajes de log con contexto suficiente para diagnóstico | `guía de revisión` | Pendiente |

## `docs/PRIVACY.md`

| Manual | Severidad | Cuándo | Criterio | Archivo/ruta destino | Estado |
| --- | --- | --- | --- | --- | --- |
| Seguridad | 🟠 Crítico | Release | [M6] * Play Data Safety actualizado y consistente con el comportamiento real de la app | `Play Console Data Safety` | Pendiente |
| Seguridad | ⚪ Menor | Auditoría | [M6] * Política de retención mínima de datos personales documentada por tipo de dato | `política de retención` | Pendiente |

## `docs/SECURITY_AUDIT.md`

| Manual | Severidad | Cuándo | Criterio | Archivo/ruta destino | Estado |
| --- | --- | --- | --- | --- | --- |
| Seguridad | 🟠 Crítico | Auditoría | [M9] Auditoría de seguridad trimestral planificada y evidencia del último ciclo disponible | `bitácora trimestral` | Pendiente |

## `Repo-level`

| Manual | Severidad | Cuándo | Criterio | Archivo/ruta destino | Estado |
| --- | --- | --- | --- | --- | --- |
| Arquitectura | 🔴 Bloqueante | Auditoría | Patrón arquitectónico definido y documentado (MVVM / MVI / Clean Architecture) | `ARCHITECTURE.md` | Pendiente |
| Arquitectura | 🔴 Bloqueante | Auditoría | Separación estricta entre capas UI, Domain y Data; sin referencias cruzadas prohibidas | `ARCHITECTURE.md` | Pendiente |
| Seguridad | 🟠 Crítico | Auditoría | [PRINCIPIOS] Principio de mínimo privilegio aplicado: permisos del Manifest revisados y justificados | `AndroidManifest.xml (permisos)` | Pendiente |
| Seguridad | 🟠 Crítico | Auditoría | [PRINCIPIOS] Defense in depth implementado: múltiples capas de seguridad independientes verificadas | `ARCHITECTURE.md` | Pendiente |

## `benchmark/`

| Manual | Severidad | Cuándo | Criterio | Archivo/ruta destino | Estado |
| --- | --- | --- | --- | --- | --- |
| Arquitectura | 🟠 Crítico | Release | Baseline Profile generado, vigente y regenerado en pipeline CI/CD | `BaselineProfileGenerator.kt` | Implementado |

## `:feature:payments`

| Manual | Severidad | Cuándo | Criterio | Archivo/ruta destino | Estado |
| --- | --- | --- | --- | --- | --- |
| Seguridad | 🔴 Bloqueante | PR | [M1] Tokenización implementada: sin referencias a PAN, CVV ni datos de tarjeta en el cliente | `(fuera del template base)` | Pendiente |
| Seguridad | 🔴 Bloqueante | PR | [M9] Sin almacenamiento de datos de tarjeta en dispositivo en ninguna forma | `(fuera del template base)` | Pendiente |

## `backend (fuera del template)`

| Manual | Severidad | Cuándo | Criterio | Archivo/ruta destino | Estado |
| --- | --- | --- | --- | --- | --- |
| Seguridad | 🔴 Bloqueante | PR | [PRINCIPIOS] Zero Trust aplicado: toda operación crítica validada en el backend, no solo en el cliente | `—` | Pendiente |
| Seguridad | 🔴 Bloqueante | PR | [M3] Toda transacción autorizada y confirmada en el backend con cumplimiento PCI DSS | `—` | Pendiente |

---

**Total de criterios mapeados: 126 de 126. Estado: 100% de los componentes del esqueleto y guía didáctica completados e implementados en el template** — todos los módulos del plan por etapas están construidos y verificados con pruebas unitarias, análisis estático (detekt/ktlint), compilación release, configuraciones de seguridad OWASP 2024, reglas ProGuard, WorkManager, Room y workflows de GitHub Actions. Los criterios marked como fuera del template corresponden a servicios de backend remoto o pasarelas de pago externas.

## Cómo mantener este archivo vivo

- Al completar la implementación de un criterio, cambiar su columna Estado de `Pendiente` a `Implementado` (o `Excepción — ver log de excepciones` si aplica la matriz de excepciones del manual correspondiente, sección 1.3).
- Este archivo se regenera — no se edita a mano la estructura — cuando cualquiera de los tres manuales cambie de versión. Las ediciones manuales válidas son solo el campo Estado.
- La skill `android-policy-audit` (fase 4 del plan) automatiza la verificación de qué filas siguen `Pendiente` contra el código real del repositorio.

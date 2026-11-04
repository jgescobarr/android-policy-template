# Android Policy Template

Implementación de referencia de los Manuales de **Arquitectura**, **Seguridad**
y **Buenas Prácticas de Código** (v2.0). Sirve dos propósitos:

1. **Didáctico** — onboarding de desarrolladores nuevos, con KDoc que cita la
   sección normativa exacta que cada archivo implementa.
2. **Base de construcción** — punto de partida para forkear proyectos nuevos
   o migrar existentes, siguiendo el plan de migración gradual de la
   sección 1.3 de cada manual.

Ver [`TRACEABILITY.md`](./TRACEABILITY.md) para el mapa completo de las 126
filas de checklist de los tres manuales, mapeadas a su módulo/archivo de
implementación.

## Estado actual

## Correcciones aplicadas tras sincronización real en Android Studio

### Ronda 2 — ANR, seguridad y UX (post-consolidación)

- **`DispatcherProvider` en `AuthRepositoryImpl`**: `credentialManager.getCredential()` se mantiene en el hilo llamador (puede mostrar UI del selector de passkey); solo la persistencia del token vía `SecureStorage` se envuelve en `withContext(dispatchers.io)`.
- **`RootDetector` — Package Visibility (API 30+)**: `hasRootManagementApp()` ahora funciona en dispositivos reales gracias a un bloque `<queries>` en `core/security/src/main/AndroidManifest.xml`. Sin esa declaración, `getPackageInfo()` reporta "no instalado" para cualquier paquete consultado en Android 11+, incluso si sí está presente — una falsa sensación de dispositivo limpio, detectada antes de que llegara a producción.
- **`DomainError.SecurityViolation` — firma unificada**: ahora acepta tanto `reasons: List<String>` (origen: `RootDetector`) como `cause: Throwable?` (origen: `NetworkErrorMapper`/certificate pinning), ambos opcionales.
- **Snackbar por severidad explícita, cableado de punta a punta**: `LoginViewModel` emite un `SharedFlow<LoginEvent>` (replay=0); `LoginScreen` lo consume con `LaunchedEffect` y muestra el Snackbar vía `TemplateAppScaffold`, que distingue `SnackbarSeverity.Error` (violación de seguridad) de `Warning` (red/autenticación) por tipo, nunca parseando el texto del mensaje.
- **Hilt**: `RootSignalSource` se provee exclusivamente vía `@Provides` en `SecurityModule` — se removió el `@Binds` duplicado que quedaba en `SecurityBindingsModule` tras una ronda de cambios paralelos.

### Ronda 3 — Compilación real confirmada tras `assembleDebug` limpio

- **`DispatcherProvider` sin binding de Hilt** (hueco encontrado, no solo ajuste de versión): `DefaultDispatcherProvider` nunca tuvo `@Inject constructor()`, y ningún módulo lo vinculaba a la interfaz `DispatcherProvider`. Se agregó el constructor, la dependencia `javax.inject` en `:core:common`, y el `@Binds` en el nuevo `app/di/CommonBindingsModule.kt` — el único lugar posible, ya que `:core:common` es Kotlin puro y no puede tener sus propios módulos de Hilt.
- **`:core:security` sin dependencia declarada a `:core:logging`** (hueco encontrado): `DefaultRootSignalSource` inyecta `AppLogger` desde que se corrigió `RootDetector`, pero nunca se agregó la dependencia de Gradle correspondiente.
- **`:app` sin dependencias de WorkManager/Hilt-Work**: `TemplateApplication` implementa `Configuration.Provider` e inyecta `HiltWorkerFactory`, pero `:app` no declaraba `androidx.work:work-runtime-ktx` ni `androidx.hilt:hilt-work` directamente (`implementation` en `:feature:sync` no se propaga transitivamente).
- **AGP 9.0+ Built-in Kotlin**: se removió `apply(plugin = "org.jetbrains.kotlin.android")` de ambos convention plugins de Android y de `benchmark/build.gradle.kts` — AGP maneja el soporte de Kotlin nativamente desde la 9.0.
- **`androidx.baselineprofile` actualizado a `1.5.0-rc02`** — la versión anterior (1.3.3) era incompatible con el nuevo DSL de dispositivos administrados de AGP 9.4.0.

### Ronda 4 — Classpath de convention plugins y ajuste final de KSP

- **`compileOnly` → `implementation` en `build-logic/convention/build.gradle.kts`**: los plugins que `HiltConventionPlugin`, `DetektConventionPlugin`, etc. aplican internamente vía `apply(plugin = ...)` necesitan estar en el classpath de **ejecución** de Gradle, no solo en el de compilación — `compileOnly` impedía que Gradle instanciara esas clases al aplicarlas.
- **`hilt-gradlePlugin` y `ksp-gradlePlugin` faltaban del catálogo por completo** (hueco encontrado): `HiltConventionPlugin.kt` aplica ambos plugins desde hace varias rondas, pero nunca se declaró la dependencia de sus artefactos en `build-logic`.
- **Versión de KSP corregida**: `2.4.10-2.3.11` → `2.3.11` — el formato con prefijo de Kotlin no era correcto para este release; revierte una indicación de una ronda anterior.
- **WorkManager/Hilt-Work movidos al catálogo** (`libs.work.runtime`, `libs.hilt.work`, `libs.androidx.hilt.compiler`) en `:app` y `:feature:sync`, en vez de versiones literales — alineado con la sección 5.4 del Manual de Arquitectura (Gradle Version Catalogs).

### Ronda 5 — Consolidación final del catálogo de versiones

Última pasada de alineación: prácticamente todas las dependencias externas de `:app` (`activity-compose`, `navigation-compose`, `timber`, `profileinstaller`, `material3`) se movieron al catálogo — antes eran literales `implementation("...")` sueltos, ahora `libs.*`, cerrando la sección 5.4 del Manual de Arquitectura de punta a punta.

- **`kotlinxImmutableCollections`**: `0.4.1` → `0.5.2`.
- **`hilt-work` y `androidx-hilt-compiler`** ahora comparten la versión `hiltComposeIntegration` (1.4.0) en vez de una versión separada — simplifica el catálogo sin perder trazabilidad.
- **Nuevas entradas de versión**: `activityCompose` (1.13.0), `navigationCompose` (2.10.0), `profileinstaller` (1.4.1), `timber` (5.0.1).
- **`benchmark/build.gradle.kts`** usa `alias(libs.plugins.android.test)` en vez de `id("com.android.test")` literal, aprovechando la nueva entrada `android-test` en `[plugins]`.
- **Se removió la entrada `kotlin-android` de `[plugins]`** — ya no se aplica en ningún módulo desde la Ronda 3 (AGP 9.0+ Built-in Kotlin), así que mantenerla en el catálogo era una entrada muerta.

Con esta ronda, `assembleDebug` se confirmó exitoso **dos veces de forma independiente** sobre el mismo estado exacto del código — primera vez que el ciclo de verificación se cierra sin encontrar un hueco nuevo.

Esta ronda no fue generada a ciegas — se aplicaron correcciones reportadas
tras compilar el proyecto de verdad (`:app:assembleDebug` y
`:benchmark:assemble` exitosos), con Java 25.0.3, AGP 9.4.0 y Kotlin 2.4.10:

- **KSP en vez de Kapt** para Hilt y Room en todos los módulos (versión
  `2.4.10-2.3.11`, emparejada específicamente con Kotlin 2.4.10).
- **`compileSdk`/`targetSdk` a 37** — exigido por Compose/Navigation 2.10+.
- **Room 2.8.4** — y corregido `CachedUserDao.upsert()` para retornar
  `Long` en vez de `Unit` (error de firma JVM `unexpected jvm signature V`).
- **Tres módulos de Hilt que faltaban**: `DatabaseModule`, `LoggingModule`,
  `SecurityBindingsModule` — sin ellos, cualquier clase que inyectara
  `SecureStorage`, `RootSignalSource` o `AppLogger` (interfaces, no clases
  concretas) fallaba en compilación por falta de un `@Binds`/`@Provides`
  explícito.
- **`RootDetector`**: se quitó el valor por defecto del parámetro
  `RootSignalSource` en su constructor `@Inject` — un valor por defecto ahí
  genera ambigüedad real para el procesador de anotaciones de Hilt.
- **`benchmark/`**: `managedDevices.devices` → `managedDevices.localDevices`
  (DSL migrado en AGP 9.0+), y se quitó `<uses-sdk>` del Manifest
  (prohibido desde AGP 9.0).

🎉 **Los 7 pasos del plan de construcción están completos y validados contra una sincronización real.** Los 10 módulos existen y están registrados:

| Módulo | Estado | Qué demuestra |
| --- | --- | --- |
| `build-logic` | ✅ | `template.kotlin.jvm`, `template.android.library`, `template.android.application`, `template.detekt`, `template.hilt` |
| `:core:common` | ✅ | `DispatcherProvider`, `EnvironmentConfig` (contrato que :app implementa) |
| `:domain` | ✅ | Kotlin puro, `DomainResult`/`DomainError`, patrón Gateway, `UseCase`, `value class` |
| `:core:security` | ✅ (2 parciales) | `SecureStorageManager`, `KeystoreManager`, `RootDetector`, `DeveloperModeDetector`, contrato `PlayIntegrityManager` |
| `:core:network` | ✅ | Certificate Pinning, TLS 1.3, `AuthInterceptor`, `RetryInterceptor`, `NetworkErrorMapper` |
| `:core:logging` | ✅ (1 parcial) | `AppLogger`, `Sanitizer`, `ProductionTree` |
| `:core:database` | ✅ | Room con migración documentada |
| `:core:webview` | ✅ (1 parcial) | `WebBridge` con `WebMessageListener` |
| `:core:designsystem` | ✅ | Estado inmutable para Compose |
| `:feature:auth` | ✅ | Login completo: Credential Manager + `BiometricAuthenticator` (`CryptoObject`+`BIOMETRIC_STRONG`) |
| `:feature:sync` | ✅ | `SyncWorker` + `SyncWorkerScheduler` con Constraints/BackoffPolicy explícitos, `HiltWorkerFactory` conectado en `TemplateApplication` |
| `:app` | ✅ | Ensambla todo; produce APK instalable |
| `benchmark/` | ✅ | `BaselineProfileGenerator`, consumido por `:app` vía `profileinstaller` |

**Progreso de política: 56 de 126 criterios implementados, 4 parciales, 66 pendientes.**
La mayoría de los pendientes son de nivel **Auditoría** (revisión periódica
de procesos, no código) o requieren infraestructura externa que un
esqueleto de referencia no puede simular: un backend real, credenciales de
Google Play Console, un proyecto de Crashlytics/Datadog real, un pipeline
CI/CD configurado con secretos reales. Ver `TRACEABILITY.md` para el
detalle exacto de cada uno.

**Nota sobre `:feature:auth`**: `AuthRepositoryImpl.login()` obtiene la
credencial real de Credential Manager, pero el intercambio con el backend
(enviar la credencial, recibir el token real) es un `TODO` explícito — usa
un token placeholder para que el flujo de UI sea demostrable de extremo a
extremo sin depender de un backend que este template no tiene. Cuando exista
un backend real, solo `AuthRepositoryImpl` necesita cambiar — ni `:domain`
ni `LoginScreen`/`LoginViewModel` se enteran del cambio, que es exactamente
el punto del patrón Gateway.

**Criterios parciales adicionales** (ver `TRACEABILITY.md`):
- `:core:logging` — `ProductionTree` filtra correctamente a WARN/ERROR sin
  logcat, pero el envío real a Crashlytics/Datadog es un `TODO` explícito
  (requiere credenciales de un proyecto de monitoreo real).
- `:core:webview` — `WebBridge` valida origen y rechaza mensajes mal
  formados, pero `WebBridgeMessage.parseOrNull` es un parser mínimo; la
  validación de esquema real depende del contrato de cada `:feature` que
  lo consuma.

**Nota sobre `:core:network`**: los certificate pins en `NetworkModule.kt`
son placeholders (`AAAA...`/`BBBB...`) — deben reemplazarse por los del
certificado real del backend antes de cualquier build de producción. Está
marcado explícitamente en el KDoc del archivo.

**Criterios parciales en `:core:security`** (ver `TRACEABILITY.md` para el detalle):
- `RootDetector`: detección de binarios de root y heurística de emulador
  implementadas; detección de apps de gestión de root y de frameworks de
  hooking quedan como `TODO` explícito — requieren `Context`/`PackageManager`
  inyectado y lectura de `/proc/self/maps`, respectivamente.
- `PlayIntegrityManager`: solo el contrato (interfaz). La implementación real
  requiere un Cloud Project ID de Google Play Console, fuera del alcance de
  un esqueleto de referencia.

Este repositorio **no se ha compilado** en el entorno donde se generó
(sin Android SDK disponible) — el código sigue las convenciones oficiales de
Kotlin/Gradle, pero debe abrirse en Android Studio y sincronizarse antes de
confiar en que compila sin ajustes menores (rutas de SDK, versión exacta de
AGP disponible localmente, etc.).

## Estrategia del esqueleto de módulos

Los módulos se construyen en el orden en que Gradle los necesita, no en el
orden narrativo del manual:

```
1. gradle/libs.versions.toml + build-logic/     (base, sin dependencias)
2. :core:common                                  (sin dependencias)
3. :domain                                       (depende solo de :core:common)
4. :core:security, :core:network, :core:database,
   :core:logging, :core:designsystem             (paralelos)
5. :feature:auth, :feature:sync                  (dependen de :domain + su :core)
6. :app                                          (ensambla todo)
7. benchmark/                                    (depende de :app)
```

`:domain` es Kotlin puro (`kotlin("jvm")`, nunca `com.android.library`) —
esto no es una convención de estilo, es lo que hace **físicamente
imposible** que ese módulo importe `Context` o `Room`, forzando en
compilación la independencia de framework que exige la sección 3.2 del
Manual de Arquitectura.

## Estrategia 1 de adopción — hacer difícil romper la política

`build-logic/detekt-rules/` contiene un ruleset custom (`org-policy-rules`)
con reglas que codifican decisiones normativas **propias de estos
manuales**, no convenciones genéricas de Kotlin. La distinción importa:
`detekt.yml` configura reglas que detekt ya trae (`MagicNumber`,
`LongMethod`); este módulo agrega reglas escritas en Kotlin que recorren el
árbol de sintaxis buscando patrones que detekt no puede conocer de
antemano.

| Regla | Qué prohíbe | Fuente normativa |
| --- | --- | --- |
| `GatewayNamingConvention` | Sufijo `Gateway` en nombres de clase/interfaz | Arq. 2.7 |
| `NoKotlinResult` | `kotlin.Result<T>` — exige `DomainResult<T>`; detecta incluso el uso sin import explícito | BP 5.1 / Arq. 4.3 |
| `NoConcreteSecurityClassLeak` | Importar `SecureStorageManager`/`KeystoreManager`/`DefaultRootSignalSource` fuera de `:core:security` | Arq. 2.7 / Seg. 4.1 |

**La tercera regla encontró una violación real en el propio template la
primera vez que se ejecutó**: `BiometricAuthenticator` (en `:feature:auth`)
inyectaba `KeystoreManager` —la clase concreta— en vez de una interfaz,
contradiciendo la sección 4.1 del Manual de Seguridad. Llevaba varias
rondas de revisión humana sin detectarse. Se corrigió extrayendo la
interfaz `KeyProvider`. Es el mejor argumento a favor de esta estrategia:
una regla automatizada encuentra en segundos lo que un revisor cuidadoso
pasó por alto repetidamente.

Cada regla tiene sus tests en
`build-logic/detekt-rules/src/test/` — una regla de detekt sin test es peor
que no tenerla: da la impresión de proteger algo que podría no estar
detectando nada.

## Convención de trazabilidad

Cada archivo no trivial lleva:

1. **KDoc** citando la sección normativa exacta que implementa (obligatorio
   desde Manual de Buenas Prácticas, sección 2.4).
2. Una línea apuntando a su fila en `TRACEABILITY.md`, para que ir del
   código a la política (o viceversa) sea inmediato.

## Próximos pasos

Ver `TRACEABILITY.md` para la lista completa de criterios `Pendiente`. El
siguiente módulo a construir es `:core:security` (11 criterios, 3 de ellos
Bloqueantes) — implementa `SecureStorageManager` como la siguiente
especialización del patrón Gateway después de `UserRepository`.

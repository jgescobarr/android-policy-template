# Manual de Principios de Arquitectura en el Desarrollo de Aplicaciones Móviles Android
## Borrador de cambios v2.0 — Solo secciones nuevas/modificadas

> Este documento NO es el manual completo. Contiene únicamente los 14 cambios acordados, redactados en el mismo estilo normativo del original y ubicados según su numeración de destino. Sirve para revisión antes de fusionarlos en el documento maestro y convertir a .docx.

---

## CAMBIO 1 — Control de Cambios (Portada)

**CONTROL DE CAMBIOS DEL DOCUMENTO**

| Versión | Descripción | Autor | Autoriza | Fecha |
| --- | --- | --- | --- | --- |
| 1.0 | Creación del documento | Desarrollo de aplicaciones móviles Cuernavaca | | 05/05/2019 |
| 2.0 | Actualización integral: cierre de brechas técnicas frente a la guía oficial Android/Kotlin vigente, corrección de fidelidad a Clean Architecture, e incorporación de mecanismos de gobernanza (excepciones, severidad, trazabilidad de fuentes) | [Autor] | [Autoriza] | [Fecha] |

**Cadencia de Revisión**

Este documento debe revisarse formalmente al menos una vez cada seis meses, y de forma extraordinaria cuando ocurra cualquiera de los siguientes eventos: lanzamiento de una versión major de Android, actualización major de Android Gradle Plugin (AGP), actualización major de Kotlin, o cambio en los requisitos de `targetSdk` impuestos por Google Play. El responsable de arquitectura debe documentar en esta tabla si la revisión resultó en cambios o si el documento se ratifica sin modificaciones.

---

## CAMBIO 2 — Nueva sección 1.3 Aplicabilidad y Excepciones

### 1.3 Aplicabilidad y Excepciones

**Aplicabilidad.** Este manual es de cumplimiento obligatorio desde la fase de diseño en todo proyecto Android nuevo iniciado a partir de la fecha de publicación de esta versión. Para proyectos existentes, aplica bajo el esquema de migración gradual descrito a continuación.

**Migración de proyectos existentes.** Todo proyecto existente debe ser evaluado contra el Checklist de Verificación (sección 14) dentro de los 30 días posteriores a la publicación de esta versión. Los hallazgos se priorizan según su severidad:

| Severidad | Criterio | Plazo de remediación |
| --- | --- | --- |
| Bloqueante | Riesgo de seguridad activo, pérdida de datos o incumplimiento legal (accesibilidad, privacidad) | Inmediato, antes del siguiente release |
| Crítico | Violación de un principio arquitectónico que compromete mantenibilidad o testabilidad a corto plazo | Próximo sprint de arquitectura o máximo 2 releases |
| Menor | Desviación de convención o estilo sin impacto funcional | Se registra como deuda técnica documentada, sin plazo fijo |

**Matriz de excepciones.** Ninguna regla de este manual puede omitirse de forma tácita. Toda excepción debe:

1. Ser solicitada por el líder técnico del proyecto, documentando la regla que se excepciona y la justificación técnica o de negocio.
2. Ser autorizada por el responsable de arquitectura de la organización (o el comité de arquitectura, si existe).
3. Registrarse en un log de excepciones con fecha de autorización y **vigencia máxima de 6 meses**, tras la cual debe re-evaluarse o resolverse definitivamente.
4. Nunca aplicar a reglas marcadas como severidad Bloqueante en la sección 14.

---

## CAMBIO 3 — Nueva sección 2.7 Patrón Gateway y Convención de Nombres

### 2.7 Patrón Gateway y Convención de Nombres

Toda interacción del sistema con un actor externo al núcleo de negocio — persistencia local, red, hardware del dispositivo, SDKs de terceros, WebViews o servicios del sistema operativo — debe pasar por una interfaz declarada en la capa de Dominio e implementada en la capa de Datos o Infraestructura. A este principio se le conoce conceptualmente como **Gateway**: el dominio nunca conoce el tipo concreto que existe al otro lado de la interfaz, solo su contrato.

Este manual ya aplica el patrón Gateway en varios puntos, bajo nombres específicos por tipo de actor externo:

| Sección | Implementación | Actor externo que aísla |
| --- | --- | --- |
| 3.3 / 8 | `Repository` | Persistencia local y red |
| 10.1 | `BiometricRepository` | Hardware biométrico |
| 11.2 | `WebBridge` | WebView / comunicación híbrida |

**Convención de nombres obligatoria.** A nivel de código, este manual usa exclusivamente `Repository` (convención: tipo de dato + `Repository`, p. ej. `PaymentsRepository`, `UserRepository`) para el caso de acceso a datos, alineado con la guía oficial de Android. **No se debe usar el sufijo `Gateway` en nombres de clase o interfaz** — "Gateway" es únicamente el término conceptual de este principio, no una convención de naming. Para actores externos que no encajan en datos (hardware, WebView, SDKs de pago, sensores), se debe nombrar la interfaz según el dominio que resuelve, siguiendo el mismo patrón que `BiometricRepository` y `WebBridge`.

**Traducción de errores en el borde.** Toda implementación de un Gateway debe capturar las excepciones específicas del framework o SDK subyacente (`IOException`, `SecurityException`, errores propios de WebView, excepciones de un SDK de terceros) y traducirlas a un tipo de error de dominio antes de que crucen hacia capas internas. El dominio nunca debe recibir ni manejar una excepción cuyo tipo pertenezca a un framework externo.

**Segregación de interfaces.** Un Gateway no debe convertirse en una interfaz de responsabilidades múltiples ("god interface"). Se prefieren varios Gateways pequeños y específicos (p. ej. `UserDataRepository`, `PaymentsRepository` separados) sobre uno genérico que acumula métodos de dominios distintos, en línea con el principio de Interface Segregation ya establecido en la sección 2.5.

---

## CAMBIO 4 — Ampliación de 2.1 y 3.2: Modelos por Capa y Mapeo Explícito

### Adición a 2.1 Clean Architecture (nuevo párrafo)

Cada capa debe definir su propio modelo de datos, y el paso de información entre capas debe realizarse siempre mediante mappers explícitos — nunca reutilizando directamente el modelo de una capa en otra. Esta regla no es exclusiva de la capa de Datos (ver 8.1); aplica igualmente a la frontera Domain → UI. Un modelo de dominio nunca debe usarse directamente como estado de UI (`UiState`), ni un `UiState` debe filtrarse de regreso hacia un UseCase.

### Adición a 3.2 Domain Layer (nuevo bullet)

* La capa de dominio no debe conocer, importar ni depender de las funciones de mapeo hacia Data o hacia UI. La responsabilidad de mapear un modelo de dominio hacia un modelo de presentación (`UiState`) reside en la capa de Presentación (ViewModel o mapper dedicado), nunca en el dominio mismo.

---

## CAMBIO 5 — Corrección de 4.3: Separar Resultado de Negocio del Estado de UI

### 4.3 Estados con Sealed Classes *(sustituye el contenido actual)*

Los resultados de una operación de negocio invocada por un UseCase deben modelarse con un sealed class que represente **únicamente el desenlace de negocio**: éxito o falla. El estado transitorio de carga (`Loading`) es un concepto de presentación y **no debe formar parte del tipo de retorno de un UseCase**.

```kotlin
// Dominio — solo desenlace de negocio
sealed class DomainResult<out T> {
    data class Success<T>(val data: T) : DomainResult<T>()
    data class Failure(val error: DomainError) : DomainResult<Nothing>()
}
```

El estado de `Loading`, junto con el resto del estado visual de la pantalla, se modela exclusivamente en el `UiState` del ViewModel:

```kotlin
// Presentación — incluye Loading porque es un concepto de UI
data class MyScreenState(
    val isLoading: Boolean = false,
    val data: MyUiModel? = null,
    val error: String? = null
)
```

El ViewModel es responsable de traducir un `DomainResult` en las transiciones de `isLoading` que correspondan (activarlo antes de invocar el UseCase, desactivarlo al recibir `Success` o `Failure`). El compilador de Kotlin debe seguir verificando en tiempo de compilación que todo `DomainResult` se maneje de forma exhaustiva mediante `when`.

---

## CAMBIO 6 — Ampliación de 5.2 Core Modules: Aislamiento Estructural del Dominio

### Adición a 5.2 Core Modules (nuevo párrafo)

El módulo `:domain` de cada feature debe configurarse como módulo Kotlin puro (`kotlin("jvm")` o `kotlin("multiplatform")`), **sin aplicar el plugin de Android** (`com.android.library`). Esto no es una convención estilística: es el mecanismo que hace cumplir en tiempo de compilación la independencia de framework que se declara en la sección 3.2. Un módulo de dominio con el plugin de Android aplicado permite, aunque sea por accidente, importar `Context`, clases de Room o cualquier API de la plataforma — rompiendo la garantía de independencia sin que ninguna revisión de código lo detecte hasta que ya está en producción.

---

## CAMBIO 7 — Expansión de sección 10: de "Biometría" a "Identidad y Autenticación"

### 10. IDENTIDAD Y AUTENTICACIÓN *(renombra la sección actual "10. BIOMETRÍA")*

#### 10.0 Credential Manager como Capa Recomendada

Para flujos de inicio de sesión (passkeys, contraseñas guardadas, sign-in federado con Google), se recomienda usar **Credential Manager** como la API unificada de autenticación, en lugar de integraciones directas y dispersas por proveedor. Credential Manager consolida en una sola superficie los distintos mecanismos de credenciales que antes requerían SDKs independientes, y es la dirección oficial actual de Android para autenticación.

La integración debe seguir el mismo patrón Gateway ya definido: un `AuthRepository` en la capa de Datos implementa la interacción con Credential Manager, expone al dominio un contrato agnóstico del mecanismo específico (passkey, contraseña, federado), y traduce cualquier excepción de Credential Manager a un error de dominio antes de que cruce hacia el UseCase.

Este punto debe evaluarse en conjunto con la arquitectura de autenticación JWT ya definida para el stack .NET/Angular/móvil, de forma que el flujo de emisión y renovación de tokens sea consistente independientemente de si el usuario se autenticó vía passkey, contraseña o proveedor federado.

#### 10.1 Integración vía UseCase + Repository *(sin cambios respecto al original — se conserva para autenticación biométrica específicamente)*

#### 10.2 Fallback Seguro (PIN / Pattern) *(sin cambios)*

#### 10.3 Consistencia UX en Prompts Biométricos *(sin cambios)*

---

## CAMBIO 8 — Nueva sección 12.5 Trabajo en Segundo Plano

### 12.5 Trabajo en Segundo Plano

**WorkManager** es el único mecanismo aprobado para trabajo diferido, periódico o que debe sobrevivir el cierre de la aplicación (sincronización de datos, subida de archivos pendientes, procesamiento que no requiere respuesta inmediata al usuario). Se prohíbe el uso de `Service` en primer plano indefinido o `AlarmManager` como sustituto de WorkManager para estos casos.

* **Constraints obligatorios.** Todo `Worker` debe declarar explícitamente sus `Constraints` (conectividad requerida, nivel de batería, almacenamiento disponible) en lugar de asumir condiciones ideales del dispositivo.
* **Política de reintentos.** Los `Worker` deben definir una estrategia de `BackoffPolicy` explícita (exponencial por defecto) y un límite razonable de reintentos; no se permite reintento indefinido sin backoff.
* **Integración con Hilt.** La inyección de dependencias en un `Worker` debe realizarse mediante `HiltWorkerFactory`, manteniendo la inyección de dependencias como único mecanismo de provisión de colaboradores, consistente con la sección 6.
* **Testing.** La lógica de negocio invocada dentro de un `Worker` debe residir en un UseCase testeable de forma aislada; el `Worker` mismo se limita a orquestar la ejecución y debe probarse con `WorkManagerTestInitHelper`.

---

## CAMBIO 9 — Columna de Severidad en el Checklist (sección 14)

### Adición de columna a la tabla existente de la sección 14

| Punto de verificación | Cumple | N/A | **Severidad** |
| --- | --- | --- | --- |
| Patrón arquitectónico definido y documentado (MVVM / MVI / Clean Architecture) | ✓ | | Bloqueante |
| Modelo de dominio separado de UiState y DTO, con mappers explícitos en cada frontera | | | Crítico |
| Módulo `:domain` sin plugin de Android | | | Crítico |
| Biometría integrada vía UseCase + Repository con fallback PIN/Pattern | ✓ | | Bloqueante |
| Tests unitarios para UseCases y ViewModels con cobertura ≥ 80% en lógica de negocio | ✓ | | Crítico |
| *(el resto de las filas existentes conserva su contenido; se les asigna severidad Bloqueante, Crítico o Menor caso por caso durante la redacción final)* | | | |

> Nota de redacción: cada fila del checklist original de la sección 14 debe pasar por esta clasificación de severidad antes de publicar la v2.0 — no se listan aquí las ~25 filas completas para evitar duplicar contenido que no cambia de fondo, solo de estructura.

---

## CAMBIO 10 — Baseline Profiles en 16.1 Métricas de Rendimiento Objetivo

### Adición a 16.1 (nuevo bullet y párrafo)

* Todo proyecto debe generar y mantener un **Baseline Profile**, que precompila las rutas de código críticas (arranque en frío, navegación inicial, pantallas de mayor tráfico) para reducir jank y tiempo de arranque en dispositivos reales.

El Baseline Profile debe generarse mediante `Macrobenchmark` con el módulo `baselineprofile`, integrarse en el pipeline de CI/CD para regenerarse en cada release significativo, y su ausencia o desactualización (más de dos versiones de retraso respecto al código actual) se considera un hallazgo de severidad Crítico en el checklist de la sección 14. El umbral objetivo de mejora en tiempo de arranque en frío debe documentarse por proyecto, tomando como referencia la medición sin Baseline Profile como línea base.

---

## CAMBIO 11 — Nueva sección 17.5 Compatibilidad de Plataforma

### 17.5 Compatibilidad de Plataforma

* **Tamaño de página de 16 KB.** Toda librería nativa (`.so`) incluida directa o transitivamente en el proyecto debe ser compatible con dispositivos que usan tamaño de página de memoria de 16 KB. Se debe verificar esta compatibilidad en cada actualización de dependencias nativas antes de publicar.
* **`targetSdk` mínimo obligatorio.** El proyecto debe mantener el `targetSdk` en la versión mínima exigida vigente por Google Play para nuevas publicaciones y actualizaciones, revisada como parte de la cadencia de revisión de este manual (sección 1, Control de Cambios).
* **Edge-to-edge por defecto.** Las pantallas nuevas deben diseñarse asumiendo que el contenido se dibuja borde a borde, manejando explícitamente los insets del sistema (barra de estado, barra de navegación, recorte de cámara) en lugar de depender de comportamiento legado.
* **Pantallas grandes y plegables.** Las pantallas de features con probabilidad de uso en tablets o dispositivos plegables deben validarse con layouts adaptativos (`WindowSizeClass`), evitando diseños rígidos pensados solo para una proporción de pantalla de teléfono.

---

## CAMBIO 12 — Nueva sección 19.5 Predictive Back

### 19.5 Predictive Back

Toda pantalla debe manejar el gesto de retroceso mediante `OnBackInvokedCallback` (o su equivalente en Compose/`AndroidX Activity`), habilitando el comportamiento de retroceso predictivo del sistema. Se prohíbe el manejo directo y no migrado de `onBackPressed()` en código nuevo.

Para código legado aún no migrado, debe documentarse como excepción temporal según la matriz de excepciones (sección 1.3), con plan de migración explícito, dado que el sistema aplica este comportamiento de forma cada vez más generalizada — incluyendo navegación por 3 botones, no solo gestos.

---

## CAMBIO 13 — Nota sobre Kotlin Multiplatform (adición menor a sección 2)

### Adición a sección 2 (párrafo introductorio, antes de 2.1)

Para proyectos con necesidad comprobada de compartir lógica de negocio entre Android e iOS, **Kotlin Multiplatform (KMP)** es una opción arquitectónica evaluable — no obligatoria — especialmente para la capa de Dominio y Datos, dado que ya se exige que estas capas sean independientes del framework Android (secciones 3.2 y 5.2). La adopción de KMP en un proyecto debe decidirse a nivel de arquitectura de organización, no por equipo individual, y documentarse como decisión arquitectónica formal.

---

## CAMBIO 14 — Nueva sección 21 Referencias Oficiales

### 21. REFERENCIAS OFICIALES

Esta sección ancla las reglas clave del manual a su fuente oficial vigente, para permitir auditar cuándo una recomendación queda desactualizada frente a cambios futuros de Google o JetBrains.

| Sección del manual | Regla | Fuente oficial | Última verificación |
| --- | --- | --- | --- |
| 2 / 3 | Arquitectura por capas (UI/Domain/Data) | developer.android.com — Guide to app architecture | [fecha] |
| 3.2 | Capa de Dominio y UseCases | developer.android.com — Domain layer | [fecha] |
| 5 | Patrones de modularización | developer.android.com — Common modularization patterns | [fecha] |
| 10.0 | Credential Manager | developer.android.com — About Credential Manager | [fecha] |
| 12.5 | WorkManager | developer.android.com — Guide to background processing | [fecha] |
| 16.1 | Baseline Profiles | developer.android.com — Baseline Profiles overview | [fecha] |
| 17.5 | Tamaño de página 16 KB / targetSdk | developer.android.com — Page size / Meet Google Play's target API requirement | [fecha] |
| 19.5 | Predictive Back | developer.android.com — Predictive back gesture | [fecha] |

> Nota de redacción: completar la columna "Última verificación" con la fecha real de consulta al momento de publicar la v2.0, y repetir esta verificación en cada revisión semestral (sección 1).

---

## CAMBIO 15 — Nueva sección 19.6 Navigation 3 como Alternativa Evaluable

### 19.6 Navigation 3 como Alternativa Evaluable

Navigation 3 es la nueva librería de navegación de Google diseñada específicamente para Jetpack Compose, y representa la dirección hacia la que se dirige la navegación en Android. A diferencia de Navigation Compose (secciones 19.1 a 19.5), donde el back stack vive oculto dentro de un NavController, en Navigation 3 el back stack es simplemente una lista de datos gestionada como estado propio de la aplicación.

Esta diferencia resuelve una tensión real con el principio de Single Source of Truth (sección 2.3): Navigation Compose mantiene el estado de navegación fuera del ViewModel como una segunda fuente de verdad; en Navigation 3 puede vivir junto al resto del estado, bajo un único origen.

**Criterios para evaluar su adopción** (opción evaluable, no obligatoria en esta versión): proyectos nuevos 100% Compose sin interoperabilidad con Views/Fragments; requisitos de layouts adaptativos/multi-panel; equipos dispuestos a asumir un ecosistema de terceros aún en maduración. **No se recomienda todavía** para navegación híbrida XML/Compose ni para implementaciones extensas y estables de Navigation Compose sin necesidad funcional de migrar. La adopción se decide a nivel de arquitectura de organización y se documenta como decisión arquitectónica formal, con el mismo criterio ya establecido para Kotlin Multiplatform (sección 2).

---

## CAMBIO 16 — Corrección de 4.3 y 4.5: DomainError explícito con variantes

### Ampliación a 4.3 Estados con Sealed Classes

El tipo de error usado por `DomainResult.Failure` debe ser un `DomainError` propio del proyecto — un sealed class con variantes semánticas, nunca un `Throwable` genérico ni una excepción de framework filtrada desde la capa de Datos (ver también el patrón Gateway, sección 2.7, que exige esta misma traducción en el borde de cualquier fuente externa):

```kotlin
sealed class DomainError {
    data class Network(val cause: Throwable) : DomainError()
    data class NotFound(val entityId: String) : DomainError()
    data class Validation(val field: String, val reason: String) : DomainError()
    data class Unauthorized(val cause: Throwable? = null) : DomainError()
    data class Unknown(val cause: Throwable) : DomainError()
}
```

Esta corrección surgió de una revisión de correspondencia entre los tres manuales: `DomainError` se usaba como tipo en el código de ejemplo desde la v2.0 original, pero nunca se definía formalmente — mientras que el Manual de Buenas Prácticas y su glosario ya afirmaban que sí estaba "definido en Arquitectura, sección 4.3" con variantes de ejemplo. Ahora la afirmación es exacta.

### Ajuste a 4.5 Manejo Centralizado de Errores

Se prohíbe atrapar excepciones genéricas (`catch(e: Exception)`) sin re-lanzarlas o transformarlas en la variante de `DomainError` correspondiente (sección 4.3) — antes decía "un tipo de error semántico", sin nombrarlo.

---

## CAMBIO 17 — Ampliación de 2.7: SecureStorageManager como especialización de Gateway

### Adición a la tabla de especializaciones de Gateway (sección 2.7)

| Sección | Implementación | Actor externo que aísla |
| --- | --- | --- |
| Manual de Seguridad, 4.1 | SecureStorageManager | Almacenamiento seguro (Keystore, DataStore cifrado) |

Se agrega esta fila para que la tabla sea bidireccional: el Manual de Seguridad ya referencia el patrón Gateway de esta sección al describir `SecureStorageManager` (sección 4.1 de Seguridad), pero la tabla de Arquitectura no incluía esa especialización como ejemplo — quedaba la referencia apuntando hacia un principio que no mencionaba el caso que la originó.

---

## CAMBIO 18 — Columna "Cuándo Evaluar" en el Checklist (sección 14)

### Ajuste a la tabla de la sección 14

Se agrega una quinta columna, **Cuándo Evaluar**, con tres valores posibles: **PR** (verificable por el autor/revisor en cada Pull Request, automatizable en CI), **Release** (se evalúa antes de publicar una versión) y **Auditoría** (estructural; solo se revisa periódicamente o al auditar un proyecto existente). Esto evita que un mismo checklist de ~30 filas se use sin distinción para ambos propósitos — el mismo esquema se aplicó también a Seguridad y a Buenas Prácticas para mantener homologación entre los tres manuales.

Ejemplo de asignación (patrón a replicar en las filas restantes):

| Criterio | Severidad | Cuándo Evaluar |
| --- | --- | --- |
| Patrón arquitectónico definido y documentado | Bloqueante | Auditoría |
| Separación estricta entre capas UI/Domain/Data | Bloqueante | Auditoría |
| Modularización con grafo acíclico | Crítico | Auditoría |
| Módulo :domain sin plugin de Android | Crítico | Auditoría |
| Hilt como único framework DI | Crítico | PR |
| Manejo de errores centralizado con sealed classes | Crítico | PR |
| ProGuard/R8 activo en release | Bloqueante | Release |
| Pipeline CI/CD ejecutando tests | Crítico | Release |
| Baseline Profile generado y regenerado | Crítico | Release |

---

## CAMBIO 19 — Nueva variante DomainError.SecurityViolation

### Ampliación a 4.3 Estados con Sealed Classes

Se agrega `SecurityViolation` a `DomainError`, entre `Unauthorized` y `Unknown`. Origen: al construir el `android-policy-template` (repositorio de referencia derivado de estos tres manuales), `NetworkErrorMapper.kt` necesitaba distinguir un fallo de certificate pinning (`SSLPeerUnverifiedException`, Manual de Seguridad sección 7.2 — señal de posible ataque man-in-the-middle) de un `SSLHandshakeException` genérico (problema de TLS ordinario) o de un `DomainError.Network` común (sin conexión). Ninguna de las 5 variantes originales representaba correctamente "posible compromiso de seguridad en la comunicación", así que se agregó una nueva en vez de forzar el caso dentro de `Unauthorized` (semánticamente incorrecto, esa variante es sobre autenticación del usuario, no integridad de la conexión) o `Network` (perdería la señal de alerta).

**Corrección posterior (firma unificada).** La primera versión de esta variante fue `SecurityViolation(val cause: Throwable)`, pensada solo para el caso de `NetworkErrorMapper`. Al implementar `RootDetector` (Seguridad, sección 10.2) se encontró un segundo origen legítimo de esta misma variante — root/hooking detectados — que reporta hallazgos como `List<String>` descriptivo, no como una excepción. La firma final, ya reflejada en el cuerpo del manual, es:

```kotlin
data class SecurityViolation(
    val reasons: List<String> = emptyList(),
    val cause: Throwable? = null,
) : DomainError()
```

Ambos campos son opcionales porque cada origen puebla solo el que le corresponde.

### Ampliación al Glosario (sección 20)

La entrada de `DomainError` se actualiza para listar las 6 variantes: `Network, NotFound, Validation, Unauthorized, SecurityViolation, Unknown`.

---

## CAMBIO 20 — Nota de Package Visibility en Seguridad, sección 10.2

*(Este cambio no se aplica al manual de Arquitectura — se aplica al Manual de Seguridad. Se documenta aquí para mantener la bitácora completa de hallazgos derivados del `android-policy-template`.)*

### Adición a 10.2 Anti Reverse Engineering (Manual de Seguridad)

Se agrega una nota técnica: en Android 11+ (API 30, Package Visibility), consultar la existencia de un paquete específico de otra app (para detectar Magisk, SuperSU, etc.) requiere declarar ese paquete en un bloque `<queries>` del `AndroidManifest` — sin esa declaración, `PackageManager.getPackageInfo()` reporta "no instalado" incluso si el paquete sí está presente, produciendo una falsa sensación de dispositivo limpio. Encontrado al validar `RootDetector` contra Android 11+ real, no era evidente por lectura de código.

---

## Resumen de cobertura (actualizado)

| Tipo de cambio | Cantidad |
| --- | --- |
| Técnico (brechas frente a guía oficial) | 9 (incluye Navigation 3) |
| Gobernanza | 5 (incluye columna Cuándo Evaluar) |
| Corrección de fidelidad a Clean Architecture | 3 (incluye Gateway) |
| Corrección de correspondencia entre manuales | 4 (DomainError explícito, tabla Gateway bidireccional, DomainError.SecurityViolation, Package Visibility en Seguridad) |
| **Total** | **20** |

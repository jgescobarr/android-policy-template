# Guía de Arquitectura del Proyecto (Android Policy Template v2.0)

Este documento describe la arquitectura global del proyecto `android-policy-template`, basada en el **Manual de Principios de Arquitectura en el Desarrollo de Aplicaciones Móviles Android v2.0**.

---

## 1. Visión General de la Arquitectura

El proyecto adopta **Clean Architecture** estructurada por capas y modularizada por característica (*feature*) e infraestructura (*core*):

```
+-------------------------------------------------------------------+
|                            :app                                   |
| (Ensambla el grafo raíz, MainActivity, NavGraph y Hilt Modules)   |
+---------------------------------+---------------------------------+
                                  |
            +---------------------+---------------------+
            |                                           |
            v                                           v
    :feature:auth / :feature:sync                :core:designsystem
    (UI Compose, ViewModels, Workers)            (Componentes M3, Scaffold)
            |                                           |
            v                                           v
    +-------+------------------+------------------------+-------+
    | :core:security | :core:network | :core:database | :core:webview |
    +------------------+------------------------+---------------+
                               |
                               v
                           :domain
      (Pure Kotlin JVM, UseCases, DomainResult, DomainError)
```

---

## 2. Principios Arquitectónicos Clave

1. **Capa de Dominio Pura (`:domain`)**:
   - Módulo Kotlin JVM puro (`kotlin("jvm")`), **sin el plugin de Android**.
   - Garantiza en tiempo de compilación la independencia del SDK de Android.
   - Define los modelos de dominio (`UserId`, `UserProfile`), UseCases (`GetUserProfileUseCase`, `LoginUseCase`) e interfaces de repositorio Gateway.

2. **Resultados de Negocio (`DomainResult<T>`)**:
   - `DomainResult.Success(data)` y `DomainResult.Failure(error)`.
   - Sin estado transitorio de carga (`Loading`), el cual vive exclusivamente en el `UiState` de la capa de presentación.

3. **Patrón Gateway (Arquitectura 2.7)**:
   - Toda interacción externa (red, base de datos, biometría, WebView, almacenamiento seguro) pasa por una interfaz declarada en `:domain` o `:core`.
   - Las excepciones de plataforma son capturadas en el borde del Gateway y traducidas a `DomainError`.

4. **Inyección de Dependencias con Hilt**:
   - Hilt es el único contenedor de DI del proyecto.
   - Los dispatchers se inyectan vía `DispatcherProvider` para garantizar pruebas unitarias deterministas.

5. **Navegación y Gestión de Estado**:
   - Jetpack Compose en el 100% de la UI.
   - MVI/MVVM con `StateFlow` e inmutabilidad (`ImmutableList`).
   - Side effects emitidos vía `SharedFlow` (`replay = 0`).

6. **Estrategia de Cobertura y Pruebas (`org.jetbrains.kotlinx.kover`)**:
   - Medición automatizada de cobertura de código en los módulos de negocio (`:domain` y `:feature:*`).
   - Verificación estricta de umbral `>= 80%` en la tarea `./gradlew koverVerify`.

7. **Capa de Red Segura y Resiliente (`:core:network`)**:
   - [AuthInterceptor.kt](file:///C:/WorkSpaces/Android%20Projects/android-policy-template/core/network/src/main/kotlin/org/template/core/network/AuthInterceptor.kt): Soporte de cabecera `No-Auth` para prevenir fuga de tokens Bearer en peticiones públicas.
   - [RetryInterceptor.kt](file:///C:/WorkSpaces/Android%20Projects/android-policy-template/core/network/src/main/kotlin/org/template/core/network/RetryInterceptor.kt): Reintentos automáticos condicionados a operaciones idempotentes (`GET`/`HEAD` o cabecera `Idempotency-Key`) incorporando Jitter aleatorio en el backoff exponencial.

---

## 3. Seguridad y Trazabilidad

Consulte [TRACEABILITY.md](file:///C:/WorkSpaces/Android%20Projects/android-policy-template/.agents/TRACEABILITY.md) para ver la correspondencia exacta entre cada archivo y las 126 reglas normativas de los manuales v2.0.

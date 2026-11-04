# Manual de Principios de Arquitectura en el Desarrollo Móvil Android
## Adiciones v2.1 — Estándares Avanzados de Arquitectura

---

## 1. Diseño Adaptativo y Clases de Tamaño de Ventana (`WindowSizeClass`)
* **Uso Obligatorio de `WindowWidthSizeClass`**: Las pantallas deben adaptarse dinámicamente a dispositivos teléfono (`Compact`), plegables (`Medium`) y tablets/escritorio (`Expanded`).
* **Scaffolds Adaptativos M3**: Se norma el uso de `ListDetailPaneScaffold` y `SupportingPaneScaffold` de Compose Material 3 para pantallas con relaciones lista-detalle o paneles secundarios.

## 2. Exposición de Capacidades a Agentes de IA (`AppFunctions API` - SDK 36)
* **Descubrimiento On-Device**: Las funcionalidades principales deben exponer anotaciones `@AppFunction` en la capa de uso para permitir su descubrimiento y ejecución directa por parte de agentes del sistema operativo (Gemini Nano) sin abrir la interfaz de usuario.

## 3. Patrón *Offline-First* con Room como Única Fuente de Verdad (SSOT)
* **Persistencia Cero-Parpadeo**: La capa de interfaz solo observa los `Flow` provenientes de Room. La capa de red (`:core:network`) sincroniza y persiste los datos en Room mediante `SyncWorker` o repositorios de sincronización.

## 4. Encadenamiento Funcional en Capa de Dominio (`DomainResult.kt`)
* **Mónadas y Side-Effects**: [DomainResult.kt](file:///C:/WorkSpaces/Android%20Projects/android-policy-template/domain/src/main/kotlin/org/template/domain/model/DomainResult.kt) incorpora las utilidades de extensión `flatMap`, `getOrNull()`, `onSuccess()` y `onFailure()` para permitir la composición fluida de UseCases y el manejo expresivo de efectos secundarios en ViewModels sin anidar bloques `when`.

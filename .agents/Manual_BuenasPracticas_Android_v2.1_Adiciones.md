# Manual de Buenas Prácticas de Codificación en el Desarrollo Móvil Android
## Adiciones v2.1 — Estándares Avanzados de Código y Compilación

---

## 1. Prohibición Total de `kapt` y Estandarización en KSP2
* **Compilación Incremental Rápida**: Se prohíbe el uso del plugin `kotlin-kapt` en todos los módulos. Todo procesador de anotaciones (Room, Hilt) debe utilizar **KSP2** (Kotlin Symbol Processing), reduciendo los tiempos de build hasta un 30%.

## 2. Reglas Estrictas de Structured Concurrency en Coroutines
* **Uso de `SupervisorJob`**: Se prohíbe pasar `Job()` directamente a un `CoroutineScope` personalizado, ya que cancela todo el scope ante la falla de un hijo. Debe usarse `SupervisorJob()` o los scopes administrados (`viewModelScope`).
* **Propagación Cooperativa**: Toda coroutine suspendida debe permitir o relanzar `CancellationException`.

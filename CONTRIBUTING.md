# Guía de Contribución y Código Limpio (Android Policy Template)

Esta guía establece el flujo de trabajo y el checklist de revisión obligatoria para todo Pull Request en el repositorio `android-policy-template`.

---

## 1. Reglas de Código Obligatorias

1. **Responsabilidad Única (SRP)**:
   - Ninguna clase debe mezclar responsabilidades de UI con lógica de negocio o llamadas de red.
   - Actividades, Fragments y Composables solo gestionan renderizado visual y delegación de eventos al `ViewModel`.

2. **Resultados de Negocio**:
   - Usar siempre `DomainResult<T>` (jamás `kotlin.Result<T>`).
   - Mapear las excepciones en la capa de datos/red/seguridad hacia `DomainError` antes de cruzar hacia el dominio.

3. **Inmutabilidad y Compose**:
   - Colecciones expuestas a la UI deben usar `ImmutableList<T>` de `kotlinx.collections.immutable`.
   - Estados representados mediante `data class` inmutables.

4. **Calidad de Código y Estilo**:
   - Respetar el límite de 500 líneas por clase (`LargeClass`).
   - Sin uso del operador `!!` en código de producción.
   - Documentación KDoc en funciones públicas y clases clave.

---

## 2. Checklist para Revisión de Pull Requests (PR)

Antes de solicitar la aprobación de un PR, asegúrese de ejecutar:

```bash
# 1. Análisis estático de código
./gradlew detektMain

# 2. Pruebas unitarias
./gradlew testDebugUnitTest

# 3. Compilación de producción
./gradlew :app:assembleRelease
```

---

## 3. Matriz de Trazabilidad

Consulte [TRACEABILITY.md](file:///C:/WorkSpaces/Android%20Projects/android-policy-template/.agents/TRACEABILITY.md) para mapear cualquier cambio nuevo contra los tres manuales normativos v2.0.

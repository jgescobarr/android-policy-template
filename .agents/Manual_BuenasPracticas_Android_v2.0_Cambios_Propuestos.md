# Manual de Principios de Buenas Prácticas de Codificación en el Desarrollo de Aplicaciones Móviles Android
## Borrador de cambios v2.0 — Solo secciones nuevas/modificadas

> Este documento NO es el manual completo. Contiene únicamente los 11 cambios acordados, redactados en el mismo estilo normativo del original y ubicados según su numeración de destino. Sirve para revisión antes de fusionarlos en el documento maestro y convertir a .docx.

---

## CAMBIO 1 — Control de Cambios (Portada)

**CONTROL DE CAMBIOS DEL DOCUMENTO**

| Versión | Descripción | Autor | Autoriza | Fecha |
| --- | --- | --- | --- | --- |
| 1.0 | Creación del documento | Desarrollo de aplicaciones móviles Cuernavaca | | 05/05/2019 |
| 2.0 | Unificación del tipo de resultado de negocio con Arquitectura v2.0 (DomainResult), corrección de la contradicción interna sobre el umbral de God Class, eliminación de duplicación de reglas de logging con Seguridad, cobertura de value classes e inmutabilidad para Compose, y homologación de gobernanza y estructura (Glosario, Referencias Oficiales) con los otros dos manuales | [Autor] | [Autoriza] | [Fecha] |

**Cadencia de Revisión.** Este documento debe revisarse formalmente al menos una vez cada seis meses, y de forma extraordinaria ante cualquier cambio en los tipos de resultado o convenciones definidas en el Manual de Arquitectura, dado que este manual depende de mantenerse coherente con aquel.

---

## CAMBIO 2 — Nueva sección 1.3 Aplicabilidad y Excepciones

### 1.3 Aplicabilidad y Excepciones

**Aplicabilidad.** Este manual es de cumplimiento obligatorio desde la fase de diseño en todo proyecto Android nuevo iniciado a partir de la fecha de publicación de esta versión. Para proyectos existentes, aplica bajo el esquema de migración gradual descrito a continuación, con el mismo esquema de severidad y plazos definido en el Manual de Arquitectura.

**Migración de proyectos existentes.** Todo proyecto existente debe ser evaluado contra el Checklist de Verificación (sección 10) dentro de los 30 días posteriores a la publicación de esta versión.

| Severidad | Criterio | Plazo de remediación |
| --- | --- | --- |
| Bloqueante | Código que introduce riesgo de fallo en producción o incumple una regla de seguridad de codificación (p. ej. datos sensibles en logs) | Inmediato, antes del siguiente release |
| Crítico | Violación de un principio de diseño que compromete mantenibilidad o testabilidad a corto plazo | Próximo sprint o máximo 2 releases |
| Menor | Desviación de convención de estilo sin impacto funcional | Deuda técnica documentada, sin plazo fijo |

**Matriz de excepciones.** Ninguna regla de este manual puede omitirse de forma tácita. Toda excepción debe:

1. Ser solicitada por el autor del cambio o el líder técnico, documentando la regla que se excepciona y la justificación técnica.
2. Ser autorizada por el responsable de arquitectura de la organización.
3. Registrarse en el log de excepciones con vigencia máxima de 6 meses, tras la cual debe re-evaluarse o resolverse definitivamente.
4. Nunca aplicar a reglas marcadas como severidad Bloqueante en la sección 10.

---

## CAMBIO 3 — Reescritura de 5.1: DomainResult en lugar de kotlin.Result

### 5.1 DomainResult y Sealed Classes para Éxito/Error *(sustituye el contenido actual)*

Las operaciones que pueden fallar deben comunicar su resultado mediante `DomainResult<T>`, el sealed class definido de forma autoritativa en el Manual de Arquitectura, sección 4.3 — **no mediante `kotlin.Result<T>`**. `kotlin.Result<T>` envuelve cualquier `Throwable` sin distinguir su naturaleza semántica, lo cual es exactamente lo que la sección 4.5 de Arquitectura prohíbe ("sin catch genérico sin transformación"). Usar dos convenciones distintas entre este manual y Arquitectura genera ambigüedad real sobre qué tipo debe implementar un desarrollador nuevo.

```kotlin
suspend fun getUserProfile(userId: String): DomainResult<UserProfile> =
    try {
        val response = apiService.getUser(userId)
        DomainResult.Success(response.toDomainModel())
    } catch (e: IOException) {
        DomainResult.Failure(DomainError.Network(e))
    }

// En el ViewModel:
viewModelScope.launch {
    when (val result = getUserProfileUseCase(userId)) {
        is DomainResult.Success -> _uiState.update { it.copy(profile = result.data) }
        is DomainResult.Failure -> _uiState.update { it.copy(errorMessage = result.error.toMessage()) }
    }
}
```

Este enfoque hace que el posible fallo sea explícito en la firma de la función, obliga al llamador a manejar ambos casos de forma exhaustiva (verificado por el compilador mediante `when`), y mantiene el tipo de error como un `DomainError` semántico en vez de un `Throwable` genérico.

---

## CAMBIO 4 — Ajuste de 5.3: Terminología Alineada con Arquitectura

### Adición a 5.3 Manejo Centralizado en Domain/Data (ajuste de terminología)

Sustituir "un tipo de error semántico del dominio (por ejemplo, `NetworkError`, `NotFoundError`, `ValidationError`)" por: "la variante correspondiente de `DomainError`, definida junto con `DomainResult` en el Manual de Arquitectura, sección 4.3 (por ejemplo, `DomainError.Network`, `DomainError.NotFound`, `DomainError.Validation`)". El resto de la sección no cambia.

---

## CAMBIO 5 — Reducción de 5.4 y 6.2: Referencia a Seguridad

### 5.4 Logging de Errores Sin Datos Sensibles *(sustituye el contenido actual)*

Cada error inesperado debe registrarse con suficiente contexto para ser diagnosticado: el tipo de error, el módulo donde ocurrió, el estado de la operación en ese momento y el stack trace. Los datos que nunca deben incluirse en un log están definidos de forma autoritativa en el Manual de Seguridad, sección 12.1 — esta sección se limita a exigir su cumplimiento en el contexto específico del logging de errores. Se recomienda definir una clase `ErrorLogger` que centralice el formateo del log y aplique los filtros de datos sensibles correspondientes antes del registro.

### 6.2 Datos Sensibles Fuera de los Logs *(sustituye el contenido actual)*

La lista de datos prohibidos en cualquier log, y el mecanismo de sanitización automática, están definidos de forma autoritativa en el Manual de Seguridad, sección 12.1. Esta regla no admite excepciones y aplica a todos los niveles de log en todos los entornos, incluyendo desarrollo y staging. Se recomienda crear una función de extensión `maskSensitive()` para las clases de datos que deban loggearse, mostrando únicamente los primeros y últimos caracteres del valor.

---

## CAMBIO 6 — Corrección de 9.1: Umbral de God Class

### Ajuste a 9.1 God Classes (una palabra)

Sustituir "clases de más de **500** líneas deben ser revisadas" — el texto ya decía 500; el valor correcto se mantiene. *(Este cambio no reescribe prosa: solo confirma 500 como el umbral autoritativo, para que el Cambio 7 lo alinee en el checklist.)*

---

## CAMBIO 7 — Corrección del Checklist: God Class a 500 líneas

### Ajuste a la fila de God Class en la sección 10 (Checklist)

**Antes:** "Sin clases de más de 300 líneas sin justificación arquitectónica documentada"

**Después:** "Sin clases de más de 500 líneas sin justificación arquitectónica documentada, consistente con 9.1"

---

## CAMBIO 8 — Nueva sección 3.8 Value Classes

### 3.8 Value Classes para Evitar Primitive Obsession

El uso de tipos primitivos (`String`, `Long`, `Double`) para representar conceptos de dominio con reglas propias — identificadores, montos monetarios, correos electrónicos — es una fuente frecuente de errores: nada impide pasar un `userId` donde se esperaba un `accountId`, ya que ambos son `String` para el compilador. Kotlin resuelve esto con `@JvmInline value class`, que crea un tipo distinto sin el costo de asignación en tiempo de ejecución de una clase envolvente normal.

```kotlin
@JvmInline
value class UserId(val value: String)

@JvmInline
value class AccountBalance(val cents: Long)

// El compilador ahora rechaza esto en tiempo de compilación:
fun transferTo(accountId: AccountId, amount: AccountBalance) { /* ... */ }
```

Se recomienda el uso de value classes para todo identificador de entidad de dominio y para cualquier magnitud numérica con semántica propia (montos, porcentajes, duraciones), priorizando los casos donde confundir dos valores del mismo tipo primitivo tendría consecuencias de negocio.

---

## CAMBIO 9 — Nueva sección 3.9 Inmutabilidad para Compose

### 3.9 Inmutabilidad de Colecciones Expuestas a Compose

Como se establece en el Manual de Arquitectura, sección 16.2, el compilador de Compose omite la recomposición de un composable cuando sus parámetros son estables. Las colecciones mutables de Kotlin (`List`, `Map`, `Set` declaradas con `var` o expuestas como interfaces mutables) no se consideran estables por el compilador de Compose, lo que fuerza recomposiciones innecesarias incluso cuando el contenido no cambió.

Todo estado expuesto desde un ViewModel hacia un composable que incluya colecciones debe usar tipos inmutables de `kotlinx.collections.immutable` (`ImmutableList`, `ImmutableMap`) o, para tipos propios que envuelven colecciones, marcarse explícitamente con `@Immutable` o `@Stable` cuando su inmutabilidad estructural pueda garantizarse pero el compilador no pueda inferirla automáticamente.

```kotlin
data class ScreenState(
    val items: ImmutableList<Item> = persistentListOf(),
)
```

---

## CAMBIO 10 — Nueva sección 11 Glosario de Términos

### 11. GLOSARIO DE TÉRMINOS

Homologado en formato con los glosarios de Arquitectura y Seguridad, para mantener consistencia terminológica entre los tres manuales.

| Término | Definición |
| --- | --- |
| Antipatrón | Solución recurrente a un problema recurrente que parece razonable en el corto plazo pero produce consecuencias negativas en el mediano y largo plazo. |
| detekt | Herramienta de análisis estático para Kotlin que detecta complejidad ciclomática elevada, funciones demasiado largas y otros antipatrones de diseño. |
| DomainError | Tipo sellado que representa las variantes semánticas de error del dominio (Network, NotFound, Validation, etc.), definido en el Manual de Arquitectura, sección 4.3. |
| DomainResult | Sealed class que representa el desenlace de negocio (Success/Failure) de una operación invocada por un UseCase, sin incluir estados de presentación como Loading. Definido en el Manual de Arquitectura, sección 4.3. |
| DRY | Don't Repeat Yourself. Principio que exige evitar la duplicación de lógica idéntica con el mismo propósito. |
| God Class | Clase que ha acumulado demasiadas responsabilidades a lo largo del tiempo; en este manual, señal de revisión a partir de 500 líneas. |
| KDoc | Formato de documentación estándar de Kotlin para funciones públicas y clases, análogo a Javadoc. |
| ktlint | Formateador y linter de estilo estándar para Kotlin, usado para validar las convenciones de formato de la sección 2. |
| Scope Function | Función de Kotlin (let, apply, run, also, with) que ejecuta un bloque de código en el contexto de un objeto, mejorando la expresividad de transformaciones y configuraciones. |
| SRP | Single Responsibility Principle. Cada clase debe tener una única razón para cambiar. |
| Value Class | Tipo declarado con @JvmInline value class que envuelve un valor primitivo para darle semántica de dominio propia, sin el costo de asignación de una clase normal. |

---

## CAMBIO 11 — Nueva sección 12 Referencias Oficiales

### 12. REFERENCIAS OFICIALES

| Sección del manual | Regla | Fuente oficial | Última verificación |
| --- | --- | --- | --- |
| 5.1 | DomainResult (referencia cruzada) | Manual de Arquitectura, sección 4.3 | [fecha] |
| 5.4 / 6.2 | Datos prohibidos en logs (referencia cruzada) | Manual de Seguridad, sección 12.1 | [fecha] |
| 2.4 | KDoc: fragmento resumen, orden de etiquetas, excepciones | developer.android.com — Kotlin style guide (Documentation) | [fecha] |
| 2.4 | Evitar @param/@return | kotlinlang.org — Coding conventions (Documentation comments) | [fecha] |
| 2.5 | Formato de comentarios de fin de línea y encabezado de licencia | developer.android.com — Kotlin style guide (Whitespace / Structure) | [fecha] |
| 2.6 | Constantes vs. strings.xml | developer.android.com — App resources / Localize your app; detekt.dev — Style ruleset (MagicNumber) | [fecha] |
| 2.3.1-2.3.3 | Llaves, espaciado horizontal, salto de línea | developer.android.com — Kotlin style guide (Formatting) | [fecha] |
| 3.8 | Value classes | kotlinlang.org — Inline value classes | [fecha] |
| 3.9 | Estabilidad e inmutabilidad en Compose | developer.android.com — Jetpack Compose stability | [fecha] |
| 8.1 / 8.2 | ktlint / detekt | pinterest.github.io/ktlint, detekt.dev | [fecha] |

---

## CAMBIO 12 — Ampliación de 2.4 KDoc con reglas oficiales adicionales

### Ampliación a 2.4 Documentación con KDoc

Se agregan tres reglas oficiales de la guía de estilo de Kotlin de Android (developer.android.com/kotlin/style-guide) que no estaban cubiertas:

- **Fragmento resumen**: todo bloque KDoc debe iniciar con una frase nominal o verbal breve (no oración completa), sin empezar con "Este método regresa..." ni "Retorna...", pero con mayúscula y puntuación de oración completa — es el único texto que aparece en índices generados automáticamente.
- **Orden de etiquetas de bloque**: @constructor, @receiver, @param, @property, @return, @throws, @see.
- **Cuándo KDoc es opcional**: funciones "simples y obvias" (como un getFoo sin nada más que decir) y métodos que sobrescriben (override) un método de supertipo, sin usar esta excepción para omitir información que un lector típico sí necesitaría.

También se corrigió el ejemplo de código de esta sección, que antes usaba @param/@return contradiciendo la propia recomendación de evitarlos.

---

## CAMBIO 13 — Nueva sección 2.5 Comentarios en Línea

### 2.5 Comentarios en Línea

Distingue explícitamente regla oficial de práctica de industria, para que el manual no atribuya autoridad oficial a reglas que no la tienen:

- **Regla oficial — formato**: un espacio a ambos lados del `//` de un comentario de fin de línea.
- **Regla oficial — encabezado de licencia/copyright**: debe ir como comentario de bloque multilínea normal (`/* ... */`), nunca en estilo KDoc ni de línea simple.
- **Práctica de industria (no prescrita oficialmente)**: el comentario explica el porqué, no repite el qué; sin código muerto comentado en el repositorio.

---

## CAMBIO 14 — Nueva sección 2.6 Constantes vs. Recursos de Texto (strings.xml)

### 2.6 Constantes vs. Recursos de Texto (strings.xml)

Tres preguntas para decidir dónde vive un valor literal: ¿lo ve el usuario final? → `strings.xml` (regla oficial de Android, referenciada desde Arquitectura 18.1, sin repetirse aquí). ¿Se repite o su significado no es evidente? → `const val` en `companion object` o archivo de constantes, `UPPER_SNAKE_CASE` (sección 2.1). ¿Es de un solo uso y evidente? → puede quedar in-line. Se aclara que `strings.xml` es exclusivamente para contenido de UI — un endpoint o una clave técnica no dejan de ser "texto técnico" por ser cadenas de caracteres. El cumplimiento automático de la regla de constantes se ancla a la regla `MagicNumber` de detekt (sección 8.2).

---

## CAMBIO 15 — Nuevas subsecciones 2.3.1, 2.3.2, 2.3.3 (Formato de Código)

### 2.3.1 Uso Obligatorio de Llaves

Las llaves pueden omitirse solo en `when`/`if` con un `else` que cabe en una línea; fuera de ese caso son obligatorias en `if`, `for`, `when`, `do`, `while`, incluso con cuerpo vacío o de una instrucción.

### 2.3.2 Espaciado Horizontal

Tabla completa de dónde va (y no va) un espacio: palabra reservada + paréntesis, `else`/`catch` + llave, antes de toda llave de apertura, alrededor de operadores binarios y de `->`, después de coma/dos puntos, antes de dos puntos solo en declaración de clase base. Sin espacio alrededor de `::`, `.`, `..`.

### 2.3.3 Salto de Línea (Line-Wrapping)

Reglas de dónde romper una línea de más de 100 caracteres: `.`/`?.`/`::` se rompen antes del símbolo; nombre de función pegado al paréntesis; coma pegada al token anterior. Firmas de función: un parámetro por línea con +4 de indentación; paréntesis de cierre y tipo de retorno sin indentación adicional. Inicializadores de propiedad: se rompe después del `=`.

También se agregó un sexto bullet a la lista general de 2.3 ("Una instrucción por línea; no se usan punto y coma") y se ajustó el bullet de longitud de línea para remitir a estas reglas.

---

## CAMBIO 16 — Columnas "Severidad" y "Cuándo Evaluar" en el Checklist (sección 10)

### Ajuste a la tabla de la sección 10

Se detectó, en una revisión de correspondencia entre los tres manuales, que este checklist **nunca tuvo columna de Severidad** — pese a que la propia sección 1.3 define los tres niveles (Bloqueante/Crítico/Menor) y dice explícitamente que el checklist debe evaluarse con ellos. Arquitectura y Seguridad sí la tenían desde su v2.0. Se corrige agregando ambas columnas nuevas:

- **Severidad**: Bloqueante / Crítico / Menor, igual que los otros dos manuales.
- **Cuándo Evaluar**: PR / Release / Auditoría. En este manual, casi todas las filas quedan en **PR** — lo cual no es una limitación sino una confirmación de algo que ya era cierto por diseño: la propia sección 1.3 dice que el checklist se evalúa "antes de cada Pull Request", a diferencia de Arquitectura y Seguridad, que mezclan niveles PR/Release/Auditoría porque cubren tanto código como estructura de proyecto.

---

## Resumen de cobertura (actualizado)

| Tipo de cambio | Cantidad |
| --- | --- |
| Corrección de coherencia entre manuales | 3 |
| Corrección de coherencia interna | 2 |
| Técnico (Kotlin/Compose actual) | 2 |
| Técnico (Kotlin/Android — formato, comentarios, constantes) | 4 (KDoc ampliado, Comentarios en Línea, Constantes/strings.xml, Llaves-Espaciado-Salto de línea) |
| Gobernanza (incluye homologación de estructura, Severidad y Cuándo Evaluar) | 5 |
| **Total** | **16** |

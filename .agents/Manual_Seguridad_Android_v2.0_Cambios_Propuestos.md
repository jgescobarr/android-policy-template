# Manual de Principios de Seguridad en el Desarrollo de Aplicaciones Móviles Android
## Borrador de cambios v2.0 — Solo secciones nuevas/modificadas

> Este documento NO es el manual completo. Contiene únicamente los 12 cambios acordados, redactados en el mismo estilo normativo del original y ubicados según su numeración de destino. Sirve para revisión antes de fusionarlos en el documento maestro y convertir a .docx.

---

## CAMBIO 1 — Control de Cambios (Portada)

**CONTROL DE CAMBIOS DEL DOCUMENTO**

| Versión | Descripción | Autor | Autoriza | Fecha |
| --- | --- | --- | --- | --- |
| 1.0 | Creación del documento | Desarrollo de aplicaciones móviles Cuernavaca | | 05/05/2019 |
| 2.0 | Migración completa a la taxonomía OWASP Mobile Top 10 2024, cobertura de Credential Manager, corrección de binding criptográfico en biometría, reglas de backup y App Links, y gobernanza alineada con el Manual de Arquitectura v2.0 (con plazos más estrictos) | [Autor] | [Autoriza] | [Fecha] |

**Cadencia de Revisión.** Este documento debe revisarse formalmente al menos una vez cada seis meses, y de forma extraordinaria ante: actualización de la lista OWASP Mobile Top 10, cambios en el estándar PCI DSS aplicable, actualización major de Android relacionada con seguridad (Keystore, Play Integrity), o hallazgos de severidad Bloqueante en una auditoría trimestral (sección 14.2).

---

## CAMBIO 2 — Nueva sección 1.3 Aplicabilidad y Excepciones

### 1.3 Aplicabilidad y Excepciones

**Aplicabilidad.** Este manual es de cumplimiento obligatorio desde la fase de diseño en todo proyecto Android nuevo iniciado a partir de la fecha de publicación de esta versión. Para proyectos existentes, aplica bajo el esquema de migración gradual descrito a continuación, siguiendo el mismo esquema definido en el Manual de Arquitectura, con plazos más estrictos dado el mayor riesgo asociado a hallazgos de seguridad.

**Migración de proyectos existentes.** Todo proyecto existente debe ser evaluado contra el Checklist de Verificación (sección 15) dentro de los 30 días posteriores a la publicación de esta versión.

| Severidad | Criterio | Plazo de remediación |
| --- | --- | --- |
| Bloqueante | Vulnerabilidad explotable, exposición de datos sensibles o incumplimiento regulatorio (PCI DSS, privacidad) | Inmediato, antes del siguiente release |
| Crítico | Desviación de un control de seguridad que aumenta significativamente la superficie de ataque | Máximo 1 release, sin excepción |
| Menor | Desviación de convención o de buena práctica sin explotabilidad directa conocida | Deuda técnica documentada, plazo máximo de 3 meses |

**Matriz de excepciones.** Ninguna regla de este manual puede omitirse de forma tácita. Toda excepción debe:

1. Ser solicitada por el líder técnico del proyecto, documentando el control que se excepciona, la justificación técnica y el riesgo residual aceptado.
2. Ser autorizada conjuntamente por el responsable de arquitectura y el equipo de seguridad de la organización — a diferencia de Arquitectura, ninguna excepción de seguridad puede autorizarse con un solo aprobador.
3. Registrarse en el log de excepciones con **vigencia máxima de 3 meses** (no 6, como en Arquitectura), tras la cual debe re-evaluarse o resolverse definitivamente.
4. Nunca aplicar a reglas marcadas como severidad Bloqueante en la sección 15.

---

## CAMBIO 3 — Migración de 2.1 a OWASP Mobile Top 10 2024

### 2.1 OWASP Mobile Top 10 *(sustituye el contenido actual)*

El proyecto OWASP Mobile Top 10 es el estándar de referencia internacional para la identificación y mitigación de los riesgos de seguridad más críticos en aplicaciones móviles. La edición vigente es la **2024**, la primera actualización mayor desde 2016. Todos los desarrollos deben evaluarse contra esta lista antes de cada release.

| Categoría 2024 | Descripción | Sección(es) de este manual que la cubren |
| --- | --- | --- |
| M1 – Improper Credential Usage | Manejo inseguro de credenciales: hardcoding, almacenamiento inadecuado, mal uso de tokens | 4 (Secure Storage), 5.0 (Credential Manager), 9 (Secretos) |
| M2 – Inadequate Supply Chain Security | Vulnerabilidades introducidas por dependencias de terceros, SDKs o el propio pipeline de build | 13 (Gestión de Dependencias), 2.1.1 (nueva) |
| M3 – Insecure Authentication/Authorization | Autenticación o autorización débil, o validada solo en el cliente | 5 (Biometría), 5.0 (Credential Manager), 2.4 (Zero Trust) |
| M4 – Insufficient Input/Output Validation | Falta de validación de datos de entrada o salida, incluyendo inyección | 8.3 (Sanitización WebView), 11.2 (Intents) |
| M5 – Insecure Communication | Comunicación sin cifrar o mal configurada | 7 (Comunicación Segura) |
| M6 – Inadequate Privacy Controls | Manejo inadecuado de datos personales y minimización insuficiente | Nueva sección 6.5 (Privacidad y Minimización de Datos) |
| M7 – Insufficient Binary Protections | Falta de ofuscación, anti-tampering o verificación de integridad | 10 (Protección de la Aplicación) |
| M8 – Security Misconfiguration | Configuraciones inseguras: backups, componentes expuestos, flags de debug | 11 (Componentes), 11.4 (nueva, Backup) |
| M9 – Insecure Data Storage | Datos sensibles almacenados sin cifrado adecuado | 3 (Protección de Datos), 4 (Secure Storage) |
| M10 – Insufficient Cryptography | Algoritmos débiles o mal implementados | 3.2, 4.3 (Tink) |

> Nota de redacción: las secciones 2.2–2.4 (Mínimo Privilegio, Defense in Depth, Zero Trust) no cambian — son principios independientes de la taxonomía OWASP y se conservan tal cual.

---

## CAMBIO 4 — Nueva sección 2.1.1 Cadena de Suministro de Software

### 2.1.1 Cadena de Suministro de Software (M2 – Inadequate Supply Chain Security)

Además de lo ya establecido en la sección 13 (Gestión de Dependencias), todo proyecto debe generar un **SBOM (Software Bill of Materials)** en cada release, listando todas las dependencias directas y transitivas con su versión exacta. El SBOM debe generarse en formato estándar (CycloneDX o SPDX) mediante un plugin de Gradle integrado al pipeline CI/CD, y almacenarse como artefacto asociado a cada release para permitir auditoría retroactiva ante la divulgación de una nueva vulnerabilidad (CVE) en cualquier dependencia usada históricamente.

---

## CAMBIO 5 — Nueva sección 6.5 Privacidad y Minimización de Datos

### 6.5 Privacidad y Minimización de Datos (M6 – Inadequate Privacy Controls)

* **Minimización de datos.** La aplicación debe recolectar únicamente los datos personales estrictamente necesarios para la funcionalidad que los solicita. Cualquier campo de dato personal nuevo debe justificarse explícitamente ante el equipo de seguridad antes de su implementación.
* **Permisos runtime justificados.** Cada permiso peligroso (`dangerous permission`) solicitado en tiempo de ejecución debe ir acompañado de una explicación contextual visible al usuario en el momento de la solicitud, y debe solicitarse únicamente cuando la funcionalidad correspondiente está a punto de usarse — nunca de forma anticipada al iniciar la aplicación.
* **Play Data Safety.** La sección de seguridad de los datos en Google Play Console debe mantenerse actualizada en cada release, reflejando con precisión qué datos se recolectan, con qué propósito y si se comparten con terceros. Cualquier discrepancia entre lo declarado y el comportamiento real de la aplicación es una violación de esta política.
* **Retención mínima.** Los datos personales deben eliminarse o anonimizarse cuando ya no son necesarios para el propósito que justificó su recolección, siguiendo una política de retención documentada por tipo de dato.

---

## CAMBIO 6 — Nueva sección 5.0 Credential Manager

### 5.0 Credential Manager

Credential Manager es la API unificada de Android para autenticación (passkeys, contraseñas guardadas, inicio de sesión federado), ya definida como capa recomendada en el Manual de Arquitectura (sección 10.0). Esta sección cubre su modelo de amenazas específico, complementario a esa definición arquitectónica.

* **Resistencia a phishing.** Las passkeys gestionadas por Credential Manager son resistentes a phishing por diseño, ya que la clave privada nunca sale del dispositivo y la autenticación está criptográficamente vinculada al origen (dominio) que la registró. Esto las hace preferibles a contraseñas u OTP por SMS para flujos de autenticación nuevos.
* **Validación de origen en integraciones federadas.** Para inicio de sesión federado (Google Sign-In vía Credential Manager), el token recibido debe validarse siempre en el backend contra el emisor esperado (issuer) y la audiencia (audience) del token, nunca confiando únicamente en la respuesta del cliente.
* **Sin almacenamiento propio de credenciales.** La aplicación no debe implementar un almacenamiento propio de contraseñas o tokens de passkey en paralelo a Credential Manager; esto duplicaría la superficie de ataque que la API está diseñada para eliminar.

---

## CAMBIO 7 — Ampliación de 5.2: Binding Criptográfico en Biometría

### Adición a 5.2 No Almacenamiento de Datos Biométricos (nuevo contenido)

Para operaciones de **alto riesgo** (autorización de pagos, cambio de credenciales de seguridad, revelado de datos sensibles), no es suficiente que `BiometricPrompt` retorne un booleano de éxito o fallo. La autenticación debe realizarse mediante `BiometricPrompt.CryptoObject`, vinculando criptográficamente el resultado de la autenticación a una operación específica de Android Keystore (por ejemplo, firmar o descifrar con una clave que requiere `setUserAuthenticationRequired(true)`). Esto garantiza que el resultado de la autenticación no pueda falsificarse mediante manipulación del callback de la aplicación (hooking), ya que la operación criptográfica subyacente solo se completa si la autenticación biométrica ocurrió realmente a nivel de hardware.

Adicionalmente, estas operaciones de alto riesgo deben exigir explícitamente `Authenticators.BIOMETRIC_STRONG` (Class 3) al configurar el `BiometricPrompt.PromptInfo`. El uso de `BIOMETRIC_WEAK` (Class 2) se restringe a operaciones de bajo riesgo (por ejemplo, desbloquear la vista de un saldo ya autenticado en la sesión), siguiendo el mismo criterio de severidad de la sección 1.3.

---

## CAMBIO 8 — Nueva sección 11.4 Reglas de Backup

### 11.4 Reglas de Backup (M8 – Security Misconfiguration)

Toda aplicación debe declarar explícitamente su política de backup en el `AndroidManifest.xml`. Por defecto, `android:allowBackup` debe configurarse en `false`. Si el proyecto requiere backup parcial de datos no sensibles (por ejemplo, preferencias de interfaz), debe usarse `android:dataExtractionRules` (o `android:fullBackupContent` en versiones anteriores) para definir explícitamente qué archivos y bases de datos quedan excluidos del backup — en particular, la base de datos Room, cualquier DataStore cifrado y los directorios usados por el SecureStorageManager (sección 4) deben excluirse siempre, incluso si el backup general está habilitado. La ausencia de esta configuración se considera un hallazgo de severidad Crítico en el checklist de la sección 15.

---

## CAMBIO 9 — Nueva sección 11.5 App Links Verificados

### 11.5 App Links Verificados (M4 – Insufficient Input/Output Validation)

Los deep links que abren la aplicación desde un dominio propio deben implementarse como **Android App Links** con `android:autoVerify="true"`, respaldados por un archivo `assetlinks.json` publicado en `/.well-known/` del dominio correspondiente (Digital Asset Links). Esto garantiza que el sistema operativo verifique la propiedad del dominio antes de asociar el deep link a la aplicación, previniendo que otra aplicación instalada en el dispositivo registre un intent filter que intercepte el mismo enlace (deep link hijacking). Los deep links sin verificación de dominio (esquemas personalizados tipo `miapp://`) deben tratarse siempre como input no confiable, conforme a la sección 11.2, y no deben usarse para flujos que otorguen acceso a datos sensibles sin autenticación adicional.

---

## CAMBIO 10 — Ajuste a Arquitectura v2.0 (sección 15.3, no a Seguridad)

> Este cambio no se aplica al manual de Seguridad — se aplica al Manual de Arquitectura v2.0 ya entregado, para eliminar la duplicación identificada.

### Sustitución de 15.3 Datos Prohibidos en Logs (Manual de Arquitectura)

**Antes:** lista completa de datos prohibidos en logs, duplicada del manual de Seguridad.

**Después:**

> Los datos prohibidos en logs (contraseñas, tokens, PII, datos biométricos, claves criptográficas) y el mecanismo de sanitización automática están definidos de forma autoritativa en el Manual de Principios de Seguridad, sección 12.1. Esta sección se limita a referenciarlo: ningún componente de la aplicación debe registrar en logs los datos ahí listados, sin excepción por entorno.

---

## CAMBIO 11 — Sección 15 Checklist reestructurado por OWASP 2024 + Severidad

### 15. CHECKLIST DE VERIFICACIÓN *(reestructura completa)*

El checklist se reagrupa bajo las 10 categorías de OWASP Mobile Top 10 2024 (en lugar de la agrupación temática anterior), y se agrega la columna Severidad, siguiendo el mismo formato ya aprobado en el Manual de Arquitectura.

**Ejemplo de la nueva estructura (primeras filas, patrón a replicar en las ~40 filas restantes):**

| Categoría OWASP 2024 | Criterio de Verificación | Cumple | N/A | Severidad |
| --- | --- | --- | --- | --- |
| M1 – Improper Credential Usage | Sin secretos en BuildConfig, código fuente ni repositorio | ✓ | | Bloqueante |
| M1 – Improper Credential Usage | Credential Manager integrado para nuevos flujos de autenticación | | | Crítico |
| M2 – Inadequate Supply Chain Security | SBOM generado en cada release | | | Crítico |
| M2 – Inadequate Supply Chain Security | OWASP Dependency-Check sin hallazgos altos/críticos sin aceptación | ✓ | | Bloqueante |
| M3 – Insecure Authentication/Authorization | Operaciones de alto riesgo usan CryptoObject + BIOMETRIC_STRONG | | | Bloqueante |
| M6 – Inadequate Privacy Controls | Play Data Safety actualizado y consistente con el comportamiento real | | | Crítico |
| M8 – Security Misconfiguration | android:allowBackup=false o dataExtractionRules configuradas | | | Crítico |
| M8 – Security Misconfiguration | App Links verificados con Digital Asset Links | | | Menor |

> Nota de redacción: el resto de las ~40 filas del checklist original se remapean a su categoría 2024 correspondiente conservando su texto (ver tabla de mapeo del Cambio 3), sin reescribirse una por una en este borrador para evitar duplicar contenido que no cambia de fondo.

---

## CAMBIO 12 — Nueva sección final Referencias Oficiales

### 17. REFERENCIAS OFICIALES *(antes numerada 16; se recorrió a 17 al insertar el Glosario como nueva sección 16 — ver Cambio 14)*

| Sección del manual | Regla | Fuente oficial | Última verificación |
| --- | --- | --- | --- |
| 2.1 | OWASP Mobile Top 10 2024 | owasp.org — OWASP Mobile Top 10 | [fecha] |
| 5.0 | Credential Manager | developer.android.com — About Credential Manager | [fecha] |
| 5.2 | CryptoObject / BIOMETRIC_STRONG | developer.android.com — Biometric authentication | [fecha] |
| 10.2 | Detección de root y entornos comprometidos | developer.android.com — App and device security best practices | [fecha] |
| 10.3 | Play Integrity API | developer.android.com — Play Integrity API overview | [fecha] |
| 10.4 | Opciones de Desarrollador / ADB (Settings.Secure) | developer.android.com — Settings.Secure reference | [fecha] |
| 11.4 | Reglas de Backup | developer.android.com — Back up user data with Auto Backup | [fecha] |
| 11.5 | App Links | developer.android.com — Verify Android App Links | [fecha] |
| 13 | Gestión de dependencias / SBOM | developer.android.com — Gradle dependency management | [fecha] |

---

## CAMBIO 13 — Nueva sección 10.4 Detección de Modo Desarrollador y Depuración USB

### 10.4 Detección de Modo Desarrollador y Depuración USB

Adicionalmente a la detección de root (10.2), la aplicación debe verificar si el dispositivo tiene activadas las Opciones de Desarrollador (`Settings.Secure.DEVELOPMENT_SETTINGS_ENABLED`) o la depuración USB (`Settings.Secure.ADB_ENABLED`). Un dispositivo con depuración USB activa facilita significativamente el hooking en tiempo de ejecución y la extracción de datos vía ADB, incluso sin root. La respuesta ante esta condición sigue el mismo criterio de proporcionalidad al riesgo de 10.2.

```kotlin
fun isDeveloperModeEnabled(context: Context): Boolean {
    return Settings.Secure.getInt(
        context.contentResolver,
        Settings.Secure.DEVELOPMENT_SETTINGS_ENABLED, 0
    ) != 0
}

fun isAdbEnabled(context: Context): Boolean {
    return Settings.Secure.getInt(
        context.contentResolver,
        Settings.Secure.ADB_ENABLED, 0
    ) != 0
}
```

Nueva fila de checklist (M7, marcada con `*`): "Detección de Opciones de Desarrollador y depuración USB (ADB) activa implementada".

---

## CAMBIO 14 — Nueva sección 16 Glosario de Términos

### 16. GLOSARIO DE TÉRMINOS

Homologado en formato con los glosarios de Arquitectura y Buenas Prácticas. 16 términos, entre ellos: AEAD, App Links, Certificate Pinning, Credential Manager, CryptoObject, DAST, Defense in Depth, EncryptedSharedPreferences, **Gateway** (referencia cruzada al principio de Arquitectura 2.7, señalando a `SecureStorageManager` como su especialización de almacenamiento seguro), Hardware-Backed Keys, OWASP Mobile Top 10, Play Integrity API, SAST, SBOM, SecureStorageManager, Tink, Zero Trust.

También se agregó una aclaración a la sección 4.1 (SecureStorageManager): esta clase es, en términos del Manual de Arquitectura (sección 2.7), una especialización del patrón Gateway — cerrando la referencia que Arquitectura ya hacía hacia este manual sin que existiera el enlace de regreso.

---

## CAMBIO 15 — Columna "Cuándo Evaluar" en el Checklist (sección 15)

### Ajuste a la tabla de la sección 15

Se agrega la columna **Cuándo Evaluar** (PR / Release / Auditoría), homologada con Arquitectura y Buenas Prácticas. Ejemplo de asignación:

| Criterio | Categoría | Severidad | Cuándo Evaluar |
| --- | --- | --- | --- |
| Zero Trust aplicado en backend | PRINCIPIOS | Bloqueante | PR |
| Escaneo de secretos en pipeline | M1 | Crítico | Auditoría |
| SBOM generado en cada release | M2 | Crítico | Release |
| CryptoObject + BIOMETRIC_STRONG | M3 | Bloqueante | PR |
| Play Data Safety actualizado | M6 | Crítico | Release |
| Auditoría de seguridad trimestral | M9 | Crítico | Auditoría |

---

## CAMBIO 16 — Nota de Package Visibility en 10.2 Anti Reverse Engineering

Se agrega una nota técnica tras el párrafo existente de 10.2: en Android 11+ (API 30, Package Visibility), consultar la existencia de un paquete específico de otra app (para detectar Magisk, SuperSU, etc. — la base de `hasRootManagementApp()`) requiere declarar ese paquete en un bloque `<queries>` del `AndroidManifest`. Sin esa declaración, `PackageManager.getPackageInfo()` reporta "no instalado" incluso si el paquete sí está presente en el dispositivo — una falsa sensación de dispositivo limpio, no una simple limitación de la API.

Origen: encontrado al validar `RootDetector` (implementación de referencia derivada de esta sección) contra Android 11+ real, en el `android-policy-template`. No era evidente por lectura de código ni por la documentación general de detección de root — solo se manifestó al compilar y ejecutar contra un dispositivo con el nivel de API correcto.

Se agrega también una fila a la tabla de Referencias Oficiales (sección 17): `developer.android.com — Package visibility in Android 11`.

---

## Resumen de cobertura (actualizado)

| Tipo de cambio | Cantidad |
| --- | --- |
| Técnico (OWASP 2024, Credential Manager, biometría, backup, App Links, SBOM, privacidad, modo desarrollador) | 9 |
| Gobernanza | 5 (incluye Glosario y columna Cuándo Evaluar) |
| Resolución de traslape (edita Arquitectura) | 1 |
| Corrección de correspondencia entre manuales | 1 (referencia bidireccional a Gateway) |
| Corrección derivada de implementación real | 1 (Package Visibility) |
| **Total** | **16** |

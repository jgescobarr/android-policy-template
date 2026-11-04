# Política de Privacidad, Minimización de Datos y Play Data Safety

Este documento describe los controles de privacidad y retención de datos personales aplicados en `android-policy-template`, en cumplimiento con el **Manual de Principios de Seguridad v2.0, Sección 6.5** (M6 – Inadequate Privacy Controls).

---

## 1. Minimización y Retención de Datos

- **Principio de Minimización**: La aplicación recaba únicamente la información indispensable para autenticar al usuario y sincronizar preferencias (correo, identificador de usuario).
- **Almacenamiento Seguro**: Ningún dato de identificación personal (PII) o token de acceso se almacena en texto plano en el dispositivo. Se utiliza [SecureStorageManager](file:///C:/WorkSpaces/Android%20Projects/android-policy-template/core/security/src/main/kotlin/org/template/core/security/SecureStorageManager.kt) backed por DataStore cifrado y Android Keystore.
- **Política de Retención**:
  - Tokens de sesión: se eliminan al cerrar sesión (`logout()`) o al desinstalar la app.
  - Caché local de base de datos (Room): se expira periódicamente y no contiene datos bancarios o sensibles no cifrados.

---

## 2. Declaración de Google Play Data Safety

| Tipo de Dato | Recolectado | Compartido con Terceros | Propósito | Cifrado en Tránsito |
| --- | --- | --- | --- | --- |
| Identificadores de usuario (`UserId`) | Sí | No | Funcionalidad de la app / Autenticación | Sí (HTTPS / TLS 1.3) |
| Dirección de correo electrónico | Sí | No | Gestión de cuenta / Autenticación | Sí (HTTPS / TLS 1.3) |
| Datos de diagnóstico / Errores | Sí (Sanitizados) | No | Monitoreo de estabilidad | Sí (HTTPS / TLS 1.3) |

---

## 3. Registros de Logging y Sanitización

Todos los mensajes de log son procesados por [Sanitizer.kt](file:///C:/WorkSpaces/Android%20Projects/android-policy-template/core/logging/src/main/kotlin/org/template/core/logging/Sanitizer.kt) antes de emitirse, redactando automáticamente correos electrónicos, tokens Bearer, contraseñas y números de tarjeta, así como bloqueando cualquier intento de inyección de scripts (XSS), falsificación de logs (CRLF) o secuencias de control.

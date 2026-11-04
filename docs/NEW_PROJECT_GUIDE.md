# Guía de Inicialización de Proyectos Nuevos (Starter Kit)

Esta guía explica el procedimiento estándar para crear un nuevo proyecto Android a partir de este repositorio `android-policy-template`.

---

## Paso a Paso de Inicialización

### Paso 1: Clonar y Limpiar el Repositorio
```bash
git clone https://github.com/tu-organizacion/android-policy-template.git mi-nueva-app
cd mi-nueva-app
rm -rf .git
git init
```

---

### Paso 2: Actualizar Identificadores de Proyecto
1. **`settings.gradle.kts`**:
   ```kotlin
   rootProject.name = "mi-nueva-app"
   ```
2. **`app/build.gradle.kts`**:
   ```kotlin
   android {
       namespace = "com.miempresa.minuevaapp"
       defaultConfig {
           applicationId = "com.miempresa.minuevaapp"
       }
   }
   ```
3. **Refactorizar Paquete Principal en `:app`**:
   - Renombrar la estructura de carpetas de `org.template.app` a `com.miempresa.minuevaapp`.

---

### Paso 3: Configurar Entornos (`env.properties`)
1. Copiar la plantilla de entorno:
   ```bash
   cp env.properties.example env.properties
   ```
2. Configurar las URLs base de los servidores:
   ```properties
   DEV_BASE_URL=https://api-dev.miempresa.com/
   QA_BASE_URL=https://api-qa.miempresa.com/
   PROD_BASE_URL=https://api.miempresa.com/
   ```

---

### Paso 4: Crear una Nueva Feature
1. Crear el nuevo módulo de característica (p. ej. `:feature:payments`).
2. Incluir los plugins de convención en su `build.gradle.kts`:
   ```kotlin
   plugins {
       id("template.android.library")
       id("template.hilt")
       id("template.detekt")
       alias(libs.plugins.kotlin.compose)
       id("org.jetbrains.kotlinx.kover")
   }
   ```
3. Registrar el módulo en `settings.gradle.kts`:
   ```kotlin
   include(":feature:payments")
   ```

---

### Paso 5: Validar la Compilación y Pruebas Iniciales
```bash
./gradlew detektMain test assembleDebug
```

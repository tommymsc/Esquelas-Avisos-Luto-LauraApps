# Compilar «Esquelas y Avisos de Luto» — v1.7

## Opción recomendada: GitHub Actions (no necesitas Android Studio)

1. En GitHub, crea un **nuevo repositorio privado**, por ejemplo `Esquelas-Avisos-Luto-LauraApps` (no lo mezcles con otras apps).
2. Extrae este ZIP en una carpeta. Sube a **la raíz** del repositorio su contenido completo, incluidos `.github/workflows/android.yml`, `app/`, `tests/`, `settings.gradle`, `build.gradle` y `gradle.properties`. No subas el ZIP como único archivo: GitHub debe ver los archivos extraídos.
3. Abre **Actions → Compilar Esquelas y Avisos de Luto (APK)**.
4. Haz clic en **Run workflow → Run workflow**. Si acabas de subir los archivos a la rama `main` o `master`, el flujo también debería iniciarse automáticamente.
5. Abre la ejecución. Solo cuando la compilación muestre el indicador verde, abre **Artifacts** y descarga `Esquelas-Avisos-Luto-v1-7-debug-APK`.
6. Descomprime el artifact y tendrás **app-debug.apk**. Instálalo solo en tu dispositivo de pruebas.
7. Si falla: abre el paso rojo `Compilar APK y analizar Android`, copia el mensaje de error y compártelo para corregirlo. También habrá reportes de lint si se generaron.

**IMPORTANTE:** esta edición es un APK de depuración para pruebas, **no es el AAB firmado para Google Play**. Nunca compartas contraseñas ni llaves de firma.

## Compilación local

Si ya tienes Android Studio con SDK 36 y Gradle 8.11.1, abre el proyecto. Requiere Java 17 o superior compatible.
Desde la raíz del proyecto, una vez disponible Gradle:

```bash
gradle :app:assembleDebug :app:lintDebug --no-daemon
```

El archivo quedará en `app/build/outputs/apk/debug/app-debug.apk` después de una compilación **exitosa**.

## Requisitos del proyecto

- Paquete: `com.lauraapps.esquelas`.
- `compileSdk 36`, `targetSdk 36`, `minSdk 29`.
- Android Gradle Plugin 8.10.1 / Gradle 8.11.1 / Java 17.
- El APK todavía no está compilado ni probado en Android en el entorno donde se produjo este paquete.

# Esquelas y Avisos de Luto — LauraApps v1.8.0 (evaluación)

Proyecto Android nativo offline y demo web para crear lazos, esquelas, novenarios, homenajes con fotografías y estados de WhatsApp. Conserva el icono de lazo negro, lirio y vela aprobado.

## Contenido

- `app/`: app Android nativa Java (API mínima 29, objetivo 36).
- `demo/index.html`: prototipo visual offline (abrir en Chrome). La demo no sustituye pruebas Android.
- `tests/`: pruebas Java y navegador, y captura de prueba visual.
- `.github/workflows/android.yml`: compilación automática, lint y generación de APK debug.
- `INFORME_AUDITORIA.md`: hallazgos corregidos, pruebas realizadas y limitaciones.

## Compilar en GitHub Actions

1. Crea un repositorio **nuevo** y sube el **contenido del ZIP a la raíz**, incluyendo la carpeta oculta `.github`.
2. En GitHub, abre **Actions > Android APK > Run workflow** (o haz push a `main`).
3. Verifica que `Check standalone Java rules`, `Validate demo JavaScript` y `Build and lint APK` pasen.
4. Si el flujo termina bien, descarga el artefacto **Esquelas-Avisos-de-Luto-debug**.
5. Instala el APK en un teléfono de pruebas y sigue los escenarios de `INFORME_AUDITORIA.md`.

## Sin promesa de compilación

En el entorno de revisión no existe Android SDK/Gradle y no se ha compilado ni ejecutado una APK. Los resultados positivos de la demo web **no prueban** que Android carezca de errores.

## Privacidad y uso

La versión actual no incluye permisos de internet, analíticas, AdMob, nube ni cuentas. Fotografías y proyectos permanecen en el dispositivo. Los recuerdos finales se pueden guardar como PNG o compartir. Desinstalar la aplicación puede eliminar borradores y fotos privadas.

## Próximo hito

Compilación de debug + instalación Android + depuración de cualquier error de compilación o ejecución. Después: build AAB firmado y requisitos de Google Play.


## Cambios de estabilidad y presentación v1.8

- Se mantiene `main` como referencia probada v1.7; esta mejora vive en `improve/stability-visual-v1.8`.
- Botones y tarjetas muestran respuesta táctil y son enfocables para accesibilidad.
- Favoritos, recientes y Mis diseños se organizan verticalmente en pantallas estrechas.
- El editor mejora separación, jerarquía de encabezados y contraste del texto dorado.
- El selector de fotografía solo aparece en composiciones que realmente la utilizan.
- Los diseños con fotografía muestran una instrucción explícita y bloquean exportación hasta seleccionar una imagen.
- Si no se puede abrir el selector de aplicaciones para compartir, el PNG ya guardado permanece en la galería y se notifica al usuario.
- Comprobaciones de regresión automatizadas adicionales en `tests/verify_package.py`.

**Alcance de verificación:** GitHub Actions compila `assembleDebug`, ejecuta reglas Java, validaciones y lint; esto no sustituye instalación, pruebas de interfaz y revisión de pantallas en teléfonos reales.

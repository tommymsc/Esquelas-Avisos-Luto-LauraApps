# Auditoría profunda de Esquelas y Avisos de Luto — v1.7

**Fecha:** 11 de noviembre de 2026. **Base de trabajo:** v1.6. **Producto:** LauraApps, Android nativo y demo HTML offline.

## Alcance

Se inspeccionó `MainActivity.java`, `ProjectRules.java`, el manifiesto, Gradle/CI, el icono de lanzamiento, la demo HTML/JavaScript, y los flujos de fotografías, proyectos, exportación, compartir, navegación y estados.

**Esta entrega no certifica ausencia absoluta de errores.** El SDK de Android/Gradle no está instalado en este entorno y no se obtuvo un APK compilado ni se ejecutaron pruebas reales Android. La compilación y el lint se configuraron en GitHub Actions.

## Hallazgos de la versión 1.6 y acciones en 1.7

| Prioridad | Riesgo real identificado | Acción aplicada | Cómo verificar |
|---|---|---|---|
| Crítica | Proyecto Android nuevo apuntaba a API 35, fuera del requisito de Google Play vigente desde agosto de 2026 | compileSdk/targetSdk 36, AGP 8.10.1, Gradle 8.11.1 y SDK 36 en CI | Build de GitHub Actions y revisión en Play Console |
| Alta | Foto guardada como URI externa; al mover la foto o perder permisos podía desaparecer del recuerdo | Decodificación acotada y copia privada a archivo JPG con escritura temporal+renombrado | Guardar un proyecto, borrar/mover el original y reabrir en Android |
| Alta | Lectura y decodificación de fotos en hilo de interfaz | Procesamiento en `ExecutorService` con resultado validado por generación de petición | Foto de 12–30 MP; rotación/cierre durante carga |
| Alta | Compresión PNG y escritura a galería bloqueaban la interfaz | Render al búfer y compresión/MediaStore en ejecutor de fondo; única exportación concurrente | Compartir PNG en dispositivo lento |
| Alta | Proyectos guardados podían contener índices y offsets malformados | Reglas de validación, saneamiento de textos, zoom y posición; IDs únicos al cargar | Reabrir copias antiguas/JSON de preferencias dañado |
| Alta | El botón Atrás de inicio entraba en bucle y no permitía salir | Navegación controlada y API predictiva para Android 13+ | Back desde editor y desde home |
| Alta | Aplicaciones Android 15/16 podían cubrir contenido con barra del sistema | Insets explícitos para API 35+, tema y elementos táctiles de altura mínima | Android 15 y 16, navegación por gestos y por tres botones |
| Media | Acumulación de permisos de lectura persistentes de documentos nuevos | Copia privada inmediata sin solicitar permisos persistentes nuevos | Seleccionar muchas fotos seguidas |
| Media | Fotografías privadas huérfanas tras editar/eliminar diseños | Limpieza diferida de archivos privados no referenciados por proyectos/borrador | Duplicar y eliminar proyectos, revisar almacenamiento |
| Media | Renderizar 12 miniaturas enormes en web tras cada tecla podía congelar UI | Miniaturas de baja resolución y actualización bajo demanda | Edición de texto rápida y cambios de plantilla |
| Media | Una foto grande se guardaba sin optimizar en IndexedDB | Normalización web a JPG acotado, validación de tipo/tamaño y error comprensible | Elegir fotos grandes y archivos corruptos |
| Media | Recuperar dos proyectos rápidamente podía mostrar la foto del proyecto anterior | Tokens de carga asíncrona, cancelación lógica por nueva selección | Abrir proyectos A/B rápidamente |
| Media | `toDataURL` elevaba consumo de memoria en descarga web | Exportación mediante Blob y Object URL temporal | Descargar PNG repetidamente |
| Media | Se podía indicar “guardado” cuando el navegador no ofrecía almacenamiento | Aviso claro de guardado temporal y recomendación de descargar PNG | Browser con almacenamiento bloqueado |
| Media | Nombres/IDs inválidos y fotos corruptas podían causar excepciones JS | Validaciones y captura de errores; preservación del trabajo ya cargado | Pruebas automatizadas con datos anómalos |
| Media | Elipsis por recorte podía romper pares de caracteres Unicode | Recorte por punto de código para no partir emojis | Nombres con emojis/acentos |

### Nota sobre los PNG

La exportación Android usa MediaStore con `IS_PENDING=1` durante la escritura y publica la imagen después de que termina la compresión. Los archivos incompletos se intentan borrar al fallar. Compartir usa un URI de MediaStore y permiso temporal de lectura.

### Nota sobre proyectos y fotos

- Las copias locales se guardan en el almacenamiento **privado** de la aplicación. No se solicitan permisos generales a la galería ni conexión de red.
- El editor mantiene hasta 25 proyectos con ID propio. Una copia duplicada puede compartir el archivo de la foto hasta que cambie la fotografía; eliminar un proyecto no debe borrar la foto de otra copia que la use.
- Las referencias de versiones anteriores (`content://`) se migran a la copia privada cuando se recuperan correctamente. Si la referencia original ya no se puede abrir, la app avisa al usuario para que la seleccione de nuevo.
- No se implementó nube ni sistema de cuentas. **Desinstalar la app puede eliminar proyectos privados**, por lo que el usuario debe exportar sus imágenes finales.

## Verificaciones ejecutadas

- `node --check` sobre la lógica completa de `demo/index.html`: **aprobado**.
- Navegador Chromium real con Playwright, prueba interactiva de la demo: **14 casos aprobados** (entre ellos 42 renders, carga de foto, duplicar, recuperar, eliminar, invalidación de plantilla y archivo corrupto).
- Java puro: `ProjectRules.java` compila con `javac`; **14 aserciones aprobadas**.
- Demostración con almacenamiento web bloqueado: la demo abre sin errores y muestra aviso de uso temporal.
- El icono aprobado permanece dentro del proyecto (`app_icon.jpg`), sin reemplazos.
- ZIP y estructura validados al generar el paquete.

## Pendiente obligatorio antes de afirmar estabilidad de la APK

1. Ejecutar en GitHub Actions `:app:assembleDebug :app:lintDebug` con SDK 36. **No se ejecutó en esta sesión**.
2. Instalar Android 10, Android 15 y Android 16; comprobar barras, botones, giros, teclado y retroceso.
3. Probar fotografías reales JPG/PNG/HEIC, verticales y horizontales, 12–30 MP, archivos dañados y fuente eliminada.
4. Probar cierre forzado/reapertura, cambio de permisos y recuperación de los 25 proyectos, más duplicados.
5. Revisar exportación 4:5 y 9:16, galería, WhatsApp, espacio bajo y rendimiento en gama baja.
6. Comprobar accesibilidad TalkBack, fuente aumentada 200%, tamaños de pantalla y contraste.
7. Ejecutar pruebas con usuarios reales para validar la claridad de la interfaz y resultado visual.
8. Antes de publicar, preparar firma y **AAB release**, fichas, privacidad y clasificación de contenido.

## Comandos de prueba

```bash
# Pruebas de Java puro
mkdir -p .build/test
javac -d .build/test app/src/main/java/com/lauraapps/esquelas/ProjectRules.java tests/ProjectRulesTest.java
java -ea -cp .build/test com.lauraapps.esquelas.ProjectRulesTest

# Sintaxis JavaScript
node --check tests/demo.js

# Navegador (requiere playwright + chromium + pillow)
python tests/test_web_e2e.py

# En entorno con Android SDK 36 y Gradle 8.11.1
gradle :app:assembleDebug :app:lintDebug --stacktrace
```

## Fuentes de compatibilidad utilizadas

- https://support.google.com/googleplay/android-developer/answer/11926878
- https://developer.android.com/build/releases/about-agp
- https://developer.android.com/develop/ui/views/layout/edge-to-edge

**Conclusión:** la versión 1.7 es una base de código más robusta y estática/web verificada. La aplicación Android aún debe compilarse y someterse a pruebas funcionales reales antes de aprobarla para Google Play.

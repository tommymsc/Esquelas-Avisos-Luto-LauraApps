# Verificación — Paso 4

Fecha: 11 de noviembre de 2026.

## Comprobado

- Se conservaron las 30 entradas de `STATUS_PRESETS` de Android y las 30 entradas de `readyPresets` de la demo.
- Código de la demo: sintaxis JavaScript validada mediante Node.js.
- Prueba interactiva en Chromium con contenido inyectado (el entorno bloquea navegación directa a URLs locales): sin excepciones JavaScript al editar texto, elegir frase sugerida, marcar favorito, guardar y duplicar proyecto, listar proyectos y abrir uno.
- Dos copias separadas registradas en la prueba; restauración del nombre verificada.
- Captura de esta prueba: `Prueba_editor_paso4.png`.
- El proyecto Android incluye favoritos, recientes, persistencia de proyectos, duplicación, frases y almacenamiento del último borrador. Verificación de código por inspección, no compilación.

## Pendiente / limitaciones

- La navegación directa de Chromium a `file://` o `http://localhost` está bloqueada administrativamente en este entorno; por ello no se verificó persistencia web entre reinicios reales ni el uso de IndexedDB en un origen normal.
- La prueba de navegador con contenido inyectado usa un origen restringido; no permite validar todas las operaciones de almacenamiento del dispositivo.
- No se compiló APK, no se realizó prueba de ejecución Android y no se deben dar por verificadas las funciones Android en teléfonos.
- Antes de producción: verificar que los proyectos preservan la autorización de lectura de cada foto Android y se restauran tras reiniciar, y comprobar la compatibilidad de compartir/guardar PNG.
- Evaluar nombres largos, fotografías de diferentes orientaciones, cancelación del selector y guardado con poco espacio.

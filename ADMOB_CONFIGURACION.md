# AdMob · Esquelas y Avisos de Luto v1.9

## Estado actual
La app incluye la integración técnica de **Google Mobile Ads 25.5.0 (SDK heredado aún compatible)** y **UMP 4.0.0**. La versión DEBUG utiliza exclusivamente identificadores de prueba oficiales de Google: **no produce ingresos**.

La versión RELEASE exige identificadores propios de **Esquelas y Avisos de Luto**, no de otra app de LauraApps. Si faltan, las unidades quedan vacías y no se solicitan anuncios. Antes de publicar se deben sustituir y verificar los identificadores y políticas de privacidad.

## Crear la aplicación en AdMob
1. Registrar como nueva aplicación Android: **Esquelas y Avisos de Luto**, paquete **com.lauraapps.esquelas**.
2. Obtener el **ID de aplicación**: `ca-app-pub-XXXXXXXXXXXXXXXX~YYYYYYYYYY`.
3. Crear una unidad **Banner**: `ca-app-pub-XXXXXXXXXXXXXXXX/YYYYYYYYYY`.
4. Crear una unidad **Intersticial**: `ca-app-pub-XXXXXXXXXXXXXXXX/ZZZZZZZZZZ`.
5. Configurar **Privacidad y mensajes** de AdMob para mercados relevantes y revisar consentimiento UMP.
6. En la compilación de producción configurar las propiedades de Gradle por mecanismo seguro, fuera del repositorio:
   `ADMOB_APP_ID`, `ADMOB_BANNER_UNIT_ID`, `ADMOB_INTERSTITIAL_UNIT_ID`.
   Ejemplo local: `gradle :app:bundleRelease -PADMOB_APP_ID=... -PADMOB_BANNER_UNIT_ID=... -PADMOB_INTERSTITIAL_UNIT_ID=...`
7. Verificar que el AAB definitivo contenga los identificadores propios y política de privacidad publicada, y completar formulario de seguridad de datos de Google Play.

## Ubicaciones y frecuencia
- **Banner adaptable:** en la pantalla de biblioteca, separado de la edición; no se inserta en la imagen ni encima de controles.
- **Intersticial:** solo cuando el usuario regresa a la biblioteca tras completar al menos **3 guardados de imagen** y han pasado **3 minutos** de sesión y desde el último intersticial.
- **No hay anuncios** durante la elección de fotografías, escritura, exportación ni el selector de compartir.
- **Sin red o consentimiento:** la creación/guardado de diseños sigue funcionando. Las solicitudes de anuncios solo ocurren si UMP permite solicitarlas.
- La opción de **privacidad y anuncios** aparece cuando UMP exige acceso a las opciones de privacidad.

## Antes de producción
- Confirmar que la app puede usarse sin Google Play Services y que un fallo publicitario no bloquea el editor.
- Verificar prueba en EE. UU., UE/EEE y Brasil; configurar las normativas aplicables, consentimiento UMP y, si corresponde, reglas para niños.
- Declarar el uso de SDKs de publicidad y los datos recopilados/compartidos en **Google Play Data Safety**.
- Añadir política de privacidad pública real; no reutilizar una política que diga «sin internet/anuncios» y esté desactualizada.
- Aplicar `app-ads.txt` conforme a AdMob con el dominio del desarrollador.
- Activar AdMob solo con los identificadores reales de esta aplicación, no de otra.
- Revisar reportes de ANR, crashes, rendimiento de anuncios y retención tras publicación.
- **Ninguna cantidad de ingresos está garantizada.** Los anuncios DEBUG no pagan.

## Referencias oficiales
- https://developers.google.com/admob/android/privacy
- https://developers.google.com/admob/android/test-ads
- https://developers.google.com/admob/android/interstitial
- https://support.google.com/admob/answer/6201362

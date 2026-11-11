# Publicación de Esquelas y Avisos de Luto · Android v2.0.0

## Identidad de la versión
- Aplicación: **Esquelas y Avisos de Luto**
- Paquete: `com.lauraapps.esquelas`
- VersionCode: `11`
- VersionName: `2.0.0`
- Android compileSdk/targetSdk: 36
- AdMob: ID de aplicación y unidades **propias**, solo en variante release
- Debug usa IDs de anuncios de Google para pruebas. No subas debug a Google Play.

## Flujo de anuncios seguro
- Banner adaptable de AdMob en la biblioteca desde la apertura, cuando la gestión UMP permite anuncios y hay un banner disponible.
- No usar un intersticial genérico al primer inicio de la aplicación; se necesita unidad específica de formato «App Open» para ese caso. Google recomienda empezar a mostrarlos tras varias aperturas y únicamente durante una pantalla de carga.
- Guardar imagen: el PNG se escribe y finaliza primero en MediaStore.
- Tras **3 guardados completados**, con al menos 3 minutos desde el inicio y desde el último intersticial, se puede mostrar un intersticial ya precargado antes de la confirmación del guardado.
- Si el anuncio no está listo, falla, no tiene consentimiento o se cierra, la foto queda guardada y la confirmación aparece igual.
- No hay anuncios incrustados en diseños ni durante escritura/elección de fotografía.

## AAB de Google Play
El workflow de GitHub genera:
1. `app-debug.apk` solo para probar.
2. `app-release.aab` **SIN FIRMA**. No subas un AAB sin firmar a Play Console.

Para tener un AAB instalable en Play Console se firma con una **clave de subida (upload key)** mediante `jarsigner`, y se valida con `jarsigner -verify -verbose -certs`.

Google Play App Signing conserva la clave de firma de distribución de Google. El desarrollador **debe custodiar la clave de subida** para actualizaciones futuras. Nunca subir `.jks`, `.keystore`, contraseñas o credenciales a repositorios públicos ni secretos expuestos en logs.

## Checklist Play Console
1. Crear la aplicación Android en Play Console (si no existe), ID de paquete `com.lauraapps.esquelas`.
2. Activar Google Play App Signing, usando la clave de subida asociada al AAB.
3. Subir el **AAB firmado**, no debug APK.
4. Añadir URL de política de privacidad actualizada (incluye AdMob, UMP y datos tratados por publicidad).
5. Declaraciones de seguridad de datos, anuncios, clasificación, contenido, público objetivo, acceso a aplicación.
6. Capturas reales, icono, imágenes promocionales, descripción corta/larga y datos de contacto.
7. Comprobar AdMob «Debe revisarse» y vinculación de ficha de tienda cuando esté disponible.
8. Revisar `app-ads.txt` y estado de verificación de aplicación en AdMob.
9. Probar en prueba interna/cerrada antes de producción y cumplir cualquier requisito de acceso a producción de la cuenta de Play Console.
10. Tras publicación, verificar Ads / ANR / Crash / retención y que la descarga funciona sin conectividad.

## Advertencia
El AAB firmado y la configuración de AdMob **no equivalen a aprobación o publicación**. Play Console puede solicitar cambios o verificaciones antes de aprobarlo.

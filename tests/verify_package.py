from pathlib import Path
import re
root=Path(__file__).resolve().parents[1]
manifest=(root/'app/src/main/AndroidManifest.xml').read_text()
gradle=(root/'app/build.gradle').read_text()
ci=(root/'.github/workflows/android.yml').read_text()
java=(root/'app/src/main/java/com/lauraapps/esquelas/MainActivity.java').read_text()
html=(root/'demo/index.html').read_text()
assert 'android.permission.INTERNET' in manifest
assert 'com.google.android.gms.ads.APPLICATION_ID' in manifest
assert 'android:allowBackup="false"' in manifest
assert 'targetSdk 36' in gradle and 'compileSdk 36' in gradle
assert 'versionName \'1.9.1\'' in gradle
assert 'gradle-version: \'8.11.1\'' in ci
assert 'platforms;android-36' in ci
assert 'private_portraits' in java
assert 'ExecutorService photoWorker' in java
assert 'ExecutorService exportWorker' in java
assert 'IS_PENDING' in java
assert 'getOnBackInvokedDispatcher' in java
assert 'function drawTiles()' in html and 'function prepareWebPhoto' in html
assert (root/'app/src/main/res/drawable/app_icon.jpg').is_file()
assert len(re.findall(r'\{"[^"\n]+", "[^"\n]+", "[^"\n]+"\}',java))>=30
assert 'Falta la fotografía' in java and 'Elegir fotografía' in java
assert 'RippleDrawable' in java
assert 'setFocusable(true)' in java
assert 'No fue posible abrir el menú para compartir' in java
assert 'GOLD=Color.rgb(128,92,50)' in java
ads=(root/'app/src/main/java/com/lauraapps/esquelas/AdsController.java').read_text()
assert 'com.google.android.gms:play-services-ads:25.5.0' in gradle
assert 'com.google.android.ump:user-messaging-platform:4.0.0' in gradle
assert 'ca-app-pub-3940256099942544/9214589741' in gradle
assert 'getLargeAnchoredAdaptiveBannerAdSize' in ads
assert 'canRequestAds()' in ads
assert 'MIN_DELAY_MS' in ads and 'completedDownloads < 3' in ads
assert 'renderReadyStatus(ca,w,h,d);\n            return;' in java
assert 'final int dedicationY=story?1755:1225;' in java
assert 'final int middleDivider=story?1460:1010;' in java
assert 'ca-app-pub-2506564165861733~2453279576' in gradle
assert 'ca-app-pub-2506564165861733/5215174288' in gradle
assert 'ca-app-pub-2506564165861733/4281366564' in gradle
assert "manifestPlaceholders = [ADMOB_APP_ID: 'ca-app-pub-3940256099942544~3347511713']" in gradle
assert 'drawPremiumAccents' in java
assert 'ads.attachBanner(homeBanner)' in java
assert 'ads.destroy()' in java
print('PASS 27 checks: privacy, AdMob test IDs, UMP, UX, export, Android 16, assets')

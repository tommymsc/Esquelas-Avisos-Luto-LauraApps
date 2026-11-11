from pathlib import Path
import re
root=Path(__file__).resolve().parents[1]
manifest=(root/'app/src/main/AndroidManifest.xml').read_text()
gradle=(root/'app/build.gradle').read_text()
ci=(root/'.github/workflows/android.yml').read_text()
java=(root/'app/src/main/java/com/lauraapps/esquelas/MainActivity.java').read_text()
html=(root/'demo/index.html').read_text()
assert 'android.permission.INTERNET' not in manifest
assert 'android:allowBackup="false"' in manifest
assert 'targetSdk 36' in gradle and 'compileSdk 36' in gradle
assert 'versionName \'1.7.0\'' in gradle
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
print('PASS 13 checks: privacy, Android 16, build settings, photos, export, navigation, templates and icon')

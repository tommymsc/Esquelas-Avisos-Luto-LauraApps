#!/usr/bin/env python3
"""Secure CI signing setup: only sign release with complete app-specific AdMob IDs and upload key.
No private keys or ad IDs are printed or committed.
"""
from pathlib import Path
import os
import re
import base64

secret_names = (
    "ESQUELAS_UPLOAD_KEYSTORE_BASE64",
    "ESQUELAS_UPLOAD_KEYSTORE_PASSWORD",
    "ESQUELAS_UPLOAD_KEY_ALIAS",
    "ESQUELAS_UPLOAD_KEY_PASSWORD",
    "ESQUELAS_ADMOB_APP_ID",
    "ESQUELAS_ADMOB_BANNER_UNIT_ID",
    "ESQUELAS_ADMOB_INTERSTITIAL_UNIT_ID",
)
secrets = {key: os.environ.get(key, "") for key in secret_names}
out = Path(os.environ["GITHUB_OUTPUT"])
enabled = all(secrets.values()) and os.environ.get("GITHUB_EVENT_NAME") == "push"
with out.open("a", encoding="utf-8") as stream:
    stream.write("release_ready=" + ("true" if enabled else "false") + "\n")
if not enabled:
    print("Release AAB disabled: a required signing/AdMob secret is unavailable or this is a pull request.")
    raise SystemExit(0)
if not re.fullmatch(r"ca-app-pub-[0-9]{16}~[0-9]{10}", secrets["ESQUELAS_ADMOB_APP_ID"]):
    raise SystemExit("Invalid app-specific AdMob app ID format.")
if secrets["ESQUELAS_ADMOB_APP_ID"].startswith("ca-app-pub-3940256099942544"):
    raise SystemExit("Production cannot use Google test app ID.")
for key in ("ESQUELAS_ADMOB_BANNER_UNIT_ID", "ESQUELAS_ADMOB_INTERSTITIAL_UNIT_ID"):
    if not re.fullmatch(r"ca-app-pub-[0-9]{16}/[0-9]{10}", secrets[key]) or secrets[key].startswith("ca-app-pub-3940256099942544"):
        raise SystemExit("Invalid production AdMob unit ID or test ID.")
try:
    data = base64.b64decode(secrets["ESQUELAS_UPLOAD_KEYSTORE_BASE64"], validate=True)
except Exception as exc:
    raise SystemExit("Invalid upload keystore base64.") from exc
if len(data) < 100:
    raise SystemExit("Invalid or empty upload keystore.")
keystore = Path(os.environ["RUNNER_TEMP"]) / "esquelas-upload.jks"
keystore.write_bytes(data)
with Path(os.environ["GITHUB_ENV"]).open("a", encoding="utf-8") as stream:
    stream.write(f"ESQUELAS_UPLOAD_KEYSTORE_PATH={keystore}\n")
print("Production release signing configured; actual key and AdMob IDs kept private.")

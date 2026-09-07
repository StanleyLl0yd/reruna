#!/usr/bin/env python3
import xml.etree.ElementTree as ET
from pathlib import Path

ANDROID = "{http://schemas.android.com/apk/res/android}"
MANIFEST = Path("app/src/main/AndroidManifest.xml")
ALLOWED_EXPORTED = {".MainActivity"}

root = ET.parse(MANIFEST).getroot()
errors = []

permissions = [node.get(ANDROID + "name", "") for node in root.findall("uses-permission")]
if permissions:
    errors.append("Android permissions are not allowed by the current offline baseline: " + ", ".join(sorted(permissions)))

application = root.find("application")
if application is None:
    errors.append("Missing <application> element")
else:
    if application.get(ANDROID + "usesCleartextTraffic") != "false":
        errors.append('android:usesCleartextTraffic must be explicitly "false"')
    if application.get(ANDROID + "debuggable") == "true":
        errors.append("android:debuggable must not be enabled in the manifest")

    exported = []
    for tag in ("activity", "activity-alias", "service", "receiver", "provider"):
        for node in application.findall(tag):
            if node.get(ANDROID + "exported") == "true":
                exported.append((tag, node.get(ANDROID + "name", "")))

    unexpected = [
        f"{tag}:{name}"
        for tag, name in exported
        if tag != "activity" or name not in ALLOWED_EXPORTED
    ]
    if unexpected:
        errors.append("Unexpected exported Android components: " + ", ".join(sorted(unexpected)))

    launcher = [node for node in application.findall("activity") if node.get(ANDROID + "name") == ".MainActivity"]
    if len(launcher) != 1 or launcher[0].get(ANDROID + "exported") != "true":
        errors.append(".MainActivity must be the single exported launcher activity")

if errors:
    raise SystemExit("\n".join(errors))

print("Android manifest security baseline: OK")

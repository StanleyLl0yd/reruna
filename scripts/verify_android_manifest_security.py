#!/usr/bin/env python3
import re
from pathlib import Path

MANIFEST = Path("app/src/main/AndroidManifest.xml")
ALLOWED_EXPORTED = {("activity", ".MainActivity")}
COMPONENT = re.compile(r"<(activity|activity-alias|service|receiver|provider)\b([^>]*)>", re.DOTALL)
ATTRIBUTE = re.compile(r'android:([A-Za-z0-9_]+)="([^"]*)"')

content = MANIFEST.read_text(encoding="utf-8")
errors = []

if re.search(r"<uses-permission\b", content):
    errors.append("Android permissions are not allowed by the current offline baseline")

if 'android:usesCleartextTraffic="false"' not in content:
    errors.append('android:usesCleartextTraffic must be explicitly "false"')

if 'android:debuggable="true"' in content:
    errors.append("android:debuggable must not be enabled in the manifest")

exported = []
for tag, raw_attributes in COMPONENT.findall(content):
    attributes = dict(ATTRIBUTE.findall(raw_attributes))
    if attributes.get("exported") == "true":
        exported.append((tag, attributes.get("name", "")))

unexpected = sorted(set(exported) - ALLOWED_EXPORTED)
if unexpected:
    errors.append(
        "Unexpected exported Android components: "
        + ", ".join(f"{tag}:{name}" for tag, name in unexpected)
    )

if exported.count(("activity", ".MainActivity")) != 1:
    errors.append(".MainActivity must be the single exported launcher activity")

if "android.intent.action.MAIN" not in content or "android.intent.category.LAUNCHER" not in content:
    errors.append(".MainActivity must retain the MAIN/LAUNCHER intent filter")

if errors:
    raise SystemExit("\n".join(errors))

print("Android manifest security baseline: OK")

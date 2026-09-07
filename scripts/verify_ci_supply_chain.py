#!/usr/bin/env python3
import re
from pathlib import Path

ROOTS = (Path(".github/workflows"), Path(".github/actions"))
ACTION_REF = re.compile(r"^\s*uses:\s*([^\s#]+)")
IMAGE = re.compile(r"^\s*image:\s*([^\s#]+)")
FULL_SHA = re.compile(r"^[0-9a-f]{40}$")
DIGEST = re.compile(r"@sha256:[0-9a-f]{64}$")
PINNED_GRADLE_VERSION = "9.6.0"
PINNED_GRADLE_SHA256 = "bbaeb2fef8710818cf0e261201dab964c572f92b942812df0c3620d62a529a01"

errors = []

for root in ROOTS:
    for path in sorted(root.rglob("*")):
        if path.suffix not in {".yml", ".yaml"}:
            continue
        content = path.read_text(encoding="utf-8")
        if "pull_request_target:" in content:
            errors.append(f"{path}: pull_request_target is forbidden")
        if "persist-credentials: true" in content:
            errors.append(f"{path}: checkout credentials must not persist")
        if "secrets: inherit" in content:
            errors.append(f"{path}: secrets inheritance is forbidden")
        if path.parent == Path(".github/workflows") and not re.search(
            r"(?m)^permissions:\s*(?:\{\}|$)", content
        ):
            errors.append(f"{path}: explicit workflow permissions are required")

        for number, line in enumerate(content.splitlines(), start=1):
            action = ACTION_REF.match(line)
            if action:
                target = action.group(1)
                if target.startswith("./") or target.startswith("docker://"):
                    continue
                if "@" not in target:
                    errors.append(f"{path}:{number}: action is not pinned")
                    continue
                _, ref = target.rsplit("@", 1)
                if not FULL_SHA.fullmatch(ref):
                    errors.append(f"{path}:{number}: action ref must be a full 40-character SHA")

            image = IMAGE.match(line)
            if image:
                target = image.group(1)
                if target.startswith("${{"):
                    continue
                if not DIGEST.search(target):
                    errors.append(f"{path}:{number}: container image must be pinned by sha256 digest")

setup_action = Path(".github/actions/android-gradle-setup/action.yml").read_text(encoding="utf-8")
if f'version="{PINNED_GRADLE_VERSION}"' not in setup_action:
    errors.append("Android Gradle setup must pin the expected Gradle version")
if PINNED_GRADLE_SHA256 not in setup_action:
    errors.append("Android Gradle setup must pin the expected Gradle distribution SHA-256")
if "sha256sum --check --strict" not in setup_action:
    errors.append("Android Gradle setup must verify the Gradle distribution checksum")

if errors:
    raise SystemExit("\n".join(errors))

print("GitHub Actions supply-chain policy: OK")

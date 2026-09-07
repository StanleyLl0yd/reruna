#!/usr/bin/env python3
import hashlib
import re
import stat
from pathlib import Path

ROOTS = (Path(".github/workflows"), Path(".github/actions"))
ACTION_REF = re.compile(r"^\s*uses:\s*([^\s#]+)")
IMAGE = re.compile(r"^\s*image:\s*([^\s#]+)")
FULL_SHA = re.compile(r"^[0-9a-f]{40}$")
DIGEST = re.compile(r"@sha256:[0-9a-f]{64}$")
GRADLE_VERSION = "9.6.0"
GRADLE_DISTRIBUTION_SHA256 = "bbaeb2fef8710818cf0e261201dab964c572f92b942812df0c3620d62a529a01"
GRADLE_WRAPPER_JAR_SHA256 = "497c8c2a7e5031f6aa847f88104aa80a93532ec32ee17bdb8d1d2f67a194a9c7"

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

wrapper_properties = Path("gradle/wrapper/gradle-wrapper.properties")
wrapper_jar = Path("gradle/wrapper/gradle-wrapper.jar")
gradlew = Path("gradlew")

if not wrapper_properties.is_file():
    errors.append("Gradle wrapper properties are missing")
else:
    properties = wrapper_properties.read_text(encoding="utf-8")
    expected_url = f"distributionUrl=https\\://services.gradle.org/distributions/gradle-{GRADLE_VERSION}-bin.zip"
    expected_checksum = f"distributionSha256Sum={GRADLE_DISTRIBUTION_SHA256}"
    if expected_url not in properties:
        errors.append("Gradle wrapper distribution URL/version is not pinned as expected")
    if expected_checksum not in properties:
        errors.append("Gradle wrapper distribution SHA-256 is missing or unexpected")
    if "validateDistributionUrl=true" not in properties:
        errors.append("Gradle wrapper must validate its distribution URL")

if not wrapper_jar.is_file():
    errors.append("Gradle wrapper JAR is missing")
else:
    wrapper_digest = hashlib.sha256(wrapper_jar.read_bytes()).hexdigest()
    if wrapper_digest != GRADLE_WRAPPER_JAR_SHA256:
        errors.append("Gradle wrapper JAR SHA-256 does not match the approved Gradle release")

if not gradlew.is_file():
    errors.append("Gradle wrapper launcher is missing")
elif not gradlew.stat().st_mode & stat.S_IXUSR:
    errors.append("Gradle wrapper launcher must be executable")

if errors:
    raise SystemExit("\n".join(errors))

print("GitHub Actions and Gradle supply-chain policy: OK")

#!/bin/bash
set -euo pipefail

PACKAGE="${PACKAGE:-com.sl.reruna}"
OUT_DIR="${1:-playtest-profile-$(date +%Y%m%d-%H%M%S)}"

command -v adb >/dev/null 2>&1 || {
  echo "adb not found. Install Android platform-tools first." >&2
  exit 1
}

adb get-state >/dev/null 2>&1 || {
  echo "No authorized Android device is connected." >&2
  exit 1
}

mkdir -p "$OUT_DIR"

{
  echo "package=$PACKAGE"
  echo "captured_at=$(date -u +%Y-%m-%dT%H:%M:%SZ)"
  echo
  adb shell getprop ro.product.manufacturer 2>/dev/null || true
  adb shell getprop ro.product.model 2>/dev/null || true
  adb shell getprop ro.build.version.release 2>/dev/null || true
  adb shell getprop ro.build.version.sdk 2>/dev/null || true
  adb shell wm size 2>/dev/null || true
  adb shell wm density 2>/dev/null || true
  adb shell settings get system peak_refresh_rate 2>/dev/null || true
  adb shell settings get system min_refresh_rate 2>/dev/null || true
} > "$OUT_DIR/device.txt"

adb shell dumpsys meminfo "$PACKAGE" > "$OUT_DIR/meminfo-before.txt"
adb shell dumpsys gfxinfo "$PACKAGE" reset >/dev/null

echo "Play RERUNA normally for about 5 minutes, including at least one restart."
read -r -p "Press Enter when the play interval is finished..."

adb shell dumpsys gfxinfo "$PACKAGE" framestats > "$OUT_DIR/gfxinfo-framestats.txt"
adb shell dumpsys meminfo "$PACKAGE" > "$OUT_DIR/meminfo-after.txt"

echo "Profile captured in: $OUT_DIR"

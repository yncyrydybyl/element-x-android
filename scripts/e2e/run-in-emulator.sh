#!/usr/bin/env bash
# Runs inside the reactivecircus/android-emulator-runner script block.
# Drives the Android emulator through the live-location sharing flow and
# asserts that a headless matrix-nio receiver sees the expected beacons.
set +e
set -x

ARTIFACTS=e2e-artifacts
mkdir -p "$ARTIFACTS"
echo "boot" > "$ARTIFACTS/.alive"

emit_annotation() {
  local level=$1 title=$2 path=$3
  if [ -s "$path" ]; then
    local msg
    # Annotation message limit is 64KB. Keep the last 60000 chars.
    msg=$(tail -c 60000 "$path" | sed 's/%/%25/g; s/\r/%0D/g' | tr '\n' '\001' | sed 's/\x01/%0A/g')
    echo "::${level} title=${title}::${msg}"
  fi
}

on_exit() {
  local rc=${1:-?}
  echo "=== on_exit (rc=$rc) artifacts dir ==="
  ls -la "$ARTIFACTS/" || true
  echo "=== Synapse /_matrix/client/versions ==="
  curl -sS http://localhost:8008/_matrix/client/versions 2>&1 | tee "$ARTIFACTS/synapse-versions.json" || true

  echo "=== Room state (did sender publish any beacon events?) ==="
  if [ -n "${E2E_SENDER_TOKEN:-}" ] && [ -n "${E2E_ROOM_ID:-}" ]; then
    curl -sS -H "Authorization: Bearer ${E2E_SENDER_TOKEN}" \
      "http://localhost:8008/_matrix/client/v3/rooms/${E2E_ROOM_ID}/state" \
      > "$ARTIFACTS/room-state.json" 2>&1 || true
    python3 - <<'PY' > "$ARTIFACTS/room-state-summary.txt" 2>&1 || true
import json
try:
    s = json.load(open("e2e-artifacts/room-state.json"))
    if isinstance(s, list):
        for e in s:
            t = e.get("type") or ""
            k = e.get("state_key", "")
            sender = e.get("sender", "")
            print(f"{t}  state_key={k!r}  sender={sender}")
        print("---beacon state events---")
        for e in s:
            t = (e.get("type") or "").lower()
            if "beacon" in t or "location" in t:
                print(json.dumps(e, indent=2))
    else:
        print("unexpected response:", json.dumps(s)[:500])
except Exception as ex:
    print("parse error:", ex)
PY
    cat "$ARTIFACTS/room-state-summary.txt" || true
    echo "=== Room messages (any m.beacon timeline events?) ==="
    curl -sS -H "Authorization: Bearer ${E2E_SENDER_TOKEN}" \
      "http://localhost:8008/_matrix/client/v3/rooms/${E2E_ROOM_ID}/messages?dir=b&limit=50" \
      > "$ARTIFACTS/room-messages.json" 2>&1 || true
    python3 - <<'PY' > "$ARTIFACTS/room-messages-summary.txt" 2>&1 || true
import json
try:
    s = json.load(open("e2e-artifacts/room-messages.json"))
    chunk = s.get("chunk", []) if isinstance(s, dict) else []
    for e in chunk:
        print(f"{e.get('type')}  sender={e.get('sender')}")
    print("---beacon/location timeline events---")
    for e in chunk:
        t = (e.get("type") or "").lower()
        if "beacon" in t or "location" in t:
            print(json.dumps(e, indent=2))
except Exception as ex:
    print("parse error:", ex)
PY
    cat "$ARTIFACTS/room-messages-summary.txt" || true
  else
    echo "(E2E_SENDER_TOKEN or E2E_ROOM_ID missing)"
  fi

  echo "=== UI hierarchy (last known) ==="
  adb shell uiautomator dump /sdcard/ui.xml 2>&1 || true
  adb exec-out cat /sdcard/ui.xml > "$ARTIFACTS/ui.xml" 2>/dev/null || true
  head -c 4000 "$ARTIFACTS/ui.xml" 2>/dev/null || echo "(no ui.xml)"
  echo "=== verifier.log ==="
  cat "$ARTIFACTS/verifier.log" 2>/dev/null || echo "(no verifier.log)"
  echo "=== maestro.log (tail 200) ==="
  tail -n 200 "$ARTIFACTS/maestro.log" 2>/dev/null || echo "(no maestro.log)"
  echo "=== logcat: Element X app only ==="
  grep -E "io\.element|LiveLocation|startForeground|startLiveLocation|sendLiveLocation|matrix\.rust" \
    "$ARTIFACTS/logcat.txt" 2>/dev/null | tail -n 200 | tee "$ARTIFACTS/logcat-app.txt" || true
  emit_annotation warning "maestro.log" "$ARTIFACTS/maestro.log"
  emit_annotation warning "logcat-app" "$ARTIFACTS/logcat-app.txt"
  emit_annotation warning "verifier.log" "$ARTIFACTS/verifier.log"
  emit_annotation notice "room-state-summary" "$ARTIFACTS/room-state-summary.txt"
  emit_annotation notice "room-messages-summary" "$ARTIFACTS/room-messages-summary.txt"
  emit_annotation notice "synapse-versions" "$ARTIFACTS/synapse-versions.json"
  emit_annotation notice "ui.xml (head)" "$ARTIFACTS/ui.xml"
  echo "::notice title=e2e trap::exit=$rc maestro_rc=${MAESTRO_RC:-?} verifier_rc=${VERIFIER_RC:-?}"
}
trap 'on_exit $?' EXIT

# Install Maestro if it's not already on PATH.
if [ ! -x "$HOME/.maestro/bin/maestro" ]; then
  curl -Ls "https://get.maestro.mobile.dev" | bash
fi
export PATH="$HOME/.maestro/bin:$PATH"

echo "=== environment ==="
echo "pwd=$(pwd)"
echo "PATH=$PATH"
echo "HOME=$HOME"
which adb || true
which maestro || true
maestro --version || true

echo "=== APKs produced ==="
ls -la app/build/outputs/apk/gplay/debug/ || true

adb logcat -c || true
adb logcat -v time > "$ARTIFACTS/logcat.txt" 2>&1 &
LOGCAT_PID=$!

echo "=== APK install ==="
adb install app/build/outputs/apk/gplay/debug/app-gplay-x86_64-debug.apk \
  || adb install app/build/outputs/apk/gplay/debug/app-gplay-arm64-v8a-debug.apk \
  || adb install app/build/outputs/apk/gplay/debug/app-gplay-universal-debug.apk \
  || echo "APK install FAILED"

adb shell pm grant io.element.android.x.debug android.permission.ACCESS_FINE_LOCATION || true
adb shell pm grant io.element.android.x.debug android.permission.ACCESS_COARSE_LOCATION || true
adb emu geo fix 13.4050 52.5200 || true

echo "=== starting verifier in background ==="
HS_URL=http://localhost:8008 \
  python3 scripts/e2e/verify_beacon.py > "$ARTIFACTS/verifier.log" 2>&1 &
VERIFIER_PID=$!

export MAESTRO_HOMESERVER_URL=http://10.0.2.2:8008
export MAESTRO_USER="${E2E_SENDER_USER:-}"
export MAESTRO_PASSWORD="${E2E_SENDER_PASS:-}"
export MAESTRO_ROOM_NAME="e2e"

echo "=== running maestro ==="
maestro test \
  --debug-output "$ARTIFACTS/maestro" \
  scripts/e2e/maestro/live-location.yaml \
  2>&1 | tee "$ARTIFACTS/maestro.log"
MAESTRO_RC=${PIPESTATUS[0]}
echo "maestro exited with rc=$MAESTRO_RC"

for i in 1 2 3 4 5 6 7 8 9 10; do
  lon=$(awk "BEGIN { printf \"%.4f\", 13.4050 + 0.001 * $i }")
  lat=$(awk "BEGIN { printf \"%.4f\", 52.5200 + 0.001 * $i }")
  adb emu geo fix "$lon" "$lat" || true
  sleep 3
done

VERIFIER_RC=0
wait "$VERIFIER_PID" || VERIFIER_RC=$?

adb exec-out screencap -p > "$ARTIFACTS/final-screen.png" 2>/dev/null || true
kill "$LOGCAT_PID" 2>/dev/null || true

# Return non-zero if either side failed.
test "$MAESTRO_RC" = 0 && test "$VERIFIER_RC" = 0

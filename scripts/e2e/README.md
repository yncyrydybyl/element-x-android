# Live location E2E (experimental)

`.github/workflows/live-location-e2e.yml` runs an end-to-end test of the live-location
sharing flow without needing any external services or two phones. The plumbing:

1. **Synapse** (Docker, local) – a Matrix homeserver with open registration.
2. **Two test users** (`sender`, `receiver`) + a shared public room, created via
   Synapse's admin register API.
3. **Sender** – an Android emulator (API 34, x86_64) running the debug APK. A
   Maestro flow drives the UI: pick server, log in, open the room, tap the
   location pin in the top app bar, accept the permission + disclaimer, pick
   15 minutes.
4. **Receiver** – a headless Python `matrix-nio` client that joins the same room
   and asserts it sees the `m.beacon_info` state event and at least one
   `m.beacon` location update (`E2E_MIN_BEACONS`, default 1).
5. **GPS** – fake coordinates are injected into the emulator via `adb emu geo
   fix` so the foreground service actually has something to report.

## Triggering

Only on-demand: **Actions tab → Live location E2E → Run workflow**.

## Expected failure modes during stabilisation

- Maestro selectors may drift with UI copy changes. Update
  `scripts/e2e/maestro/live-location.yaml`.
- Synapse service container bootstrapping is finicky — see the "Start Synapse"
  step.
- Emulator flakiness, especially on first boot. Re-run.
- Element X login path — if the onboarding flow changes, the Maestro flow will
  miss a screen.

## Local run

To debug locally you need:

- Docker
- A connected Android device or emulator
- Maestro CLI (`curl -Ls https://get.maestro.mobile.dev | bash`)
- Python 3.10+ with `matrix-nio`

Then:

```sh
docker run --rm -d -p 8008:8008 -v "$(pwd)/scripts/e2e/synapse:/e2e-config" \
  --name synapse_e2e matrixdotorg/synapse:latest
# wait for http://localhost:8008/_matrix/client/versions to answer
GITHUB_ENV=/tmp/env scripts/e2e/setup-synapse.sh
source /tmp/env
adb install app/build/outputs/apk/gplay/debug/app-gplay-arm64-v8a-debug.apk
adb shell pm grant io.element.android.x.debug android.permission.ACCESS_FINE_LOCATION
adb emu geo fix 13.4050 52.5200

python3 -m pip install -r scripts/e2e/requirements.txt
HS_URL=http://localhost:8008 python3 scripts/e2e/verify_beacon.py &

MAESTRO_HOMESERVER_URL=http://10.0.2.2:8008 \
MAESTRO_USER=$E2E_SENDER_USER MAESTRO_PASSWORD=$E2E_SENDER_PASS MAESTRO_ROOM_NAME=e2e \
maestro test scripts/e2e/maestro/live-location.yaml
```

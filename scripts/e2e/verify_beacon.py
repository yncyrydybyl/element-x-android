"""
Headless Matrix receiver that verifies the live-location beacon flow.

Uses the raw /sync HTTP endpoint (via httpx) rather than matrix-nio's
high-level typed events, because nio silently discards unknown event
types like `org.matrix.msc3672.beacon_info` that live-location sharing
uses.

Expects environment variables (set by setup-synapse.sh):
- HS_URL                   homeserver URL, default http://localhost:8008
- E2E_RECEIVER_TOKEN       access token
- E2E_ROOM_ID              shared room id
- E2E_VERIFY_TIMEOUT_S     total wait before giving up (default 180)
- E2E_MIN_BEACONS          minimum number of m.beacon updates required (default 0)

Exits 0 on success, 2 if beacon_info never arrives, 3 if fewer than
E2E_MIN_BEACONS beacon events arrived.
"""
import json
import os
import sys
import time
import urllib.error
import urllib.parse
import urllib.request


HS_URL = os.environ.get("HS_URL", "http://localhost:8008").rstrip("/")
TOKEN = os.environ["E2E_RECEIVER_TOKEN"]
ROOM_ID = os.environ["E2E_ROOM_ID"]
TIMEOUT_S = int(os.environ.get("E2E_VERIFY_TIMEOUT_S", "180"))
MIN_BEACONS = int(os.environ.get("E2E_MIN_BEACONS", "0"))

BEACON_INFO_TYPES = (
    "org.matrix.msc3672.beacon_info",
    "org.matrix.msc3489.beacon_info",
    "m.beacon_info",
)
BEACON_TYPES = (
    "org.matrix.msc3672.beacon",
    "org.matrix.msc3489.beacon",
    "m.beacon",
)


def _type_matches(etype, candidates):
    return any(etype == t or etype.startswith(t + ".") for t in candidates)


def http_get(path, read_timeout=60):
    url = HS_URL + path
    req = urllib.request.Request(url, headers={"Authorization": f"Bearer {TOKEN}"})
    with urllib.request.urlopen(req, timeout=read_timeout) as resp:
        return json.loads(resp.read())


def main():
    deadline = time.monotonic() + TIMEOUT_S
    saw_beacon_info = False
    beacon_count = 0
    next_batch = None

    print(f"Verifier polling {HS_URL} for room {ROOM_ID}, up to {TIMEOUT_S}s "
          f"(need beacon_info + {MIN_BEACONS} beacon updates)", flush=True)

    # Initial full sync to catch already-published state events.
    params = {
        "timeout": "0",
        "filter": json.dumps({
            "room": {
                "timeline": {"limit": 100},
                "state": {"lazy_load_members": False},
            },
            "account_data": {"types": []},
            "presence": {"types": []},
        }),
    }
    init = http_get("/_matrix/client/v3/sync?" + urllib.parse.urlencode(params))
    next_batch = init.get("next_batch")
    join = ((init.get("rooms") or {}).get("join") or {}).get(ROOM_ID) or {}
    state_events = (join.get("state") or {}).get("events", [])
    timeline_events = (join.get("timeline") or {}).get("events", [])
    for e in state_events + timeline_events:
        t = e.get("type") or ""
        if _type_matches(t, BEACON_INFO_TYPES):
            print(f"[beacon_info/initial] {t} from {e.get('sender')}: {e.get('content')}", flush=True)
            saw_beacon_info = True
        elif _type_matches(t, BEACON_TYPES):
            beacon_count += 1
            print(f"[beacon/initial #{beacon_count}] {t} from {e.get('sender')}: {e.get('content')}", flush=True)

    # Also query the room state endpoint directly — belt and suspenders,
    # since state events that existed BEFORE the verifier joined can get
    # consolidated into join.state which isn't always included in sync.
    try:
        room_state = http_get(f"/_matrix/client/v3/rooms/{urllib.parse.quote(ROOM_ID)}/state")
        for e in room_state if isinstance(room_state, list) else []:
            t = e.get("type") or ""
            if _type_matches(t, BEACON_INFO_TYPES):
                print(f"[beacon_info/state-endpoint] {t} from {e.get('sender')}: {e.get('content')}", flush=True)
                saw_beacon_info = True
    except Exception as ex:
        print(f"(state endpoint probe failed: {ex})", flush=True)

    # Poll sync in short bursts for new beacon updates. Also re-query the
    # /state endpoint each iteration — state events that existed before
    # the verifier's /sync cursor started may not appear in incremental
    # syncs under all conditions.
    while time.monotonic() < deadline:
        if saw_beacon_info and beacon_count >= MIN_BEACONS:
            print(f"SUCCESS: beacon_info + {beacon_count} beacon update(s) seen.", flush=True)
            return 0
        try:
            rs = http_get(f"/_matrix/client/v3/rooms/{urllib.parse.quote(ROOM_ID)}/state", read_timeout=10)
            for e in rs if isinstance(rs, list) else []:
                t = e.get("type") or ""
                if _type_matches(t, BEACON_INFO_TYPES):
                    if not saw_beacon_info:
                        print(f"[beacon_info/state-poll] {t} from {e.get('sender')}: {e.get('content')}", flush=True)
                    saw_beacon_info = True
        except Exception as ex:
            print(f"state poll error: {ex}", flush=True)
        params = {"timeout": "8000"}
        if next_batch:
            params["since"] = next_batch
        try:
            resp = http_get("/_matrix/client/v3/sync?" + urllib.parse.urlencode(params), read_timeout=30)
        except Exception as ex:
            print(f"sync error: {ex}", flush=True)
            time.sleep(2)
            continue
        next_batch = resp.get("next_batch", next_batch)
        join = ((resp.get("rooms") or {}).get("join") or {}).get(ROOM_ID) or {}
        for bucket_name in ("state", "timeline"):
            for e in ((join.get(bucket_name) or {}).get("events") or []):
                t = e.get("type") or ""
                if _type_matches(t, BEACON_INFO_TYPES):
                    print(f"[beacon_info/{bucket_name}] {t} from {e.get('sender')}: {e.get('content')}", flush=True)
                    saw_beacon_info = True
                elif _type_matches(t, BEACON_TYPES):
                    beacon_count += 1
                    print(f"[beacon/{bucket_name} #{beacon_count}] {t} from {e.get('sender')}: {e.get('content')}", flush=True)

    if not saw_beacon_info:
        print("TIMEOUT waiting for beacon_info.", flush=True)
        return 2
    if beacon_count < MIN_BEACONS:
        print(f"TIMEOUT waiting for beacon updates (got {beacon_count}, need {MIN_BEACONS}).", flush=True)
        return 3
    return 0


if __name__ == "__main__":
    sys.exit(main())

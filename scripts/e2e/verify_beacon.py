"""
Headless Matrix receiver that verifies the live-location beacon flow.

Expects environment variables (set by setup-synapse.sh):
- HS_URL                   homeserver URL, default http://localhost:8008
- E2E_RECEIVER_ID          fully-qualified user id
- E2E_RECEIVER_TOKEN       access token
- E2E_ROOM_ID              shared room id
- E2E_VERIFY_TIMEOUT_S     total wait before giving up (default 180)
- E2E_MIN_BEACONS          minimum number of m.beacon updates required (default 1)

Exits 0 on success, non-zero on failure.
"""
import asyncio
import os
import sys

from nio import AsyncClient, RoomMessage, MatrixRoom


HS_URL = os.environ.get("HS_URL", "http://localhost:8008")
USER_ID = os.environ["E2E_RECEIVER_ID"]
TOKEN = os.environ["E2E_RECEIVER_TOKEN"]
ROOM_ID = os.environ["E2E_ROOM_ID"]
TIMEOUT_S = int(os.environ.get("E2E_VERIFY_TIMEOUT_S", "180"))
MIN_BEACONS = int(os.environ.get("E2E_MIN_BEACONS", "1"))

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

saw_beacon_info = asyncio.Event()
beacon_count = 0
beacon_enough = asyncio.Event()


def _type_matches(event_type: str, candidates) -> bool:
    return any(event_type == t or event_type.startswith(t + ".") for t in candidates)


async def main() -> int:
    client = AsyncClient(HS_URL, USER_ID)
    client.access_token = TOKEN
    client.user_id = USER_ID
    # Derive a device_id from the token — nio needs one set.
    client.device_id = "E2E-verifier"

    async def on_state_event(room: MatrixRoom, event) -> None:
        global beacon_count
        etype = getattr(event, "type", None) or getattr(event.source, "get", lambda *_: None)("type")
        if etype and _type_matches(etype, BEACON_INFO_TYPES):
            print(f"[beacon_info] from {event.sender}: {event.source.get('content')}", flush=True)
            saw_beacon_info.set()

    async def on_timeline_event(room: MatrixRoom, event) -> None:
        global beacon_count
        if room.room_id != ROOM_ID:
            return
        etype = getattr(event, "type", None) or event.source.get("type")
        if not etype:
            return
        if _type_matches(etype, BEACON_INFO_TYPES):
            print(f"[beacon_info/timeline] from {event.sender}", flush=True)
            saw_beacon_info.set()
        elif _type_matches(etype, BEACON_TYPES):
            beacon_count += 1
            content = event.source.get("content", {})
            print(f"[beacon {beacon_count}] from {event.sender}: {content}", flush=True)
            if beacon_count >= MIN_BEACONS:
                beacon_enough.set()

    # nio's callback registration is by event class, but we want raw types.
    # Use the dispatcher on generic events.
    async def sync_forever() -> None:
        next_batch = None
        while True:
            resp = await client.sync(timeout=10_000, since=next_batch, full_state=True)
            if hasattr(resp, "next_batch"):
                next_batch = resp.next_batch
            rooms = getattr(resp, "rooms", None)
            if rooms and rooms.join:
                for rid, joined in rooms.join.items():
                    if rid != ROOM_ID:
                        continue
                    for state_event in joined.state.events if joined.state else []:
                        await on_state_event(MatrixRoom(rid, USER_ID), state_event)
                    for tl_event in joined.timeline.events if joined.timeline else []:
                        await on_timeline_event(MatrixRoom(rid, USER_ID), tl_event)

    try:
        sync_task = asyncio.create_task(sync_forever())
        print(f"Waiting up to {TIMEOUT_S}s for beacon_info + {MIN_BEACONS} beacon update(s)...", flush=True)
        try:
            await asyncio.wait_for(saw_beacon_info.wait(), timeout=TIMEOUT_S)
            print("beacon_info received.", flush=True)
        except asyncio.TimeoutError:
            print("TIMEOUT waiting for beacon_info.", flush=True)
            return 2
        try:
            await asyncio.wait_for(beacon_enough.wait(), timeout=TIMEOUT_S)
            print(f"Got {beacon_count} beacon update(s). OK.", flush=True)
        except asyncio.TimeoutError:
            print(f"TIMEOUT waiting for beacons (got {beacon_count}).", flush=True)
            return 3
        return 0
    finally:
        sync_task.cancel()
        await client.close()


if __name__ == "__main__":
    sys.exit(asyncio.run(main()))

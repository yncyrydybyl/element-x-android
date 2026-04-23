#!/usr/bin/env bash
# Register two test users on the local Synapse and create a shared room.
# Writes credentials to $GITHUB_ENV so later steps can use them.
set -euo pipefail

HS_URL="${HS_URL:-http://localhost:8008}"
SHARED_SECRET="${SHARED_SECRET:-e2eregistration}"

SENDER_USER="sender"
SENDER_PASS="senderpass123"
RECEIVER_USER="receiver"
RECEIVER_PASS="receiverpass123"

echo "Waiting for Synapse at $HS_URL ..."
for _ in {1..60}; do
  if curl -sf "$HS_URL/_matrix/client/versions" >/dev/null; then
    echo "Synapse is up."
    break
  fi
  sleep 1
done

register() {
  local user=$1 pass=$2
  local nonce
  nonce=$(curl -s "$HS_URL/_synapse/admin/v1/register" | jq -r .nonce)
  local mac
  mac=$(printf '%s\0%s\0%s\0notadmin' "$nonce" "$user" "$pass" \
    | openssl dgst -sha1 -hmac "$SHARED_SECRET" | awk '{print $2}')
  curl -sf -X POST "$HS_URL/_synapse/admin/v1/register" \
    -H 'Content-Type: application/json' \
    -d "{\"nonce\":\"$nonce\",\"username\":\"$user\",\"password\":\"$pass\",\"admin\":false,\"mac\":\"$mac\"}" \
    | jq .
}

echo "Registering sender..."
SENDER_JSON=$(register "$SENDER_USER" "$SENDER_PASS")
SENDER_ID=$(echo "$SENDER_JSON" | jq -r .user_id)
SENDER_TOKEN=$(echo "$SENDER_JSON" | jq -r .access_token)

echo "Registering receiver..."
RECEIVER_JSON=$(register "$RECEIVER_USER" "$RECEIVER_PASS")
RECEIVER_ID=$(echo "$RECEIVER_JSON" | jq -r .user_id)
RECEIVER_TOKEN=$(echo "$RECEIVER_JSON" | jq -r .access_token)

echo "Creating shared room as sender..."
ROOM_JSON=$(curl -sf -X POST "$HS_URL/_matrix/client/v3/createRoom" \
  -H "Authorization: Bearer $SENDER_TOKEN" \
  -H 'Content-Type: application/json' \
  -d "{\"name\":\"e2e\",\"preset\":\"public_chat\",\"invite\":[\"$RECEIVER_ID\"]}")
ROOM_ID=$(echo "$ROOM_JSON" | jq -r .room_id)

echo "Receiver joining room..."
curl -sf -X POST "$HS_URL/_matrix/client/v3/rooms/$ROOM_ID/join" \
  -H "Authorization: Bearer $RECEIVER_TOKEN" >/dev/null

echo "Sender: $SENDER_ID  Receiver: $RECEIVER_ID  Room: $ROOM_ID"

{
  echo "E2E_SENDER_ID=$SENDER_ID"
  echo "E2E_SENDER_USER=$SENDER_USER"
  echo "E2E_SENDER_PASS=$SENDER_PASS"
  echo "E2E_SENDER_TOKEN=$SENDER_TOKEN"
  echo "E2E_RECEIVER_ID=$RECEIVER_ID"
  echo "E2E_RECEIVER_USER=$RECEIVER_USER"
  echo "E2E_RECEIVER_PASS=$RECEIVER_PASS"
  echo "E2E_RECEIVER_TOKEN=$RECEIVER_TOKEN"
  echo "E2E_ROOM_ID=$ROOM_ID"
} >> "${GITHUB_ENV:-/dev/stdout}"

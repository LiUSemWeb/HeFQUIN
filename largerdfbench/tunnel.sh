#!/usr/bin/env bash
set -euo pipefail

# Fixed servers
JUMP_HOST="ssh.liu.se"
TARGET_HOST="ontology.ida.liu.se"

# Fixed local forwards
FORWARDS=(
  "8080:127.0.0.1:8080"
  "8890:127.0.0.1:8890"
)

usage() {
  cat <<EOF
Usage:
  tunnel.sh [-u1 username_for_${JUMP_HOST}] [-u2 username_for_${TARGET_HOST}]

You must provide usernames either via arguments or interactively.
Passwords will be prompted by ssh.
EOF
}

USER_JUMP=""
USER_TARGET=""

while (($#)); do
  case "$1" in
    -u1) shift; USER_JUMP="${1:?missing username for ${JUMP_HOST}}" ;;
    -u2) shift; USER_TARGET="${1:?missing username for ${TARGET_HOST}}" ;;
    -h|--help) usage; exit 0 ;;
    *)
      echo "Unknown argument: $1" >&2
      usage >&2
      exit 2
      ;;
  esac
  shift
done

# Prompt if missing
if [[ -z "$USER_JUMP" ]]; then
  read -r -p "Username for ${JUMP_HOST}: " USER_JUMP
fi

if [[ -z "$USER_TARGET" ]]; then
  read -r -p "Username for ${TARGET_HOST}: " USER_TARGET
fi

# Enforce non-empty
if [[ -z "${USER_JUMP// }" ]]; then
  echo "Error: Username for ${JUMP_HOST} is required." >&2
  exit 1
fi

if [[ -z "${USER_TARGET// }" ]]; then
  echo "Error: Username for ${TARGET_HOST} is required." >&2
  exit 1
fi

SSH_ARGS=(
  -J "${USER_JUMP}@${JUMP_HOST}"
  -o PubkeyAuthentication=no
  -o PreferredAuthentications=password
  -o IdentitiesOnly=yes
  -o IdentityFile=/dev/null
  -N
)

for f in "${FORWARDS[@]}"; do
  SSH_ARGS+=( -L "$f" )
done

exec ssh "${SSH_ARGS[@]}" "${USER_TARGET}@${TARGET_HOST}"

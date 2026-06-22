#!/bin/bash
# Generate Promtail file_sd targets from Docker container metadata.
# Reads /var/lib/docker/containers/*/config.v2.json to extract container name + ID,
# then writes a targets JSON that Promtail's file_sd_configs picks up.
#
# Also applies overrides:
#   middleware-service -> apigateway-service
#   media-service-elx  -> media-service

# Find a working Python interpreter
for py in python3 python; do
    command -v "$py" >/dev/null 2>&1 && PYTHON="$py" && break
done
[ -z "$PYTHON" ] && { echo "ERROR: no python interpreter found" >&2; exit 1; }

TARGETS_DIR="${1:-/tmp/promtail-targets}"
mkdir -p "$TARGETS_DIR"

CONTAINERS_DIR="/var/lib/docker/containers"

if [ ! -d "$CONTAINERS_DIR" ]; then
    echo "ERROR: $CONTAINERS_DIR not found. Are you on a Docker host?" >&2
    exit 1
fi

# If the containers directory isn't readable, try sudo
READ_CMD=""
if [ ! -r "$CONTAINERS_DIR" ]; then
    if command -v sudo >/dev/null 2>&1; then
        READ_CMD="sudo"
    else
        echo "ERROR: $CONTAINERS_DIR not readable and no sudo available" >&2
        exit 1
    fi
fi

first=true
echo "[" > "$TARGETS_DIR/containers.json"

for config in "$CONTAINERS_DIR"/*/config.v2.json; do
    [ -f "$config" ] || continue

    cid=$(basename "$(dirname "$config")")

    # Read config via $READ_CMD if needed
    name=$($READ_CMD $PYTHON -c "
import json, sys
try:
    with open('$config') as f:
        cfg = json.load(f)
    name = cfg.get('Name', '').lstrip('/')
    overrides = {'middleware-service': 'apigateway-service', 'media-service-elx': 'media-service'}
    print(overrides.get(name, name))
except Exception:
    print('')
" 2>/dev/null)

    [ -z "$name" ] && continue

    logfile="$CONTAINERS_DIR/$cid/${cid}-json.log"
    [ -f "$logfile" ] || continue

    if [ "$first" = true ]; then
        first=false
    else
        echo "," >> "$TARGETS_DIR/containers.json"
    fi

    cat >> "$TARGETS_DIR/containers.json" <<EOF
  {
    "targets": ["localhost"],
    "labels": {
      "__path__": "$logfile",
      "container": "$name",
      "stream": "stdout"
    }
  }
EOF
done

echo "]" >> "$TARGETS_DIR/containers.json"

echo "Generated $TARGETS_DIR/containers.json with $(grep -c '"targets"' "$TARGETS_DIR/containers.json" 2>/dev/null || echo 0) targets"

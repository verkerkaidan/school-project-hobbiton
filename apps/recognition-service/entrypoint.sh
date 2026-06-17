#!/bin/bash
set -e

FALLBACK_MODEL_URL="https://gitlab.inf-hsleiden.nl/s1147087/my-model-artifacts/-/raw/c2eb35b3759f28216fd97f0c39e25cfabbebc67e/Hobbithon/car_classifier_v2-1.pt"

if [ -z "$MODEL_URL" ]; then
  echo "ERROR: MODEL_URL environment variable is not set"
  MODEL_URL="${FALLBACK_MODEL_URL}"
else
  MODEL_URL=${MODEL_URL/"/blob/"/"/raw/"}
fi

MODEL_DIR=/app/models
mkdir -p $MODEL_DIR

echo "Downloading model from ${MODEL_URL}..."

curl -L "$MODEL_URL" -o "$MODEL_DIR/car_classifier.pt"

echo "Model downloaded."

if [ ! -f "$MODEL_DIR/car_classifier.pt" ]; then
  echo "Model missing after download"
  exit 1
fi

exec java -jar app.jar
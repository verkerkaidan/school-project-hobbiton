#!/bin/bash
set -e

MODEL_DIR=/app/models
mkdir -p $MODEL_DIR

MODEL_URL="https://gitlab.inf-hsleiden.nl/s1147087/my-model-artifacts/-/raw/8e0367231b8ea4b166447b55d3ee3fc949b3ea52/Hobbithon/${MODEL_NAME}"
echo "Downloading model ${MODEL_NAME}..."

curl -L "$MODEL_URL" -o "$MODEL_DIR/${MODEL_NAME}"

echo "Model downloaded."

if [ ! -f "$MODEL_DIR/$MODEL_NAME" ]; then
  echo "Model missing after download"
  exit 1
fi

exec java -jar app.jar
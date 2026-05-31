#!/bin/bash
set -e

if [ -z "$MODEL_NAME" ]; then
  echo "ERROR: MODEL_NAME environment variable is not set"
fi

MODEL_DIR=/app/models
mkdir -p $MODEL_DIR

MODEL_NAME="${MODEL_NAME:-car_classifier_v2.pt}"
echo "MODEL_NAME is: ${MODEL_NAME}"

MODEL_URL="https://gitlab.inf-hsleiden.nl/s1147087/my-model-artifacts/-/raw/c2eb35b3759f28216fd97f0c39e25cfabbebc67e/Hobbithon/${MODEL_NAME}"
echo "Downloading model ${MODEL_NAME}..."

curl -L "$MODEL_URL" -o "$MODEL_DIR/${MODEL_NAME}"

echo "Model downloaded."

if [ ! -f "$MODEL_DIR/$MODEL_NAME" ]; then
  echo "Model missing after download"
  exit 1
fi

exec java -jar app.jar
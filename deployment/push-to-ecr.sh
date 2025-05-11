#!/usr/bin/env bash
set -euo pipefail

# ─── Locate project root & Dockerfile ──────────────────────────────────────────
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
DOCKERFILE_PATH="$SCRIPT_DIR/Dockerfile"

if [[ ! -f "$PROJECT_ROOT/pom.xml" || ! -d "$PROJECT_ROOT/src" ]]; then
  echo "❌  Could not find pom.xml or src/ in project root ($PROJECT_ROOT)."
  echo "    Are you sure this script is in deployment/ under the project root?"
  exit 1
fi

if [[ ! -f "$DOCKERFILE_PATH" ]]; then
  echo "❌  Dockerfile not found at $DOCKERFILE_PATH"
  exit 1
fi

# ─── Configuration ────────────────────────────────────────────────────────────
AWS_REGION="us-east-1"
AWS_ACCOUNT_ID="122610499913"
ECR_REPOSITORY="process-order-service"
IMAGE_NAME="process-order-service"
IMAGE_TAG="latest"
ECR_URI="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${ECR_REPOSITORY}"

# ─── Login, Build, Tag & Push ──────────────────────────────────────────────────
echo "⏳ Logging in to ECR ($ECR_URI)..."
aws ecr get-login-password --region "${AWS_REGION}" \
  | docker login --username AWS --password-stdin "${ECR_URI}"

echo "⏳ Building Docker image from:"
echo "    context:      $PROJECT_ROOT"
echo "    dockerfile:   $DOCKERFILE_PATH"
docker build \
  -f "$DOCKERFILE_PATH" \
  -t "${IMAGE_NAME}:${IMAGE_TAG}" \
  "$PROJECT_ROOT"

echo "⏳ Tagging image for ECR..."
docker tag "${IMAGE_NAME}:${IMAGE_TAG}" "${ECR_URI}:${IMAGE_TAG}"

echo "⏳ Pushing image to ECR..."
docker push "${ECR_URI}:${IMAGE_TAG}"

echo "✅ Done! Your image is available at:"
echo "   ${ECR_URI}:${IMAGE_TAG}"

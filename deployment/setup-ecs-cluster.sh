#!/usr/bin/env bash
set -euo pipefail

# ── User configuration ─────────────────────────────────────────────────────────
AWS_REGION="us-east-1"
TAG="process-order"
ECS_CLUSTER="${TAG}-cluster"
ECS_CONFIG="${TAG}-config"
ECS_PROFILE="default"

# These come from your network bootstrap (or run create-network.sh once to get them)
VPC_ID="vpc-042002cf5dab30595"
SUBNETS="subnet-09cdbfbb8a40efd73,subnet-0897cc923952c0d21"
SECURITY_GROUP="sg-002b92a959004008f"

# ── Configure ECS CLI for Fargate ───────────────────────────────────────────────
echo "⚙️  Configuring ECS CLI for Fargate cluster '${ECS_CLUSTER}' in ${AWS_REGION}..."
ecs-cli configure \
  --cluster "${ECS_CLUSTER}" \
  --default-launch-type FARGATE \
  --config-name "${ECS_CONFIG}" \
  --region "${AWS_REGION}"

# ── Boot up the cluster with networking ──────────────────────────────────────────
echo "🆙  Creating Fargate cluster '${ECS_CLUSTER}' with vpc/subnets/sg..."
ecs-cli up \
  --cluster-config "${ECS_CONFIG}" \
  --ecs-profile "${ECS_PROFILE}" \
  --force \
  --vpc "${VPC_ID}" \
  --subnets "${SUBNETS}" \
  --security-group "${SECURITY_GROUP}"

echo "✅  Cluster '${ECS_CLUSTER}' is ready!"

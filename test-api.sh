#!/usr/bin/env bash
set -euo pipefail

BASE_URL="http://localhost:8080/api"
CONTENT="Content-Type: application/json"

echo "step 1: Place an order (POST /orders)…"
order_json=$(curl -s -X POST "$BASE_URL/orders" \
  -H "$CONTENT" \
  -d "{
    \"shopId\": 1,
    \"menuItemId\": 101,
    \"customerId\": 1
  }")
echo "   → $order_json"

# extract orderId
if command -v jq &> /dev/null; then
  order_id=$(echo "$order_json" | jq -r '.id')
else
  order_id=$(echo "$order_json" | sed -nE 's/.*"id"[[:space:]]*:[[:space:]]*([0-9]+).*/\1/p')
fi
echo "   → orderId = $order_id"
echo

echo "step 2: Check order status (GET /orders/$order_id/status)…"
status_json=$(curl -s "$BASE_URL/orders/$order_id/status")
echo "   → $status_json"
echo

echo "step 3: Cancel the order (DELETE /orders/$order_id)…"
http_code=$(curl -s -o /dev/null -w "%{http_code}" -X DELETE "$BASE_URL/orders/$order_id")
echo "   → HTTP $http_code"
echo

echo "step 4: Verify cancellation (GET /orders/$order_id/status)…"
final_json=$(curl -s -o /dev/null -w "HTTP %{http_code}\n" "$BASE_URL/orders/$order_id/status")
echo "   → $final_json"

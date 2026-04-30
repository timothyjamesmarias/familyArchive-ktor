#!/usr/bin/env bash
#
# Captures golden master API responses from the Spring Boot app.
# Run this ONCE against the Spring app to establish the baseline.
#
# Usage:
#   ./scripts/capture-golden-masters.sh [spring_port]
#
set -uo pipefail

SPRING_PORT="${1:-8080}"
SPRING_BASE="http://localhost:${SPRING_PORT}"
OUTPUT_DIR="src/test/resources/golden-masters/api"

mkdir -p "$OUTPUT_DIR"

echo "=== Golden Master Capture ==="
echo "Spring: ${SPRING_BASE}"
echo "Output: ${OUTPUT_DIR}"
echo ""

# Verify Spring is running
STATUS=$(curl -s -o /dev/null -w "%{http_code}" "${SPRING_BASE}/" 2>/dev/null || echo "000")
if [[ "$STATUS" == "000" ]]; then
    echo "ERROR: Spring app not running on port ${SPRING_PORT}"
    exit 1
fi
echo "Spring app is running (status: $STATUS)"
echo ""

capture() {
    local name="$1"
    local url="$2"
    local method="${3:-GET}"
    local body="${4:-}"
    local auth_cookie="${5:-}"

    local file="${OUTPUT_DIR}/${name}.json"

    local curl_args=(-s --max-time 10)

    if [[ -n "$auth_cookie" ]]; then
        curl_args+=(-b "$auth_cookie")
    fi

    if [[ "$method" == "GET" ]]; then
        curl_args+=("${SPRING_BASE}${url}")
    else
        curl_args+=(-X "$method" -H "Content-Type: application/json")
        if [[ -n "$body" ]]; then
            curl_args+=(-d "$body")
        fi
        curl_args+=("${SPRING_BASE}${url}")
    fi

    local response
    response=$(curl "${curl_args[@]}" 2>/dev/null)
    local status=$?

    if [[ $status -ne 0 || -z "$response" ]]; then
        echo "  SKIP  ${name} — no response"
        return
    fi

    # Pretty-print JSON and save
    echo "$response" | python3 -m json.tool > "$file" 2>/dev/null
    if [[ $? -ne 0 ]]; then
        # Not valid JSON, save raw
        echo "$response" > "$file"
    fi

    local size=$(wc -c < "$file" | tr -d ' ')
    echo "  SAVED ${name} (${size} bytes)"
}

echo "--- Public API endpoints ---"

# Family tree
capture "family-tree-initial" "/api/family-tree/initial"
capture "family-tree-expand-root" "/api/family-tree/expand?personId=1&generationsUp=1&generationsDown=0&includeSiblings=false"
capture "family-tree-expand-with-siblings" "/api/family-tree/expand?personId=1&generationsUp=1&generationsDown=1&includeSiblings=true"

# Individuals
capture "individuals-root" "/api/individuals/root"
capture "individual-1" "/api/individuals/1"
capture "individual-2" "/api/individuals/2"
capture "individual-999-not-found" "/api/individuals/999"

echo ""
echo "--- Response structure analysis ---"
echo ""

# Analyze the family tree initial response structure
if [[ -f "${OUTPUT_DIR}/family-tree-initial.json" ]]; then
    echo "family-tree-initial:"
    echo "  individuals count: $(python3 -c "import json; d=json.load(open('${OUTPUT_DIR}/family-tree-initial.json')); print(len(d.get('individuals', [])))" 2>/dev/null || echo "?")"
    echo "  families count: $(python3 -c "import json; d=json.load(open('${OUTPUT_DIR}/family-tree-initial.json')); print(len(d.get('families', [])))" 2>/dev/null || echo "?")"

    # Show structure of first individual if exists
    echo "  individual fields: $(python3 -c "
import json
d=json.load(open('${OUTPUT_DIR}/family-tree-initial.json'))
if d.get('individuals'):
    print(sorted(d['individuals'][0].keys()))
else:
    print('(empty)')
" 2>/dev/null || echo "?")"

    # Show structure of first family if exists
    echo "  family fields: $(python3 -c "
import json
d=json.load(open('${OUTPUT_DIR}/family-tree-initial.json'))
if d.get('families'):
    print(sorted(d['families'][0].keys()))
else:
    print('(empty)')
" 2>/dev/null || echo "?")"
fi

echo ""
echo "=== Capture complete ==="
echo "Files saved to: ${OUTPUT_DIR}/"
ls -la "${OUTPUT_DIR}/"

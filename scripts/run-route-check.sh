#!/usr/bin/env bash
#
# Wrapper: runs route-checker and logs results to a timestamped file
#
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
LOG_DIR="${SCRIPT_DIR}/../logs"
mkdir -p "$LOG_DIR"

TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
LOG_FILE="${LOG_DIR}/route-check_${TIMESTAMP}.log"

echo "Running route checker..."
echo "Log file: ${LOG_FILE}"
echo ""

# Run both comparison and ktor-only verbose modes
{
    echo "============================================"
    echo "  Route Check — $(date)"
    echo "============================================"
    echo ""

    echo "=== SIDE-BY-SIDE COMPARISON (Spring 8080 vs Ktor 8081) ==="
    echo ""
    SPRING_PORT=8080 KTOR_PORT=8081 "${SCRIPT_DIR}/route-checker.sh" 2>&1 || true

    echo ""
    echo ""
    echo "=== KTOR-ONLY VERBOSE (with error bodies) ==="
    echo ""
    KTOR_PORT=8081 "${SCRIPT_DIR}/route-checker.sh" --ktor-only --verbose 2>&1 || true

    echo ""
    echo ""
    echo "=== KTOR SERVER LOG (last 100 lines) ==="
    echo ""
    tail -100 /tmp/ktor.log 2>/dev/null || echo "(no ktor log found)"

} | tee "$LOG_FILE"

echo ""
echo "Full log saved to: ${LOG_FILE}"

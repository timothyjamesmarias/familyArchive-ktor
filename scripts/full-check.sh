#!/usr/bin/env bash
#
# Full check: starts both apps, runs route checker, saves results, stops apps
#
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="${SCRIPT_DIR}/.."
SPRING_DIR="/Users/timmarias/projects/familyArchive"
LOG_DIR="${PROJECT_DIR}/logs"
mkdir -p "$LOG_DIR"

TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
RESULTS_FILE="${LOG_DIR}/route-results_${TIMESTAMP}.txt"
KTOR_LOG="${LOG_DIR}/ktor-server_${TIMESTAMP}.log"
SPRING_LOG="${LOG_DIR}/spring-server_${TIMESTAMP}.log"

cleanup() {
    echo "Stopping apps..."
    kill "$KTOR_PID" 2>/dev/null || true
    kill "$SPRING_PID" 2>/dev/null || true
    wait "$KTOR_PID" 2>/dev/null || true
    wait "$SPRING_PID" 2>/dev/null || true
}
trap cleanup EXIT

# Make sure ports are free — kill all java processes on these ports
echo "=== Clearing ports ==="
for port in 8080 8081; do
    PIDS=$(lsof -ti :$port 2>/dev/null || true)
    if [[ -n "$PIDS" ]]; then
        echo "Killing processes on port $port: $PIDS"
        echo "$PIDS" | xargs kill -9 2>/dev/null || true
    fi
done
echo "Waiting for ports to clear..."
sleep 5
# Verify ports are free
for port in 8080 8081; do
    if lsof -ti :$port >/dev/null 2>&1; then
        echo "ERROR: Port $port still in use!"
        exit 1
    fi
done
echo "Ports clear."

echo "=== Starting apps ==="

# Start Ktor on 8081
echo "Starting Ktor on port 8081..."
(cd "$PROJECT_DIR" && PORT=8081 KTOR_DEVELOPMENT=true ./gradlew run --no-daemon > "$KTOR_LOG" 2>&1) &
KTOR_PID=$!

# Start Spring on 8080
echo "Starting Spring Boot on port 8080..."
(cd "$SPRING_DIR" && SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun --no-daemon > "$SPRING_LOG" 2>&1) &
SPRING_PID=$!

# Wait for both to be ready (minimum 10s to avoid picking up stale servers)
echo "Waiting for apps to start (this takes 30-60s for Spring)..."
sleep 10

KTOR_READY=false
SPRING_READY=false

for i in $(seq 1 120); do
    if [[ "$KTOR_READY" == "false" ]]; then
        KTOR_UP=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8081/health 2>/dev/null || echo "000")
        if [[ "$KTOR_UP" != "000" ]]; then
            KTOR_READY=true
            echo "  Ktor ready (${i}s)"
        fi
    fi

    if [[ "$SPRING_READY" == "false" ]]; then
        SPRING_UP=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/ 2>/dev/null || echo "000")
        if [[ "$SPRING_UP" != "000" ]]; then
            SPRING_READY=true
            echo "  Spring ready (${i}s)"
        fi
    fi

    if [[ "$KTOR_READY" == "true" && "$SPRING_READY" == "true" ]]; then
        echo "Both apps ready!"
        sleep 2  # settle time
        break
    fi

    if [[ "$i" == "120" ]]; then
        echo "Timeout. Ktor=$KTOR_READY Spring=$SPRING_READY"
        echo "--- Ktor log tail ---"
        tail -20 "$KTOR_LOG"
        echo "--- Spring log tail ---"
        tail -20 "$SPRING_LOG"
        exit 1
    fi

    sleep 1
done

# Run the route checker
echo ""
echo "=== Running route checker ==="
echo ""

{
    echo "============================================"
    echo "  Route Check Results — $(date)"
    echo "============================================"
    echo ""
    echo "Spring: http://localhost:8080"
    echo "Ktor:   http://localhost:8081"
    echo ""

    echo "=== SIDE-BY-SIDE COMPARISON ==="
    echo ""
    SPRING_PORT=8080 KTOR_PORT=8081 "${SCRIPT_DIR}/route-checker.sh" 2>&1 || true

    echo ""
    echo "=== KTOR-ONLY VERBOSE ==="
    echo ""
    KTOR_PORT=8081 "${SCRIPT_DIR}/route-checker.sh" --ktor-only --verbose 2>&1 || true

} > "$RESULTS_FILE" 2>&1

# Strip ANSI codes for clean reading
sed -i '' 's/\x1b\[[0-9;]*m//g' "$RESULTS_FILE"

echo ""
echo "=== Results ==="
cat "$RESULTS_FILE"

echo ""
echo "Results:    ${RESULTS_FILE}"
echo "Ktor log:   ${KTOR_LOG}"
echo "Spring log: ${SPRING_LOG}"

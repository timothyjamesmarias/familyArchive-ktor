#!/usr/bin/env bash
#
# Route Checker — compares responses between Spring Boot and Ktor servers
#
# Usage:
#   ./scripts/route-checker.sh                    # Compare both servers
#   ./scripts/route-checker.sh --ktor-only        # Check Ktor only
#   ./scripts/route-checker.sh --route /articles  # Check a single route
#
# Prerequisites:
#   Spring on port 8080, Ktor on 8081 (or set SPRING_PORT / KTOR_PORT)

set -uo pipefail

SPRING_PORT="${SPRING_PORT:-8080}"
KTOR_PORT="${KTOR_PORT:-8081}"
SPRING_BASE="http://localhost:${SPRING_PORT}"
KTOR_BASE="http://localhost:${KTOR_PORT}"

KTOR_ONLY=false
SINGLE_ROUTE=""
VERBOSE=false

# Parse args
while [[ $# -gt 0 ]]; do
    case "$1" in
        --ktor-only) KTOR_ONLY=true; shift ;;
        --route) SINGLE_ROUTE="$2"; shift 2 ;;
        --verbose|-v) VERBOSE=true; shift ;;
        *) echo "Unknown arg: $1"; exit 1 ;;
    esac
done

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
CYAN='\033[0;36m'
NC='\033[0m'

# Counters
PASS=0
FAIL=0
SKIP=0

# All public GET routes
PUBLIC_ROUTES=(
    "/"
    "/login"
    "/family-tree"
    "/articles"
    "/photos"
    "/videos"
    "/documents"
    "/letters"
    "/audio"
    "/ledgers"
    "/artifacts"
    "/health"
)

# API routes (JSON)
API_ROUTES=(
    "/api/family-tree/initial"
    "/api/individuals/root"
)

# Admin routes (require auth — will get 302 redirect to /login without session)
ADMIN_ROUTES=(
    "/admin/dashboard"
    "/admin/users"
    "/admin/users/new"
    "/admin/articles"
    "/admin/articles/new"
    "/admin/places"
    "/admin/places/new"
    "/admin/artifacts"
    "/admin/artifacts/upload"
    "/admin/individuals"
    "/admin/individuals/new"
    "/admin/families"
    "/admin/families/new"
    "/admin/system/utilities"
)

check_route() {
    local route="$1"
    local category="$2"
    local expect_redirect="${3:-false}"

    # Ktor request
    local ktor_status ktor_size ktor_type
    ktor_response=$(curl -s -o /dev/null -w "%{http_code}|%{size_download}|%{content_type}|%{redirect_url}" \
        --max-time 10 "${KTOR_BASE}${route}" 2>/dev/null || echo "000|0||")
    ktor_status=$(echo "$ktor_response" | cut -d'|' -f1)
    ktor_size=$(echo "$ktor_response" | cut -d'|' -f2)
    ktor_type=$(echo "$ktor_response" | cut -d'|' -f3)
    ktor_redirect=$(echo "$ktor_response" | cut -d'|' -f4)

    if [[ "$KTOR_ONLY" == "true" ]]; then
        # Ktor-only mode: just check for non-500 responses
        if [[ "$ktor_status" == "000" ]]; then
            printf "${RED}CONN ERR${NC}  %-40s  Ktor: connection failed\n" "$route"
            ((FAIL++))
        elif [[ "$ktor_status" =~ ^5 ]]; then
            printf "${RED}  FAIL  ${NC}  %-40s  Ktor: %s (%s bytes)\n" "$route" "$ktor_status" "$ktor_size"
            ((FAIL++))
            if [[ "$VERBOSE" == "true" ]]; then
                echo "         Response body (first 500 chars):"
                curl -s --max-time 5 "${KTOR_BASE}${route}" 2>/dev/null | head -c 500
                echo ""
            fi
        elif [[ "$expect_redirect" == "true" && "$ktor_status" =~ ^3 ]]; then
            printf "${GREEN}  PASS  ${NC}  %-40s  Ktor: %s -> %s\n" "$route" "$ktor_status" "$ktor_redirect"
            ((PASS++))
        elif [[ "$ktor_status" == "200" ]]; then
            printf "${GREEN}  PASS  ${NC}  %-40s  Ktor: %s (%s bytes)\n" "$route" "$ktor_status" "$ktor_size"
            ((PASS++))
        else
            printf "${YELLOW}  WARN  ${NC}  %-40s  Ktor: %s (%s bytes)\n" "$route" "$ktor_status" "$ktor_size"
            ((PASS++))
        fi
        return
    fi

    # Spring request
    local spring_status spring_size
    spring_response=$(curl -s -o /dev/null -w "%{http_code}|%{size_download}|%{content_type}|%{redirect_url}" \
        --max-time 10 "${SPRING_BASE}${route}" 2>/dev/null || echo "000|0||")
    spring_status=$(echo "$spring_response" | cut -d'|' -f1)
    spring_size=$(echo "$spring_response" | cut -d'|' -f2)
    spring_redirect=$(echo "$spring_response" | cut -d'|' -f4)

    # Compare
    if [[ "$spring_status" == "000" ]]; then
        printf "${YELLOW}  SKIP  ${NC}  %-40s  Spring not running\n" "$route"
        ((SKIP++))
    elif [[ "$ktor_status" == "000" ]]; then
        printf "${RED}CONN ERR${NC}  %-40s  Ktor: connection failed  Spring: %s\n" "$route" "$spring_status"
        ((FAIL++))
    elif [[ "$spring_status" == "$ktor_status" ]]; then
        local size_diff=$((ktor_size - spring_size))
        local size_pct=""
        if [[ "$spring_size" -gt 0 ]]; then
            size_pct=$(( (size_diff * 100) / spring_size ))
        fi
        if [[ "${size_pct#-}" -gt 20 && "$ktor_size" -gt 100 ]]; then
            printf "${YELLOW}  SIZE  ${NC}  %-40s  Status: %s=%s  Size: %s vs %s (%+d%%)\n" \
                "$route" "$spring_status" "$ktor_status" "$spring_size" "$ktor_size" "$size_pct"
            ((PASS++))
        else
            printf "${GREEN}  PASS  ${NC}  %-40s  Status: %s=%s  Size: %s vs %s\n" \
                "$route" "$spring_status" "$ktor_status" "$spring_size" "$ktor_size"
            ((PASS++))
        fi
    else
        printf "${RED}  FAIL  ${NC}  %-40s  Spring: %s  Ktor: %s (%s bytes)\n" \
            "$route" "$spring_status" "$ktor_status" "$ktor_size"
        ((FAIL++))
        if [[ "$VERBOSE" == "true" && "$ktor_status" =~ ^5 ]]; then
            echo "         Ktor response (first 500 chars):"
            curl -s --max-time 5 "${KTOR_BASE}${route}" 2>/dev/null | head -c 500
            echo ""
        fi
    fi
}

echo ""
echo "========================================"
echo "  Route Checker"
echo "========================================"
if [[ "$KTOR_ONLY" == "true" ]]; then
    echo "  Mode:  Ktor-only (${KTOR_BASE})"
else
    echo "  Spring: ${SPRING_BASE}"
    echo "  Ktor:   ${KTOR_BASE}"
fi
echo "========================================"
echo ""

if [[ -n "$SINGLE_ROUTE" ]]; then
    echo "--- Checking single route ---"
    check_route "$SINGLE_ROUTE" "single" "false"
else
    echo "--- Public Routes ---"
    for route in "${PUBLIC_ROUTES[@]}"; do
        check_route "$route" "public" "false"
    done

    echo ""
    echo "--- API Routes ---"
    for route in "${API_ROUTES[@]}"; do
        check_route "$route" "api" "false"
    done

    echo ""
    echo "--- Admin Routes (expect 302 redirect without auth) ---"
    for route in "${ADMIN_ROUTES[@]}"; do
        check_route "$route" "admin" "true"
    done
fi

echo ""
echo "========================================"
printf "  Results: ${GREEN}%d passed${NC}  ${RED}%d failed${NC}  ${YELLOW}%d skipped${NC}\n" "$PASS" "$FAIL" "$SKIP"
echo "========================================"
echo ""

#!/bin/bash

# down.sh - Stop all project services
set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m'

log() { echo -e "${BLUE}[INFO]${NC} $1"; }
success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
error() { echo -e "${RED}[ERROR]${NC} $1"; }

stop_spring_boot() {
    log "Stopping Spring Boot application..."

    # Find and kill Spring Boot processes
    local pids=$(pgrep -f "spring-boot:run")
    if [[ -z "$pids" ]]; then
        success "Spring Boot not running"
        return
    fi

    echo "$pids" | xargs kill -TERM
    sleep 3

    # Force kill if still running
    local remaining=$(pgrep -f "spring-boot:run")
    if [[ -n "$remaining" ]]; then
        echo "$remaining" | xargs kill -KILL
        sleep 1
    fi

    if pgrep -f "spring-boot:run" >/dev/null; then
        error "Failed to stop Spring Boot"
        exit 1
    else
        success "Spring Boot stopped"
    fi
}

stop_ollama() {
    if ! command -v docker >/dev/null && ! command -v podman >/dev/null; then
        log "No container manager found, skipping Ollama"
        return
    fi

    local cmd="docker"
    [[ ! -x "$(command -v docker)" ]] && cmd="podman"

    if ! $cmd ps -a | grep -q ollama; then
        success "Ollama container not found"
        return
    fi

    log "Stopping Ollama..."
    $cmd stop ollama
    $cmd rm ollama
    success "Ollama stopped and removed"
}

main() {
    log "Stopping services..."

    stop_spring_boot
    stop_ollama

    success "All services stopped!"
}

main "$@"

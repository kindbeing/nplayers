#!/bin/bash

# run.sh - Start project services
set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m'

log() { echo -e "${BLUE}[INFO]${NC} $1"; }
success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
error() { echo -e "${RED}[ERROR]${NC} $1"; }

start_spring_boot() {
    local backend_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/player-service-backend"
    cd "$backend_dir"

    # Check if Spring Boot is already running
    if pgrep -f "spring-boot:run" >/dev/null; then
        success "Spring Boot already running"
        return
    fi

    log "Starting Spring Boot application..."
    nohup mvn spring-boot:run > ../logs/spring-boot.log 2>&1 &
    sleep 5

    if pgrep -f "spring-boot:run" >/dev/null; then
        success "Spring Boot started"
    else
        error "Failed to start Spring Boot"
        exit 1
    fi
}

start_ollama() {
    if ! command -v docker >/dev/null && ! command -v podman >/dev/null; then
        log "No container manager found, skipping Ollama"
        return
    fi

    local cmd="docker"
    [[ ! -x "$(command -v docker)" ]] && cmd="podman"

    if $cmd ps | grep -q ollama; then
        success "Ollama already running"
        return
    fi

    log "Starting Ollama..."
    $cmd pull ollama/ollama
    $cmd run -d -v ollama:/root/.ollama -p 11434:11434 --name ollama ollama/ollama
    sleep 3
    $cmd exec ollama ollama run tinyllama
    success "Ollama started"
}

main() {
    log "Starting services..."

    # Create logs directory if it doesn't exist
    mkdir -p "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/logs"

    start_spring_boot
    start_ollama

    success "Services started!"
    log "Spring Boot: http://localhost:8080"
    log "Ollama: http://localhost:11434"
    log "Stop services: ./scripts/down.sh"
}

main "$@"

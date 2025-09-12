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
    # Save current directory and navigate to backend directory
    local original_dir="$(pwd)"
    cd "player-service-backend"

    # Check if Spring Boot is already running
    if pgrep -f "spring-boot:run" >/dev/null; then
        success "Spring Boot already running"
        cd "$original_dir"
        return
    fi

    log "Starting Spring Boot application..."
    ~/.sdkman/candidates/maven/current/bin/mvn spring-boot:run > ../logs/spring-boot.log 2>&1 &
    sleep 5

    if pgrep -f "spring-boot:run" >/dev/null; then
        success "Spring Boot started"
    else
        error "Failed to start Spring Boot"
        exit 1
    fi

    # Return to original directory
    cd "$original_dir"
}

start_frontend() {
    # Save current directory and navigate to frontend directory
    local original_dir="$(pwd)"
    cd "players-ui-react"

    # Check if React dev server is already running
    if pgrep -f "react-scripts/scripts/start.js" >/dev/null; then
        success "React frontend already running"
        cd "$original_dir"
        return
    fi

    log "Starting React frontend..."
    yarn start > "../logs/react-frontend.log" 2>&1 &
    sleep 5

    if pgrep -f "react-scripts/scripts/start.js" >/dev/null; then
        success "React frontend started"
    else
        error "Failed to start React frontend"
        exit 1
    fi

    # Return to original directory
    cd "$original_dir"
}

start_ollama() {
    if ! command -v docker >/dev/null && ! command -v podman >/dev/null; then
        log "No container manager found, skipping Ollama"
        return
    fi

    local cmd="docker"
    [[ ! -x "$(command -v docker)" ]] && cmd="podman"

    # Check if Ollama is running
    if $cmd ps | grep -q ollama; then
        success "Ollama already running"
        return
    fi

    # Check if container exists (but is stopped)
    if $cmd ps -a | grep -q ollama; then
        log "Starting existing Ollama container..."
        $cmd start ollama
        sleep 3
        success "Ollama started"
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

    # Ensure we're in the project root directory
    local script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
    local project_root="$(dirname "$script_dir")"
    cd "$project_root"


    # Create logs directory if it doesn't exist
    mkdir -p "logs"

    start_spring_boot
    start_frontend
    start_ollama

    success "All services started!"
    log "React Frontend: http://localhost:3000"
    log "Spring Boot API: http://localhost:8080"
    log "Ollama: http://localhost:11434"
    log "Stop services: ./scripts/down.sh"
}

main "$@"

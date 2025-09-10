#!/bin/bash

# setup.sh - Prepare project environment and dependencies
set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m'

log() { echo -e "${BLUE}[INFO]${NC} $1"; }
success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
error() { echo -e "${RED}[ERROR]${NC} $1"; }

# Source SDKMAN once at start if available
[[ -f "$HOME/.sdkman/bin/sdkman-init.sh" ]] && source "$HOME/.sdkman/bin/sdkman-init.sh"

install_sdkman() {
    if [[ -d "$HOME/.sdkman" ]]; then
        success "SDKMAN already installed"
        return
    fi

    log "Installing SDKMAN..."
    curl -s "https://get.sdkman.io" | bash
    source "$HOME/.sdkman/bin/sdkman-init.sh"
    success "SDKMAN installed"
}

setup_java() {
    if command -v java >/dev/null && java --version | grep -q "17\."; then
        success "Java 17 already installed"
        return
    fi

    log "Installing Java 17.0.16-tem..."
    sdk install java 17.0.16-tem
    sdk use java 17.0.16-tem
    success "Java 17 installed"
}

setup_maven() {
    if command -v mvn >/dev/null; then
        success "Maven already installed"
        return
    fi

    log "Installing Maven..."
    sdk install maven
    success "Maven installed"
}

install_deps() {
    # Navigate to backend directory (assuming script is run from project root)
    cd "player-service-backend"

    if [[ -d "target" ]]; then
        success "Backend dependencies already installed"
    else
        log "Installing backend dependencies..."
        mvn clean install -DskipTests
        success "Backend dependencies installed"
    fi

    # Install frontend dependencies
    cd "../players-ui-react"

    if [[ -d "node_modules" ]]; then
        success "Frontend dependencies already installed"
    else
        log "Installing frontend dependencies..."
        yarn install
        success "Frontend dependencies installed"
    fi
}

main() {
    log "Starting project setup..."

    install_sdkman
    setup_java
    setup_maven
    install_deps

    success "Setup complete!"
    log "Run services: ./scripts/run.sh"
}

main "$@"

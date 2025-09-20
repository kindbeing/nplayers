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
    if command -v java >/dev/null && java --version | grep -q "17\.0\.16-tem"; then
        success "Java 17.0.16-tem already installed"
        return
    fi

    log "Installing Java 17.0.16-tem..."
    sdk install java 17.0.16-tem
    sdk use java 17.0.16-tem
    success "Java 17.0.16-tem installed and set as default"
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

install_poetry() {
    if command -v poetry >/dev/null; then
        success "Poetry already installed"
        return
    fi

    log "Installing Poetry..."
    if ! curl -sSL https://install.python-poetry.org | python3 -; then
        error "Failed to install Poetry"
        exit 1
    fi
    export PATH="$HOME/.local/bin:$PATH"

    # Verify Poetry installation
    if command -v poetry >/dev/null; then
        success "Poetry installed"
    else
        error "Poetry installation failed - poetry command not found"
        exit 1
    fi
}

install_justfile() {
    if command -v just >/dev/null; then
        success "Justfile (just) already installed"
        return
    fi

    log "Installing Justfile (just)..."

    # Check if Homebrew is available (macOS)
    if command -v brew >/dev/null; then
        if ! brew install just; then
            error "Failed to install just via Homebrew"
            exit 1
        fi
    else
        # Fallback to direct installation script
        if ! curl --proto '=https' --tlsv1.2 -sSf https://just.systems/install.sh | bash -s -- --to ~/.local/bin; then
            error "Failed to install just via install script"
            exit 1
        fi
        export PATH="$HOME/.local/bin:$PATH"
    fi

    # Verify just installation
    if command -v just >/dev/null; then
        success "Justfile (just) installed"
        log "Run 'just --help' to see available commands"
    else
        error "Justfile installation failed - just command not found"
        exit 1
    fi
}

setup_python_ml() {
    # install_deps leaves us in players-ui-react, so go back to project root
    cd "../"

    # Navigate to Python ML directory
    cd "player-service-backend/player-service-model"

    # Check if dependencies are already installed
    if [[ -d ".venv" ]] && poetry check >/dev/null 2>&1; then
        success "Python ML dependencies already installed"
        return
    fi

    log "Setting up Python ML environment..."
    if ! poetry install; then
        error "Failed to install Python ML dependencies"
        exit 1
    fi
    success "Python ML dependencies installed"
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

    # Ensure we're in the project root directory
    local script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
    local project_root="$(dirname "$script_dir")"
    cd "$project_root"

    install_sdkman
    setup_java
    setup_maven
    install_poetry
    install_justfile
    install_deps
    setup_python_ml

    success "Setup complete!"
    log "Run services: ./scripts/run.sh"
    log "Or use justfile: just start"
}

main "$@"

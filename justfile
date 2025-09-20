# Justfile for Full Stack Player Service
# Run 'just --help' to see all available commands

# Default recipe (run when you just type 'just')
default:
    @echo "🚀 Full Stack Player Service Commands:"
    @echo ""
    @echo "Development:"
    @echo "  just start          - Start all services"
    @echo "  just stop           - Stop all services"
    @echo "  just restart        - Restart all services"
    @echo "  just status         - Check service status"
    @echo ""
    @echo "Individual Services:"
    @echo "  just backend        - Start Spring Boot backend"
    @echo "  just frontend       - Start React frontend"
    @echo "  just ml             - Start Python ML service"
    @echo "  just ollama         - Start Ollama AI service"
    @echo ""
    @echo "Logs:"
    @echo "  just logs           - Show all service logs"
    @echo "  just logb           - Show backend logs"
    @echo "  just logf           - Show frontend logs"
    @echo "  just logm           - Show ML service logs"
    @echo ""
    @echo "Development:"
    @echo "  just setup          - Run initial setup"
    @echo "  just clean          - Clean all build artifacts"
    @echo "  just test           - Run tests"
    @echo ""

# Start all services
start: start-backend start-frontend start-ml start-ollama
    @echo "🎉 All services started!"
    @echo "📱 Frontend: http://localhost:3000"
    @echo "🔧 Backend:  http://localhost:8080"
    @echo "🤖 ML API:   http://localhost:5000"
    @echo "🧠 Ollama:   http://localhost:11434"

# Stop all services
stop:
    @echo "🛑 Stopping all services..."
    # Stop Spring Boot
    -@echo "Stopping Spring Boot..." && pkill -TERM -f "spring-boot:run" 2>/dev/null || true && sleep 2 && pkill -KILL -f "spring-boot:run" 2>/dev/null || true
    # Stop React Frontend
    -@echo "Stopping React..." && pkill -TERM -f "react-scripts" 2>/dev/null || true && sleep 2 && pkill -KILL -f "react-scripts" 2>/dev/null || true
    # Stop Python ML
    -@echo "Stopping Python ML..." && pkill -TERM -f "python.*server.py" 2>/dev/null || true && sleep 1 && pkill -KILL -f "python.*server.py" 2>/dev/null || true
    # Stop Ollama container
    -@echo "Stopping Ollama..." && docker stop ollama 2>/dev/null || true
    @echo "✅ All services stopped"

# Restart all services
restart: stop start

# Check service status
status:
    @echo "📊 Service Status:"
    @echo ""
    @lsof -i :3000 >/dev/null && echo " ✅ React Frontend:    UP (localhost:3000)" || echo "❌ React Frontend:    DOWN"
    @lsof -i :8080 >/dev/null && echo " ✅ Spring Boot:       UP (localhost:8080)" || echo "❌ Spring Boot:       DOWN"
    @lsof -i :5000 >/dev/null && echo " ✅ Python ML:         UP (localhost:5000)" || echo "❌ Python ML:         DOWN"
    @lsof -i :11434 >/dev/null && echo " ✅ Ollama:            UP (localhost:11434)" || echo "❌ Ollama:            DOWN"
    @echo ""

# Individual service starters
backend:
    @echo "🔧 Starting Spring Boot Backend..."
    cd player-service-backend && ~/.sdkman/candidates/maven/current/bin/mvn spring-boot:run

frontend:
    @echo "📱 Starting React Frontend..."
    cd players-ui-react && yarn start

ml:
    @echo "🤖 Starting Python ML Service..."
    cd player-service-backend/player-service-model/a4a_model && poetry run python server.py

ollama:
    @echo "🧠 Starting Ollama AI Service..."
    # Check if Docker is available
    @docker info >/dev/null 2>&1 || (echo "❌ Docker not available" && exit 1)
    # If already running, we're done
    @if docker ps | grep -q "ollama"; then \
        echo " ✅ Ollama already running"; \
    else \
        if docker ps -a | grep -q "ollama"; then \
            echo "🔄 Starting existing Ollama container..." && \
            docker start ollama; \
        else \
            echo "🐳 Creating new Ollama container..." && \
            docker run -d -v ollama:/root/.ollama -p 11434:11434 --name ollama ollama/ollama; \
        fi && \
        sleep 3 && \
        if docker ps | grep -q "ollama"; then \
            echo "🤖 Loading TinyLlama model..." && \
            docker exec ollama ollama run tinyllama 2>/dev/null && \
            echo " ✅ Ollama ready"; \
        else \
            echo "❌ Failed to start Ollama"; \
        fi; \
    fi

# Start services in background (for automation)
start-backend:
    @echo "🔧 Starting Spring Boot Backend..."
    @cd player-service-backend && nohup ~/.sdkman/candidates/maven/current/bin/mvn spring-boot:run > ../logs/spring-boot.log 2>&1 &
    @sleep 5
    @lsof -i :8080 >/dev/null && echo " ✅ Spring Boot started" || echo "❌ Spring Boot failed to start"

start-frontend:
    @echo "📱 Starting React Frontend..."
    @cd players-ui-react && nohup yarn start > ../logs/react-frontend.log 2>&1 &
    @sleep 5
    @lsof -i :3000 >/dev/null && echo " ✅ React Frontend started" || echo "❌ React Frontend failed to start"

start-ml:
    @echo "🤖 Starting Python ML Service..."
    @cd player-service-backend/player-service-model/a4a_model && nohup poetry run python server.py > ../../../logs/python-ml.log 2>&1 &
    @sleep 3
    @lsof -i :5000 >/dev/null && echo " ✅ Python ML started" || echo "❌ Python ML failed to start"

start-ollama:
    @echo "🧠 Starting Ollama AI Service..."
    # If already running, we're done
    @if docker ps | grep -q "ollama"; then \
        echo " ✅ Ollama already running"; \
    else \
        if docker ps -a | grep -q "ollama"; then \
            echo "🔄 Starting existing Ollama container..." && \
            docker start ollama; \
        else \
            echo "🐳 Creating new Ollama container..." && \
            docker run -d -v ollama:/root/.ollama -p 11434:11434 --name ollama ollama/ollama; \
        fi && \
        sleep 3 && \
        if docker ps | grep -q "ollama"; then \
            echo "🤖 Loading TinyLlama model..." && \
            docker exec ollama ollama run tinyllama 2>/dev/null && \
            echo " ✅ Ollama started"; \
        else \
            echo "❌ Ollama failed to start"; \
        fi; \
    fi

# Logs
logs:
    @echo "📋 Showing all service logs..."
    tail -f logs/*.log

logb:
    @echo "🔧 Spring Boot Backend Logs:"
    tail -f logs/spring-boot.log

logf:
    @echo "📱 React Frontend Logs:"
    tail -f logs/react-frontend.log

logm:
    @echo "🤖 Python ML Service Logs:"
    tail -f logs/python-ml.log

# Setup and maintenance
setup:
    @echo "⚙️  Running initial setup..."
    ./scripts/setup.sh

clean:
    @echo "🧹 Cleaning build artifacts..."
    @cd player-service-backend && mvn clean
    @cd players-ui-react && rm -rf node_modules/.cache 2>/dev/null || true
    @cd player-service-backend/player-service-model && rm -rf .venv 2>/dev/null || true
    @echo "✅ Clean complete"

test:
    @echo "🧪 Running tests..."
    @cd player-service-backend && mvn test
    @cd players-ui-react && yarn test --watchAll=false

# Health checks
health:
    @echo "🏥 Service Health Checks:"
    @echo ""
    @curl -s http://localhost:8080/actuator/health >/dev/null && echo " ✅ Spring Boot: HEALTHY" || echo "❌ Spring Boot: UNHEALTHY"
    @curl -s http://localhost:5000/ >/dev/null && echo " ✅ Python ML:   HEALTHY" || echo "❌ Python ML:   UNHEALTHY"
    @curl -s http://localhost:11434/api/tags >/dev/null && echo " ✅ Ollama:      HEALTHY" || echo "❌ Ollama:      UNHEALTHY"
    @echo ""

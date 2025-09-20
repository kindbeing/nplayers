# Full Stack Player Service - File Inventory

## 📋 Project Overview
This is a full-stack baseball player service application with React frontend, Spring Boot backend, and integrated AI/ML components for team generation.

## 🗂️ Directory Structure & Important Files

### Root Level Files
- **[README.md](../README.md)** - Main project documentation with overview, tech stack, and installation instructions
- **[pom.xml](../pom.xml)** - Root Maven configuration for the overall project (NEW)
- **[LICENSE](../LICENSE)** - Project license file
- **[CONTRIBUTING.md](../CONTRIBUTING.md)** - Contributing guidelines for the project

### 📁 Backend (`player-service-backend/`)

#### Configuration & Build Files
- **[pom.xml](../player-service-backend/pom.xml)** - Maven configuration with Spring Boot 3.3.4, H2 database, Ollama4j SDK dependencies
- **[application.yml](../player-service-backend/src/main/resources/application.yml)** - Spring Boot configuration (H2 in-memory DB, port 8080)
- **[schema.sql](../player-service-backend/src/main/resources/schema.sql)** - Database schema initialization script that creates PLAYERS table from CSV
- **[Player.csv](../player-service-backend/Player.csv)** - Source data file containing baseball player information

#### Java Source Code
- **[PlayerServiceJavaApplication.java](../player-service-backend/src/main/java/com/app/playerservicejava/PlayerServiceJavaApplication.java)** - Main Spring Boot application entry point
- **[PlayerController.java](../player-service-backend/src/main/java/com/app/playerservicejava/controller/PlayerController.java)** - REST API controller for `/v1/players` endpoints (GET all players, GET player by ID)
- **[ChatController.java](../player-service-backend/src/main/java/com/app/playerservicejava/controller/chat/ChatController.java)** - Chat/AI integration controller for Ollama LLM interactions
- **[Player.java](../player-service-backend/src/main/java/com/app/playerservicejava/model/Player.java)** - JPA entity model for Player data
- **[Players.java](../player-service-backend/src/main/java/com/app/playerservicejava/model/Players.java)** - Wrapper model for collection of Player objects
- **[PlayerService.java](../player-service-backend/src/main/java/com/app/playerservicejava/service/PlayerService.java)** - Business logic service for player data operations
- **[ChatClientService.java](../player-service-backend/src/main/java/com/app/playerservicejava/service/chat/ChatClientService.java)** - Service for Ollama AI chat integration
- **[PlayerRepository.java](../player-service-backend/src/main/java/com/app/playerservicejava/repository/PlayerRepository.java)** - JPA repository interface for Player entity
- **[ChatClientConfiguration.java](../player-service-backend/src/main/java/com/app/playerservicejava/config/ChatClientConfiguration.java)** - Configuration for Ollama chat client

#### Test Files
- **[PlayerServiceJavaApplicationTests.java](../player-service-backend/src/test/java/com/app/playerservicejava/PlayerServiceJavaApplicationTests.java)** - Main application test class

#### Collection & Resources
- **[GetAllPlayers.http](../player-service-backend/collection/GetAllPlayers.http)** - HTTP request file for testing GET /v1/players endpoint
- **[GetPlayerById.http](../player-service-backend/collection/GetPlayerById.http)** - HTTP request file for testing GET /v1/players/{id} endpoint
- **[chat_requests.txt](../player-service-backend/collection/chat_requests.txt)** - Sample chat/AI interaction requests

### 🎨 Frontend (`players-ui-react/`)

#### Configuration & Dependencies
- **[package.json](../players-ui-react/package.json)** - Node.js dependencies and scripts (React 18.3.1, testing libraries, proxy to localhost:8080)
- **[yarn.lock](../players-ui-react/yarn.lock)** - Yarn dependency lock file
- **[README.md](../players-ui-react/README.md)** - Frontend-specific documentation and setup instructions

#### React Application
- **[index.js](../players-ui-react/src/index.js)** - React application entry point and root rendering
- **[index.css](../players-ui-react/src/index.css)** - Global CSS styles
- **[PlayerMain.jsx](../players-ui-react/src/components/PlayerMain.jsx)** - Main component displaying player header and results
- **[PlayersResults.jsx](../players-ui-react/src/components/PlayersResults.jsx)** - Component for displaying player data results
- **[DataFetcher.jsx](../players-ui-react/src/utils/DataFetcher.jsx)** - Utility function for fetching player data from backend API
- **[index.js](../players-ui-react/src/utils/index.js)** - Utility module exports
- **[logo.svg](../players-ui-react/src/assets/logo.svg)** - Application logo asset
- **[PlayersMain.css](../players-ui-react/src/styling/PlayersMain.css)** - Component-specific CSS for PlayerMain

#### Testing
- **[setupTests.js](../players-ui-react/src/tests/setupTests.js)** - Jest testing framework setup
- **[PlayerMain.test.js](../players-ui-react/src/tests/PlayerMain.test.js)** - Unit tests for PlayerMain component
- **[utils.test.js](../players-ui-react/src/tests/utils.test.js)** - Unit tests for utility functions

#### Public Assets
- **[index.html](../players-ui-react/public/index.html)** - HTML template for React app
- **[manifest.json](../players-ui-react/public/manifest.json)** - Web app manifest for PWA features
- **[robots.txt](../players-ui-react/public/robots.txt)** - Search engine crawling instructions
- **[favicon.ico](../players-ui-react/public/favicon.ico)** - Browser favicon
- **[logo192.png](../players-ui-react/public/logo192.png)** & **[logo512.png](../players-ui-react/public/logo512.png)** - PWA icons

### 🤖 AI/ML Model (`player-service-backend/player-service-model/`)

#### Python ML Service
- **[README.md](../player-service-backend/player-service-model/README.md)** - Documentation for the AI model service and API usage
- **[Dockerfile](../player-service-backend/player-service-model/Dockerfile)** - Container definition for the ML model service (exposes port 5000)
- **[pyproject.toml](../player-service-backend/player-service-model/pyproject.toml)** & **[poetry.lock](../player-service-backend/player-service-model/poetry.lock)** - Python dependency management
- **[__init__.py](../player-service-backend/player-service-model/a4a_model/__init__.py)** - Python package initialization
- **[model.py](../player-service-backend/player-service-model/a4a_model/model.py)** - Core ML model implementation for team generation
- **[server.py](../player-service-backend/player-service-model/a4a_model/server.py)** - Flask/FastAPI server for ML model inference API
- **[train.ipynb](../player-service-backend/player-service-model/a4a_model/train.ipynb)** - Jupyter notebook for model training
- **[player.csv](../player-service-backend/player-service-model/a4a_model/player.csv)** - Training data file
- **[features_db.csv](../player-service-backend/player-service-model/a4a_model/features_db.csv)** - Feature database for ML model
- **[team_model.joblib](../player-service-backend/player-service-model/a4a_model/team_model.joblib)** - Trained ML model file (serialized)

### 📜 Scripts (`scripts/`) (NEW)

#### Deployment & Management
- **[justfile](../justfile)** - Bash script to manage all services (Spring Boot, React, Ollama)
- **[setup.sh](../scripts/setup.sh)** - Bash script for initial project setup and dependency installation

### 📝 Documentation (`docs/`) (NEW)

#### Architecture & Design
- **[ddd.md](ddd.md)** - Domain-Driven Design (DDD) documentation
- **[roadmap.md](roadmap.md)** - Project roadmap and future plans
- **[status.md](status.md)** - Current project status and progress tracking

## Component Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   React UI      │    │   Spring Boot   │    │   FastAPI ML    │
│                 │    │   Backend       │    │   Service       │
│ - Components    │◄──►│ - Controllers   │◄──►│ - Models        │
│ - Services      │    │ - Services      │    │ - Predictions   │
│ - State Mgmt    │    │ - Repositories  │    │ - Analytics     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
                    ┌─────────────────┐
                    │     H2 DB       │
                    │                 │
                    │ - Player data   │
                    │ - Chat logs     │
                    └─────────────────┘
```

### 📊 Logs (`logs/`) (NEW)

#### Application Logs
- **[spring-boot.log](../logs/spring-boot.log)** - Spring Boot backend application logs
- **[react-frontend.log](../logs/react-frontend.log)** - React frontend application logs
- **[python-ml.log](../logs/python-ml.log)** - Python ML service (FastAPI) application logs

### 🔧 Build Artifacts (`player-service-backend/target/`)

#### Compiled Classes
- **`classes/`** - Compiled Java bytecode and resources
- **`generated-sources/`** - Auto-generated source code
- **`generated-test-sources/`** - Auto-generated test source code
- **`maven-status/`** - Maven build status files

## 🔗 Key Integration Points

1. **Frontend-Backend**: React app proxies to Spring Boot API via `http://localhost:8080`
2. **Backend-ML**: Spring Boot integrates with Python ML service via Ollama4j SDK
3. **Database**: H2 in-memory database initialized from CSV data
4. **AI Integration**: Ollama container runs TinyLlama model for chat functionality
5. **Containerization**: Docker support for both Ollama and ML model services

## 🚀 Key Endpoints

- `GET /v1/players` - Retrieve all players
- `GET /v1/players/{id}` - Retrieve specific player by ID
- `GET /v1/chat/list-models` - List available AI models
- `POST /team/generate` - ML model team generation (via Python service)
- `POST /team/feedback` - ML model feedback submission

## 🛠️ Development Workflow (NEW)

1. Use `scripts/setup.sh` for initial setup
2. Use `scripts/run.sh` to start all services
3. Use `scripts/down.sh` to stop services
4. Frontend runs on `http://localhost:3000`
5. Backend API on `http://localhost:8080`
6. ML model service on `http://localhost:5000`
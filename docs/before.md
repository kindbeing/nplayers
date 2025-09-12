# Full Stack Player Service - File Inventory

## 📋 Project Overview
This is a full-stack baseball player service application with React frontend, Spring Boot backend, and integrated AI/ML components for team generation.

## 🗂️ Directory Structure & Important Files

### Root Level Files
- **`README.md`** - Main project documentation with overview, tech stack, and installation instructions
- **`pom.xml`** - Root Maven configuration for the overall project
- **`LICENSE`** - Project license file
- **`CONTRIBUTING.md`** - Contributing guidelines for the project

### 📁 Backend (`player-service-backend/`)

#### Configuration & Build Files
- **`pom.xml`** - Maven configuration with Spring Boot 3.3.4, H2 database, Ollama4j SDK dependencies
- **`src/main/resources/application.yml`** - Spring Boot configuration (H2 in-memory DB, port 8080)
- **`src/main/resources/schema.sql`** - Database schema initialization script that creates PLAYERS table from CSV
- **`Player.csv`** - Source data file containing baseball player information

#### Java Source Code
- **`src/main/java/com/app/playerservicejava/PlayerServiceJavaApplication.java`** - Main Spring Boot application entry point
- **`src/main/java/com/app/playerservicejava/controller/PlayerController.java`** - REST API controller for `/v1/players` endpoints (GET all players, GET player by ID)
- **`src/main/java/com/app/playerservicejava/controller/chat/ChatController.java`** - Chat/AI integration controller for Ollama LLM interactions
- **`src/main/java/com/app/playerservicejava/model/Player.java`** - JPA entity model for Player data
- **`src/main/java/com/app/playerservicejava/model/Players.java`** - Wrapper model for collection of Player objects
- **`src/main/java/com/app/playerservicejava/service/PlayerService.java`** - Business logic service for player data operations
- **`src/main/java/com/app/playerservicejava/service/chat/ChatClientService.java`** - Service for Ollama AI chat integration
- **`src/main/java/com/app/playerservicejava/repository/PlayerRepository.java`** - JPA repository interface for Player entity
- **`src/main/java/com/app/playerservicejava/config/ChatClientConfiguration.java`** - Configuration for Ollama chat client

#### Test Files
- **`src/test/java/com/app/playerservicejava/PlayerServiceJavaApplicationTests.java`** - Main application test class

#### Collection & Resources
- **`collection/GetAllPlayers.http`** - HTTP request file for testing GET /v1/players endpoint
- **`collection/GetPlayerById.http`** - HTTP request file for testing GET /v1/players/{id} endpoint
- **`collection/chat_requests.txt`** - Sample chat/AI interaction requests

### 🎨 Frontend (`players-ui-react/`)

#### Configuration & Dependencies
- **`package.json`** - Node.js dependencies and scripts (React 18.3.1, testing libraries, proxy to localhost:8080)
- **`yarn.lock`** - Yarn dependency lock file
- **`README.md`** - Frontend-specific documentation and setup instructions

#### React Application
- **`src/index.js`** - React application entry point and root rendering
- **`src/index.css`** - Global CSS styles
- **`src/components/PlayerMain.jsx`** - Main component displaying player header and results
- **`src/components/PlayersResults.jsx`** - Component for displaying player data results
- **`src/utils/DataFetcher.jsx`** - Utility function for fetching player data from backend API
- **`src/utils/index.js`** - Utility module exports
- **`src/assets/logo.svg`** - Application logo asset
- **`src/styling/PlayersMain.css`** - Component-specific CSS for PlayerMain

#### Testing
- **`src/tests/setupTests.js`** - Jest testing framework setup
- **`src/tests/PlayerMain.test.js`** - Unit tests for PlayerMain component
- **`src/tests/utils.test.js`** - Unit tests for utility functions

#### Public Assets
- **`public/index.html`** - HTML template for React app
- **`public/manifest.json`** - Web app manifest for PWA features
- **`public/robots.txt`** - Search engine crawling instructions
- **`public/favicon.ico`** - Browser favicon
- **`public/logo192.png`** & **`public/logo512.png`** - PWA icons

### 🤖 AI/ML Model (`player-service-backend/player-service-model/`)

#### Python ML Service
- **`README.md`** - Documentation for the AI model service and API usage
- **`Dockerfile`** - Container definition for the ML model service (exposes port 5000)
- **`pyproject.toml`** & **`poetry.lock`** - Python dependency management
- **`a4a_model/__init__.py`** - Python package initialization
- **`a4a_model/model.py`** - Core ML model implementation for team generation
- **`a4a_model/server.py`** - Flask/FastAPI server for ML model inference API
- **`a4a_model/train.ipynb`** - Jupyter notebook for model training
- **`a4a_model/player.csv`** - Training data file
- **`a4a_model/features_db.csv`** - Feature database for ML model
- **`a4a_model/team_model.joblib`** - Trained ML model file (serialized)

### 📜 Scripts (`scripts/`)

#### Deployment & Management
- **`run.sh`** - Bash script to start all services (Spring Boot, React, Ollama)
- **`down.sh`** - Bash script to stop all running services
- **`setup.sh`** - Bash script for initial project setup and dependency installation

### 📝 Documentation (`docs/`)

#### Architecture & Design
- **`system-design.md`** - System architecture documentation
- **`ddd.md`** - Domain-Driven Design (DDD) documentation
- **`roadmap.md`** - Project roadmap and future plans
- **`status.md`** - Current project status and progress tracking

### 📊 Logs (`logs/`)

#### Application Logs
- **`spring-boot.log`** - Backend application logs
- **`react-frontend.log`** - Frontend application logs

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

## 🛠️ Development Workflow

1. Use `scripts/setup.sh` for initial setup
2. Use `scripts/run.sh` to start all services
3. Use `scripts/down.sh` to stop services
4. Frontend runs on `http://localhost:3000`
5. Backend API on `http://localhost:8080`
6. ML model service on `http://localhost:5000`

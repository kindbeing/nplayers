# 🐳 Docker Compose Setup for Player Service

This document explains how to run the full-stack Player Service application using Docker Compose.

## 📋 Prerequisites

- Docker Engine 20.10+
- Docker Compose 2.0+
- At least 4GB of available RAM
- At least 10GB of available disk space

## 🚀 Quick Start

### Production Mode (Recommended)

```bash
# Build and start all services
docker-compose up --build

# Or run in background
docker-compose up --build -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

### Development Mode

```bash
# Start with development configuration (hot reload enabled)
docker-compose -f docker-compose.yml -f docker-compose.override.yml up --build

# Or run in background
docker-compose -f docker-compose.yml -f docker-compose.override.yml up --build -d
```

## 🔧 Services Overview

| Service | Port | Description | Technology |
|---------|------|-------------|------------|
| `frontend` | 3000 | React web application | React 18 + Nginx |
| `backend` | 8080 | REST API server | Spring Boot 3.3.4 + Java 17 |
| `ollama` | 11434 | AI/ML inference | Ollama + TinyLlama |
| `ml-service` | 5000 | Team generation API | Python + Flask + scikit-learn |

## 🌐 Access Points

Once running, access your application at:

- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **API Documentation**: http://localhost:8080/swagger-ui.html (if enabled)
- **H2 Database Console**: http://localhost:8080/h2-console (if enabled)
- **Ollama API**: http://localhost:11434

## 🛠️ Available Commands

### Basic Operations

```bash
# Start all services
docker-compose up -d

# Stop all services
docker-compose down

# View service logs
docker-compose logs [service-name]

# Follow logs in real-time
docker-compose logs -f [service-name]

# Restart a specific service
docker-compose restart [service-name]
```

### Service-Specific Commands

```bash
# Start only backend and database
docker-compose up backend ollama

# Rebuild a specific service
docker-compose up --build frontend

# Scale services (if needed)
docker-compose up --scale frontend=2

# Execute commands in running containers
docker-compose exec backend bash
docker-compose exec ml-service sh
```

### Cleanup

```bash
# Remove containers and networks
docker-compose down

# Remove containers, networks, and volumes
docker-compose down -v

# Remove everything including images
docker-compose down --rmi all -v
```

## 🔧 Development Workflow

### Making Code Changes

**Frontend (React):**
```bash
# With hot reload enabled
docker-compose -f docker-compose.yml -f docker-compose.override.yml up frontend
```

**Backend (Spring Boot):**
```bash
# With hot reload enabled
docker-compose -f docker-compose.yml -f docker-compose.override.yml up backend
```

**ML Service (Python):**
```bash
# With auto-reload enabled
docker-compose -f docker-compose.yml -f docker-compose.override.yml up ml-service
```

### Debugging

```bash
# Check service health
docker-compose ps

# View resource usage
docker-compose stats

# Inspect container logs
docker-compose logs backend | tail -100

# Access container shell
docker-compose exec backend bash
```

## 📊 Monitoring & Troubleshooting

### Health Checks

All services include health checks that run every 30 seconds:

- **Frontend**: Checks if React app is responding
- **Backend**: Tests `/v1/players` endpoint
- **Ollama**: Tests `/api/tags` endpoint
- **ML Service**: Tests root endpoint

### Common Issues

**Port conflicts:**
```bash
# Check what's using ports
lsof -i :3000,8080,5000,11434

# Use different ports in docker-compose.override.yml
ports:
  - "3001:3000"  # Map to different host port
```

**Memory issues:**
```bash
# Increase Docker memory limit in Docker Desktop
# Or add memory limits to services
deploy:
  resources:
    limits:
      memory: 1G
```

**Build failures:**
```bash
# Clean and rebuild
docker-compose down --rmi all
docker-compose up --build
```

### Performance Optimization

```bash
# Use build cache effectively
docker-compose build --no-cache frontend

# Parallel builds
docker-compose build --parallel

# Production optimizations
docker-compose -f docker-compose.prod.yml up -d
```

## 🔐 Security Considerations

### Development
- Services run with debug/profiling enabled
- Default credentials used for databases
- CORS enabled for local development

### Production Deployment
- Use environment-specific compose files
- Implement proper secrets management
- Configure reverse proxies
- Enable TLS/SSL
- Set up proper logging and monitoring

## 📝 Environment Variables

### Backend Configuration
```yaml
environment:
  - SPRING_PROFILES_ACTIVE=docker
  - OLLAMA_HOST=http://ollama:11434
  - JAVA_OPTS=-Xmx1g -Xms512m
```

### Frontend Configuration
```yaml
environment:
  - REACT_APP_API_URL=http://localhost:8080
  - NODE_ENV=production
```

## 🚀 Deployment Options

### Local Development
```bash
docker-compose -f docker-compose.yml -f docker-compose.override.yml up -d
```

### Production
```bash
# Create production compose file
docker-compose -f docker-compose.prod.yml up -d
```

### CI/CD Integration
```bash
# Build and test
docker-compose -f docker-compose.test.yml up --abort-on-container-exit

# Deploy
docker-compose -f docker-compose.prod.yml up -d --scale frontend=3
```

## 📚 Additional Resources

- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Spring Boot with Docker](https://spring.io/guides/gs/spring-boot-docker/)
- [React with Docker](https://mherman.org/blog/dockerizing-a-react-app/)
- [Ollama Documentation](https://github.com/ollama/ollama)

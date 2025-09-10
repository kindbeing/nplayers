# System Design

## Architecture Overview

The Player Service is a fullstack application consisting of three main components:

### Frontend (React)
- **Framework**: React 18 with hooks
- **Styling**: CSS modules
- **State Management**: React Context API
- **Routing**: React Router
- **HTTP Client**: Axios

### Backend (Java)
- **Framework**: Spring Boot 3.x
- **Language**: Java 17
- **Database**: H2 (development), PostgreSQL (production)
- **ORM**: Spring Data JPA
- **API**: RESTful with OpenAPI/Swagger

### ML Service (Python)
- **Framework**: FastAPI
- **ML Library**: scikit-learn, pandas, joblib
- **Model Storage**: Serialized models (.joblib)
- **Data Processing**: CSV-based player data

## API Design

### Player Endpoints
```
GET    /api/players          # Get all players (paginated)
GET    /api/players/{id}     # Get player by ID
POST   /api/players          # Create new player
PUT    /api/players/{id}     # Update player
DELETE /api/players/{id}     # Delete player
GET    /api/players/search   # Search players
```

### Chat Endpoints
```
POST   /api/chat             # Send chat message
GET    /api/chat/history     # Get chat history
```

## Data Model

### Player Entity
```java
{
  id: Long,
  name: String,
  email: String,
  position: String,
  team: String,
  stats: Map<String, Object>,
  createdAt: LocalDateTime,
  updatedAt: LocalDateTime
}
```

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

## Security Considerations
- Input validation on all endpoints
- CORS configuration for frontend
- Basic authentication (to be enhanced)
- API rate limiting

## Performance Requirements
- API response time: <200ms for simple queries
- Concurrent users: 1000+
- Database queries: Optimized with indexes
- Frontend bundle size: <500KB gzipped

## Deployment Architecture
- **Development**: Local Docker containers
- **Staging**: AWS ECS with RDS
- **Production**: Kubernetes with PostgreSQL cluster

# Dynamic Survey Application

A full-stack survey management system built with Angular (frontend) and Spring Boot (backend). The application allows users to create, distribute, and analyze surveys with secure authentication and role-based access control.

## Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Setup](#setup)
  - [Backend Setup](#backend-setup)
  - [Frontend Setup](#frontend-setup)
- [Running the Application](#running-the-application)
  - [Backend](#backend)
  - [Frontend](#frontend)
- [API Documentation](#api-documentation)
- [Testing](#testing)
  - [Backend Tests](#backend-tests)
  - [Frontend Tests](#frontend-tests)
- [Environment Variables](#environment-variables)
- [Project Structure](#project-structure)

## Overview

Dynamic Survey is a web application designed for creating and managing online surveys. It provides:

- User authentication (login/register) with JWT-based security
- Survey creation with various question types
- Response collection and real-time analytics
- Role-based access (admin, regular user)
- Responsive design using Angular Material

## Recent Updates

- **2026-09**: Fixed navigation and redirect issues, unified global SCSS styles. See [Design Decisions](./design/PLAN.md) for details.

## Tech Stack

### Frontend

- **Framework**: Angular 19.2
- **UI Library**: Angular Material 19.2
- **Charts**: ng2-charts with Chart.js
- **State Management**: RxJS
- **Build Tool**: Angular CLI
- **Language**: TypeScript

### Backend

- **Framework**: Spring Boot 4.1.0
- **Build System**: Gradle
- **Language**: Java 17
- **Persistence**: Spring Data JPA with Hibernate
- **Database**: MySQL
- **Security**: Spring Security with JWT
- **API Documentation**: SpringDoc OpenAPI (Swagger UI)
- **Additional Features**: Lombok, AOP, DevTools

## Architecture

The application follows a traditional client-server architecture:

- **Frontend**: Single Page Application (SPA) served at `http://localhost:4200`
- **Backend**: RESTful API served at `http://localhost:8080`
- **Communication**: JSON over HTTP/HTTPS
- **Data Flow**:
  - Frontend consumes backend REST APIs
  - Backend interacts with MySQL database via JPA repositories
  - Authentication handled via JWT tokens stored in HTTP-only cookies/localStorage

## Prerequisites

Before you begin, ensure you have installed:

- [Java JDK 17](https://adoptium.net/)
- [Node.js >= 18](https://nodejs.org/) (with npm)
- [MySQL Server >= 8.0](https://dev.mysql.com/downloads/mysql/)
- [Gradle 8+](https://gradle.org/install/) (optional, wrapper included)

## Setup

### Backend Setup

1. **Clone the repository**:

   ```bash
   git clone https://github.com/DynastyCoding/dynamic-survey.git
   cd dynamic-survey/backend
   ```

2. **Configure MySQL**:
   - Create database `dynamic_survey`
   - Ensure MySQL is running on `localhost:3306`
   - Copy `.env.example` to `.env` and fill in your credentials
   - See `application.properties` for the required environment variables

3. **Install dependencies**:
   ```bash
   ./gradlew build  # Downloads dependencies and builds the project
   ```

### Frontend Setup

1. **Navigate to frontend directory**:

   ```bash
   cd dynamic-survey/frontend
   ```

2. **Install npm dependencies**:
   ```bash
   npm install
   ```

## Running the Application

### Backend

```bash
# From the backend directory
./gradlew bootRun
```

The backend will start on `http://localhost:8080`.

- API base path: `/api`
- Swagger UI (when available): `http://localhost:8080/swagger-ui.html`
- Actuator endpoints: `http://localhost:8080/actuator`

### Frontend

```bash
# From the frontend directory
npm start  # or: ng serve
```

The frontend will be available at `http://localhost:4200`.

- The application will automatically proxy API requests to `http://localhost:8080` (configured via `proxy.conf.json` if present)

### Running Both Concurrently

For development, you can run both in separate terminal tabs:

1. Terminal 1: Backend (`./gradlew bootRun`)
2. Terminal 2: Frontend (`npm start`)

## API Documentation

The backend includes SpringDoc OpenAPI for automatic API documentation.

- Access Swagger UI at: `http://localhost:8080/swagger-ui.html`
- Alternative: `http://localhost:8080/v3/api-docs/` for raw JSON specification

## Testing

### Backend Tests

```bash
# Run all tests
./gradlew test

# Run tests for a specific class (example)
./gradlew test --tests com.example.dynamic_survey.controller.AuthControllerTest
```

Test reports are generated at `build/reports/tests/test/index.html`.

### Frontend Tests

```bash
# Run unit tests
npm test  # or: ng test

# Run end-to-end tests (requires separate e2e framework setup)
npm run e2e  # or: ng e2e
```

- Unit tests use Jasmine/Karma
- Coverage reports available after running tests with coverage flag

## Environment Variables

Configuration is handled via environment variables. Copy `.env.example` to `.env` and fill in your values.

### Backend

- `DB_USERNAME`: MySQL username
- `DB_PASSWORD`: MySQL password
- `JWT_SECRET`: Secret key for JWT signing
- `JWT_EXPIRATION`: JWT expiration time (in milliseconds)

### Frontend

- Environment files in `src/environments/` (if using Angular environments)
  - API base URL
  - Feature flags

To override backend properties without modifying files, use:

```bash
./gradlew bootRun --args='--spring.datasource.username=custom_user --spring.datasource.password=custom_pass'
```

## Project Structure

```
dynamic-survey/
├── backend/                     # Spring Boot application
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/          # Java source code
│   │   │   │   └── com/example/dynamic_survey/
│   │   │   │       ├── controller/   # REST controllers
│   │   │   │       ├── service/      # Business logic
│   │   │   │       ├── repository/   # Data access interfaces
│   │   │   │       ├── model/        # JPA entities
│   │   │   │       ├── config/       # Security, Swagger, etc.
│   │   │   │       └── util/         # Utility classes
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       ├── schema.sql        # (if any)
│   │   │       └── data.sql          # (if any)
│   │   └── test/                 # Test source
│   ├── build.gradle
│   ├── gradlew / gradlew.bat
│   └── settings.gradle
└── frontend/                    # Angular application
    ├── src/
    │   ├── app/                 # Angular modules/components
    │   │   ├── core/          # Core services, guards, interceptors
    │   │   ├── shared/        # Shared components, pipes, directives
    │   │   ├── pages/         # Page-level components
    │   │   └── ...            # Feature modules
    │   ├── assets/            # Static assets
    │   ├── environments/      # Environment configurations
    │   ├── styles/            # Global styles (SCSS)
    │   └── index.html
    ├── angular.json
    ├── package.json
    └── tsconfig.json
```

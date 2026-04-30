# Development Guide

## Prerequisites

- Java 21 (via SDKMAN or manual install)
- Docker & Docker Compose (for PostgreSQL)
- Node.js 22+ (managed by Gradle, but helpful for frontend dev)

## Quick Start

```bash
# Start PostgreSQL
docker compose up -d postgres

# Run the application (dev mode)
PORT=8081 KTOR_DEVELOPMENT=true ./gradlew run

# Or with admin user creation
ADMIN_PASSWORD=changeme PORT=8081 KTOR_DEVELOPMENT=true ./gradlew run
```

The app starts at http://localhost:8081 (or whatever PORT you set).

## Stack

| Layer | Technology |
|-------|-----------|
| Web server | Ktor 3.1.3 + Netty |
| DI | Koin 4.1.1 |
| ORM | Exposed 0.61.0 (DSL) |
| Migrations | Flyway 11.14.1 |
| Database | PostgreSQL 16 |
| Templating | kotlinx-html 0.11.0 |
| Serialization | kotlinx-serialization |
| Frontend build | Vite + TypeScript + Tailwind CSS |
| Background jobs | JobRunr 8.3.1 |
| Image processing | Thumbnailator |
| GEDCOM parsing | gedcom4j |

## Configuration

Configuration is in `src/main/resources/application.conf` (HOCON format). All values can be overridden via environment variables:

| Variable | Default | Description |
|----------|---------|-------------|
| `PORT` | 8080 | Server port |
| `KTOR_DEVELOPMENT` | false | Enable dev mode (relaxed admin bootstrap) |
| `DATABASE_URL` | jdbc:postgresql://localhost:5432/familyarchive | DB connection |
| `DATABASE_USER` | familyarchive | DB user |
| `DATABASE_PASSWORD` | familyarchive | DB password |
| `STORAGE_TYPE` | local | `local` or `s3` |
| `STORAGE_LOCAL_UPLOAD_DIR` | uploads | Local file storage directory |
| `ADMIN_EMAIL` | admin@familyarchive.local | Initial admin email |
| `ADMIN_PASSWORD` | (required in prod) | Initial admin password |
| `TINYMCE_API_KEY` | no-api-key | TinyMCE editor license key |

## Project Structure

```
src/main/kotlin/com/timothymarias/familyarchive/
├── config/          # Ktor plugins, Koin module, database, auth
├── database/        # Exposed table definitions
├── dto/             # Data transfer objects for forms
├── jobs/            # Background job classes (JobRunr)
├── model/           # Enums (ArtifactType, FamilyRole, EventType)
├── repository/      # Data access layer (Exposed queries)
├── routes/          # Ktor route handlers (public, API, admin)
├── service/         # Business logic
│   ├── image/       # Image processing
│   └── storage/     # File storage (S3/local)
├── views/           # View context types, type aliases
│   ├── admin/       # Admin page views
│   └── public/      # Public page views
└── components/      # Reusable kotlinx-html components
```

## Running Tests

```bash
# Start the app first
PORT=8081 KTOR_DEVELOPMENT=true ./gradlew run &

# Run golden master tests
KTOR_TEST_PORT=8081 ./gradlew test
```

## Route Checker

Compare routes between Spring and Ktor, or check Ktor standalone:

```bash
# Ktor-only check
KTOR_PORT=8081 ./scripts/route-checker.sh --ktor-only

# Verbose (shows error bodies on 500s)
KTOR_PORT=8081 ./scripts/route-checker.sh --ktor-only --verbose

# Single route
KTOR_PORT=8081 ./scripts/route-checker.sh --ktor-only --route /articles
```

## Frontend Development

The frontend is built by Gradle via Vite. For development with hot reload:

```bash
# Terminal 1: Run the Ktor backend
PORT=8081 KTOR_DEVELOPMENT=true ./gradlew run

# Terminal 2: Run the Vite dev server
npm run dev
```

The Vite dev server runs on port 5173 and proxies API requests to the backend.

## Docker

```bash
# Build and run everything
docker compose up --build

# Just the database
docker compose up -d postgres
```

## Future: GraalVM Native Image

GraalVM native compilation is planned as a followup to further reduce memory footprint from ~50-80MB (JVM) to ~15-30MB (native). This will require reflection configuration for JobRunr, gedcom4j, and the AWS SDK.

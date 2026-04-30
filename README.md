# Family Archive

A family archive application for managing genealogical data, historical documents, photos, and family history. Built with Kotlin and Ktor for lightweight self-hosting.

## Features

- GEDCOM file import for genealogical data
- Interactive family tree visualization (D3.js + Dagre)
- Artifact management (photos, documents, letters, audio, video)
- Automatic thumbnail generation
- OCR transcription support
- Article/blog publishing
- Admin dashboard with full CRUD
- Dark mode UI

## Tech Stack

**Backend:** Ktor 3.1, Exposed, Koin, PostgreSQL, Flyway, JobRunr
**Frontend:** Vite, TypeScript, Tailwind CSS, D3.js

## Quick Start

```bash
docker compose up -d postgres
ADMIN_PASSWORD=changeme KTOR_DEVELOPMENT=true ./gradlew run
```

See [DEVELOPMENT.md](DEVELOPMENT.md) for full setup instructions.

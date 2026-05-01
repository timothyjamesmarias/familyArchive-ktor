# Future Work

## Storage Migration

Currently using local filesystem storage (`STORAGE_TYPE=local`). Plan:

1. **Now:** Use local storage with `STORAGE_LOCAL_UPLOAD_DIR` pointed at a dedicated path on the SSD
2. **When NAS arrives:** Mount NAS share (NFS) to the same upload path — zero code changes
3. **Optional future:** Add MinIO endpoint config to `S3StorageService` for self-hosted S3-compatible object storage. Would require adding a custom endpoint URL field to `S3StorageConfig`.

Set up a backup script for the uploads directory until the NAS is in place.

## GraalVM Native Image

Native compilation works in Docker (`docker build --build-arg BUILD_MODE=native .`). Remaining work:

- Test the native binary end-to-end with a real database and uploaded files
- Profile memory usage (target: 15-30MB idle)
- Verify JobRunr background jobs work in native mode (may need additional reflection config)
- Verify GEDCOM import works in native mode (gedcom4j uses XML parsing which may need runtime init tuning)
- Consider using the GraalVM tracing agent for comprehensive metadata discovery

## Testing

- Integration tests with TestContainers (boot full app against ephemeral PostgreSQL)
- Playwright E2E tests pointed at Ktor instead of Spring
- Authenticated admin route tests (login, CRUD operations, file uploads)

## CI/CD

- GitHub Actions workflow for the Ktor project
- Automated native image build and push to container registry
- Deploy pipeline for self-hosted target

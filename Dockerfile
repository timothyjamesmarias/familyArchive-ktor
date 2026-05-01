# Multi-stage Dockerfile for Family Archive (Ktor)
#
# Supports two build modes:
#   - JVM (default):   docker build .
#   - Native (GraalVM): docker build --build-arg BUILD_MODE=native .

ARG BUILD_MODE=jvm

# =============================================================================
# Stage 1: Build (shared between JVM and native)
# =============================================================================
FROM ghcr.io/graalvm/graalvm-community:21 AS builder

RUN microdnf install -y findutils tar gzip && microdnf clean all

WORKDIR /app

# Install Gradle wrapper
COPY gradle gradle
COPY gradlew .
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY gradle.properties .

RUN chmod +x gradlew && ./gradlew dependencies --no-daemon || true

# Copy source and frontend config
COPY src src
COPY vite.config.ts .
COPY postcss.config.js .
COPY tailwind.config.js .
COPY package.json .
COPY package-lock.json .

ENV NODE_ENV=production

# =============================================================================
# Stage 2a: JVM fat jar build
# =============================================================================
FROM builder AS build-jvm
RUN ./gradlew clean buildFatJar -x test --no-daemon

# =============================================================================
# Stage 2b: Native image build
# =============================================================================
FROM builder AS build-native
RUN ./gradlew clean nativeCompile -x test --no-daemon

# =============================================================================
# Stage 3a: JVM runtime
# =============================================================================
FROM eclipse-temurin:21-jre-jammy AS runtime-jvm

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
RUN groupadd -r app && useradd -r -g app app

WORKDIR /app
COPY --from=build-jvm /app/build/libs/*-all.jar app.jar
RUN chown -R app:app /app
USER app

EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8080/health || exit 1

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# =============================================================================
# Stage 3b: Native runtime (minimal image)
# =============================================================================
FROM debian:bookworm-slim AS runtime-native

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
RUN groupadd -r app && useradd -r -g app app

WORKDIR /app
COPY --from=build-native /app/build/native/nativeCompile/familyarchive .
RUN chown -R app:app /app
USER app

EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/health || exit 1

ENTRYPOINT ["./familyarchive"]

# =============================================================================
# Final stage: select based on BUILD_MODE
# =============================================================================
FROM runtime-${BUILD_MODE} AS final

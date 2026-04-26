# Multi-stage Dockerfile for Family Archive (Ktor)
# Stage 1: Build the application
FROM gradle:8.12.0-jdk21 AS builder

WORKDIR /app

# Copy Gradle wrapper and build files first (for layer caching)
COPY gradle gradle
COPY gradlew .
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Download dependencies (cached if build files unchanged)
RUN ./gradlew dependencies --no-daemon || true

# Copy source code
COPY src src
COPY vite.config.ts .
COPY postcss.config.js .
COPY tailwind.config.js .
COPY package.json .
COPY package-lock.json .

# Build the application (includes frontend build via Gradle)
ENV NODE_ENV=production
RUN ./gradlew clean buildFatJar -x test --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-jammy

# Install curl for healthchecks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create non-root user
RUN groupadd -r app && useradd -r -g app app

WORKDIR /app

# Copy the built jar from builder stage
COPY --from=builder /app/build/libs/*-all.jar app.jar

# Change ownership to non-root user
RUN chown -R app:app /app

USER app

EXPOSE 8080
EXPOSE 8000

HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8080/health || exit 1

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

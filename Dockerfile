# =============================================================================
# Dockerfile – Inventory Management System
#
# Multi-stage build:
#   Stage 1 (builder) – compiles the source and produces an executable JAR
#   Stage 2 (runtime) – copies only the JAR into a slim JRE image
#
# The resulting image is used by Render (via render.yaml) and can also be
# run locally:
#   docker build -t inventory-app .
#   docker run -p 8080:8080 inventory-app
# =============================================================================

# ── Stage 1: Build ────────────────────────────────────────────────────────────
# Use the full JDK image so Gradle can compile the project.
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /workspace/app

# Copy the Gradle wrapper and build scripts first so Docker can cache the
# dependency-download layer separately from the source code.
COPY gradlew ./
COPY gradle gradle
COPY build.gradle settings.gradle ./
COPY src src

# Make the wrapper executable and run a clean build.
# Flags: --no-daemon avoids leaving a background process inside the container.
RUN chmod +x ./gradlew && \
    ./gradlew clean bootJar \
      --no-daemon \
      --stacktrace \
      --info \
      --warning-mode all

# ── Stage 2: Runtime ──────────────────────────────────────────────────────────
# Use the smaller JRE-only image to keep the final image size down.
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy only the executable JAR from the build stage.
COPY --from=builder /workspace/app/build/libs/*.jar app.jar

# Render routes external traffic to port 8080.
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

# --- Stage 1: Build the application ---
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /workspace/app

# Copy Gradle wrapper and config
COPY gradlew ./
COPY gradle gradle
COPY build.gradle settings.gradle ./

# Copy source code
COPY src src

# Build the bootable JAR
RUN chmod +x ./gradlew && \
    ./gradlew bootJar --no-daemon

# --- Stage 2: Runtime image (small & secure) ---
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copy only the JAR from builder
COPY --from=builder /workspace/app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
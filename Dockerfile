# ✅ Java 21 runtime image
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copy the compiled JAR
COPY build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]
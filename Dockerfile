# Use a lightweight, production-grade Java 21 JRE
FROM eclipse-temurin:21-jre-jammy

# Set the working directory inside the container
WORKDIR /app

# Copy the pre-built JAR file from the repository
COPY build/libs/*.jar app.jar

# Expose the port the app runs on
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java","-jar","/app/app.jar"]
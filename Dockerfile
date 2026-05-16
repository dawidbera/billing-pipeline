# Purpose: Multi-stage Dockerfile for packaging the Java application.
# Stage 1: Build the Maven project.
# Stage 2: Packaging JRE and application jar, running as non-root user.

# ==========================================
# Build Stage
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Copy the pom.xml and fetch dependency cache
COPY java-app/pom.xml .
RUN mvn dependency:go-offline -B

# Copy the source files and package the jar
COPY java-app/src ./src
RUN mvn package -DskipTests -B

# ==========================================
# Runtime Stage
# ==========================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create a system group and non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy the packaged jar from the builder stage
COPY --from=builder /build/target/demo-app-1.0.0.jar app.jar

# Adjust ownership of files to the non-root user
RUN chown -R appuser:appgroup /app

# Run the container as the non-root user
USER appuser

# Expose HTTP port
EXPOSE 8080

# Configure JVM parameters and execute application jar
ENTRYPOINT ["java", "-XX:+UseG1GC", "-jar", "app.jar"]

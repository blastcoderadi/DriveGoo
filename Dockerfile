# Multi-stage build for DriveGo Spring Boot Application
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Build project jar
COPY src ./src
RUN mvn clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy executable jar from build stage
COPY --from=build /app/target/drivego-1.0.0.jar app.jar

# Expose standard port
EXPOSE 8080

# Run in dev profile (uses in-memory H2 database + auto-seeds 6 vehicles & admin)
ENV SPRING_PROFILES_ACTIVE=dev
ENTRYPOINT ["java", "-jar", "app.jar"]

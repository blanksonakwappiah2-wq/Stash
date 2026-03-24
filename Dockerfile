# Use a multi-stage build for a smaller runtime image
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy the source code and build the application
COPY pom.xml .
COPY backend/pom.xml backend/
COPY android/pom.xml android/ 
COPY desktop/pom.xml desktop/
# We need the source for backend
COPY backend/src backend/src
# Build only the backend module
RUN mvn -f backend/pom.xml clean package -Dmaven.test.skip=true

# Runtime stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Run as a non-root user
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring

# Copy the jar from the build stage
COPY --from=build /app/backend/target/*.jar app.jar

# Run with the production profile by default
# Use aggressive memory and startup optimizations for Render Free Tier (512MB)
ENTRYPOINT ["java", \
    "-Xmx256m", "-Xms256m", \
    "-XX:MaxRAMPercentage=50.0", \
    "-XX:InitialRAMPercentage=50.0", \
    "-XX:+ExitOnOutOfMemoryError", \
    "-XX:ReservedCodeCacheSize=40m", \
    "-Xss256k", \
    "-XX:TieredStopAtLevel=1", \
    "-Dspring.main.lazy-initialization=true", \
    "-Dspring.profiles.active=prod", \
    "-Dserver.port=${PORT}", \
    "-jar", "app.jar"]

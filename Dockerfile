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
# Build only the backend module using reactor flags
RUN mvn clean package -pl backend -am -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Run as a non-root user
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring

# Copy the jar from the build stage
COPY --from=build /app/backend/target/*.jar app.jar

# Explicitly expose the default Render port
EXPOSE 10000

# Run with the production profile by default
# Use shell-form to ensure ${PORT} is expanded correctly.
# Use aggressive memory and startup optimizations for Render Free Tier (512MB)
ENTRYPOINT java \
    -Xmx256m -Xms256m \
    -XX:MaxRAMPercentage=50.0 \
    -XX:InitialRAMPercentage=50.0 \
    -XX:+ExitOnOutOfMemoryError \
    -XX:ReservedCodeCacheSize=40m \
    -Xss256k \
    -XX:TieredStopAtLevel=1 \
    -Dspring.main.lazy-initialization=true \
    -Dspring.profiles.active=prod \
    -Dserver.port=${PORT:-10000} \
    -jar app.jar

# ============================================
# Stage 1: Build
# ============================================
FROM maven:3.8.4-openjdk-8 AS build

WORKDIR /app

# Copy pom.xml and download dependencies (for caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build application with stage profile
RUN mvn clean package -DskipTests -Pstage

# ============================================
# Stage 2: Production
# ============================================
FROM eclipse-temurin:8-jre-alpine

# Install wget for healthcheck
RUN apk add --no-cache wget

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring

# Set working directory
WORKDIR /app

# Create logs directory
RUN mkdir -p /app/logs && chown -R spring:spring /app

# Copy jar from build stage
COPY --from=build /app/target/ROOT.jar /app/app.jar

# Change ownership
RUN chown -R spring:spring /app

# Switch to non-root user
USER spring

# Expose port
EXPOSE 8082

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8082/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Dspring.profiles.active=stage", \
    "-jar", \
    "/app/app.jar"]
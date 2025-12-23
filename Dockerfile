# Multi-stage build
FROM maven:3.8.4-openjdk-8 AS build

WORKDIR /app

# Copy pom.xml and download dependencies (for caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build application
RUN mvn clean package -DskipTests -Pstage

# Production stage
FROM openjdk:8-jre-alpine

# Install wget for healthcheck
RUN apk add --no-cache wget

# Metadata
LABEL maintainer="DLL Community"
LABEL version="1.0"
LABEL description="DLL Community CRM - Stage"

WORKDIR /app

# Create directories for logs
RUN mkdir -p /app/logs

# Copy JAR from build stage
COPY --from=build /app/target/*.jar ROOT.jar

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring && \
    chown -R spring:spring /app

USER spring:spring

# Expose port
EXPOSE 8082

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8082/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Dspring.profiles.active=stage", \
    "-Xmx512m", \
    "-Xms256m", \
    "-jar", \
    "ROOT.jar"]
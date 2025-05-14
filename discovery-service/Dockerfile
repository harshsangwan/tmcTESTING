# FROM openjdk:17-jdk-slim as builder

# # Set working directory
# WORKDIR /app

# # Copy the jar file to the container
# COPY target/discovery-service-*.jar app.jar

# # Extract layers for better caching
# RUN mkdir -p extracted && \
#     java -Djarmode=layertools -jar app.jar extract --destination extracted

# # Use the same image for the final stage
# FROM openjdk:17-jdk-slim

# # Set working directory
# WORKDIR /app

# # Add label metadata
# LABEL maintainer="Task Management Team" \
#       description="Discovery Service for Task Management System" \
#       version="1.0"

# # Install wget for healthcheck
# RUN apt-get update && \
#     apt-get install -y wget && \
#     apt-get clean && \
#     rm -rf /var/lib/apt/lists/* && \
#     groupadd -r appgroup && useradd -r -g appgroup appuser

# # Create volumes for logs and config
# VOLUME /app/logs
# VOLUME /app/config

# # Copy layers from the builder stage
# COPY --from=builder /app/extracted/dependencies/ ./
# COPY --from=builder /app/extracted/spring-boot-loader/ ./
# COPY --from=builder /app/extracted/snapshot-dependencies/ ./
# COPY --from=builder /app/extracted/application/ ./

# # Switch to non-root user
# USER appuser

# # Set environment variables
# ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
# ENV SERVER_PORT=8761
# ENV SPRING_PROFILES_ACTIVE=docker

# # Expose the application port
# EXPOSE ${SERVER_PORT}

# # Run the application with proper memory settings and Spring profile
# ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} org.springframework.boot.loader.launch.JarLauncher"]

# # Healthcheck to verify the service is working
# HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
#   CMD wget --no-verbose --tries=1 --spider http://localhost:${SERVER_PORT}/actuator/health || exit 1


FROM openjdk:17-jdk-slim

WORKDIR /app

# Install wget for healthcheck
RUN apt-get update && \
    apt-get install -y wget && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Copy the pre-built jar 
COPY target/discovery-service-0.0.1-SNAPSHOT.jar app.jar

ENV SERVER_PORT=8761
ENV SPRING_PROFILES_ACTIVE=docker 

EXPOSE ${SERVER_PORT}

ENTRYPOINT ["java", "-jar", "app.jar"]

HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:${SERVER_PORT}/actuator/health || exit 1
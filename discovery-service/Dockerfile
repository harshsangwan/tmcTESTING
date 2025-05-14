FROM openjdk:17-jdk-slim

WORKDIR /app

# Install curl for healthcheck
RUN apt-get update && \
    apt-get install -y curl && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Copy the pre-built jar 
COPY target/discovery-service-0.0.1-SNAPSHOT.jar app.jar

ENV SERVER_PORT=8761
ENV SPRING_PROFILES_ACTIVE=docker 

EXPOSE ${SERVER_PORT}

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]

HEALTHCHECK --interval=30s --timeout=15s --start-period=60s --retries=5 \
  CMD curl -f http://localhost:${SERVER_PORT}/actuator/health || exit 1
# FROM openjdk:17-jdk-slim

# WORKDIR /app

# # Install wget for healthcheck
# RUN apt-get update && \
#     apt-get install -y wget && \
#     apt-get clean && \
#     rm -rf /var/lib/apt/lists/*

# # Copy the pre-built jar 
# COPY target/auth-service-0.0.1-SNAPSHOT.jar app.jar

# ENV SPRING_PROFILES_ACTIVE=docker

# EXPOSE 8081

# ENTRYPOINT ["java", "-jar", "app.jar"]

# # Use GET instead of HEAD and explicitly check for a 200 status code
# HEALTHCHECK --interval=30s --timeout=10s --start-period=120s --retries=5 \
#   CMD wget --quiet --tries=1 -O- http://localhost:8081/actuator/health | grep -q "UP" || exit 1

FROM openjdk:17-jdk-slim

WORKDIR /app

# Install necessary tools
RUN apt-get update && \
    apt-get install -y wget curl && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Copy the pre-built jar 
COPY target/auth-service-0.0.1-SNAPSHOT.jar app.jar

ENV SPRING_PROFILES_ACTIVE=docker

EXPOSE 8081

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]

HEALTHCHECK --interval=30s --timeout=10s --start-period=120s --retries=5 \
  CMD curl -f http://localhost:8081/actuator/health || exit 1
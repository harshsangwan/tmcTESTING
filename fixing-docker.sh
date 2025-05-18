#!/bin/bash
echo "=== Fixing Discovery Service Health Check ==="

# Create backup of docker-compose.yml
cp docker-compose.yml docker-compose.yml.bak

# Update the health check for discovery-service
sed -i.bak '/discovery-service:/,/healthcheck:/!b;/healthcheck:/,/networks:/!b;s/test:.*$/test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http:\/\/localhost:8761\/"]/' docker-compose.yml

echo "Docker Compose file updated. The health check now uses '/' instead of '/actuator/health'."

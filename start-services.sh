#!/bin/bash

echo "Starting Task Management System..."

# Stop any running containers
docker-compose down

# Remove orphaned containers
docker-compose down --remove-orphans

# Build images with no cache
echo "Building images..."
docker-compose build --no-cache

# Start services
echo "Starting services..."
docker-compose up -d

# Wait for services to start
echo "Waiting for services to become healthy..."
sleep 30

# Check service status
echo ""
echo "Checking service status..."
echo "========================"

services=("mysql" "discovery-service" "api-gateway" "auth-service" "project-service" "task-service" "integration-service" "admin-service" "frontend")

for service in "${services[@]}"; do
    status=$(docker-compose ps --format "table {{.Service}}\t{{.State}}" | grep $service | awk '{print $2}')
    if [[ "$status" == *"Up"* ]]; then
        echo "✅ $service: $status"
    else
        echo "❌ $service: $status"
    fi
done

echo ""
echo "Checking health status..."
echo "========================"

for service in "${services[@]}"; do
    health=$(docker inspect --format='{{json .State.Health.Status}}' $service 2>/dev/null | tr -d '"')
    if [ "$health" = "healthy" ]; then
        echo "✅ $service: healthy"
    elif [ "$health" = "starting" ]; then
        echo "🔄 $service: starting"
    elif [ "$health" = "unhealthy" ]; then
        echo "❌ $service: unhealthy"
    else
        echo "⚠️  $service: no health check"
    fi
done

echo ""
echo "Services are starting up..."
echo "You can check the logs with: docker-compose logs -f [service-name]"
echo "Access the application at: http://localhost"
echo "Access the API Gateway at: http://localhost:8080"
echo "Access Eureka Dashboard at: http://localhost:8761"
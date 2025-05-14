#!/bin/bash

echo "🔧 Quick Fix and Restart"
echo "========================"
echo "Time: $(date)"
echo ""

# Stop and clean up
echo "🛑 Stopping services and cleaning up..."
docker-compose down --remove-orphans

# Create package-lock.json for frontend
echo "📦 Creating package-lock.json for frontend..."
cd task-management
if [ ! -f "package-lock.json" ]; then
    echo "Generating package-lock.json..."
    npm install --package-lock-only
fi
cd ..

# Rebuild and restart specific services that were failing
echo "🔨 Rebuilding and restarting backend services..."

# First, make sure mysql and discovery service are up
docker-compose up -d mysql discovery-service
sleep 30

# Check MySQL health
echo "🔍 Waiting for MySQL..."
max_attempts=30
attempt=1
while [ $attempt -le $max_attempts ]; do
    if docker-compose ps mysql | grep -q "(healthy)"; then
        echo "✅ MySQL is healthy"
        break
    elif [ $attempt -eq $max_attempts ]; then
        echo "❌ MySQL failed to become healthy"
        echo "MySQL logs:"
        docker-compose logs mysql
        exit 1
    else
        echo "MySQL starting... attempt $attempt/$max_attempts"
        sleep 5
        ((attempt++))
    fi
done

# Check Discovery Service
echo "🔍 Waiting for Discovery Service..."
max_attempts=20
attempt=1
while [ $attempt -le $max_attempts ]; do
    if curl -s -f "http://localhost:8761/actuator/health" >/dev/null 2>&1; then
        echo "✅ Discovery Service is healthy"
        break
    elif [ $attempt -eq $max_attempts ]; then
        echo "❌ Discovery Service failed"
        exit 1
    else
        echo "Discovery Service starting... attempt $attempt/$max_attempts"
        sleep 5
        ((attempt++))
    fi
done

# Rebuild and start backend services
echo "🔄 Rebuilding backend services with new configurations..."
docker-compose build --no-cache auth-service project-service task-service integration-service admin-service api-gateway
docker-compose up -d auth-service project-service task-service integration-service admin-service api-gateway

# Wait for backend services
echo "⏳ Waiting for backend services (90 seconds)..."
sleep 90

# Check backend services
echo "🔍 Checking backend services..."
services=("api-gateway:8080" "auth-service:8081" "project-service:8082" "task-service:8083" "integration-service:8084" "admin-service:8085")

for service_port in "${services[@]}"; do
    IFS=':' read -r service port <<< "$service_port"
    echo -n "Checking $service... "
    if curl -s -f "http://localhost:$port/actuator/health" >/dev/null 2>&1; then
        echo "✅ Healthy"
    else
        echo "⚠️  Not responding (check logs: docker-compose logs $service)"
    fi
done

# Build and start frontend
echo "🎨 Rebuilding and starting frontend..."
docker-compose build --no-cache frontend
docker-compose up -d frontend

# Wait for frontend
sleep 20

# Final status
echo ""
echo "📊 Final Status"
echo "==============="
docker-compose ps

echo ""
echo "🌐 Access Points:"
echo "- Frontend: http://localhost"
echo "- API Gateway: http://localhost:8080"
echo "- Eureka Dashboard: http://localhost:8761"
echo ""
echo "📋 Check logs if services are failing:"
echo "docker-compose logs [service-name]"
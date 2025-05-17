#!/bin/bash

echo "🔧 Fixing Backend Services Integration"
echo "======================================"
echo "Time: $(date)"
echo ""

# Stop all services
echo "1. Stopping all services..."
docker-compose down --remove-orphans

echo ""
echo "2. Cleaning up Docker resources..."
docker system prune -f
docker volume prune -f

echo ""
echo "3. Rebuilding services with fixes..."

# Start MySQL first
echo "Starting MySQL..."
docker-compose up -d mysql

# Wait for MySQL to be ready
echo "Waiting for MySQL to be ready..."
max_attempts=30
attempt=1
while [ $attempt -le $max_attempts ]; do
    if docker-compose ps mysql | grep -q "(healthy)"; then
        echo "✅ MySQL is healthy"
        break
    elif [ $attempt -eq $max_attempts ]; then
        echo "❌ MySQL failed to become healthy"
        exit 1
    else
        echo "MySQL starting... attempt $attempt/$max_attempts"
        sleep 5
        ((attempt++))
    fi
done

# Start Discovery Service
echo ""
echo "Starting Discovery Service..."
docker-compose up -d discovery-service

# Wait for Discovery Service
echo "Waiting for Discovery Service..."
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
        sleep 10
        ((attempt++))
    fi
done

# Rebuild and start backend services one by one
echo ""
echo "Rebuilding and starting backend services..."

# Rebuild services with no cache
docker-compose build --no-cache auth-service project-service task-service integration-service admin-service api-gateway

# Start Auth Service first
echo ""
echo "Starting Auth Service..."
docker-compose up -d auth-service
sleep 30

# Check Auth Service
echo "Checking Auth Service..."
max_attempts=15
attempt=1
while [ $attempt -le $max_attempts ]; do
    if curl -s -f "http://localhost:8081/actuator/health" >/dev/null 2>&1; then
        echo "✅ Auth Service is healthy"
        break
    elif [ $attempt -eq $max_attempts ]; then
        echo "⚠️  Auth Service not responding (check logs)"
        echo "Auth Service logs:"
        docker-compose logs --tail=20 auth-service
        break
    else
        echo "Auth Service starting... attempt $attempt/$max_attempts"
        sleep 10
        ((attempt++))
    fi
done

# Start other backend services
echo ""
echo "Starting other backend services..."
docker-compose up -d project-service task-service integration-service admin-service
sleep 60

# Check backend services
echo ""
echo "Checking backend services..."
services=("project-service:8082" "task-service:8083" "integration-service:8084" "admin-service:8085")

for service_port in "${services[@]}"; do
    IFS=':' read -r service port <<< "$service_port"
    echo -n "Checking $service... "
    if curl -s -f "http://localhost:$port/actuator/health" >/dev/null 2>&1; then
        echo "✅ Healthy"
    else
        echo "⚠️  Not responding (check logs)"
    fi
done

# Start API Gateway
echo ""
echo "Starting API Gateway..."
docker-compose up -d api-gateway
sleep 30

# Check API Gateway
echo "Checking API Gateway..."
max_attempts=15
attempt=1
while [ $attempt -le $max_attempts ]; do
    if curl -s -f "http://localhost:8080/actuator/health" >/dev/null 2>&1; then
        echo "✅ API Gateway is healthy"
        break
    elif [ $attempt -eq $max_attempts ]; then
        echo "⚠️  API Gateway not responding"
        echo "API Gateway logs:"
        docker-compose logs --tail=20 api-gateway
        break
    else
        echo "API Gateway starting... attempt $attempt/$max_attempts"
        sleep 10
        ((attempt++))
    fi
done

# Check service registration in Eureka
echo ""
echo "Checking service registration in Eureka..."
sleep 10
registered_services=$(curl -s http://localhost:8761/eureka/apps | grep -o '<name>[^<]*</name>' | sed 's/<name>\(.*\)<\/name>/\1/' | sort)
if [ -n "$registered_services" ]; then
    echo "✅ Services registered in Eureka:"
    echo "$registered_services"
else
    echo "⚠️  No services registered in Eureka yet"
fi

# Start frontend
echo ""
echo "Starting frontend..."
docker-compose up -d frontend
sleep 15

# Test integration
echo ""
echo "Testing API Gateway integration..."
echo "- Testing auth endpoint: $(curl -s -o /dev/null -w "%{http_code}" http://localhost/api/auth/health)"
echo "- Testing API Gateway health: $(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health)"

echo ""
echo "📊 Final Status"
echo "==============="
docker-compose ps

echo ""
echo "🌐 Access URLs:"
echo "- Frontend: http://localhost"
echo "- API Gateway: http://localhost:8080"
echo "- Eureka Dashboard: http://localhost:8761"
echo ""
echo "📝 Next Steps:"
echo "1. Check service logs if any service is not running: docker-compose logs [service-name]"
echo "2. Run the integration test: ./test-backend-integration.sh"
echo "3. Check Eureka dashboard to see all registered services"
echo ""
echo "✅ Backend services fix completed!"
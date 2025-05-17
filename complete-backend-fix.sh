#!/bin/bash

echo "🔧 Final Fix for Backend Services"
echo "================================="
echo "Time: $(date)"
echo ""

# Check service logs first
echo "📋 Checking current service logs for errors..."
echo ""

# Check for common startup errors
services=("auth-service" "project-service" "task-service" "integration-service" "admin-service")

for service in "${services[@]}"; do
    echo "--- $service logs ---"
    docker-compose logs --tail=10 $service | grep -E "(ERROR|FATAL|Exception|Failed)"
    echo ""
done

# Stop services
echo "Stopping backend services..."
docker-compose stop auth-service project-service task-service integration-service admin-service

# Rebuild with no cache
echo ""
echo "Rebuilding backend services..."
docker-compose build --no-cache auth-service project-service task-service integration-service admin-service

# Start services one by one with proper wait times
echo ""
echo "Starting Auth Service..."
docker-compose up -d auth-service
sleep 45

# Check Auth Service
echo "Checking Auth Service health..."
max_attempts=15
attempt=1
while [ $attempt -le $max_attempts ]; do
    if curl -s -f "http://localhost:8081/actuator/health" >/dev/null 2>&1; then
        echo "✅ Auth Service is healthy"
        break
    elif [ $attempt -eq $max_attempts ]; then
        echo "❌ Auth Service failed to become healthy"
        echo "Auth Service logs:"
        docker-compose logs --tail=20 auth-service
        break
    else
        echo "Auth Service starting... attempt $attempt/$max_attempts"
        sleep 10
        ((attempt++))
    fi
done

# Start other services
echo ""
echo "Starting other backend services..."
docker-compose up -d project-service
sleep 30
docker-compose up -d task-service 
sleep 30
docker-compose up -d integration-service
sleep 30
docker-compose up -d admin-service
sleep 30

# Wait for services to fully start
echo ""
echo "Waiting for all services to complete startup..."
sleep 60

# Check each service health
echo ""
echo "Checking all backend services..."

# Test each service health endpoint
check_service_health() {
    local service=$1
    local port=$2
    local api_prefix=$3
    
    echo -n "Checking $service... "
    
    # First check actuator health
    actuator_status=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost:$port/actuator/health")
    
    # Then check API health endpoint through gateway
    api_status=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost/api/$api_prefix/health")
    
    echo "Actuator: $actuator_status, API: $api_status"
    
    if [ "$actuator_status" != "200" ]; then
        echo "  ⚠️  $service actuator health check failed"
        echo "  Last few log lines:"
        docker-compose logs --tail=5 $service
    fi
}

check_service_health "auth-service" "8081" "auth"
check_service_health "project-service" "8082" "projects"  
check_service_health "task-service" "8083" "tasks"
check_service_health "integration-service" "8084" "integrations"
check_service_health "admin-service" "8085" "admin"

# Check Eureka registration
echo ""
echo "📊 Checking Eureka registration..."
registered_services=$(curl -s http://localhost:8761/eureka/apps | grep -o '<name>[^<]*</name>' | sort | uniq)
if [ -n "$registered_services" ]; then
    echo "✅ Services registered in Eureka:"
    echo "$registered_services"
else
    echo "⚠️  Eureka registration check failed"
fi

# Test API Gateway routing
echo ""
echo "🔌 Testing API Gateway routing..."
echo "Auth health through Gateway: $(curl -s -o /dev/null -w "%{http_code}" http://localhost/api/auth/health)"
echo "Project health through Gateway: $(curl -s -o /dev/null -w "%{http_code}" http://localhost/api/projects/health)"
echo "Task health through Gateway: $(curl -s -o /dev/null -w "%{http_code}" http://localhost/api/tasks/health)"
echo "Integration health through Gateway: $(curl -s -o /dev/null -w "%{http_code}" http://localhost/api/integrations/health)"
echo "Admin health through Gateway: $(curl -s -o /dev/null -w "%{http_code}" http://localhost/api/admin/health)"

# Final status
echo ""
echo "📈 Final Service Status:"
docker-compose ps

echo ""
echo "🎯 Summary:"
echo "- Frontend: http://localhost"
echo "- API Gateway: http://localhost:8080"
echo "- Eureka Dashboard: http://localhost:8761"
echo ""
echo "✅ Backend fix completed!"
echo "Run './test-backend-integration.sh' to test full integration"
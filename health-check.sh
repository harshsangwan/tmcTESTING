#!/bin/bash

echo "Health Check Report - $(date)"
echo "================================"

# Function to check service health
check_service() {
    local service=$1
    local url=$2
    
    echo -n "Checking $service... "
    
    # Check if container is running
    if ! docker inspect "$service" &>/dev/null; then
        echo "❌ Container not found"
        return
    fi
    
    # Get container status
    status=$(docker inspect --format='{{.State.Status}}' $service)
    if [ "$status" != "running" ]; then
        echo "❌ Container not running ($status)"
        return
    fi
    
    # Check health if URL provided
    if [ ! -z "$url" ]; then
        if curl -s -f "$url" >/dev/null; then
            echo "✅ Healthy"
        else
            echo "❌ Unhealthy (HTTP check failed)"
        fi
    else
        echo "✅ Running"
    fi
}

# Check all services
check_service "mysql" ""
check_service "discovery-service" "http://localhost:8761/actuator/health"
check_service "api-gateway" "http://localhost:8080/actuator/health"
check_service "auth-service" "http://localhost:8081/actuator/health"
check_service "project-service" "http://localhost:8082/actuator/health"
check_service "task-service" "http://localhost:8083/actuator/health"
check_service "integration-service" "http://localhost:8084/actuator/health"
check_service "admin-service" "http://localhost:8085/actuator/health"
check_service "frontend" "http://localhost:80"

echo ""
echo "Detailed logs:"
echo "============="
echo "To view logs for a specific service, run:"
echo "docker-compose logs [service-name]"
echo ""
echo "To view all logs:"
echo "docker-compose logs"
#!/bin/bash

echo "🚀 Task Management System - Startup"
echo "===================================="
echo "Time: $(date)"
echo ""

# Check if docker and docker-compose are installed
if ! command -v docker &> /dev/null || ! command -v docker-compose &> /dev/null; then
    echo "❌ Error: docker and docker-compose are required"
    exit 1
fi

# Function to wait for service health
wait_for_health() {
    local service=$1
    local port=$2
    local max_attempts=$3
    local attempt=1
    
    echo -n "Waiting for $service to become healthy... "
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s -f "http://localhost:$port/actuator/health" >/dev/null 2>&1; then
            echo "✅"
            return 0
        elif [ $attempt -eq $max_attempts ]; then
            echo "⚠️ Not responding after $max_attempts attempts"
            return 1
        fi
        
        sleep 5
        ((attempt++))
    done
}

# Check if project is already running
if [ "$(docker-compose ps -q)" != "" ]; then
    echo "🔄 Some containers are already running."
    read -p "Do you want to restart all services? (y/n): " choice
    if [ "$choice" = "y" ] || [ "$choice" = "Y" ]; then
        echo "Stopping all services..."
        docker-compose down
    else
        echo "Continuing with existing containers..."
    fi
fi

# Start MySQL first
echo "1️⃣ Starting MySQL database..."
docker-compose up -d mysql

# Wait for MySQL to be ready
echo "Waiting for MySQL to initialize (60s)..."
sleep 60

# Start Discovery Service
echo "2️⃣ Starting Discovery Service..."
docker-compose up -d discovery-service
wait_for_health "Discovery Service" "8761" 12

# Start API Gateway
echo "3️⃣ Starting API Gateway..."
docker-compose up -d api-gateway
wait_for_health "API Gateway" "8080" 12

# Start backend services in the right order
echo "4️⃣ Starting Auth Service..."
docker-compose up -d auth-service
wait_for_health "Auth Service" "8081" 12

echo "5️⃣ Starting Core Services..."
docker-compose up -d project-service task-service integration-service admin-service
sleep 30

# Start frontend
echo "6️⃣ Starting Frontend..."
docker-compose up -d frontend
sleep 10

# Show all running services
echo ""
echo "📊 Service Status"
echo "================="
docker-compose ps

# Display URLs
echo ""
echo "🌐 Access URLs"
echo "=============="
echo "Frontend: http://localhost"
echo "API Gateway: http://localhost:8080"
echo "Eureka Dashboard: http://localhost:8761"

# Check health of all services
echo ""
echo "🩺 Health Check"
echo "============="
echo "Frontend:      $(curl -s -o /dev/null -w "%{http_code}" http://localhost/)"
echo "API Gateway:   $(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health)"
echo "Auth Service:  $(curl -s -o /dev/null -w "%{http_code}" http://localhost:8081/actuator/health)"
echo "Project Svc:   $(curl -s -o /dev/null -w "%{http_code}" http://localhost:8082/actuator/health)"
echo "Task Service:  $(curl -s -o /dev/null -w "%{http_code}" http://localhost:8083/actuator/health)"
echo "Integration:   $(curl -s -o /dev/null -w "%{http_code}" http://localhost:8084/actuator/health)"
echo "Admin Service: $(curl -s -o /dev/null -w "%{http_code}" http://localhost:8085/actuator/health)"

echo ""
echo "✅ Task Management System startup completed!"
echo "To check logs: docker-compose logs -f [service-name]"
echo "To stop: docker-compose down"
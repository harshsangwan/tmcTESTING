#!/bin/bash

echo "🚀 Task Management System - Complete Fix and Deploy"
echo "=================================================="
echo "Time: $(date)"
echo ""

# Set script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
echo "Running from: $SCRIPT_DIR"
echo ""

# Step 1: Cleanup
echo "🧹 Step 1: Cleaning up..."
docker-compose down --remove-orphans --volumes
docker system prune -af
docker volume prune -f

# Remove node_modules from frontend if it exists
echo "🗑️  Removing node_modules..."
if [ -d "./task-management/node_modules" ]; then
    rm -rf "./task-management/node_modules"
    echo "✅ Removed node_modules from frontend"
fi

# Remove package-lock.json if it exists
if [ -f "./task-management/package-lock.json" ]; then
    rm "./task-management/package-lock.json"
    echo "✅ Removed package-lock.json"
fi

# Ensure .dockerignore exists
echo "📝 Creating .dockerignore..."
cat > ./task-management/.dockerignore << 'EOF'
node_modules
npm-debug.log
.git
.gitignore
README.md
.env
.nyc_output
coverage
.cache
.vscode
.DS_Store
*.log
dist
.angular
EOF
echo "✅ .dockerignore created"

echo ""
echo "🔨 Step 2: Building services in stages..."

# Build and start MySQL first
echo "Starting MySQL..."
docker-compose up -d mysql
echo "⏳ Waiting for MySQL to be ready (60s)..."
sleep 60

# Check MySQL health
echo "🔍 Checking MySQL health..."
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
        sleep 10
        ((attempt++))
    fi
done

# Start Discovery Service
echo ""
echo "🔍 Starting Discovery Service..."
docker-compose up -d discovery-service
sleep 45

# Check Discovery Service
echo "🔍 Checking Discovery Service..."
max_attempts=20
attempt=1
while [ $attempt -le $max_attempts ]; do
    if curl -s -f "http://localhost:8761/actuator/health" >/dev/null 2>&1; then
        echo "✅ Discovery Service is healthy"
        break
    elif [ $attempt -eq $max_attempts ]; then
        echo "❌ Discovery Service failed"
        echo "Discovery Service logs:"
        docker-compose logs discovery-service
        exit 1
    else
        echo "Discovery Service starting... attempt $attempt/$max_attempts"
        sleep 10
        ((attempt++))
    fi
done

# Start API Gateway
echo ""
echo "🌐 Starting API Gateway..."
docker-compose up -d api-gateway
sleep 30

# Check API Gateway
echo "🔍 Checking API Gateway..."
max_attempts=20
attempt=1
while [ $attempt -le $max_attempts ]; do
    if curl -s -f "http://localhost:8080/actuator/health" >/dev/null 2>&1; then
        echo "✅ API Gateway is healthy"
        break
    elif [ $attempt -eq $max_attempts ]; then
        echo "❌ API Gateway failed"
        echo "API Gateway logs:"
        docker-compose logs api-gateway
        exit 1
    else
        echo "API Gateway starting... attempt $attempt/$max_attempts"
        sleep 10
        ((attempt++))
    fi
done

# Start backend services
echo ""
echo "⚙️  Starting Backend Services..."
docker-compose up -d auth-service project-service task-service integration-service admin-service
sleep 60

# Check backend services
echo "🔍 Checking Backend Services..."
services=("auth-service" "project-service" "task-service" "integration-service" "admin-service")
for service in "${services[@]}"; do
    echo "Checking $service..."
    max_attempts=15
    attempt=1
    while [ $attempt -le $max_attempts ]; do
        port=""
        case $service in
            "auth-service") port="8081" ;;
            "project-service") port="8082" ;;
            "task-service") port="8083" ;;
            "integration-service") port="8084" ;;
            "admin-service") port="8085" ;;
        esac
        
        if curl -s -f "http://localhost:$port/actuator/health" >/dev/null 2>&1; then
            echo "✅ $service is healthy"
            break
        elif [ $attempt -eq $max_attempts ]; then
            echo "⚠️  $service is not responding (will continue anyway)"
            break
        else
            echo "$service starting... attempt $attempt/$max_attempts"
            sleep 10
            ((attempt++))
        fi
    done
done

# Start frontend
echo ""
echo "🎨 Starting Frontend..."
docker-compose up -d frontend
sleep 30

# Final status check
echo ""
echo "📊 Final Status Check"
echo "===================="

# Check all services
all_services=("mysql" "discovery-service" "api-gateway" "auth-service" "project-service" "task-service" "integration-service" "admin-service" "frontend")

for service in "${all_services[@]}"; do
    echo -n "$service: "
    if docker-compose ps $service | grep -q "Up"; then
        echo "✅ Running"
    else
        echo "❌ Not Running"
    fi
done

echo ""
echo "🎉 Deployment Complete!"
echo "======================"
echo ""
echo "🌐 Access Points:"
echo "- Frontend: http://localhost"
echo "- API Gateway: http://localhost:8080"
echo "- Eureka Dashboard: http://localhost:8761"
echo ""
echo "📋 Helpful Commands:"
echo "- View all logs: docker-compose logs"
echo "- View service logs: docker-compose logs [service-name]"
echo "- Check service status: docker-compose ps"
echo ""
echo "If any service is failing, check its logs with:"
echo "docker-compose logs [service-name]"
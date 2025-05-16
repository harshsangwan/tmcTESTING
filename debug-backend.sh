#!/bin/bash

echo "🔍 Backend Service Debugging"
echo "============================"
echo "Time: $(date)"
echo ""

# Function to check service logs
check_service_logs() {
    local service=$1
    local port=$2
    
    echo "📋 Checking $service logs..."
    echo "----------------------------------------"
    
    # Get the last 50 lines of logs
    echo "Recent logs from $service:"
    docker-compose logs --tail=50 $service
    
    echo ""
    echo "Service status:"
    docker-compose ps $service
    echo ""
    
    # Try to curl the health endpoint
    echo "Health check:"
    if curl -s "http://localhost:$port/actuator/health" | head -n 5; then
        echo "✅ Health endpoint accessible"
    else
        echo "❌ Health endpoint not accessible"
    fi
    echo ""
    echo "=========================================="
    echo ""
}

# Check problematic services
echo "Checking backend services that are having issues..."
echo ""

check_service_logs "auth-service" "8081"
check_service_logs "project-service" "8082"
check_service_logs "task-service" "8083"
check_service_logs "integration-service" "8084"

# Check Eureka registration
echo "🔍 Checking Eureka Service Registration"
echo "======================================"
echo "Services registered with Eureka:"
curl -s "http://localhost:8761/eureka/apps" | grep -o '<name>[^<]*</name>' | sed 's/<[^>]*>//g' | sort
echo ""

# Check API Gateway routes
echo "🔍 Checking API Gateway Routes"
echo "=============================="
curl -s "http://localhost:8080/actuator/gateway/routes" | head -n 20
echo ""

echo "🔧 Suggestions:"
echo "=============="
echo "1. If services show 'Connection refused' errors, they might not be fully started"
echo "2. If you see JWT or authentication errors, check the JWT_SECRET configuration"
echo "3. If database connection errors appear, verify MySQL is accessible"
echo "4. Allow more time for services to start up and register with Eureka"
echo ""
echo "To restart a specific service:"
echo "docker-compose restart [service-name]"
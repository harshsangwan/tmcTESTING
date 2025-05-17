#!/bin/bash

echo "🔍 Diagnosing Specific Service Issues"
echo "====================================="

# Check logs for the failing services
echo "1. Checking logs for services with 500 errors..."

failing_services=("project-service" "task-service" "integration-service")

for service in "${failing_services[@]}"; do
    echo ""
    echo "--- $service logs (last 20 lines) ---"
    docker-compose logs --tail=20 $service | grep -E "(ERROR|WARN|Exception|Failed|actuator)"
done

# Test actuator endpoints with more verbose output
echo ""
echo "2. Testing actuator endpoints with curl verbose output..."

for service in "${failing_services[@]}"; do
    port=$((8082))
    case $service in
        "project-service") port=8082 ;;
        "task-service") port=8083 ;;
        "integration-service") port=8084 ;;
    esac
    
    echo ""
    echo "Testing $service ($port):"
    echo "Actuator health:"
    curl -v http://localhost:$port/actuator/health 2>&1 | head -10
    
    echo ""
    echo "Root endpoint:"
    curl -v http://localhost:$port/ 2>&1 | head -5
done

# Check if the services are properly configured
echo ""
echo "3. Checking service configuration..."

# Check if the security config was properly added
for service in "${failing_services[@]}"; do
    echo "Checking $service application-docker.properties:"
    if grep -q "management.security.enabled=false" ./${service}/src/main/resources/application-docker.properties; then
        echo "✅ $service has security disabled"
    else
        echo "❌ $service missing security config"
    fi
done

echo ""
echo "4. Looking for specific errors in logs..."
for service in "${failing_services[@]}"; do
    echo "--- $service specific errors ---"
    docker-compose logs $service | grep -i -E "(error|exception|failed|refused)" | tail -5
done
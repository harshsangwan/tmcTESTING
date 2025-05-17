#!/bin/bash

echo "🔍 Diagnosing Health Endpoint Issues"
echo "===================================="

# Test different health endpoint variations
services=("auth:8081" "project:8082" "task:8083" "integration:8084" "admin:8085")

for service_port in "${services[@]}"; do
    IFS=':' read -r service port <<< "$service_port"
    echo ""
    echo "--- Testing $service service (port $port) ---"
    
    # Test various endpoint patterns
    echo "1. Testing /actuator/health:"
    curl -v http://localhost:$port/actuator/health 2>&1 | head -5
    
    echo -e "\n2. Testing /health:"
    curl -v http://localhost:$port/health 2>&1 | head -5
    
    echo -e "\n3. Testing root (/):"
    curl -v http://localhost:$port/ 2>&1 | head -5
    
    echo -e "\n4. Container port check:"
    docker exec ${service}-service netstat -tlnp | grep $port || echo "Port not accessible"
    
    echo -e "\n5. Docker service logs (last 5 lines):"
    docker-compose logs --tail=5 ${service}-service
    
    echo "==============================================="
done

# Check API Gateway routing
echo ""
echo "Testing API Gateway direct health:"
curl -v http://localhost:8080/actuator/health 2>&1 | head -5

echo ""
echo "Checking if services are actually running on correct ports:"
docker-compose ps
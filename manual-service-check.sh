#!/bin/bash

echo "🔍 Manual Service Check"
echo "======================="

# Let's manually check what's happening with the services
echo "1. Manual curl tests for failing services..."

echo "Testing project-service:"
echo "- Actuator health:"
curl -v http://localhost:8082/actuator/health 2>&1 | head -15

echo -e "\n- Root endpoint:"
curl -v http://localhost:8082/ 2>&1 | head -10

echo -e "\n- Custom health endpoint:"
curl -v http://localhost:8082/api/projects/health 2>&1 | head -10

echo -e "\n\nTesting task-service:"
echo "- Actuator health:"
curl -v http://localhost:8083/actuator/health 2>&1 | head -15

echo -e "\n\nTesting integration-service:"
echo "- Actuator health:"
curl -v http://localhost:8084/actuator/health 2>&1 | head -15

# Check the services that are working
echo -e "\n\n2. Checking working services for comparison..."

echo "Testing auth-service (working):"
curl -v http://localhost:8081/actuator/health 2>&1 | head -10

echo -e "\nTesting admin-service (working):"
curl -v http://localhost:8085/actuator/health 2>&1 | head -10

# Check logs for specific errors
echo -e "\n\n3. Recent logs from failing services..."
echo "Project service errors:"
docker-compose logs --tail=10 project-service | grep -E "(ERROR|Exception|security)"

echo -e "\nTask service errors:"
docker-compose logs --tail=10 task-service | grep -E "(ERROR|Exception|security)"

echo -e "\nIntegration service errors:"
docker-compose logs --tail=10 integration-service | grep -E "(ERROR|Exception|security)"

# Check if we can access any endpoint at all
echo -e "\n\n4. Testing if services respond to any endpoint..."
services=("8082" "8083" "8084")
for port in "${services[@]}"; do
    echo "Port $port responses:"
    echo "- Root: $(curl -s -o /dev/null -w "%{http_code}" http://localhost:$port/)"
    echo "- Actuator: $(curl -s -o /dev/null -w "%{http_code}" http://localhost:$port/actuator)"
    echo "- Health: $(curl -s -o /dev/null -w "%{http_code}" http://localhost:$port/actuator/health)"
    echo "- Info: $(curl -s -o /dev/null -w "%{http_code}" http://localhost:$port/actuator/info)"
done
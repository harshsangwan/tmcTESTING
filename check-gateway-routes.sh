#!/bin/bash

echo "🔍 API Gateway Routes Check"
echo "========================="

# Check gateway routes
echo "1. Examining Gateway routes configuration..."
docker exec api-gateway cat /app/application.properties || echo "Properties file not found"
docker exec api-gateway cat /app/application-docker.properties || echo "Docker properties file not found"

# Test health endpoints through gateway
echo ""
echo "2. Testing health endpoints through API Gateway..."
echo "Project Service via Gateway: $(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/projects/health)"
echo "Task Service via Gateway: $(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/tasks/health)"
echo "Integration Service via Gateway: $(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/integrations/health)"

echo ""
echo "3. Adding direct health endpoint routes to gateway..."
cat > gateway-health-routes.properties << 'EOF'
# Direct health endpoint routes
spring.cloud.gateway.routes[20].id=project-health
spring.cloud.gateway.routes[20].uri=http://project-service:8082
spring.cloud.gateway.routes[20].predicates[0]=Path=/api/projects/health
spring.cloud.gateway.routes[20].filters[0]=SetPath=/actuator/health

spring.cloud.gateway.routes[21].id=task-health
spring.cloud.gateway.routes[21].uri=http://task-service:8083
spring.cloud.gateway.routes[21].predicates[0]=Path=/api/tasks/health
spring.cloud.gateway.routes[21].filters[0]=SetPath=/actuator/health

spring.cloud.gateway.routes[22].id=integration-health
spring.cloud.gateway.routes[22].uri=http://integration-service:8084
spring.cloud.gateway.routes[22].predicates[0]=Path=/api/integrations/health
spring.cloud.gateway.routes[22].filters[0]=SetPath=/actuator/health
EOF

docker cp gateway-health-routes.properties api-gateway:/app/application.properties
docker-compose restart api-gateway

echo "4. Waiting for gateway to restart..."
sleep 20

echo "5. Testing health endpoints through API Gateway again..."
echo "Project Service via Gateway: $(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/projects/health)"
echo "Task Service via Gateway: $(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/tasks/health)"
echo "Integration Service via Gateway: $(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/integrations/health)"

echo "API Gateway routes update complete."
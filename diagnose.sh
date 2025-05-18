#!/bin/bash
echo "=== API Gateway to Auth Service Diagnostic Tests ==="

# 1. Test direct connection to Auth Service
echo -e "\n1. Testing direct connection to Auth Service..."
curl -v http://localhost:8081/actuator/health

# 2. Test connection through API Gateway to Auth Service health endpoint
echo -e "\n2. Testing API Gateway route to Auth Service health..."
curl -v http://localhost:8080/actuator/health

# 3. Try a simple GET request to Auth Service through Gateway
echo -e "\n3. Testing simple GET request through Gateway..."
curl -v http://localhost:8080/api/auth/health

# 4. Check if auth service is properly registered with Eureka
echo -e "\n4. Checking Eureka registry..."
curl -s http://localhost:8761/eureka/apps | grep -A 10 AUTH-SERVICE

# 5. View networks and DNS resolution inside containers
echo -e "\n5. Checking Docker network connectivity..."
docker exec api-gateway ping -c 2 auth-service

# 6. Test request forwarding inside the API Gateway container
echo -e "\n6. Testing request from inside API Gateway container..."
docker exec api-gateway curl -v http://auth-service:8081/actuator/health

# 7. Look for specific error messages in API Gateway logs
echo -e "\n7. Searching for specific error patterns in logs..."
docker-compose logs api-gateway | grep -E 'error|exception|failed|rejected|refused' | tail -20

echo -e "\nDiagnostic tests completed."
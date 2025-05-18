#!/bin/bash
echo "=== Testing Docker Network ==="

# Get the container IDs and IP addresses
echo "Container information:"
API_GATEWAY_ID=$(docker ps -qf "name=api-gateway")
AUTH_SERVICE_ID=$(docker ps -qf "name=auth-service")
API_GATEWAY_IP=$(docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' $API_GATEWAY_ID)
AUTH_SERVICE_IP=$(docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' $AUTH_SERVICE_ID)

echo "API Gateway ID: $API_GATEWAY_ID, IP: $API_GATEWAY_IP"
echo "Auth Service ID: $AUTH_SERVICE_ID, IP: $AUTH_SERVICE_IP"

# Test network connectivity from API Gateway to Auth Service
echo -e "\nTesting network connectivity from API Gateway to Auth Service:"
docker exec -it api-gateway sh -c "apk add --no-cache curl && curl -v http://$AUTH_SERVICE_IP:8081/actuator/health"

# Check DNS resolution inside the container
echo -e "\nChecking DNS resolution inside API Gateway container:"
docker exec -it api-gateway sh -c "apk add --no-cache bind-tools && nslookup auth-service"

echo "Docker network test completed."
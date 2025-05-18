#!/bin/bash
echo "=== Testing Different Request Types to Auth Service ==="

# 1. Try a GET request directly to auth service
echo -e "\n1. GET request directly to Auth Service's health endpoint:"
curl -v http://localhost:8081/api/auth/health

# 2. Try a GET request through API Gateway
echo -e "\n2. GET request through API Gateway to Auth Service's health endpoint:"
curl -v http://localhost:8080/api/auth/health

# 3. Try using the IP address instead of hostname
echo -e "\n3. Getting Auth Service IP address and trying direct IP connection:"
AUTH_IP=$(docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' auth-service)
echo "Auth Service IP: $AUTH_IP"
docker exec api-gateway curl -v "http://$AUTH_IP:8081/api/auth/health"

# 4. Try simplified POST with minimal data
echo -e "\n4. POST with minimal data directly to Auth Service:"
curl -v -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"min","email":"min@test.com","password":"pass123"}'

# 5. Try the same POST through API Gateway
echo -e "\n5. POST with minimal data through API Gateway:"
curl -v -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"min2","email":"min2@test.com","password":"pass123"}'
#!/bin/bash
echo "=== Capturing Auth Service Logs During Registration ==="

# Start capturing logs in the background
docker-compose logs -f auth-service > auth-service-detailed.log &
LOG_PID=$!

echo "Waiting 3 seconds before making request..."
sleep 3

# Make a registration request through API Gateway
echo "Making registration request..."
curl -v -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Test User","email":"test@example.com","password":"Password123!"}'

echo "Waiting 3 seconds to capture response logs..."
sleep 3

# Stop capturing logs
kill $LOG_PID

echo "Last 30 lines of auth service logs:"
tail -n 30 auth-service-detailed.log
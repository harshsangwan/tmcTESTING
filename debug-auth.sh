#!/bin/bash
echo "=== Setting up log capture ==="
# Turn on more detailed logging
docker-compose exec auth-service bash -c 'echo "logging.level.org.springframework=DEBUG" >> /app/application.yml'
docker-compose exec auth-service bash -c 'echo "logging.level.com.taskmanagement=TRACE" >> /app/application.yml'

# Restart just the auth service to apply new logging
docker-compose restart auth-service

# Wait for service to restart
sleep 10

echo "=== Capturing logs while making registration request ==="
# In one terminal, start capturing logs
docker-compose logs -f auth-service > auth-detailed.log &
LOG_PID=$!

# In another terminal, make the request
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Test User","email":"test@example.com","password":"Password123!"}'

# Give it a moment to log the error
sleep 5

# Stop log capture
kill $LOG_PID

echo "=== Error logs saved to auth-detailed.log ==="
echo "=== Last 20 lines of log: ==="
tail -n 20 auth-detailed.log
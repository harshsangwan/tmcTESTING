#!/bin/bash
echo "=== Request Tracing Test ==="

# Start capturing logs from both services
docker-compose logs -f api-gateway auth-service > complete-trace.log &
LOG_PID=$!

# Wait for log capture to start
sleep 2

# Make a test request
echo "Making test request..."
curl -v -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"trace","email":"trace@test.com","password":"tracepass"}'

# Wait for request to complete and logs to be captured
sleep 5

# Stop log capture
kill $LOG_PID

# Search for relevant log entries
echo -e "\nSearching for relevant log entries..."
grep -A 20 -B 20 "register" complete-trace.log > register-trace.log
grep -A 20 -B 20 "error" complete-trace.log > error-trace.log

echo "Trace files created: register-trace.log and error-trace.log"
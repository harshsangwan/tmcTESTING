#!/bin/bash

echo "🔍 Backend Health Diagnosis"
echo "========================="

# Check health endpoints directly
echo "1. Testing health endpoints directly..."
echo "Project Service Health: $(curl -s -o /dev/null -w "%{http_code}" http://localhost:8082/health)"
echo "Task Service Health: $(curl -s -o /dev/null -w "%{http_code}" http://localhost:8083/health)"
echo "Integration Service Health: $(curl -s -o /dev/null -w "%{http_code}" http://localhost:8084/health)"

echo "2. Testing actuator health endpoints..."
echo "Project Service Actuator: $(curl -s -o /dev/null -w "%{http_code}" http://localhost:8082/actuator/health)"
echo "Task Service Actuator: $(curl -s -o /dev/null -w "%{http_code}" http://localhost:8083/actuator/health)"
echo "Integration Service Actuator: $(curl -s -o /dev/null -w "%{http_code}" http://localhost:8084/actuator/health)"

# Examine logs for errors
echo ""
echo "3. Checking Project Service logs for errors..."
docker logs project-service 2>&1 | grep -E "ERROR|Exception|Failed|main.java.com" | tail -15

echo ""
echo "4. Checking Task Service logs for errors..."
docker logs task-service 2>&1 | grep -E "ERROR|Exception|Failed|main.java.com" | tail -15

echo ""
echo "5. Checking Integration Service logs for errors..."
docker logs integration-service 2>&1 | grep -E "ERROR|Exception|Failed|main.java.com" | tail -15

echo ""
echo "6. Checking classpath and JAR content for microservices..."
echo "Project Service:"
docker exec project-service ls -la /app
docker exec project-service java -jar /app/app.jar --help 2>&1 | head -10 || echo "Failed to execute JAR"

echo ""
echo "Task Service:"
docker exec task-service ls -la /app
docker exec task-service java -jar /app/app.jar --help 2>&1 | head -10 || echo "Failed to execute JAR"

echo ""
echo "Integration Service:"
docker exec integration-service ls -la /app
docker exec integration-service java -jar /app/app.jar --help 2>&1 | head -10 || echo "Failed to execute JAR"

echo ""
echo "7. The issue may be with the package structure in the compiled JAR files."
echo "   The controllers we added may not be detected properly."
echo ""
echo "Let's try a workaround to make actuator health endpoints accessible..."
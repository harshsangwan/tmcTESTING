#!/bin/bash

echo "🔧 Quick Health Fix - Security Configuration"
echo "============================================="

# The issue is likely that actuator endpoints are secured by default
# Let's test if the services respond to any endpoint

# Stop all services first
echo "Stopping backend services..."
docker-compose stop auth-service project-service task-service integration-service admin-service

# Get more logs to understand the issue
echo ""
echo "Checking startup logs for security/actuator issues..."
for service in auth-service project-service task-service integration-service admin-service; do
    echo "--- $service startup logs ---"
    docker-compose logs $service | grep -E "(actuator|security|health|ERROR|WARN)" | tail -10
    echo ""
done

# Check if ports are actually exposed
echo "Checking Docker container port configuration..."
docker-compose config | grep -A5 -B5 "ports:"

# Try accessing through Docker network
echo ""
echo "Testing network connectivity..."
docker run --rm --network tmc_root_task-management-network curlimages/curl:latest \
    curl -s http://auth-service:8081/actuator/health || echo "Cannot reach auth-service through Docker network"

echo ""
echo "Services will need to be restarted. Please run:"
echo "docker-compose up -d auth-service project-service task-service integration-service admin-service"
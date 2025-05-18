#!/bin/bash

echo "=== Starting JWT Fix Process ==="

# Make all scripts executable
chmod +x fix-api-gateway.sh
chmod +x fix-auth-service.sh
chmod +x fix-project-service.sh
chmod +x fix-task-service.sh

# Run all scripts
./fix-api-gateway.sh
./fix-auth-service.sh
./fix-project-service.sh
./fix-task-service.sh

echo "=== JWT Fix Process Complete ==="
echo "Now rebuild and restart all services using Docker Compose:"
echo "docker-compose down"
echo "docker-compose build"
echo "docker-compose up -d"
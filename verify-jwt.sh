#!/bin/bash
echo "=== Verifying JWT Configuration ==="
docker-compose exec auth-service sh -c "grep JWT_SECRET /proc/1/environ"
docker-compose exec api-gateway sh -c "grep JWT_SECRET /proc/1/environ"

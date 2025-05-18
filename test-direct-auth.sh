#!/bin/bash
echo "=== Testing direct access to Auth Service ==="
echo "Attempting direct registration to auth-service (bypassing gateway):"
curl -v -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Direct Test","email":"direct@example.com","password":"Password123!"}'
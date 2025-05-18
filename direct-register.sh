#!/bin/bash
echo "=== Testing Direct Registration to Auth Service ==="

curl -v -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Direct Test","email":"direct@example.com","password":"Password123!"}'
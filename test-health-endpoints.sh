#!/bin/bash

echo "Testing health endpoints through various paths..."

echo "Testing direct service health:"
echo "- Project Service: $(curl -s -o /dev/null -w "%{http_code}" http://localhost:8082/api/projects/health)"
echo "- Task Service: $(curl -s -o /dev/null -w "%{http_code}" http://localhost:8083/api/tasks/health)"
echo "- Integration Service: $(curl -s -o /dev/null -w "%{http_code}" http://localhost:8084/api/integrations/health)"

echo ""
echo "Testing through API Gateway:"
echo "- Project Service: $(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/projects/health)"
echo "- Task Service: $(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/tasks/health)"
echo "- Integration Service: $(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/integrations/health)"

echo ""
echo "Testing through Nginx with fallbacks:"
echo "- Project Service: $(curl -s -o /dev/null -w "%{http_code}" http://localhost/api/projects/health)"
echo "- Task Service: $(curl -s -o /dev/null -w "%{http_code}" http://localhost/api/tasks/health)"
echo "- Integration Service: $(curl -s -o /dev/null -w "%{http_code}" http://localhost/api/integrations/health)"

echo ""
echo "Viewing health response content:"
curl -s http://localhost/api/projects/health | head -20

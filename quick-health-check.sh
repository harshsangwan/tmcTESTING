#!/bin/bash

echo "🚀 Quick Backend-Frontend Health Check"
echo "======================================"

# Quick status check of all services
echo "Service Status:"
echo "├── Frontend:       $(curl -s -o /dev/null -w "%{http_code}" http://localhost/)"
echo "├── API Gateway:    $(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health)"
echo "├── Auth Service:   $(curl -s -o /dev/null -w "%{http_code}" http://localhost:8081/actuator/health)"
echo "├── Project Svc:    $(curl -s -o /dev/null -w "%{http_code}" http://localhost:8082/actuator/health)"
echo "├── Task Service:   $(curl -s -o /dev/null -w "%{http_code}" http://localhost:8083/actuator/health)"
echo "├── Integration:    $(curl -s -o /dev/null -w "%{http_code}" http://localhost:8084/actuator/health)"
echo "├── Admin Service:  $(curl -s -o /dev/null -w "%{http_code}" http://localhost:8085/actuator/health)"
echo "└── Discovery:      $(curl -s -o /dev/null -w "%{http_code}" http://localhost:8761/actuator/health)"

echo ""
echo "API Gateway Routes Test:"
echo "├── Auth API:       $(curl -s -o /dev/null -w "%{http_code}" http://localhost/api/auth/health)"
echo "├── Projects API:   $(curl -s -o /dev/null -w "%{http_code}" http://localhost/api/projects/health)"
echo "├── Tasks API:      $(curl -s -o /dev/null -w "%{http_code}" http://localhost/api/tasks/health)"
echo "├── Integrations:   $(curl -s -o /dev/null -w "%{http_code}" http://localhost/api/integrations/health)"
echo "└── Admin API:      $(curl -s -o /dev/null -w "%{http_code}" http://localhost/api/admin/health)"

echo ""
echo "Access URLs:"
echo "• Frontend:         http://localhost"
echo "• API Gateway:      http://localhost:8080"
echo "• Eureka:          http://localhost:8761"

echo ""
echo "Legend: 200=OK, 401=Auth Required, 404=Not Found, 500=Error"
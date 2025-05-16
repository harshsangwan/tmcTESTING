#!/bin/bash

echo "🔗 Backend-Frontend Integration Test"
echo "===================================="
echo "Testing complete integration between frontend and backend services"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to check service health
check_service() {
    local service_name=$1
    local url=$2
    echo -n "Checking $service_name... "
    
    response=$(curl -s -w "%{http_code}" "$url" -o /dev/null)
    if [ "$response" = "200" ]; then
        echo -e "${GREEN}✓ Healthy${NC}"
        return 0
    else
        echo -e "${RED}✗ Unhealthy (HTTP: $response)${NC}"
        return 1
    fi
}

# Function to test API endpoint
test_api() {
    local endpoint=$1
    local description=$2
    local expected_status=$3
    
    echo -n "Testing $description... "
    response=$(curl -s -w "%{http_code}" "http://localhost$endpoint" -o /dev/null)
    
    if [ "$response" = "$expected_status" ]; then
        echo -e "${GREEN}✓ Success (HTTP: $response)${NC}"
        return 0
    else
        echo -e "${RED}✗ Failed (HTTP: $response, Expected: $expected_status)${NC}"
        return 1
    fi
}

# Function to test API with authentication
test_auth_api() {
    local endpoint=$1
    local description=$2
    
    echo -n "Testing $description... "
    response=$(curl -s -w "%{http_code}" "http://localhost$endpoint" -o /dev/null)
    
    # Expecting 401 for protected endpoints without auth
    if [ "$response" = "401" ] || [ "$response" = "403" ]; then
        echo -e "${GREEN}✓ Protected (HTTP: $response)${NC}"
        return 0
    elif [ "$response" = "200" ]; then
        echo -e "${YELLOW}⚠ Accessible without auth (HTTP: $response)${NC}"
        return 0
    else
        echo -e "${RED}✗ Unexpected response (HTTP: $response)${NC}"
        return 1
    fi
}

echo -e "${BLUE}1. Frontend Accessibility Test${NC}"
echo "================================"
check_service "Frontend" "http://localhost/"

echo ""
echo -e "${BLUE}2. Backend Services Health Check${NC}"
echo "================================="
check_service "API Gateway" "http://localhost:8080/actuator/health"
check_service "Auth Service" "http://localhost:8081/actuator/health"
check_service "Project Service" "http://localhost:8082/actuator/health"
check_service "Task Service" "http://localhost:8083/actuator/health"
check_service "Integration Service" "http://localhost:8084/actuator/health"
check_service "Admin Service" "http://localhost:8085/actuator/health"
check_service "Discovery Service" "http://localhost:8761/actuator/health"

echo ""
echo -e "${BLUE}3. API Gateway Integration Test${NC}"
echo "==================================="
test_api "/api/auth/health" "Auth API through Gateway" "200"
test_api "/api/projects/health" "Projects API through Gateway" "401"
test_api "/api/tasks/health" "Tasks API through Gateway" "401"
test_api "/api/integrations/health" "Integrations API through Gateway" "401"
test_api "/api/admin/health" "Admin API through Gateway" "401"

echo ""
echo -e "${BLUE}4. Authentication Endpoints Test${NC}"
echo "==================================="
test_api "/api/auth/register" "Registration endpoint" "405"
test_api "/api/auth/login" "Login endpoint" "405"

echo ""
echo -e "${BLUE}5. Protected Endpoints Test${NC}"
echo "==============================="
test_auth_api "/api/projects" "Projects list (protected)"
test_auth_api "/api/tasks" "Tasks list (protected)"
test_auth_api "/api/integrations" "Integrations list (protected)"
test_auth_api "/api/admin/users" "Admin users (protected)"

echo ""
echo -e "${BLUE}6. Service Discovery Test${NC}"
echo "============================="
echo "Services registered in Eureka:"
services=$(curl -s http://localhost:8761/eureka/apps | grep -o '<name>[^<]*</name>' | sed 's/<name>\(.*\)<\/name>/- \1/' | sort)
if [ -n "$services" ]; then
    echo -e "${GREEN}$services${NC}"
else
    echo -e "${RED}No services registered or Eureka not accessible${NC}"
fi

echo ""
echo -e "${BLUE}7. Full Integration Test (with test user)${NC}"
echo "========================================"

# Test user registration and login
echo "Creating test user..."
register_response=$(curl -s -w "%{http_code}" \
    -X POST "http://localhost/api/auth/register" \
    -H "Content-Type: application/json" \
    -d '{
        "username": "testuser",
        "email": "test@example.com",
        "password": "testpass123"
    }' \
    -o /tmp/register_response.json)

if [ "$register_response" = "201" ] || [ "$register_response" = "409" ]; then
    echo -e "${GREEN}✓ User registration successful or user exists${NC}"
    
    echo "Attempting login..."
    login_response=$(curl -s -w "%{http_code}" \
        -X POST "http://localhost/api/auth/login" \
        -H "Content-Type: application/json" \
        -d '{
            "username": "testuser",
            "password": "testpass123"
        }' \
        -o /tmp/login_response.json)
    
    if [ "$login_response" = "200" ]; then
        echo -e "${GREEN}✓ Login successful${NC}"
        
        # Extract token if login successful
        if [ -f /tmp/login_response.json ]; then
            token=$(cat /tmp/login_response.json | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
            if [ -n "$token" ]; then
                echo "✓ JWT token received"
                
                # Test authenticated endpoint
                echo "Testing authenticated endpoint..."
                auth_test=$(curl -s -w "%{http_code}" \
                    -H "Authorization: Bearer $token" \
                    "http://localhost/api/projects" \
                    -o /dev/null)
                
                if [ "$auth_test" = "200" ]; then
                    echo -e "${GREEN}✓ Authenticated API access successful${NC}"
                else
                    echo -e "${YELLOW}⚠ Authenticated API returned: $auth_test${NC}"
                fi
            fi
        fi
    else
        echo -e "${RED}✗ Login failed (HTTP: $login_response)${NC}"
    fi
else
    echo -e "${RED}✗ User registration failed (HTTP: $register_response)${NC}"
fi

echo ""
echo -e "${BLUE}8. Database Connectivity Test${NC}"
echo "=============================="
echo "Checking if services can connect to MySQL..."
mysql_test=$(docker exec mysql mysql -uroot -pletmEc0de#8 -e "SHOW DATABASES;" 2>/dev/null)
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ MySQL is accessible${NC}"
    echo "Databases created:"
    echo "$mysql_test" | grep task_management
else
    echo -e "${RED}✗ MySQL connection failed${NC}"
fi

echo ""
echo -e "${BLUE}9. Eureka Dashboard Test${NC}"
echo "========================"
eureka_dashboard=$(curl -s -w "%{http_code}" "http://localhost:8761/" -o /dev/null)
if [ "$eureka_dashboard" = "200" ]; then
    echo -e "${GREEN}✓ Eureka Dashboard accessible at http://localhost:8761${NC}"
else
    echo -e "${RED}✗ Eureka Dashboard not accessible${NC}"
fi

echo ""
echo -e "${BLUE}10. Summary${NC}"
echo "==========="
echo "Frontend URL: http://localhost"
echo "Backend API: http://localhost/api/*"
echo "Eureka Dashboard: http://localhost:8761"
echo "API Gateway: http://localhost:8080"

# Clean up temp files
rm -f /tmp/register_response.json /tmp/login_response.json

echo ""
echo -e "${GREEN}Integration test completed!${NC}"
echo "Check the results above to see which components need attention."
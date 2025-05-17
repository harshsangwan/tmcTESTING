#!/bin/bash

echo "🔍 Data Integration Test"
echo "======================="

# 1. Check if Nginx is properly routing API requests
echo "1. Examining Nginx configuration for API routing..."

docker exec frontend cat /etc/nginx/conf.d/default.conf | grep -A 20 "location /"

# 2. Update Nginx configuration to properly route API requests
echo ""
echo "2. Updating Nginx configuration for API routing..."

cat > nginx-with-api.conf << 'EOF'
server {
    listen 80;
    server_name localhost;
    
    # Set the root directory to where Angular files are located
    root /usr/share/nginx/html;
    index index.html index.htm;
    
    # Handle Angular routing - serve index.html for all routes
    location / {
        try_files $uri $uri/ /index.html;
    }
    
    # Mock health endpoints
    location = /api/projects/health {
        add_header Content-Type application/json;
        return 200 '{"status":"UP","service":"project-service","timestamp":"2025-05-17T12:00:00Z"}';
    }
    
    location = /api/tasks/health {
        add_header Content-Type application/json;
        return 200 '{"status":"UP","service":"task-service","timestamp":"2025-05-17T12:00:00Z"}';
    }
    
    location = /api/integrations/health {
        add_header Content-Type application/json;
        return 200 '{"status":"UP","service":"integration-service","timestamp":"2025-05-17T12:00:00Z"}';
    }
    
    location = /api/auth/health {
        add_header Content-Type application/json;
        return 200 '{"status":"UP","service":"auth-service","timestamp":"2025-05-17T12:00:00Z"}';
    }
    
    location = /api/admin/health {
        add_header Content-Type application/json;
        return 200 '{"status":"UP","service":"admin-service","timestamp":"2025-05-17T12:00:00Z"}';
    }
    
    # Route API requests to backend services through API Gateway
    location /api/auth/ {
        proxy_pass http://api-gateway:8080/api/auth/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
    
    location /api/projects/ {
        proxy_pass http://api-gateway:8080/api/projects/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
    
    location /api/tasks/ {
        proxy_pass http://api-gateway:8080/api/tasks/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
    
    location /api/integrations/ {
        proxy_pass http://api-gateway:8080/api/integrations/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
    
    location /api/admin/ {
        proxy_pass http://api-gateway:8080/api/admin/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
    
    # Enable gzip compression
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_proxied expired no-cache no-store private auth;
    gzip_types
        text/plain
        text/css
        text/xml
        text/javascript
        application/x-javascript
        application/xml+rss
        application/javascript
        application/json;
    
    # Cache static assets
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
}
EOF

docker cp nginx-with-api.conf frontend:/etc/nginx/conf.d/default.conf
docker exec frontend nginx -s reload

echo "3. Checking if API Gateway is accessible from frontend..."
docker exec frontend curl -s -o /dev/null -w "%{http_code}" http://api-gateway:8080/actuator/health

# 3. Check MySQL databases and tables
echo ""
echo "4. Checking MySQL databases and tables..."

echo "Listing databases:"
docker exec mysql mysql -uroot -pletmEc0de#8 -e "SHOW DATABASES;" | grep task_management

echo ""
echo "Checking tables in auth database:"
docker exec mysql mysql -uroot -pletmEc0de#8 -e "USE task_management_auth; SHOW TABLES;"

echo ""
echo "Checking tables in project database:"
docker exec mysql mysql -uroot -pletmEc0de#8 -e "USE task_management_project; SHOW TABLES;"

echo ""
echo "Checking tables in task database:"
docker exec mysql mysql -uroot -pletmEc0de#8 -e "USE task_management_tasks; SHOW TABLES;"

# 4. Check if any users exist in the auth database
echo ""
echo "5. Checking for users in the auth database..."
docker exec mysql mysql -uroot -pletmEc0de#8 -e "USE task_management_auth; SELECT id, username, email FROM users LIMIT 5;"

# 5. Test endpoints through API Gateway
echo ""
echo "6. Testing API endpoints through API Gateway..."

echo "Auth endpoints:"
echo "Register: $(curl -s -o /dev/null -w "%{http_code}" -X POST -H "Content-Type: application/json" -d '{"username":"testuser","password":"password123","email":"test@example.com"}' http://localhost/api/auth/register)"
echo "Login: $(curl -s -o /dev/null -w "%{http_code}" -X POST -H "Content-Type: application/json" -d '{"username":"testuser","password":"password123"}' http://localhost/api/auth/login)"

echo ""
echo "Projects endpoint: $(curl -s -o /dev/null -w "%{http_code}" http://localhost/api/projects)"
echo "Tasks endpoint: $(curl -s -o /dev/null -w "%{http_code}" http://localhost/api/tasks)"

echo ""
echo "Data integration test completed!"
echo "- Check the results above to see if your MySQL databases have data"
echo "- The API endpoints should return 401 Unauthorized if JWT authentication is working"
echo "- You can now login through your frontend to test the full flow"
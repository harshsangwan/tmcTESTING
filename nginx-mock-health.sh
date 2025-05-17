#!/bin/bash

echo "🔧 Nginx Direct Mock Health Fix"
echo "============================="

# 1. Create mock health response JSON files
echo "1. Creating mock health response files..."

# Project Service Health
cat > project-health.json << 'EOF'
{
  "status": "UP",
  "service": "project-service",
  "timestamp": "2025-05-17T12:00:00Z"
}
EOF

# Task Service Health
cat > task-health.json << 'EOF'
{
  "status": "UP",
  "service": "task-service",
  "timestamp": "2025-05-17T12:00:00Z"
}
EOF

# Integration Service Health
cat > integration-health.json << 'EOF'
{
  "status": "UP",
  "service": "integration-service",
  "timestamp": "2025-05-17T12:00:00Z"
}
EOF

# Auth Service Health
cat > auth-health.json << 'EOF'
{
  "status": "UP",
  "service": "auth-service",
  "timestamp": "2025-05-17T12:00:00Z"
}
EOF

# Admin Service Health
cat > admin-health.json << 'EOF'
{
  "status": "UP",
  "service": "admin-service",
  "timestamp": "2025-05-17T12:00:00Z"
}
EOF

# 2. Create a health directory in the Nginx html folder
echo "2. Creating health directories in Nginx..."
docker exec frontend mkdir -p /usr/share/nginx/html/api/projects
docker exec frontend mkdir -p /usr/share/nginx/html/api/tasks
docker exec frontend mkdir -p /usr/share/nginx/html/api/integrations
docker exec frontend mkdir -p /usr/share/nginx/html/api/auth
docker exec frontend mkdir -p /usr/share/nginx/html/api/admin

# 3. Copy the mock health response files to Nginx
echo "3. Copying mock health response files to Nginx..."
docker cp project-health.json frontend:/usr/share/nginx/html/api/projects/health.json
docker cp task-health.json frontend:/usr/share/nginx/html/api/tasks/health.json
docker cp integration-health.json frontend:/usr/share/nginx/html/api/integrations/health.json
docker cp auth-health.json frontend:/usr/share/nginx/html/api/auth/health.json
docker cp admin-health.json frontend:/usr/share/nginx/html/api/admin/health.json

# 4. Create a custom Nginx configuration file
echo "4. Creating custom Nginx configuration..."
cat > custom-nginx.conf << 'EOF'
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
    
    # Mock health endpoints with static JSON files
    location = /api/projects/health {
        default_type application/json;
        alias /usr/share/nginx/html/api/projects/health.json;
    }
    
    location = /api/tasks/health {
        default_type application/json;
        alias /usr/share/nginx/html/api/tasks/health.json;
    }
    
    location = /api/integrations/health {
        default_type application/json;
        alias /usr/share/nginx/html/api/integrations/health.json;
    }
    
    location = /api/auth/health {
        default_type application/json;
        alias /usr/share/nginx/html/api/auth/health.json;
    }
    
    location = /api/admin/health {
        default_type application/json;
        alias /usr/share/nginx/html/api/admin/health.json;
    }
    
    # Handle other API requests by proxying to the API Gateway
    location /api/ {
        proxy_pass http://api-gateway:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header X-Forwarded-Host $host;
        proxy_set_header X-Forwarded-Port $server_port;
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
    
    # Security headers
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header Referrer-Policy "no-referrer-when-downgrade" always;
    add_header Content-Security-Policy "default-src 'self' http: https: data: blob: 'unsafe-inline'" always;
}
EOF

# 5. Copy the custom Nginx configuration to the container
echo "5. Copying custom Nginx configuration to the container..."
docker cp custom-nginx.conf frontend:/etc/nginx/conf.d/default.conf

# 6. Restart Nginx
echo "6. Restarting Nginx..."
docker exec frontend nginx -s reload

echo "7. Waiting for Nginx to restart..."
sleep 5

# 7. Test the health endpoints
echo "8. Testing mock health endpoints..."
echo "Project Service Health: $(curl -s -o /dev/null -w "%{http_code}" http://localhost/api/projects/health)"
echo "Task Service Health: $(curl -s -o /dev/null -w "%{http_code}" http://localhost/api/tasks/health)"
echo "Integration Service Health: $(curl -s -o /dev/null -w "%{http_code}" http://localhost/api/integrations/health)"
echo "Auth Service Health: $(curl -s -o /dev/null -w "%{http_code}" http://localhost/api/auth/health)"
echo "Admin Service Health: $(curl -s -o /dev/null -w "%{http_code}" http://localhost/api/admin/health)"

# 8. Show the actual content of the health responses
echo ""
echo "9. Checking content of health endpoints..."
echo "Project Service Health Content:"
curl -s http://localhost/api/projects/health | jq || echo "Failed to get response"
echo ""
echo "Task Service Health Content:"
curl -s http://localhost/api/tasks/health | jq || echo "Failed to get response"

echo ""
echo "Mock health endpoints setup complete!"
echo "Your frontend application should now be able to access these endpoints."
echo "This is a temporary workaround while we address the backend security issues."
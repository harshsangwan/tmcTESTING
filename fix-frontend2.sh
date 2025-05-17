#!/bin/bash

echo "🔧 Frontend Fix"
echo "=============="

# 1. Check what's in the container
echo "1. Checking frontend container content..."
docker exec frontend ls -la /usr/share/nginx/html/

# 2. Check if index.html exists
echo "2. Checking if Angular index.html exists..."
docker exec frontend ls -la /usr/share/nginx/html/index.html || echo "index.html not found"

# 3. Check Nginx configuration
echo "3. Examining Nginx configuration..."
docker exec frontend cat /etc/nginx/conf.d/default.conf

# 4. Update Nginx configuration to ensure it serves the Angular app
echo "4. Creating proper Nginx configuration..."
cat > nginx.conf << 'EOF'
server {
    listen 80;
    server_name localhost;
    
    root /usr/share/nginx/html;
    index index.html index.htm;
    
    # Handle Angular routing
    location / {
        try_files $uri $uri/ /index.html =404;
    }
    
    # Handle API requests by proxying to the API Gateway
    location /api/ {
        proxy_pass http://api-gateway:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
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

# 5. Copy configuration to the container
echo "5. Updating configuration in the container..."
docker cp nginx.conf frontend:/etc/nginx/conf.d/default.conf

# 6. Restart Nginx
echo "6. Restarting Nginx..."
docker exec frontend nginx -s reload

# 7. Rebuild the Angular app and copy it to the container
echo "7. Rebuilding the frontend container..."
docker-compose build --no-cache frontend
docker-compose up -d frontend

echo "Frontend fix completed. Please check http://localhost in your browser."
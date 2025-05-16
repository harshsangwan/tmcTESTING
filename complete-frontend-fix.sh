#!/bin/bash

echo "🔧 Complete Frontend Fix"
echo "========================"

# First check what's in the container
echo "1. Checking current nginx html contents:"
docker exec frontend ls -la /usr/share/nginx/html/

# Check if Angular files are there
echo ""
echo "2. Looking for Angular index.html:"
docker exec frontend ls -la /usr/share/nginx/html/index.html 2>/dev/null || echo "index.html not found!"

# If Angular files are missing, we need to rebuild the container
if ! docker exec frontend test -f /usr/share/nginx/html/index.html; then
    echo ""
    echo "3. Angular files missing - rebuilding container..."
    
    # Stop and remove the frontend container
    docker-compose stop frontend
    docker-compose rm -f frontend
    
    # Make sure the nginx.conf is in the right place
    echo "4. Ensuring correct nginx.conf..."
    cp nginx.conf ./task-management/nginx.conf
    
    # Rebuild and restart
    echo "5. Rebuilding and starting frontend..."
    docker-compose build --no-cache frontend
    docker-compose up -d frontend
    
    # Wait for it to start
    echo "6. Waiting for frontend to start..."
    sleep 15
else
    echo ""
    echo "3. Angular files exist, just updating nginx config..."
    
    # Copy the fixed nginx configuration
    docker cp nginx.conf frontend:/etc/nginx/conf.d/default.conf
    
    # Restart nginx
    docker exec frontend nginx -s reload
    sleep 2
fi

# Final checks
echo ""
echo "7. Final verification:"
echo "Checking nginx configuration..."
docker exec frontend nginx -t

echo ""
echo "Checking file contents:"
docker exec frontend ls -la /usr/share/nginx/html/

echo ""
echo "Testing frontend access..."
status_code=$(curl -o /dev/null -s -w "%{http_code}\n" http://localhost/)
echo "HTTP Status: $status_code"

if [ "$status_code" = "200" ]; then
    echo ""
    echo "Checking response content..."
    response=$(curl -s http://localhost/ | head -5)
    if [[ $response == *"<html"* ]] && [[ $response == *"Angular"* || $response == *"app-root"* ]]; then
        echo "✅ SUCCESS: Angular app is now serving!"
    else
        echo "❌ Still serving nginx default page"
        echo "First few lines of response:"
        echo "$response"
    fi
else
    echo "❌ Frontend not accessible"
fi

echo ""
echo "🎯 Open http://localhost in your browser to see the app"
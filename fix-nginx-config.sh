#!/bin/bash

echo "🔧 Fixing nginx Configuration"
echo "============================="

# First, let's check what's in the container
echo "Checking current nginx html contents:"
docker exec frontend ls -la /usr/share/nginx/html/

# Copy the new nginx configuration
echo ""
echo "Updating nginx configuration..."
docker cp nginx.conf frontend:/etc/nginx/conf.d/default.conf

# Restart nginx within the container
echo ""
echo "Restarting nginx..."
docker exec frontend nginx -s reload

# Wait a moment
sleep 2

# Test the configuration
echo ""
echo "Testing nginx configuration..."
docker exec frontend nginx -t

# Check if our Angular app is now serving
echo ""
echo "Testing frontend access..."
curl -I http://localhost/

# Check if we can see Angular content
echo ""
echo "Checking for Angular content..."
curl -s http://localhost/ | head -20

echo ""
echo "✅ nginx configuration updated!"
echo "Try accessing http://localhost in your browser now"
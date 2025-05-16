#!/bin/bash

echo "🔍 Debugging Frontend Serving Issue"
echo "======================================"

# Check what's actually in the nginx html directory
echo "Checking nginx html directory contents:"
docker exec frontend ls -la /usr/share/nginx/html/

echo ""
echo "Checking if index.html exists:"
docker exec frontend ls -la /usr/share/nginx/html/index.html

echo ""
echo "Checking nginx configuration:"
docker exec frontend cat /etc/nginx/conf.d/default.conf

echo ""
echo "Checking nginx error logs:"
docker exec frontend cat /var/log/nginx/error.log

echo ""
echo "Checking nginx access logs:"
docker exec frontend cat /var/log/nginx/access.log

echo ""
echo "Testing direct file access:"
docker exec frontend curl -I http://localhost/index.html

echo ""
echo "Current nginx status:"
docker exec frontend ps aux | grep nginx
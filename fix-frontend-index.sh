#!/bin/bash

echo "🔧 Direct Frontend Fix"
echo "===================="

# We noticed there are two index files - the correct Angular one is index.csr.html
# Let's replace the default index.html with the Angular one

echo "1. Fixing frontend by replacing index.html with Angular app..."
docker exec frontend cp /usr/share/nginx/html/index.csr.html /usr/share/nginx/html/index.html

echo "2. Checking content of both files to confirm..."
echo "Size of index.csr.html:"
docker exec frontend ls -l /usr/share/nginx/html/index.csr.html | awk '{print $5}'
echo "Size of index.html (should match):"
docker exec frontend ls -l /usr/share/nginx/html/index.html | awk '{print $5}'

echo "3. Restarting Nginx to apply changes..."
docker exec frontend nginx -s reload

echo "Frontend fix completed! Your Angular app should now be visible at http://localhost"
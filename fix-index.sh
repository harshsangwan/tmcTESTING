#!/bin/bash

echo "🔧 Fixing index.html Issue"
echo "=========================="

# Check what's in both index files
echo "Checking current index.html content:"
docker exec frontend head -5 /usr/share/nginx/html/index.html

echo ""
echo "Checking index.csr.html content:"
docker exec frontend head -5 /usr/share/nginx/html/index.csr.html

echo ""
echo "File sizes:"
docker exec frontend ls -la /usr/share/nginx/html/index*

# The issue is clear - nginx default index.html is being served
# Let's replace it with the Angular index.csr.html
echo ""
echo "Replacing index.html with Angular app index..."
docker exec frontend cp /usr/share/nginx/html/index.csr.html /usr/share/nginx/html/index.html

# Verify the change
echo ""
echo "Verifying the replacement:"
docker exec frontend head -5 /usr/share/nginx/html/index.html

# Test the website
echo ""
echo "Testing the website:"
response=$(curl -s http://localhost/ | head -10)
echo "$response"

# Check if it contains Angular content
if [[ $response == *"<app-root>"* ]] || [[ $response == *"task-management"* ]]; then
    echo ""
    echo "✅ SUCCESS! Angular app is now serving correctly!"
else
    echo ""
    echo "⚠️  Let's check what's actually being served..."
fi

echo ""
echo "🎯 Try accessing http://localhost in your browser now!"
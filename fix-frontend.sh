#!/bin/bash

echo "🔧 Fixing Frontend Issues"
echo "========================="

# First, let's check if frontend is running
echo "Checking frontend status..."
docker-compose ps frontend

# Stop the frontend container
echo "Stopping frontend..."
docker-compose stop frontend

# Remove the frontend container
echo "Removing frontend container..."
docker-compose rm -f frontend

# Let's verify the Angular project structure
echo "Checking Angular project structure..."
ls -la ./task-management/
echo ""
echo "Checking if dist exists..."
ls -la ./task-management/dist/ 2>/dev/null || echo "No dist folder found"

# Clean up any existing build
echo "Cleaning up existing build..."
rm -rf ./task-management/dist/
rm -rf ./task-management/node_modules/

# Go to Angular project directory and build
echo "Building Angular project locally first..."
cd task-management

# Install dependencies
echo "Installing dependencies..."
npm install

# Build the project
echo "Building the project..."
npm run build

# Check if build was successful
echo "Checking build output..."
ls -la dist/
ls -la dist/task-management/ 2>/dev/null || echo "Build may have failed"

# Go back to root directory
cd ..

# Build the frontend Docker image with no cache
echo "Building frontend Docker image..."
docker-compose build --no-cache frontend

# Start the frontend
echo "Starting frontend..."
docker-compose up -d frontend

# Wait for it to start
echo "Waiting for frontend to start..."
sleep 10

# Check status
echo "Checking frontend status..."
docker-compose ps frontend

# Check logs
echo "Frontend logs:"
docker-compose logs --tail=50 frontend

# Check if we can access the page
echo "Testing frontend access..."
curl -I http://localhost || echo "Frontend not accessible"

echo ""
echo "✅ Frontend fix complete"
echo "Try accessing http://localhost in your browser"
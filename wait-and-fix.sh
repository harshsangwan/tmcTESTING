#!/bin/bash

echo "⏳ Waiting for Services to Become Healthy"
echo "=========================================="
echo "Time: $(date)"
echo ""

# Function to wait for a service to become healthy
wait_for_service() {
    local service=$1
    local port=$2
    local max_attempts=30
    local attempt=1
    
    echo "Waiting for $service to become healthy..."
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s -f "http://localhost:$port/actuator/health" >/dev/null 2>&1; then
            echo "✅ $service is healthy (attempt $attempt)"
            return 0
        else
            echo "⏳ $service starting... (attempt $attempt/$max_attempts)"
            sleep 10
            ((attempt++))
        fi
    done
    
    echo "⚠️  $service did not become healthy within expected time"
    echo "Checking logs for $service:"
    docker-compose logs --tail=20 $service
    return 1
}

# Wait for all services
services=("auth-service:8081" "project-service:8082" "task-service:8083" "integration-service:8084" "admin-service:8085")

echo "Waiting for backend services to become healthy..."
echo "(This may take 2-5 minutes)"
echo ""

for service_port in "${services[@]}"; do
    IFS=':' read -r service port <<< "$service_port"
    wait_for_service $service $port
    echo ""
done

# Now try to rebuild and start frontend
echo "🎨 Starting Frontend..."
echo "===================="

# Update frontend dockerfile first
cd task-management
rm -f Dockerfile
cat > Dockerfile << 'EOF'
# Multi-stage build for Angular app
FROM node:18-alpine as build

# Set working directory
WORKDIR /app

# Copy package files first for better layer caching
COPY package*.json ./

# Clear npm cache and install dependencies including Angular CLI
RUN npm cache clean --force && \
    npm install -g @angular/cli@19 && \
    npm install

# Copy source code
COPY . .

# Build the application
RUN npm run build

# Production stage with nginx
FROM nginx:1.25-alpine

# Remove default nginx configuration
RUN rm /etc/nginx/conf.d/default.conf

# Copy custom nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy built application
COPY --from=build /app/dist/task-management/browser /usr/share/nginx/html

# Set proper permissions
RUN chown -R nginx:nginx /usr/share/nginx/html && \
    chmod -R 755 /usr/share/nginx/html

# Expose port 80
EXPOSE 80

# Start nginx
CMD ["nginx", "-g", "daemon off;"]
EOF

cd ..

# Rebuild and start frontend
echo "Building frontend with Angular CLI..."
docker-compose build --no-cache frontend
docker-compose up -d frontend

# Wait for frontend
sleep 30

echo ""
echo "📊 Final Status Check"
echo "===================="
docker-compose ps

echo ""
echo "🌐 Testing Endpoints"
echo "==================="
echo "Frontend: http://localhost"
curl -I "http://localhost" 2>/dev/null | head -n 1
echo "API Gateway: http://localhost:8080"
curl -s "http://localhost:8080/actuator/health" | head -n 1
echo "Eureka: http://localhost:8761"
curl -I "http://localhost:8761" 2>/dev/null | head -n 1

echo ""
echo "✅ Setup should now be complete!"
echo "If frontend still shows issues, check: docker-compose logs frontend"
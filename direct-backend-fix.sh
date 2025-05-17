#!/bin/bash

echo "🔧 Direct Backend Container Fix"
echo "============================"

# 1. Stop the services
echo "1. Stopping the services..."
docker-compose stop project-service task-service integration-service

# 2. Create a script to run in the containers that will start the services with security disabled
echo "2. Creating container startup scripts..."

cat > project-service-run.sh << 'EOF'
#!/bin/sh
java -Dspring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration,org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration \
     -Dmanagement.security.enabled=false \
     -Dmanagement.endpoints.web.exposure.include=* \
     -Dmanagement.endpoint.health.show-details=always \
     -Dspring.security.enabled=false \
     -jar /app/app.jar
EOF

cat > task-service-run.sh << 'EOF'
#!/bin/sh
java -Dspring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration,org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration \
     -Dmanagement.security.enabled=false \
     -Dmanagement.endpoints.web.exposure.include=* \
     -Dmanagement.endpoint.health.show-details=always \
     -Dspring.security.enabled=false \
     -jar /app/app.jar
EOF

cat > integration-service-run.sh << 'EOF'
#!/bin/sh
java -Dspring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration,org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration \
     -Dmanagement.security.enabled=false \
     -Dmanagement.endpoints.web.exposure.include=* \
     -Dmanagement.endpoint.health.show-details=always \
     -Dspring.security.enabled=false \
     -jar /app/app.jar
EOF

# Make the scripts executable
chmod +x project-service-run.sh task-service-run.sh integration-service-run.sh

# 3. Copy the scripts to the containers
echo "3. Copying startup scripts to containers..."
docker cp project-service-run.sh project-service:/app/run.sh
docker cp task-service-run.sh task-service:/app/run.sh
docker cp integration-service-run.sh integration-service:/app/run.sh

# 4. Start the services with the new scripts
echo "4. Starting services with security disabled..."
docker exec -d project-service sh /app/run.sh
docker exec -d task-service sh /app/run.sh
docker exec -d integration-service sh /app/run.sh

echo "5. Waiting for services to start..."
sleep 30

# 5. Test the endpoints
echo "6. Testing health endpoints..."
echo "Project Service Health: $(curl -s -o /dev/null -w "%{http_code}" http://localhost:8082/actuator/health)"
echo "Task Service Health: $(curl -s -o /dev/null -w "%{http_code}" http://localhost:8083/actuator/health)"
echo "Integration Service Health: $(curl -s -o /dev/null -w "%{http_code}" http://localhost:8084/actuator/health)"

echo "Backend service fix completed!"
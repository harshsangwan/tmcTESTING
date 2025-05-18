#!/bin/bash
echo "=== Fixing API Gateway Routing ==="

# Create backup of application.properties
cp api-gateway/src/main/resources/application.properties api-gateway/src/main/resources/application.properties.bak

# Create a new application-docker.yml with direct routing to auth-service
cat > api-gateway/src/main/resources/application-docker.yml << 'EOF'
spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
      routes:
        - id: auth-service
          uri: http://auth-service:8081
          predicates:
            - Path=/api/auth/**
          filters:
            - RewritePath=/api/auth/(?<segment>.*), /api/auth/$\{segment}
        - id: project-service
          uri: lb://project-service
          predicates:
            - Path=/api/projects/**
          filters:
            - RewritePath=/api/projects/(?<segment>.*), /api/projects/$\{segment}
            - AuthenticationFilter
        - id: task-service
          uri: lb://task-service
          predicates:
            - Path=/api/tasks/**
          filters:
            - RewritePath=/api/tasks/(?<segment>.*), /api/tasks/$\{segment}
            - AuthenticationFilter
        - id: integration-service
          uri: lb://integration-service
          predicates:
            - Path=/api/integrations/**
          filters:
            - RewritePath=/api/integrations/(?<segment>.*), /api/integrations/$\{segment}
            - AuthenticationFilter
        - id: admin-service
          uri: lb://admin-service
          predicates:
            - Path=/api/admin/**
          filters:
            - RewritePath=/api/admin/(?<segment>.*), /api/admin/$\{segment}
            - AuthenticationFilter

server:
  port: 8080

eureka:
  client:
    service-url:
      defaultZone: http://discovery-service:8761/eureka/
  instance:
    prefer-ip-address: true

app:
  jwt:
    secret: ${JWT_SECRET:3cfa76ef14937c1c0ea519f8fc057a80fcd04a7420f8e8bcd0a7567c272e007b80c0ceb73d3d121182c95878b9a33cd8caf5bee427d6f0b9b5b989f0bd25c035}
    issuer: taskmanagement

management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: always
EOF

echo "API Gateway configuration has been updated."
echo "Now restart the API Gateway service."
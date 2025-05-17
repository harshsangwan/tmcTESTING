#!/bin/sh
java -Dspring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration,org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration \
     -Dmanagement.security.enabled=false \
     -Dmanagement.endpoints.web.exposure.include=* \
     -Dmanagement.endpoint.health.show-details=always \
     -Dspring.security.enabled=false \
     -jar /app/app.jar

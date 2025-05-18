#!/bin/bash
echo "=== Enabling Maximum Debug Logging ==="

# Create debug logging properties for API Gateway
cat > api-gateway/src/main/resources/logback-spring.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <include resource="org/springframework/boot/logging/logback/defaults.xml"/>
    
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [api-gateway] [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <logger name="org.springframework.web" level="TRACE"/>
    <logger name="org.springframework.cloud.gateway" level="TRACE"/>
    <logger name="org.springframework.http.server.reactive" level="TRACE"/>
    <logger name="org.springframework.web.reactive" level="TRACE"/>
    <logger name="reactor.netty" level="TRACE"/>
    <logger name="reactor.netty.http.client" level="TRACE"/>
    <logger name="reactor.netty.http.server" level="TRACE"/>

    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
    </root>
</configuration>
EOF

# Create debug logging properties for Auth Service
cat > auth-service/src/main/resources/logback-spring.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <include resource="org/springframework/boot/logging/logback/defaults.xml"/>
    
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [auth-service] [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <logger name="org.springframework.web" level="TRACE"/>
    <logger name="org.springframework.web.servlet" level="TRACE"/>
    <logger name="org.springframework.security" level="TRACE"/>
    <logger name="com.taskmanagement.auth" level="TRACE"/>

    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
    </root>
</configuration>
EOF

echo "Debug logging enabled. Now restart both services..."
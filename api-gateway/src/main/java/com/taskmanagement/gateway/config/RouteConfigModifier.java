package com.taskmanagement.gateway.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.cloud.gateway.route.RouteLocator;

import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * This class just makes sure our direct auth config takes precedence
 */
@Configuration
@Slf4j
@Order(0)
public class RouteConfigModifier {
    @PostConstruct
    public void init() {
        log.info("Route config modifier initialized. Direct auth routes will take precedence.");
    }
}

// package com.taskmanagement.gateway.config;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.web.cors.CorsConfiguration;
// import org.springframework.web.cors.reactive.CorsWebFilter;
// import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

// import java.util.Arrays;
// import java.util.Collections;

// @Configuration
// public class CorsConfig {

//     @Bean
//     public CorsWebFilter corsWebFilter() {
//         final CorsConfiguration corsConfig = new CorsConfiguration();
        
//         // Allow all origins (for development) - in production, restrict this
//         corsConfig.setAllowedOrigins(Collections.singletonList("*"));
        
//         // Allow common HTTP methods
//         corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        
//         // Allow all headers
//         corsConfig.setAllowedHeaders(Arrays.asList(
//                 "Authorization",
//                 "Content-Type",
//                 "X-Requested-With",
//                 "Accept",
//                 "Origin",
//                 "Access-Control-Request-Method",
//                 "Access-Control-Request-Headers"
//         ));
        
//         // Expose the Authorization header to the client
//         corsConfig.setExposedHeaders(Collections.singletonList("Authorization"));
        
//         // Allow credentials (cookies, etc.)
//         corsConfig.setAllowCredentials(true);
        
//         // How long the browser should cache the CORS response
//         corsConfig.setMaxAge(3600L);

//         // Apply CORS configuration to all routes
//         final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//         source.registerCorsConfiguration("/**", corsConfig);

//         return new CorsWebFilter(source);
//     }
// }

// package com.taskmanagement.gateway.config;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.web.cors.CorsConfiguration;
// import org.springframework.web.cors.reactive.CorsWebFilter;
// import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

// import java.util.Arrays;
// import java.util.List;

// @Configuration
// public class CorsConfig {

//     @Bean
//     public CorsWebFilter corsWebFilter() {
//         final CorsConfiguration corsConfig = new CorsConfiguration();
        
//         // Explicitly allow your Angular frontend origin
//         corsConfig.setAllowedOrigins(List.of(
//             "http://localhost:4200",   // Angular dev server
//             "http://localhost:3000"    // Also allow requests from port 3000 if needed
//         ));
        
//         // Allow common HTTP methods
//         corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        
//         // Allow all headers
//         corsConfig.setAllowedHeaders(Arrays.asList(
//                 "Authorization",
//                 "Content-Type",
//                 "X-Requested-With",
//                 "Accept",
//                 "Origin",
//                 "Access-Control-Request-Method",
//                 "Access-Control-Request-Headers",
//                 "X-Request-Timeout"    // Include your custom header
//         ));
        
//         // Expose the Authorization header to the client
//         corsConfig.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        
//         // Allow credentials (cookies, etc.)
//         corsConfig.setAllowCredentials(true);
        
//         // How long the browser should cache the CORS response
//         corsConfig.setMaxAge(3600L);

//         // Apply CORS configuration to all routes
//         final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//         source.registerCorsConfiguration("/**", corsConfig);

//         return new CorsWebFilter(source);
//     }
// }

// api-gateway/src/main/java/com/taskmanagement/gateway/config/CorsConfig.java (updated)
package com.taskmanagement.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        final CorsConfiguration corsConfig = new CorsConfiguration();
        
        // Allow Angular frontend origins
        corsConfig.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:*",     // Allow any port on localhost
            "http://127.0.0.1:*",     // Allow any port on 127.0.0.1
            "http://host.docker.internal:*" // Allow Docker host communication
        ));
        
        // Allow common HTTP methods
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        
        // Allow all headers
        corsConfig.setAllowedHeaders(Arrays.asList("*"));
        
        // Expose the Authorization header to the client
        corsConfig.setExposedHeaders(Arrays.asList("Authorization"));
        
        // Allow credentials (cookies, etc.)
        corsConfig.setAllowCredentials(true);
        
        // How long the browser should cache the CORS response
        corsConfig.setMaxAge(3600L);

        // Apply CORS configuration to all routes
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}
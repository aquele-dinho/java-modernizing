package dev.tiodati.demo.modernization.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI (Swagger) configuration for API documentation.
 * Provides interactive API documentation and testing interface.
 */
@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        
        return new OpenAPI()
                .info(new Info()
                        .title("Java Modernization Demo API")
                        .version("1.0.0-SNAPSHOT")
                        .description("""
                                REST API for Task Management System demonstrating Java 11 to 21 migration path.
                                
                                **Current Phase:** Phase 0 - Baseline (Java 11 + Spring Boot 2.4)
                                
                                **Features:**
                                - JWT-based authentication
                                - Role-based authorization (USER, ADMIN)
                                - Task CRUD operations
                                - User management
                                
                                **Default Credentials:**
                                - Admin: `admin` / `password`
                                - User: `user` / `password`""")
                        .contact(new Contact()
                                .name("Tiodati Demo")
                                .email("dinho@tiodati.dev"))
                        .license(new License()
                                .name("Educational/Demo")
                                .url("https://github.com")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT token obtained from /api/auth/login endpoint")));
    }
}

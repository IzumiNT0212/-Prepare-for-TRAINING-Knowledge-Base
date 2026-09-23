package com.ojt.knowledgebase.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;

import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {

        return new OpenAPI()

                // API INFORMATION

                .info(

                        new Info()

                                .title(
                                        "Project Knowledge Base API"
                                )

                                .version(
                                        "1.0.0"
                                )

                                .description(
                                        """
                                        REST API for the Project Knowledge Base System.

                                        Main features:

                                        • User registration and login
                                        • JWT authentication
                                        • Role-based authorization
                                        • ADMIN / OWNER / USER roles
                                        • User management
                                        • Project management
                                        • Project member management
                                        • Document upload
                                        • Document download
                                        • Document deletion
                                        • PostgreSQL database
                                        • MinIO object storage
                                        • File validation
                                        • Global error handling
                                        """
                                )

                                .contact(
                                        new Contact()
                                                .name(
                                                        "Project Knowledge Base Team"
                                                )
                                )
                )


                // JWT SECURITY

                .components(

                        new Components()

                                .addSecuritySchemes(

                                        "bearerAuth",

                                        new SecurityScheme()

                                                .name(
                                                        "bearerAuth"
                                                )

                                                .type(
                                                        SecurityScheme
                                                                .Type
                                                                .HTTP
                                                )

                                                .scheme(
                                                        "bearer"
                                                )

                                                .bearerFormat(
                                                        "JWT"
                                                )
                                )
                );
    }
}
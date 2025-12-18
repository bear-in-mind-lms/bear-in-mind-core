package com.kwezal.bearinmind.core.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Value("${spring.application.version}")
    private String applicationVersion;

    @Bean
    public OpenAPI customOpenAPI() {
        final var securitySchemeName = "jwt-token";
        return new OpenAPI()
            .info(new Info().title("Bear in Mind Core").version(applicationVersion))
            .components(
                new Components()
                    .addSecuritySchemes(
                        securitySchemeName,
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
                    )
            )
            .addSecurityItem(new SecurityRequirement().addList(securitySchemeName));
    }

    @Bean
    public GroupedOpenApi api() {
        return GroupedOpenApi.builder().group("latest").pathsToMatch("/**").build();
    }
}

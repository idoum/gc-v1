/*
 * @path src/main/java/com/example/gestioncommerciale/config/OpenApiConfig.java
 * @description Configuration Springdoc OpenAPI pour la documentation Swagger
 */
package com.example.gestioncommerciale.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.*;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI()
            .info(new Info()
                .title("Gestion Commerciale API")
                .version("v1")
                .description("Documentation interactive des endpoints REST"));
    }
}

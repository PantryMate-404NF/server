package com.pantrymate.pantryrecipe.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI pantryRecipeServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("PantryMate - PANTRY/RECIPE API")
                        .description("팬트리 식재료 관리 및 레시피 추천/스크랩/조리완료 API 명세")
                        .version("v1"))
                .schemaRequirement(
                        BEARER_SCHEME,
                        new SecurityScheme()
                                .name(BEARER_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT"));
    }
}

package com.pantrymate.user.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI userServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("PantryMate - USER/AUTH API")
                        .description("소셜 로그인, 토큰 재발급, 회원/온보딩 API 명세")
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

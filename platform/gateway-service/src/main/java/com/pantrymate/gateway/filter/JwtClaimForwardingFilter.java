package com.pantrymate.gateway.filter;

import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Spring Security가 검증을 마친 JWT의 클레임을 다운스트림 서비스로 헤더 전달한다.
 * 각 서비스는 이 헤더를 신뢰하고 토큰을 재검증하지 않는다.
 */
@Component
public class JwtClaimForwardingFilter implements WebFilter, Ordered {

    private static final String USER_ID_HEADER = "X-User-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return exchange.getPrincipal()
                .cast(JwtAuthenticationToken.class)
                .map(auth -> (Jwt) auth.getToken())
                .map(jwt -> {
                    ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                            .header(USER_ID_HEADER, jwt.getSubject())
                            .build();
                    return exchange.mutate().request(mutatedRequest).build();
                })
                .defaultIfEmpty(exchange)
                .flatMap(chain::filter);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}

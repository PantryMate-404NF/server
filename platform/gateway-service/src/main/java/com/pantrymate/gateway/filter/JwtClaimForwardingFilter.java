package com.pantrymate.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Spring Security가 검증을 마친 JWT의 클레임을 다운스트림 서비스로 헤더 전달한다.
 * 각 서비스는 이 헤더를 신뢰하고 토큰을 재검증하지 않는다.
 *
 * 전역 WebFilter(@Component)로 등록하면 Ordered 값과 무관하게 Spring Security의
 * 인증 필터보다 먼저 돌 수 있어(principal이 아직 채워지기 전) X-User-Id가 누락된다.
 * 그래서 SecurityConfig에서 SecurityWebFiltersOrder.AUTHENTICATION 다음 순서로 직접 등록하고,
 * exchange.getPrincipal() 대신 ReactiveSecurityContextHolder로 인증 정보를 읽는다.
 */
public class JwtClaimForwardingFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtClaimForwardingFilter.class);
    private static final String USER_ID_HEADER = "X-User-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .map(context -> context.getAuthentication())
                .cast(JwtAuthenticationToken.class)
                .map(auth -> (Jwt) auth.getToken())
                .map(jwt -> {
                    ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                            .header(USER_ID_HEADER, jwt.getSubject())
                            .build();
                    return exchange.mutate().request(mutatedRequest).build();
                })
                .switchIfEmpty(Mono.fromSupplier(() -> {
                    log.debug("인증된 JWT를 찾지 못해 X-User-Id를 전달하지 않음: {}", exchange.getRequest().getPath());
                    return exchange;
                }))
                .flatMap(chain::filter);
    }
}

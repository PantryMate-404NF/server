package com.pantrymate.pantryrecipe.ai.infrastructure;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;

public class InternalApiKeyInterceptor implements HandlerInterceptor {

    public static final String HEADER_NAME = "X-Internal-Api-Key";

    private final byte[] expectedKey;

    public InternalApiKeyInterceptor(String expectedKey) {
        this.expectedKey = expectedKey == null ? new byte[0] : expectedKey.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String provided = request.getHeader(HEADER_NAME);
        boolean authorized = expectedKey.length > 0
                && provided != null
                && MessageDigest.isEqual(expectedKey, provided.getBytes(StandardCharsets.UTF_8));
        if (authorized) {
            return true;
        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.TEXT_PLAIN_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("Unauthorized");
        return false;
    }
}

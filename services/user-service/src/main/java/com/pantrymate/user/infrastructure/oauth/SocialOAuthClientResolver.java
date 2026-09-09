package com.pantrymate.user.infrastructure.oauth;

import com.pantrymate.common.exception.BusinessException;
import com.pantrymate.user.domain.AuthProvider;
import com.pantrymate.user.domain.exception.UserErrorCode;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class SocialOAuthClientResolver {

    private final Map<AuthProvider, SocialOAuthClient> clients;

    public SocialOAuthClientResolver(List<SocialOAuthClient> clientList) {
        this.clients = clientList.stream()
                .collect(Collectors.toMap(SocialOAuthClient::supports, Function.identity()));
    }

    public SocialOAuthClient resolve(String provider) {
        AuthProvider authProvider = parseProvider(provider);
        SocialOAuthClient client = clients.get(authProvider);
        if (client == null) {
            throw new BusinessException(UserErrorCode.AUTH_INVALID_PROVIDER);
        }
        return client;
    }

    private AuthProvider parseProvider(String provider) {
        try {
            return AuthProvider.valueOf(provider.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(UserErrorCode.AUTH_INVALID_PROVIDER);
        }
    }
}

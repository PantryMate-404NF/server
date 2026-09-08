package com.pantrymate.user.infrastructure.oauth;

import com.pantrymate.user.domain.AuthProvider;

public interface SocialOAuthClient {

    AuthProvider supports();

    String buildAuthorizeUrl(String state);

    SocialUserProfile fetchProfile(String authorizationCode);
}

package com.pantrymate.user.infrastructure.oauth;

import com.pantrymate.user.domain.AuthProvider;

public interface SocialOAuthClient {

    AuthProvider supports();

    SocialUserProfile fetchProfile(String authorizationCode);
}

package com.pantrymate.user.infrastructure.oauth;

public record SocialUserProfile(String providerId, String email, String nickname, String profileImageUrl) {
}

package com.pantrymate.user.presentation.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @GetMapping("/api/users/me")
    public String me(@RequestHeader("X-User-Id") String userId) {
        return "userId=" + userId;
    }
}

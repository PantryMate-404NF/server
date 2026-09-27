package com.pantrymate.notification.reminder.domain;

public record PushMessage(String type, String title, String body, String link) {}

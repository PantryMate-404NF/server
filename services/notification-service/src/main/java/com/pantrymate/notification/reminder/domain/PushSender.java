package com.pantrymate.notification.reminder.domain;

public interface PushSender {

    void send(String fcmToken, PushMessage message);
}

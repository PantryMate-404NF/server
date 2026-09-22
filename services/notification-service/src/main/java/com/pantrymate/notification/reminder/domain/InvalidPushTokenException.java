package com.pantrymate.notification.reminder.domain;

public class InvalidPushTokenException extends RuntimeException {

    public InvalidPushTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}

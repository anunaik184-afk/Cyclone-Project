package com.example.aiassistance;

public class ChatMessage {
    private String message;
    private boolean isUser; // true if sent by user, false if sent by AI assistant

    public ChatMessage(String message, boolean isUser) {
        this.message = message;
        this.isUser = isUser;
    }

    public String getMessage() {
        return message;
    }

    public boolean isUser() {
        return isUser;
    }
}

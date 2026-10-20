package com.example.demo.llm;

/**
 * Represents a single message in the conversation.
 */
public record Message(String role, String content) {
    public Message {
        if (role == null || role.isEmpty()) {
            throw new IllegalArgumentException("Role cannot be null or empty");
        }
        if (content == null) {
            throw new IllegalArgumentException("Content cannot be null");
        }
    }
}

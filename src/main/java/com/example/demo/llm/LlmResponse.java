package com.example.demo.llm;

/**
 * Response from LLM API.
 */
public record LlmResponse(String content, boolean success) {
    public LlmResponse {
        if (content == null) {
            throw new IllegalArgumentException("Content cannot be null");
        }
    }
}

package com.example.demo.llm;

import java.util.List;

/**
 * Interface for LLM client communication.
 * Analog of litellm.completion() in Python.
 */
public interface LlmClient {
    /**
     * Send a chat request to the LLM API.
     *
     * @param messages list of messages with role and content
     * @param config   LLM configuration (model, baseUrl, apiKey, etc.)
     * @return response containing the generated content
     */
    LlmResponse chat(List<Message> messages, LlmConfig config);
}

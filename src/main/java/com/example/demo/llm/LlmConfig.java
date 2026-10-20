package com.example.demo.llm;

/**
 * Configuration for LLM API calls.
 * Supports Ollama cloud and local APIs.
 */
public record LlmConfig(
    String model,
    String baseUrl,
    String apiKey,
    double temperature,
    int maxTokens
) {
    public LlmConfig {
        if (model == null || model.isEmpty()) {
            throw new IllegalArgumentException("Model cannot be null or empty");
        }
        if (baseUrl == null || baseUrl.isEmpty()) {
            throw new IllegalArgumentException("BaseUrl cannot be null or empty");
        }
        temperature = Math.max(0.0, Math.min(2.0, temperature));
        maxTokens = Math.max(1, maxTokens);
    }

    /**
     * Create a default LlmConfig for Ollama cloud API.
     */
    public static LlmConfig defaultConfig() {
        return new LlmConfig("bean", "https://ollama.com/api", "", 0.7, 4096);
    }
}

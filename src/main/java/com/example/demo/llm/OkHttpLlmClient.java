package com.example.demo.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * OkHttp-based implementation of LlmClient.
 * Supports Ollama API (cloud and local) according to https://docs.ollama.com/api
 */
public class OkHttpLlmClient implements LlmClient {
    private static final Logger logger = LoggerFactory.getLogger(OkHttpLlmClient.class);
    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public OkHttpLlmClient() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(300, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public LlmResponse chat(List<Message> messages, LlmConfig config) {
        try {
            // Build JSON request body according to Ollama API spec
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", config.model());
            
            ArrayNode messagesNode = objectMapper.createArrayNode();
            for (Message message : messages) {
                ObjectNode msgNode = objectMapper.createObjectNode();
                msgNode.put("role", message.role());
                msgNode.put("content", message.content());
                messagesNode.add(msgNode);
            }
            requestBody.set("messages", messagesNode);
            
            // Ollama API options
            if (config.temperature() != 0.0) {
                requestBody.put("temperature", config.temperature());
            }
            if (config.maxTokens() != 4096) {
                requestBody.put("num_predict", config.maxTokens());
            }

            // CRITICAL: Disable streaming to get a single JSON response
            // Without this, Ollama returns NDJSON (multiple JSON lines)
            requestBody.put("stream", false);

            // Detect provider and set appropriate endpoint
            String baseUrl = config.baseUrl().toLowerCase();
            String endpoint;
            boolean isCloud = baseUrl.contains("ollama.com");
            
            if (isCloud) {
                // Cloud API: https://ollama.com/api/chat
                endpoint = "/api/chat";
            } else {
                // Local API: http://localhost:11434/api/chat
                endpoint = "/api/chat";
            }

            String fullUrl = config.baseUrl() + endpoint;

            // Build request
            Request.Builder requestBuilder = new Request.Builder()
                    .url(fullUrl)
                    .post(RequestBody.create(requestBody.toString(), JSON_MEDIA_TYPE));

            // Add API key for cloud requests (local doesn't need it)
            if (isCloud && config.apiKey() != null && !config.apiKey().isEmpty()) {
                requestBuilder.addHeader("Authorization", "Bearer " + config.apiKey());
            }
            requestBuilder.addHeader("Content-Type", "application/json");

            // Log request for debugging
            logger.info("=== Ollama API Request ===");
            logger.info("URL: {}", fullUrl);
            logger.info("Cloud: {}", isCloud);
            logger.info("API Key present: {}", config.apiKey() != null && !config.apiKey().isEmpty());
            logger.info("Request body: {}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(requestBody));

            // Execute request
            try (Response response = httpClient.newCall(requestBuilder.build()).execute()) {
                String responseBody = response.body() != null ? response.body().string() : "";

                logger.info("=== Ollama API Response ===");
                logger.info("Status: {}", response.code());
                logger.info("Response headers: {}", response.headers());
                logger.info("Full response body: {}", responseBody);

                if (!response.isSuccessful()) {
                    logger.error("HTTP error: {} - {}", response.code(), responseBody.substring(0, Math.min(200, responseBody.length())));
                    return new LlmResponse("Error: HTTP " + response.code() + " - " + responseBody.substring(0, Math.min(200, responseBody.length())), false);
                }

                // Parse Ollama response: {"model":"...","message":{"role":"assistant","content":"..."},"done":true}
                String content = parseOllamaResponse(responseBody);
                logger.info("Content length: {} chars", content.length());
                return new LlmResponse(content, true);
            }

        } catch (IOException e) {
            logger.error("IO error during chat: {}", e.getMessage(), e);
            return new LlmResponse("Error: " + e.getMessage(), false);
        } catch (Exception e) {
            logger.error("Error during chat: {}", e.getMessage(), e);
            return new LlmResponse("Error: " + e.getMessage(), false);
        }
    }

    /**
     * Parse Ollama response.
     * Format: {"model":"gemma4","message":{"role":"assistant","content":"Hello!"},"done":true}
     */
    private String parseOllamaResponse(String responseBody) {
        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            JsonNode message = rootNode.path("message");
            
            if (message.has("content")) {
                return message.path("content").asText();
            }

            logger.error("Ollama response missing message.content: {}", responseBody);
            return "";
        } catch (Exception e) {
            logger.error("Error parsing Ollama response: {}", e.getMessage());
            return "";
        }
    }
}

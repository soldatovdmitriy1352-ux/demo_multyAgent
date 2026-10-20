package com.example.demo.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Configuration loader for the application.
 * Loads from .env file or application.properties.
 */
public class AppConfig {
    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);

    private final Map<String, String> config = new HashMap<>();

    /**
     * Load configuration from .env file or application.properties.
     */
    public AppConfig() {
        loadFromEnv();
        loadFromProperties();
        logger.info("Configuration loaded: {} keys", config.size());
    }

    /**
     * Load from .env file (using dotenv-java).
     */
    private void loadFromEnv() {
        try {
            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();

            // dotenv-java doesn't expose keys(), so we read known LLM keys
            String[] commonKeys = {
                "LLM_PROVIDER", "LLM_MODEL", "LLM_BASE_URL", "LLM_API_KEY",
                "LLM_TEMPERATURE", "LLM_MAX_TOKENS",
                "ORCHESTRATOR_MAX_ROUNDS"
            };
            int loadedCount = 0;
            for (String key : commonKeys) {
                String value = dotenv.get(key);
                if (value != null && !value.isEmpty()) {
                    // Normalize to lowercase with dots
                    String normalizedKey = key.toLowerCase().replace('_', '.');
                    config.put(normalizedKey, value);
                    loadedCount++;
                }
            }
            logger.debug("Loaded {} variables from .env file", loadedCount);
        } catch (Exception e) {
            logger.warn("Could not load .env file: {}", e.getMessage());
        }
    }

    /**
     * Load from application.properties in classpath.
     */
    private void loadFromProperties() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input != null) {
                Properties properties = new Properties();
                properties.load(input);

                for (String name : properties.stringPropertyNames()) {
                    // Only add if not already set from .env
                    config.putIfAbsent(name, properties.getProperty(name));
                }
                logger.debug("Loaded {} properties from application.properties", properties.size());
            } else {
                logger.debug("application.properties not found in classpath");
            }
        } catch (IOException e) {
            logger.warn("Could not load application.properties: {}", e.getMessage());
        }
    }

    /**
     * Get a configuration value.
     *
     * @param key the configuration key
     * @return the value, or null if not found
     */
    public String get(String key) {
        return config.get(key);
    }

    /**
     * Get a configuration value with a default.
     *
     * @param key          the configuration key
     * @param defaultValue the default value if key not found
     * @return the value, or defaultValue if not found
     */
    public String get(String key, String defaultValue) {
        return config.getOrDefault(key, defaultValue);
    }

    /**
     * Get all configuration keys.
     */
    public java.util.Set<String> keys() {
        return config.keySet();
    }

    /**
     * Check if a key exists.
     */
    public boolean has(String key) {
        return config.containsKey(key);
    }

    /**
     * Get all configuration as a map (read-only).
     */
    public Map<String, String> getAll() {
        return Map.copyOf(config);
    }

    // Convenience methods for common config keys

    /**
     * Get LLM provider.
     */
    public String getLlmProvider() {
        return get("llm.provider", "ollama-cloud");
    }

    /**
     * Get LLM model name.
     */
    public String getLlmModel() {
        return get("llm.model", "bean");
    }

    /**
     * Get LLM base URL.
     */
    public String getLlmBaseUrl() {
        return get("llm.base-url", "https://ollama.com/api");
    }

    /**
     * Get LLM API key.
     */
    public String getLlmApiKey() {
        return get("llm.api-key", "");
    }

    /**
     * Get LLM temperature.
     */
    public double getLlmTemperature() {
        try {
            return Double.parseDouble(get("llm.temperature", "0.7"));
        } catch (NumberFormatException e) {
            return 0.7;
        }
    }

    /**
     * Get LLM max tokens.
     */
    public int getLlmMaxTokens() {
        try {
            return Integer.parseInt(get("llm.max-tokens", "4096"));
        } catch (NumberFormatException e) {
            return 4096;
        }
    }

    /**
     * Get orchestrator max rounds.
     */
    public int getOrchestratorMaxRounds() {
        try {
            return Integer.parseInt(get("orchestrator.max-rounds", "4"));
        } catch (NumberFormatException e) {
            return 4;
        }
    }
}

package com.example.demo.service;

import com.example.demo.config.AppConfig;
import com.example.demo.llm.*;
import com.example.demo.orchestrator.AgentOrchestrator;
import com.example.demo.orchestrator.OrchestratorResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Service for running the agent orchestration in a background thread.
 * Provides callbacks for UI updates.
 */
public class AgentService {
    private static final Logger logger = LoggerFactory.getLogger(AgentService.class);

    private ExecutorService executor;
    private Future<?> currentTask;
    private volatile boolean running = false;
    private final AppConfig appConfig;

    /**
     * Callback interface for UI updates.
     */
    public interface UpdateCallback {
        void onLog(String message);
        void onStatus(String status);
        void onSuccess(OrchestratorResult result);
        void onError(String error);
    }

    /**
     * Initialize the service with a thread pool.
     */
    public AgentService() {
        this.executor = Executors.newSingleThreadExecutor();
        this.appConfig = new AppConfig();
        logger.debug("AgentService initialized");
    }

    /**
     * Run the agent orchestration.
     *
     * @param task       the user's task description
     * @param provider   the LLM provider (unused, for UI compatibility)
     * @param model      the LLM model name (overrides config if not empty)
     * @param baseUrl    the LLM API base URL (overrides config if not empty)
     * @param apiKey     the LLM API key (overrides config if not empty)
     * @param buildDir   the build directory
     * @param callback   callback for UI updates
     */
    public void run(String task, String provider, String model, String baseUrl, 
                    String apiKey, String buildDir, UpdateCallback callback) {
        if (running) {
            callback.onError("Service is already running");
            return;
        }

        running = true;
        callback.onStatus("Starting agent orchestration...");
        logger.info("Starting agent service for task: {}", task.substring(0, Math.min(100, task.length())));

        try {
            // Use UI values if provided, otherwise fall back to config
            String modelValue = (model != null && !model.isEmpty()) ? model : appConfig.getLlmModel();
            String baseUrlValue = (baseUrl != null && !baseUrl.isEmpty()) ? baseUrl : appConfig.getLlmBaseUrl();
            String apiKeyValue = (apiKey != null && !apiKey.isEmpty()) ? apiKey : appConfig.getLlmApiKey();
            double temperature = appConfig.getLlmTemperature();
            int maxTokens = appConfig.getLlmMaxTokens();

            logger.info("Using model: {}, baseUrl: {}", modelValue, baseUrlValue);

            // Create LLM config
            LlmConfig config = new LlmConfig(modelValue, baseUrlValue, apiKeyValue, temperature, maxTokens);

            // Create LLM client
            LlmClient llmClient = new OkHttpLlmClient();
            callback.onLog("LLM client initialized (model: " + modelValue + ")");

            // Create orchestrator
            int maxRounds = appConfig.getOrchestratorMaxRounds();
            AgentOrchestrator orchestrator = new AgentOrchestrator(llmClient, config, maxRounds);
            callback.onLog("Orchestrator initialized with " + orchestrator.getAgents().size() + " agents");

            // Run orchestration
            callback.onStatus("Running agents...");
            OrchestratorResult result = orchestrator.run(task, buildDir);

            // Report results
            if (result.success()) {
                callback.onStatus("Completed successfully!");
                callback.onSuccess(result);
            } else {
                callback.onError(result.errorMessage() != null ? result.errorMessage() : "Unknown error");
            }

        } catch (Exception e) {
            logger.error("Agent service error: {}", e.getMessage(), e);
            callback.onError("Error: " + e.getMessage());
        } finally {
            running = false;
            callback.onStatus("Ready");
        }
    }

    /**
     * Stop the current task.
     */
    public void stop() {
        if (currentTask != null) {
            currentTask.cancel(true);
            logger.info("Agent service stopped");
        }
        running = false;
    }

    /**
     * Check if the service is currently running.
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Shutdown the service.
     */
    public void shutdown() {
        stop();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        logger.debug("AgentService shut down");
    }
}

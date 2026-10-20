package com.example.demo.agent;

import com.example.demo.llm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for all agents.
 * Provides common functionality like chat, history management, etc.
 */
public abstract class BaseAgent implements Agent {
    protected static final Logger logger = LoggerFactory.getLogger(BaseAgent.class);

    protected final String name;
    protected final String systemMessage;
    protected final LlmClient llmClient;
    protected final LlmConfig config;
    protected final List<Message> history = new ArrayList<>();

    /**
     * Create a new BaseAgent.
     *
     * @param name           the name of the agent
     * @param systemMessage  the system message that defines the agent's role
     * @param llmClient      the LLM client to use for API calls
     * @param config         the LLM configuration
     */
    protected BaseAgent(String name, String systemMessage, LlmClient llmClient, LlmConfig config) {
        this.name = name;
        this.systemMessage = systemMessage;
        this.llmClient = llmClient;
        this.config = config;
        // Add system message to history
        history.add(new Message("system", systemMessage));
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public LlmResponse chat(String userMessage) {
        // Add user message to history
        history.add(new Message("user", userMessage));
        logger.debug("{}: Received message: {}", name, userMessage.substring(0, Math.min(100, userMessage.length())));

        // Call LLM
        LlmResponse response = llmClient.chat(new ArrayList<>(history), config);

        if (response.success()) {
            // Add assistant response to history
            history.add(new Message("assistant", response.content()));
            logger.debug("{}: Response received: {} chars", name, response.content().length());
        } else {
            logger.error("{}: LLM error: {}", name, response.content());
        }

        return response;
    }

    @Override
    public void resetHistory() {
        history.clear();
        history.add(new Message("system", systemMessage));
        logger.debug("{}: History reset", name);
    }

    @Override
    public List<Message> getHistory() {
        return List.copyOf(history);
    }
}

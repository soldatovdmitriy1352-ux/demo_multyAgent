package com.example.demo.agent;

import com.example.demo.llm.LlmResponse;
import com.example.demo.llm.Message;

import java.util.List;

/**
 * Interface for all agents in the multi-agent system.
 */
public interface Agent {
    /**
     * Get the name of the agent.
     */
    String getName();

    /**
     * Send a message to the agent and get a response.
     *
     * @param userMessage the message from the user/orchestrator
     * @return the agent's response
     */
    LlmResponse chat(String userMessage);

    /**
     * Reset the conversation history.
     */
    void resetHistory();

    /**
     * Get the conversation history.
     */
    List<Message> getHistory();
}

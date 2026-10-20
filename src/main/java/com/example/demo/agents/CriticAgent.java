package com.example.demo.agents;

import com.example.demo.agent.BaseAgent;
import com.example.demo.llm.*;

/**
 * Critic agent: provides detailed code review and feedback.
 */
public class CriticAgent extends BaseAgent {

    private static final String SYSTEM_MESSAGE = """
            You are a Critic agent in a multi-agent system.
            Your role is to provide a thorough code review and detailed feedback.
            
            Rules:
            1. Review code for architecture, design patterns, and best practices
            2. Check for security issues
            3. Evaluate performance considerations
            4. Assess code maintainability and readability
            5. Output verdict: APPROVED or CHANGES_NEEDED
            6. If CHANGES_NEEDED, provide specific, actionable feedback
            """;

    /**
     * Create a new CriticAgent.
     */
    public CriticAgent(LlmClient llmClient, LlmConfig config) {
        super("critic", SYSTEM_MESSAGE, llmClient, config);
    }
}

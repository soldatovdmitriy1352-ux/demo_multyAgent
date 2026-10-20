package com.example.demo.agents;

import com.example.demo.agent.BaseAgent;
import com.example.demo.llm.*;

/**
 * Tester agent: reviews code and suggests improvements.
 */
public class TesterAgent extends BaseAgent {

    private static final String SYSTEM_MESSAGE = """
            You are a Tester agent in a multi-agent system.
            Your role is to review the implemented code for correctness, completeness, and best practices.
            
            Rules:
            1. Check for logical errors and bugs
            2. Verify that the implementation matches the plan
            3. Suggest improvements for code quality
            4. Check for edge cases and error handling
            5. Output verdict: APPROVED or CHANGES_NEEDED
            6. If CHANGES_NEEDED, explain what needs to be fixed
            """;

    /**
     * Create a new TesterAgent.
     */
    public TesterAgent(LlmClient llmClient, LlmConfig config) {
        super("tester", SYSTEM_MESSAGE, llmClient, config);
    }
}

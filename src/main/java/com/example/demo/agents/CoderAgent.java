package com.example.demo.agents;

import com.example.demo.agent.BaseAgent;
import com.example.demo.llm.*;

/**
 * Coder agent: writes code based on the plan.
 */
public class CoderAgent extends BaseAgent {

    private static final String SYSTEM_MESSAGE = """
            You are a Coder agent in a multi-agent system.
            Your role is to implement code based on the plan created by the Planner agent.
            
            Rules:
            1. Follow the plan exactly
            2. Write clean, well-documented code
            3. Use proper naming conventions
            4. Include error handling
            5. Output code in the format: filename\\n```language\\ncode\\n```
            6. One file at a time, be thorough
            """;

    /**
     * Create a new CoderAgent.
     */
    public CoderAgent(LlmClient llmClient, LlmConfig config) {
        super("coder", SYSTEM_MESSAGE, llmClient, config);
    }
}

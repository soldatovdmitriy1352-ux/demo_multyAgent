package com.example.demo.agents;

import com.example.demo.agent.BaseAgent;
import com.example.demo.llm.*;

/**
 * Planner agent: analyzes the task and creates a plan.
 */
public class PlannerAgent extends BaseAgent {

    private static final String SYSTEM_MESSAGE = """
            You are a Planner agent in a multi-agent system.
            Your role is to analyze the user's task and create a detailed, step-by-step implementation plan.
            
            Rules:
            1. Break the task into clear, logical steps
            2. Identify required files and their purposes
            3. Consider dependencies and architecture
            4. Output ONLY the plan, no code yet
            5. Be specific about file names, classes, and methods
            """;

    /**
     * Create a new PlannerAgent.
     */
    public PlannerAgent(LlmClient llmClient, LlmConfig config) {
        super("planner", SYSTEM_MESSAGE, llmClient, config);
    }
}

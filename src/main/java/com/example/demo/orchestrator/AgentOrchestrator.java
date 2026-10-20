package com.example.demo.orchestrator;

import com.example.demo.agent.Agent;
import com.example.demo.agents.*;
import com.example.demo.llm.*;
import com.example.demo.util.FileParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Orchestrator that manages the multi-agent workflow.
 * Analog of GroupChat in Python implementation.
 */
public class AgentOrchestrator {
    private static final Logger logger = LoggerFactory.getLogger(AgentOrchestrator.class);

    private final List<Agent> agents;
    private final Map<String, Agent> agentMap = new LinkedHashMap<>();
    private final List<String> agentOrder = List.of("planner", "coder", "tester", "critic", "builder");
    private final List<Verdict> verdicts = new ArrayList<>();
    private final FileParser fileParser;
    private final ObjectMapper objectMapper;
    private String plan = "";
    private int maxRounds;

    /**
     * Create an AgentOrchestrator with all agents.
     */
    public AgentOrchestrator(LlmClient llmClient, LlmConfig config, int maxRounds) {
        this.maxRounds = maxRounds;
        this.fileParser = new FileParser();
        this.objectMapper = new ObjectMapper();

        // Create all agents
        PlannerAgent planner = new PlannerAgent(llmClient, config);
        CoderAgent coder = new CoderAgent(llmClient, config);
        TesterAgent tester = new TesterAgent(llmClient, config);
        CriticAgent critic = new CriticAgent(llmClient, config);
        BuilderAgent builder = new BuilderAgent(llmClient, config);

        this.agents = List.of(planner, coder, tester, critic, builder);

        // Build map for easy lookup
        for (Agent agent : agents) {
            agentMap.put(agent.getName().toLowerCase(), agent);
        }

        logger.info("Orchestrator initialized with {} agents", agents.size());
    }

    /**
     * Run the orchestrator with the given task and build directory.
     *
     * @param task       the user's task description
     * @param buildDir   the directory where files will be created
     * @return the result of the orchestration
     */
    public OrchestratorResult run(String task, String buildDir) {
        logger.info("Starting orchestration for task: {}", task.substring(0, Math.min(100, task.length())));
        verdicts.clear();

        try {
            // Round 1: Planner creates a plan
            Agent planner = agentMap.get("planner");
            if (planner != null) {
                String planPrompt = String.format(
                        "Create a detailed implementation plan for this task:\n\n%s\n\n" +
                        "Break it down into clear steps, identify required files, and specify the architecture.",
                        task
                );
                var response = planner.chat(planPrompt);
                if (response.success()) {
                    plan = response.content();
                    logger.info("Planner created plan: {} chars", plan.length());
                } else {
                    return OrchestratorResult.failure("Planner failed: " + response.content());
                }
            }

            // Round 2: Coder generates code based on plan
            Agent coder = agentMap.get("coder");
            if (coder != null) {
                String codePrompt = String.format(
                        "Implement the following plan:\n\n%s\n\nTask: %s\n\nGenerate the code for all required files.",
                        plan, task
                );
                var response = coder.chat(codePrompt);
                if (!response.success()) {
                    return OrchestratorResult.failure("Coder failed: " + response.content());
                }

                // Round 3: Tester reviews the code
                Agent tester = agentMap.get("tester");
                if (tester != null) {
                    String testPrompt = String.format(
                            "Review this code implementation:\n\n%s\n\nPlan:\n%s\n\n" +
                            "Check for correctness, completeness, and best practices.",
                            response.content(), plan
                    );
                    var testResponse = tester.chat(testPrompt);
                    Verdict testerVerdict = parseVerdict(tester.getName(), testResponse.content());
                    verdicts.add(testerVerdict);

                    if (testerVerdict.status() == Verdict.Status.CHANGES_NEEDED) {
                        logger.info("Tester requested changes: {}", testerVerdict.feedback());
                        // Coder revises based on feedback
                        String revisePrompt = String.format(
                                "Revise the code based on this feedback:\n\n%s\n\nOriginal code:\n%s",
                                testerVerdict.feedback(), response.content()
                        );
                        response = coder.chat(revisePrompt);
                    }
                }

                // Round 4: Critic provides detailed review
                Agent critic = agentMap.get("critic");
                if (critic != null) {
                    String criticPrompt = String.format(
                            "Provide a detailed code review:\n\n%s\n\nPlan:\n%s",
                            response.content(), plan
                    );
                    var criticResponse = critic.chat(criticPrompt);
                    Verdict criticVerdict = parseVerdict(critic.getName(), criticResponse.content());
                    verdicts.add(criticVerdict);

                    if (criticVerdict.status() == Verdict.Status.CHANGES_NEEDED) {
                        logger.info("Critic requested changes: {}", criticVerdict.feedback());
                    }
                }

                // Round 5: Builder writes files
                Agent builder = agentMap.get("builder");
                if (builder != null && builder instanceof com.example.demo.agent.FileAgent fileAgent) {
                    String buildPrompt = String.format(
                            "Write the following code to files in directory: %s\n\n%s",
                            buildDir, response.content()
                    );
                    var buildResponse = builder.chat(buildPrompt);

                    // Parse and write files
                    List<FileParser.FileEntry> files = fileParser.parseResponse(buildResponse.content());
                    int writtenCount = 0;
                    List<String> errors = new ArrayList<>();

                    for (FileParser.FileEntry entry : files) {
                        String fullPath = Paths.get(buildDir, entry.path()).toString();
                        if (fileAgent.writeFile(fullPath, entry.code())) {
                            writtenCount++;
                        } else {
                            errors.add("Failed to write: " + entry.path());
                        }
                    }

                    logger.info("Builder wrote {} files, {} errors", writtenCount, errors.size());

                    // Save plan to JSON
                    savePlanToJson(plan, task, writtenCount, errors);

                    BuildInfo buildInfo = new BuildInfo(
                            buildDir,
                            ((com.example.demo.agent.FileAgent) builder).getCreatedFiles(),
                            errors,
                            maxRounds,
                            errors.isEmpty()
                    );

                    return OrchestratorResult.success(plan, buildInfo, verdicts);
                }
            }

            return OrchestratorResult.failure("Orchestration completed without generating code");

        } catch (Exception e) {
            logger.error("Orchestration error: {}", e.getMessage(), e);
            return OrchestratorResult.failure("Error: " + e.getMessage());
        }
    }

    /**
     * Parse a verdict from agent response text.
     */
    private Verdict parseVerdict(String agentName, String response) {
        String lowerResponse = response.toLowerCase();
        Verdict.Status status;

        if (lowerResponse.contains("terminate") || lowerResponse.contains("approved") || lowerResponse.contains("complete")) {
            status = Verdict.Status.APPROVED;
        } else if (lowerResponse.contains("changes") || lowerResponse.contains("fix") || lowerResponse.contains("improve")) {
            status = Verdict.Status.CHANGES_NEEDED;
        } else {
            status = Verdict.Status.APPROVED;
        }

        return new Verdict(agentName, status, response);
    }

    /**
     * Save the plan to a JSON file.
     */
    private void savePlanToJson(String plan, String task, int filesWritten, List<String> errors) {
        try {
            ObjectNode planNode = objectMapper.createObjectNode();
            planNode.put("task", task);
            planNode.put("plan", plan);
            planNode.put("filesWritten", filesWritten);

            ArrayNode errorsNode = planNode.putArray("errors");
            for (String error : errors) {
                errorsNode.add(error);
            }

            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(planNode);
            String planPath = "plan.json";
            Files.writeString(Paths.get(planPath), json);
            logger.info("Plan saved to {}", planPath);
        } catch (IOException e) {
            logger.error("Failed to save plan: {}", e.getMessage());
        }
    }

    /**
     * Get the list of all agents.
     */
    public List<Agent> getAgents() {
        return agents;
    }

    /**
     * Get the agent order.
     */
    public List<String> getAgentOrder() {
        return agentOrder;
    }

    /**
     * Get the generated plan.
     */
    public String getPlan() {
        return plan;
    }

    /**
     * Get all verdicts.
     */
    public List<Verdict> getVerdicts() {
        return List.copyOf(verdicts);
    }

    /**
     * Get an agent by name.
     */
    public Agent getAgent(String name) {
        return agentMap.get(name.toLowerCase());
    }
}

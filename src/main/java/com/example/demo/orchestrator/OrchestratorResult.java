package com.example.demo.orchestrator;

import java.util.List;

/**
 * Result from the orchestrator run.
 */
public record OrchestratorResult(
    boolean success,
    String plan,
    BuildInfo buildInfo,
    List<Verdict> verdicts,
    String errorMessage
) {
    public OrchestratorResult {
        if (verdicts == null) verdicts = List.of();
        if (errorMessage == null) errorMessage = "";
    }

    /**
     * Create a successful result.
     */
    public static OrchestratorResult success(String plan, BuildInfo buildInfo, List<Verdict> verdicts) {
        return new OrchestratorResult(true, plan, buildInfo, verdicts, null);
    }

    /**
     * Create a failed result.
     */
    public static OrchestratorResult failure(String errorMessage) {
        return new OrchestratorResult(false, null, null, List.of(), errorMessage);
    }
}

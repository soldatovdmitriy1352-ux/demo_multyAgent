package com.example.demo.orchestrator;

/**
 * Verdict from an agent after reviewing the work.
 */
public record Verdict(
    String agentName,
    Status status,
    String feedback
) {
    public Verdict {
        if (agentName == null || agentName.isEmpty()) {
            throw new IllegalArgumentException("Agent name cannot be null or empty");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
    }

    /**
     * Possible verdict statuses.
     */
    public enum Status {
        APPROVED,
        CHANGES_NEEDED,
        TERMINATE
    }
}

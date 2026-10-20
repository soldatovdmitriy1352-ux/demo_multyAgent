package com.example.demo.orchestrator;

import java.util.List;

/**
 * Information about the build process.
 */
public record BuildInfo(
    String buildDirectory,
    List<String> createdFiles,
    List<String> errors,
    int totalRounds,
    boolean success
) {
    public BuildInfo {
        if (buildDirectory == null || buildDirectory.isEmpty()) {
            throw new IllegalArgumentException("Build directory cannot be null or empty");
        }
        if (createdFiles == null) createdFiles = List.of();
        if (errors == null) errors = List.of();
    }
}

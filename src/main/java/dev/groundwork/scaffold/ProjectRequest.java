package dev.groundwork.scaffold;

import java.nio.file.Path;
import java.util.Map;

public record ProjectRequest(
        String name,
        String description,
        Path destination,
        Map<String, String> variables,
        boolean initializeGit
) {
    public ProjectRequest {
        variables = Map.copyOf(variables);
    }
}

package dev.groundwork.template;

import java.util.Map;

public record ClaudeMdSpec(String template, Map<String, String> variables) {
    public ClaudeMdSpec {
        variables = Map.copyOf(variables);
    }
}

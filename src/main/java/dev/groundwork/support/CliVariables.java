package dev.groundwork.support;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CliVariables {
    private CliVariables() {
    }

    public static Map<String, String> parse(List<String> rawVariables) {
        Map<String, String> variables = new LinkedHashMap<>();
        for (String rawVariable : rawVariables) {
            int separator = rawVariable.indexOf('=');
            if (separator <= 0 || separator == rawVariable.length() - 1) {
                throw new IllegalArgumentException("Template variables must use key=value format: " + rawVariable);
            }
            String key = rawVariable.substring(0, separator).trim();
            String value = rawVariable.substring(separator + 1).trim();
            if (key.isBlank() || value.isBlank()) {
                throw new IllegalArgumentException("Template variables must use non-blank key=value format: " + rawVariable);
            }
            variables.put(key, value);
        }
        return Map.copyOf(variables);
    }
}

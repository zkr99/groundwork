package dev.groundwork.template;

import java.util.List;

public record TemplateValidationResult(List<String> issues) {
    public TemplateValidationResult {
        issues = List.copyOf(issues);
    }

    public boolean isValid() {
        return issues.isEmpty();
    }

    public String render(String header) {
        StringBuilder builder = new StringBuilder(header).append(System.lineSeparator());
        for (String issue : issues) {
            builder.append("- ").append(issue).append(System.lineSeparator());
        }
        return builder.toString().trim();
    }
}

package dev.groundwork.generate;

import dev.groundwork.scaffold.ProjectRequest;
import dev.groundwork.template.ReferenceSpec;
import dev.groundwork.template.Template;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class RenderContextFactory {
    public Map<String, Object> create(Template template, ProjectRequest request) {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("name", request.name());
        context.put("project_name", request.name());
        context.put("description", request.description());
        context.put("template_name", template.name());
        context.put("template_description", template.description());
        context.put("references_count", template.references().size());
        context.put("references_table", referencesTable(template.references()));
        context.put("references_bullets", referencesBullets(template.references()));
        context.put("directories_bullets", directoriesBullets(template.directories()));
        context.putAll(request.variables());
        return context;
    }

    private String referencesTable(List<ReferenceSpec> references) {
        if (references.isEmpty()) {
            return "No reference repositories configured.";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("| Reference | Purpose |").append(System.lineSeparator());
        builder.append("|-----------|---------|").append(System.lineSeparator());
        for (ReferenceSpec reference : references) {
            builder.append("| ")
                    .append(reference.targetDirectoryName())
                    .append(" | ")
                    .append(reference.readFor().isBlank() ? "Reference patterns" : reference.readFor())
                    .append(" |")
                    .append(System.lineSeparator());
        }
        return builder.toString().trim();
    }

    private String referencesBullets(List<ReferenceSpec> references) {
        if (references.isEmpty()) {
            return "- No reference repositories configured.";
        }
        return references.stream()
                .map(reference -> "- `" + reference.targetDirectoryName() + "`"
                        + (reference.readFor().isBlank() ? "" : " for " + reference.readFor()))
                .collect(Collectors.joining(System.lineSeparator()));
    }

    private String directoriesBullets(List<String> directories) {
        if (directories.isEmpty()) {
            return "- No starter directories configured.";
        }
        return directories.stream()
                .map(directory -> "- `" + directory + "`")
                .collect(Collectors.joining(System.lineSeparator()));
    }
}

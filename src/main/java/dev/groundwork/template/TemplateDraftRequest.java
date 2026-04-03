package dev.groundwork.template;

import java.nio.file.Path;
import java.util.List;

public record TemplateDraftRequest(
        String name,
        String description,
        List<TemplateDraftReference> references,
        List<String> stripPatterns,
        String claudeTemplate,
        boolean generateReadme,
        boolean generateGitignore,
        String gitignoreProfile,
        List<String> directories,
        Path blueprint
) {
    public TemplateDraftRequest {
        references = List.copyOf(references);
        stripPatterns = List.copyOf(stripPatterns);
        directories = List.copyOf(directories);
    }
}

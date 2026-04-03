package dev.groundwork.template;

import dev.groundwork.config.GroundworkPaths;
import dev.groundwork.support.ClasspathResources;
import java.nio.file.InvalidPathException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class TemplateValidator {
    private final GroundworkPaths paths;
    private final ClasspathResources resources;

    public TemplateValidator(GroundworkPaths paths, ClasspathResources resources) {
        this.paths = paths;
        this.resources = resources;
    }

    public TemplateValidationResult validate(Template template) {
        List<String> issues = new ArrayList<>();

        if (template.name() == null || template.name().isBlank()) {
            issues.add("Template name must not be blank.");
        }
        if (template.description() == null || template.description().isBlank()) {
            issues.add("Template description must not be blank.");
        }

        Set<String> referenceDirectoryNames = new HashSet<>();
        for (ReferenceSpec reference : template.references()) {
            if (!Files.exists(reference.repo())) {
                issues.add("Reference repo does not exist: " + reference.repo());
                continue;
            }
            if (!Files.isDirectory(reference.repo())) {
                issues.add("Reference repo is not a directory: " + reference.repo());
            }
            if (!referenceDirectoryNames.add(reference.targetDirectoryName())) {
                issues.add("Reference target directory name is duplicated: " + reference.targetDirectoryName());
            }
        }

        for (String stripPattern : template.stripPatterns()) {
            if (stripPattern == null || stripPattern.isBlank()) {
                issues.add("Strip patterns must not contain blank values.");
                continue;
            }
            if (looksLikeGlob(stripPattern)) {
                try {
                    FileSystems.getDefault().getPathMatcher("glob:" + stripPattern);
                } catch (IllegalArgumentException exception) {
                    issues.add("Invalid strip glob pattern `" + stripPattern + "`: " + exception.getMessage());
                }
            }
        }

        if (template.claudeMd() != null) {
            String claudeTemplate = template.claudeMd().template();
            if (claudeTemplate == null || claudeTemplate.isBlank()) {
                issues.add("claude_md.template must not be blank.");
            } else if (looksLikePath(claudeTemplate)) {
                Path resolvedClaudeTemplate = paths.resolvePath(claudeTemplate, template.sourceFile().getParent());
                if (!Files.exists(resolvedClaudeTemplate)) {
                    issues.add("CLAUDE template file does not exist: " + resolvedClaudeTemplate);
                }
            } else if (!resources.exists("claude-templates/" + claudeTemplate + ".md")) {
                issues.add("Unknown built-in CLAUDE template: " + claudeTemplate);
            }
        }

        if (template.generate().gitignore()) {
            String profile = template.generate().gitignoreProfile();
            if (!resources.exists("defaults/gitignore-" + profile + ".txt")) {
                issues.add("Unknown .gitignore profile: " + profile);
            }
        }

        if (template.blueprint() != null) {
            if (!Files.exists(template.blueprint())) {
                issues.add("Blueprint file does not exist: " + template.blueprint());
            } else if (!Files.isRegularFile(template.blueprint())) {
                issues.add("Blueprint path is not a file: " + template.blueprint());
            }
        }

        for (String directory : template.directories()) {
            if (directory == null || directory.isBlank()) {
                issues.add("Directories list must not contain blank values.");
                continue;
            }
            try {
                Path normalized = Path.of(directory).normalize();
                if (normalized.isAbsolute()) {
                    issues.add("Directory entries must be relative, not absolute: " + directory);
                } else if (normalized.startsWith("..") || "..".equals(normalized.toString())) {
                    issues.add("Directory entries must stay within the scaffold root: " + directory);
                }
            } catch (InvalidPathException exception) {
                issues.add("Invalid directory entry `" + directory + "`: " + exception.getMessage());
            }
        }

        return new TemplateValidationResult(issues);
    }

    private boolean looksLikeGlob(String value) {
        return value.contains("*") || value.contains("?") || value.contains("[") || value.contains("{");
    }

    private boolean looksLikePath(String value) {
        return value.contains("/") || value.contains("\\") || value.startsWith("~") || value.endsWith(".md");
    }
}

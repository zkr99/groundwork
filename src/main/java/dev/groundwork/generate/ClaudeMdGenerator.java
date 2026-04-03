package dev.groundwork.generate;

import dev.groundwork.config.GroundworkPaths;
import dev.groundwork.scaffold.ProjectRequest;
import dev.groundwork.support.ClasspathResources;
import dev.groundwork.template.ClaudeMdSpec;
import dev.groundwork.template.Template;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ClaudeMdGenerator {
    private final GroundworkPaths paths;
    private final TemplateRenderer renderer;
    private final RenderContextFactory contextFactory;
    private final ClasspathResources resources;

    public ClaudeMdGenerator(
            GroundworkPaths paths,
            TemplateRenderer renderer,
            RenderContextFactory contextFactory,
            ClasspathResources resources
    ) {
        this.paths = paths;
        this.renderer = renderer;
        this.contextFactory = contextFactory;
        this.resources = resources;
    }

    public String generate(Template template, ProjectRequest request) throws IOException {
        ClaudeMdSpec claudeMd = template.claudeMd();
        if (claudeMd == null) {
            throw new IllegalArgumentException("Template does not define claude_md generation.");
        }

        String templateText = resolveTemplate(template, claudeMd.template());
        Map<String, Object> context = new LinkedHashMap<>(contextFactory.create(template, request));
        Map<String, String> resolvedVariables = resolveVariables(claudeMd.variables(), context);
        context.putAll(resolvedVariables);
        return renderer.render(templateText, context);
    }

    private String resolveTemplate(Template template, String templateReference) throws IOException {
        if (looksLikePath(templateReference)) {
            Path path = paths.resolvePath(templateReference, template.sourceFile().getParent());
            return Files.readString(path);
        }
        return resources.readText("claude-templates/" + templateReference + ".md");
    }

    private Map<String, String> resolveVariables(Map<String, String> variables, Map<String, Object> baseContext) {
        Map<String, String> resolved = new LinkedHashMap<>();
        Map<String, Object> workingContext = new LinkedHashMap<>(baseContext);
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String value = renderer.render(entry.getValue(), workingContext);
            resolved.put(entry.getKey(), value);
            workingContext.put(entry.getKey(), value);
        }
        return resolved;
    }

    private boolean looksLikePath(String value) {
        return value.contains("/") || value.contains("\\") || value.startsWith("~") || value.endsWith(".md");
    }
}

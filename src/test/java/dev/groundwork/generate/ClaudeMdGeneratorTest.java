package dev.groundwork.generate;

import dev.groundwork.config.GroundworkPaths;
import dev.groundwork.scaffold.ProjectRequest;
import dev.groundwork.support.ClasspathResources;
import dev.groundwork.template.ClaudeMdSpec;
import dev.groundwork.template.GenerateSpec;
import dev.groundwork.template.Template;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ClaudeMdGeneratorTest {
    @TempDir
    Path tempDir;

    @Test
    void resolvesCustomClaudeTemplatesRelativeToTheTemplateFile() throws Exception {
        Path templateDirectory = Files.createDirectories(tempDir.resolve("templates"));
        Path customClaude = templateDirectory.resolve("claude/custom-guide.md");
        Files.createDirectories(customClaude.getParent());
        Files.writeString(customClaude, "# {{project_name}}\n{{description}}\n");

        Template template = new Template(
                templateDirectory.resolve("camera-ai-app.yml"),
                "camera-ai-app",
                "Camera to AI app",
                1,
                List.of(),
                List.of(),
                new ClaudeMdSpec("claude/custom-guide.md", Map.of()),
                GenerateSpec.defaults(),
                List.of(),
                null
        );

        ClaudeMdGenerator generator = new ClaudeMdGenerator(
                new GroundworkPaths(),
                new TemplateRenderer(),
                new RenderContextFactory(),
                new ClasspathResources()
        );

        String rendered = generator.generate(
                template,
                new ProjectRequest("PlantSnap", "AI plant species identifier", tempDir.resolve("PlantSnap"), Map.of(), false)
        );

        assertTrue(rendered.contains("PlantSnap"));
        assertTrue(rendered.contains("AI plant species identifier"));
    }
}

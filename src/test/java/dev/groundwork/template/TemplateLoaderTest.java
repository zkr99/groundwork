package dev.groundwork.template;

import dev.groundwork.config.GroundworkPaths;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TemplateLoaderTest {
    @TempDir
    Path tempDir;

    @Test
    void loadsTemplateWithReferencesAndGenerateSettings() throws Exception {
        Path repo = Files.createDirectories(tempDir.resolve("core-agent"));
        Path templateFile = tempDir.resolve("camera-ai-app.yml");
        Files.writeString(templateFile, """
                name: camera-ai-app
                description: "Camera to AI app"
                version: 1
                references:
                  - repo: %s
                    read_for: "Vision patterns"
                strip:
                  - .git
                  - "*.pyc"
                claude_md:
                  template: python-fastapi
                  variables:
                    project_name: "{{name}}"
                generate:
                  - README.md
                  - .gitignore: python-nextjs
                directories:
                  - backend/
                  - docs/
                """.formatted(repo.toAbsolutePath()));

        Template template = new TemplateLoader(new GroundworkPaths()).load(templateFile);

        assertEquals("camera-ai-app", template.name());
        assertEquals("Camera to AI app", template.description());
        assertEquals(1, template.references().size());
        assertEquals(repo.toAbsolutePath(), template.references().getFirst().repo());
        assertEquals("python-nextjs", template.generate().gitignoreProfile());
        assertEquals(2, template.directories().size());
        assertNotNull(template.claudeMd());
    }

    @Test
    void resolvesRelativePathsAgainstTheTemplateDirectory() throws Exception {
        Path templateDirectory = Files.createDirectories(tempDir.resolve("templates"));
        Path repo = Files.createDirectories(templateDirectory.resolve("references/core-agent"));
        Path blueprint = templateDirectory.resolve("docs/BLUEPRINT.md");
        Files.createDirectories(blueprint.getParent());
        Files.writeString(blueprint, "# Blueprint");
        Path templateFile = templateDirectory.resolve("camera-ai-app.yml");

        Files.writeString(templateFile, """
                name: camera-ai-app
                description: "Camera to AI app"
                references:
                  - repo: references/core-agent
                blueprint: docs/BLUEPRINT.md
                """);

        Template template = new TemplateLoader(new GroundworkPaths()).load(templateFile);

        assertEquals(repo.toAbsolutePath(), template.references().getFirst().repo());
        assertEquals(blueprint.toAbsolutePath(), template.blueprint());
    }

    @Test
    void rejectsUnsupportedGenerateEntries() throws Exception {
        Path repo = Files.createDirectories(tempDir.resolve("core-agent"));
        Path templateFile = tempDir.resolve("camera-ai-app.yml");
        Files.writeString(templateFile, """
                name: camera-ai-app
                description: "Camera to AI app"
                references:
                  - repo: %s
                generate:
                  - FOO.md
                """.formatted(repo.toAbsolutePath()));

        assertThrows(IllegalArgumentException.class, () -> new TemplateLoader(new GroundworkPaths()).load(templateFile));
    }
}

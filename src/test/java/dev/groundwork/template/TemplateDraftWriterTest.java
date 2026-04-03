package dev.groundwork.template;

import dev.groundwork.config.GroundworkPaths;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TemplateDraftWriterTest {
    @TempDir
    Path tempDir;

    @Test
    void writesTemplateYamlThatRoundTripsThroughTheLoader() throws Exception {
        Path referenceRepo = Files.createDirectories(tempDir.resolve("reference-app"));
        Path blueprint = tempDir.resolve("docs/BLUEPRINT.md");
        Files.createDirectories(blueprint.getParent());
        Files.writeString(blueprint, "# Blueprint");
        Path output = tempDir.resolve("templates/smoke-template.yml");

        TemplateDraftRequest request = new TemplateDraftRequest(
                "smoke-template",
                "Smoke template",
                List.of(new TemplateDraftReference(referenceRepo, "Reference patterns")),
                List.of(".turbo"),
                "python-fastapi",
                true,
                true,
                "python-nextjs",
                List.of("app/", "docs/"),
                blueprint
        );

        new TemplateDraftWriter().write(output, request, false);

        Template template = new TemplateLoader(new GroundworkPaths()).load(output);
        assertEquals("smoke-template", template.name());
        assertEquals(referenceRepo.toAbsolutePath(), template.references().getFirst().repo());
        assertEquals("Reference patterns", template.references().getFirst().readFor());
        assertEquals(".turbo", template.stripPatterns().getFirst());
        assertEquals("python-fastapi", template.claudeMd().template());
        assertEquals("python-nextjs", template.generate().gitignoreProfile());
        assertEquals(blueprint.toAbsolutePath(), template.blueprint());
        assertTrue(template.directories().contains("app/"));
    }
}

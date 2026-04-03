package dev.groundwork.template;

import dev.groundwork.config.GroundworkPaths;
import dev.groundwork.support.ClasspathResources;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TemplateValidatorTest {
    @TempDir
    Path tempDir;

    @Test
    void rejectsDirectoriesThatEscapeTheProjectRoot() throws Exception {
        Path repo = Files.createDirectories(tempDir.resolve("core-agent"));
        Template template = new Template(
                tempDir.resolve("template.yml"),
                "camera-ai-app",
                "Camera to AI app",
                1,
                List.of(new ReferenceSpec(repo, "Vision patterns")),
                List.of(),
                new ClaudeMdSpec("python-fastapi", Map.of()),
                new GenerateSpec(true, true, "general"),
                List.of("../outside", "/absolute/path"),
                null
        );

        TemplateValidationResult result = new TemplateValidator(new GroundworkPaths(), new ClasspathResources()).validate(template);

        assertFalse(result.isValid());
        assertTrue(result.issues().stream().anyMatch(issue -> issue.contains("stay within the scaffold root")));
        assertTrue(result.issues().stream().anyMatch(issue -> issue.contains("must be relative")));
    }
}

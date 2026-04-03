package dev.groundwork.commands;

import dev.groundwork.config.GroundworkPaths;
import dev.groundwork.template.Template;
import dev.groundwork.template.TemplateLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TemplateCreateCommandTest {
    @TempDir
    Path tempDir;

    @Test
    void createsATemplateFileWithoutManualYamlAuthoring() throws Exception {
        Path referenceRepo = Files.createDirectories(tempDir.resolve("reference-app"));
        Path outputFile = tempDir.resolve("templates/generated-template.yml");

        int exitCode = new CommandLine(new TemplateCreateCommand()).execute(
                "camera-ai-app",
                "--description", "Camera to AI template",
                "--reference", referenceRepo.toString() + "::Vision patterns",
                "--dir", "backend/",
                "--dir", "mobile/",
                "--strip", ".turbo",
                "--claude-template", "python-fastapi",
                "--gitignore-profile", "python-nextjs",
                "--output", outputFile.toString()
        );

        assertEquals(0, exitCode);

        Template template = new TemplateLoader(new GroundworkPaths()).load(outputFile);
        assertEquals("camera-ai-app", template.name());
        assertEquals(1, template.references().size());
        assertEquals("Vision patterns", template.references().getFirst().readFor());
        assertEquals("python-fastapi", template.claudeMd().template());
        assertEquals("python-nextjs", template.generate().gitignoreProfile());
    }

    @Test
    void writesRelativePathsWhenExplicitOutputIsUsed() throws Exception {
        Path referenceRepo = Files.createDirectories(tempDir.resolve("reference-app"));
        Path outputFile = tempDir.resolve("templates/generated-template.yml");

        int exitCode = new CommandLine(new TemplateCreateCommand()).execute(
                "camera-ai-app",
                "--description", "Camera to AI template",
                "--reference", referenceRepo.toString() + "::Vision patterns",
                "--output", outputFile.toString()
        );

        assertEquals(0, exitCode);

        String yaml = Files.readString(outputFile);
        assertTrue(yaml.contains("../reference-app"));
    }

    @Test
    void doesNotLeaveBehindAnInvalidTemplateFile() throws Exception {
        Path referenceRepo = Files.createDirectories(tempDir.resolve("reference-app"));
        Path outputFile = tempDir.resolve("templates/generated-template.yml");

        int exitCode = new CommandLine(new TemplateCreateCommand()).execute(
                "camera-ai-app",
                "--description", "Camera to AI template",
                "--reference", referenceRepo.toString() + "::Vision patterns",
                "--gitignore-profile", "missing-profile",
                "--output", outputFile.toString()
        );

        assertEquals(1, exitCode);
        assertFalse(Files.exists(outputFile));
    }
}

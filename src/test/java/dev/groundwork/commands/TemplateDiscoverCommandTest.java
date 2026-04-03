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

class TemplateDiscoverCommandTest {
    @TempDir
    Path tempDir;

    @Test
    void createsATemplateFromGitReposInAWorkspace() throws Exception {
        Path workspace = Files.createDirectories(tempDir.resolve("camera-stack"));
        Files.createDirectories(workspace.resolve("core-agent/.git"));
        Files.createDirectories(workspace.resolve("mobile-app/.git"));
        Files.createDirectories(workspace.resolve("notes"));

        Path outputFile = tempDir.resolve("templates/discovered-template.yml");

        int exitCode = new CommandLine(new TemplateDiscoverCommand()).execute(
                "camera-ai-app",
                "--from", workspace.toString(),
                "--description", "Camera to AI template",
                "--dir", "backend/",
                "--output", outputFile.toString()
        );

        assertEquals(0, exitCode);

        Template template = new TemplateLoader(new GroundworkPaths()).load(outputFile);
        assertEquals("camera-ai-app", template.name());
        assertEquals(2, template.references().size());
        assertEquals("core-agent", template.references().get(0).targetDirectoryName());
        assertEquals("mobile-app", template.references().get(1).targetDirectoryName());
    }
}

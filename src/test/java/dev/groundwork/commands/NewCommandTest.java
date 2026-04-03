package dev.groundwork.commands;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class NewCommandTest {
    @TempDir
    Path tempDir;

    @Test
    void rejectsProjectNamesThatEscapeTheOutputDirectory() throws Exception {
        Path referenceRepo = Files.createDirectories(tempDir.resolve("reference-app"));
        Path claudeTemplate = tempDir.resolve("claude-template.md");
        Files.writeString(claudeTemplate, "# Template");

        Path templateFile = tempDir.resolve("template.yml");
        Files.writeString(templateFile, """
                name: "camera-ai-app"
                description: "Camera to AI template"
                references:
                  - repo: ./reference-app
                    read_for: "Vision patterns"
                claude_md:
                  template: ./claude-template.md
                generate: []
                """);

        Path outputRoot = tempDir.resolve("output");
        int exitCode = new CommandLine(new NewCommand()).execute(
                "../escaped-project",
                "--template", templateFile.toString(),
                "--output-dir", outputRoot.toString()
        );

        assertEquals(1, exitCode);
        assertFalse(Files.exists(tempDir.resolve("escaped-project")));
        assertFalse(Files.exists(outputRoot.resolve("../escaped-project").normalize()));
    }
}

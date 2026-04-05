package dev.groundwork.commands;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ValidateCommandTest {
    @TempDir
    Path tempDir;

    @Test
    void validatesATemplateByAbsolutePath() throws Exception {
        Path referenceRepo = Files.createDirectories(tempDir.resolve("reference-app"));
        Path templateFile = tempDir.resolve("template.yml");
        Files.writeString(templateFile, """
                name: "camera-ai-app"
                description: "Camera to AI template"
                references:
                  - repo: ./reference-app
                    read_for: "Vision patterns"
                generate: []
                """);

        int exitCode = new CommandLine(new ValidateCommand()).execute(templateFile.toString());

        assertEquals(0, exitCode);
    }

    @Test
    void validatesATemplateByRegisteredName() throws Exception {
        String originalHome = currentUserHome();
        try {
            System.setProperty("user.home", tempDir.toString());

            Path templatesDirectory = Files.createDirectories(tempDir.resolve(".groundwork/templates"));
            Path referenceRepo = Files.createDirectories(tempDir.resolve("reference-app"));
            Path templateFile = templatesDirectory.resolve("camera-ai-app.yml");
            Files.writeString(templateFile, """
                    name: "camera-ai-app"
                    description: "Camera to AI template"
                    references:
                      - repo: ../../reference-app
                        read_for: "Vision patterns"
                    generate: []
                    """);

            int exitCode = new CommandLine(new ValidateCommand()).execute("camera-ai-app");

            assertEquals(0, exitCode);
        } finally {
            restoreUserHome(originalHome);
        }
    }

    private String currentUserHome() {
        return System.getProperty("user.home");
    }

    private void restoreUserHome(String originalHome) {
        if (originalHome == null) {
            System.clearProperty("user.home");
            return;
        }
        System.setProperty("user.home", originalHome);
    }
}

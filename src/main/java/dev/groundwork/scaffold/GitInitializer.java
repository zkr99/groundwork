package dev.groundwork.scaffold;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public final class GitInitializer {
    public void initialize(Path projectDirectory) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder("git", "init");
        builder.directory(projectDirectory.toFile());
        builder.redirectErrorStream(true);
        Process process = builder.start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IllegalStateException("git init failed in " + projectDirectory + ": " + output.trim());
        }
    }
}

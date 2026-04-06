package dev.groundwork.config;

import java.nio.file.Path;

public final class GroundworkPaths {
    private static final String GROUNDWORK_HOME_ENV = "GROUNDWORK_HOME";

    public Path homeDirectory() {
        return Path.of(System.getProperty("user.home")).toAbsolutePath().normalize();
    }

    public Path groundworkHomeDirectory() {
        String override = System.getenv(GROUNDWORK_HOME_ENV);
        if (override != null && !override.isBlank()) {
            return expandPath(override);
        }
        return homeDirectory().resolve(".groundwork");
    }

    public Path templatesDirectory() {
        return groundworkHomeDirectory().resolve("templates");
    }

    public Path claudeTemplatesDirectory() {
        return groundworkHomeDirectory().resolve("claude-templates");
    }

    public Path expandPath(String rawPath) {
        return resolvePath(rawPath, null);
    }

    public Path resolvePath(String rawPath, Path baseDirectory) {
        if (rawPath == null || rawPath.isBlank()) {
            throw new IllegalArgumentException("Path value must not be blank.");
        }
        if ("~".equals(rawPath)) {
            return homeDirectory();
        }
        if (rawPath.startsWith("~/")) {
            return homeDirectory().resolve(rawPath.substring(2)).normalize();
        }
        Path path = Path.of(rawPath);
        if (path.isAbsolute()) {
            return path.normalize();
        }
        if (baseDirectory != null) {
            return baseDirectory.resolve(path).toAbsolutePath().normalize();
        }
        return path.toAbsolutePath().normalize();
    }
}

package dev.groundwork.scaffold;

public record CleanupReport(int deletedFiles, int deletedDirectories) {
    public static CleanupReport empty() {
        return new CleanupReport(0, 0);
    }
}

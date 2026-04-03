package dev.groundwork.template;

public record GenerateSpec(boolean readme, boolean gitignore, String gitignoreProfile) {
    public static GenerateSpec defaults() {
        return new GenerateSpec(true, true, "general");
    }
}

package dev.groundwork.template;

import java.nio.file.Path;
import java.util.List;

public record Template(
        Path sourceFile,
        String name,
        String description,
        int version,
        List<ReferenceSpec> references,
        List<String> stripPatterns,
        ClaudeMdSpec claudeMd,
        GenerateSpec generate,
        List<String> directories,
        Path blueprint
) {
    public Template {
        references = List.copyOf(references);
        stripPatterns = List.copyOf(stripPatterns);
        directories = List.copyOf(directories);
    }
}

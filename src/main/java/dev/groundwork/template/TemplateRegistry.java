package dev.groundwork.template;

import dev.groundwork.config.GroundworkPaths;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public final class TemplateRegistry {
    private final GroundworkPaths paths;

    public TemplateRegistry(GroundworkPaths paths) {
        this.paths = paths;
    }

    public List<Path> listTemplateFiles() throws IOException {
        Path directory = paths.templatesDirectory();
        if (!Files.isDirectory(directory)) {
            return List.of();
        }
        try (Stream<Path> stream = Files.list(directory)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(this::isTemplateFile)
                    .sorted(Comparator.comparing(Path::getFileName))
                    .toList();
        }
    }

    public Optional<Path> findTemplateFile(String templateNameOrPath) throws IOException {
        Path expanded = paths.expandPath(templateNameOrPath);
        if (Files.exists(expanded) && isTemplateFile(expanded)) {
            return Optional.of(expanded);
        }

        String candidateName = stripExtension(templateNameOrPath);
        return listTemplateFiles().stream()
                .filter(path -> stripExtension(path.getFileName().toString()).equals(candidateName))
                .findFirst();
    }

    private boolean isTemplateFile(Path path) {
        String fileName = path.getFileName().toString().toLowerCase();
        return fileName.endsWith(".yml") || fileName.endsWith(".yaml");
    }

    private String stripExtension(String value) {
        int extensionIndex = value.lastIndexOf('.');
        return extensionIndex >= 0 ? value.substring(0, extensionIndex) : value;
    }
}

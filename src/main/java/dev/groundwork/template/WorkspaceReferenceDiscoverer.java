package dev.groundwork.template;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public final class WorkspaceReferenceDiscoverer {
    public List<TemplateDraftReference> discover(Path workspaceRoot) throws IOException {
        Path normalizedRoot = workspaceRoot.toAbsolutePath().normalize();
        if (!Files.exists(normalizedRoot)) {
            throw new IllegalArgumentException("Workspace does not exist: " + normalizedRoot);
        }
        if (!Files.isDirectory(normalizedRoot)) {
            throw new IllegalArgumentException("Workspace is not a directory: " + normalizedRoot);
        }

        if (isGitRepository(normalizedRoot)) {
            return List.of(new TemplateDraftReference(normalizedRoot, ""));
        }

        try (Stream<Path> stream = Files.list(normalizedRoot)) {
            List<TemplateDraftReference> references = stream
                    .filter(Files::isDirectory)
                    .filter(this::isGitRepository)
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .map(path -> new TemplateDraftReference(path.toAbsolutePath().normalize(), ""))
                    .toList();
            if (references.isEmpty()) {
                throw new IllegalArgumentException(
                        "No git repositories were found in workspace: " + normalizedRoot);
            }
            return references;
        }
    }

    private boolean isGitRepository(Path directory) {
        return Files.exists(directory.resolve(".git"));
    }
}

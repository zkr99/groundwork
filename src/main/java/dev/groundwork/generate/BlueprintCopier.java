package dev.groundwork.generate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class BlueprintCopier {
    public Path copy(Path blueprint, Path docsDirectory) throws IOException {
        Files.createDirectories(docsDirectory);
        Path target = docsDirectory.resolve(blueprint.getFileName());
        Files.copy(blueprint, target, StandardCopyOption.COPY_ATTRIBUTES);
        return target;
    }
}

package dev.groundwork.template;

import dev.groundwork.config.GroundworkPaths;
import dev.groundwork.support.ClasspathResources;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

public final class TemplateDraftService {
    private final GroundworkPaths paths;
    private final ClasspathResources resources;
    private final TemplateDraftWriter writer;

    public TemplateDraftService(GroundworkPaths paths, ClasspathResources resources, TemplateDraftWriter writer) {
        this.paths = paths;
        this.resources = resources;
        this.writer = writer;
    }

    public TemplateValidationResult writeValidated(
            Path target,
            TemplateDraftRequest request,
            boolean overwrite,
            boolean preferRelativePaths
    ) throws IOException {
        Path normalizedTarget = target.toAbsolutePath().normalize();
        Path parent = normalizedTarget.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        if (Files.exists(normalizedTarget) && !overwrite) {
            throw new IllegalStateException("Template file already exists: " + normalizedTarget);
        }

        Path tempFile = Files.createTempFile(parent, normalizedTarget.getFileName().toString() + ".", ".tmp");
        try {
            Files.writeString(
                    tempFile,
                    writer.render(normalizedTarget, request, preferRelativePaths),
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );

            Template template = new TemplateLoader(paths).load(tempFile);
            TemplateValidationResult validation = new TemplateValidator(paths, resources).validate(template);
            if (!validation.isValid()) {
                return validation;
            }

            moveIntoPlace(tempFile, normalizedTarget, overwrite);
            return validation;
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    private void moveIntoPlace(Path source, Path target, boolean overwrite) throws IOException {
        try {
            if (overwrite) {
                Files.move(source, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } else {
                Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
            }
        } catch (AtomicMoveNotSupportedException exception) {
            if (overwrite) {
                Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.move(source, target);
            }
        }
    }
}

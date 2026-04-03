package dev.groundwork.scaffold;

import dev.groundwork.template.ReferenceSpec;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public final class ReferenceCopier {
    public List<ReferenceCopy> copyReferences(List<ReferenceSpec> references, Path referenceRoot, StripPolicy stripPolicy)
            throws IOException {
        if (references.isEmpty()) {
            return List.of();
        }

        Files.createDirectories(referenceRoot);
        List<ReferenceCopy> copies = new ArrayList<>();
        for (ReferenceSpec reference : references) {
            Path destination = referenceRoot.resolve(reference.targetDirectoryName());
            if (Files.exists(destination)) {
                throw new IllegalStateException("Reference destination already exists: " + destination);
            }
            copySingleReference(reference.repo(), destination, stripPolicy);
            copies.add(new ReferenceCopy(reference.repo(), destination, reference.readFor()));
        }
        return List.copyOf(copies);
    }

    private void copySingleReference(Path sourceRoot, Path destinationRoot, StripPolicy stripPolicy) throws IOException {
        Files.walkFileTree(sourceRoot, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                if (Files.isSymbolicLink(dir)) {
                    throw new IllegalStateException("Symlinks are not supported during scaffold copy: " + dir);
                }
                if (stripPolicy.shouldStripDirectory(dir, dir.equals(sourceRoot))) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                Path relativePath = sourceRoot.relativize(dir);
                Path target = destinationRoot.resolve(relativePath);
                Files.createDirectories(target);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (Files.isSymbolicLink(file)) {
                    throw new IllegalStateException("Symlinks are not supported during scaffold copy: " + file);
                }
                if (stripPolicy.shouldStripFile(file)) {
                    return FileVisitResult.CONTINUE;
                }
                Path relativePath = sourceRoot.relativize(file);
                Path target = destinationRoot.resolve(relativePath);
                Files.copy(file, target, StandardCopyOption.COPY_ATTRIBUTES);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}

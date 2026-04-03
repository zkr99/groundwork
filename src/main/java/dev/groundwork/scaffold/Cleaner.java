package dev.groundwork.scaffold;

import dev.groundwork.support.FileTreeDeleter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public final class Cleaner {
    public CleanupReport clean(Path root, StripPolicy stripPolicy) throws IOException {
        if (!Files.exists(root)) {
            return CleanupReport.empty();
        }

        AtomicInteger deletedFiles = new AtomicInteger();
        AtomicInteger deletedDirectories = new AtomicInteger();
        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                if (stripPolicy.shouldStripDirectory(dir, dir.equals(root))) {
                    FileTreeDeleter.deleteRecursively(dir);
                    deletedDirectories.incrementAndGet();
                    return FileVisitResult.SKIP_SUBTREE;
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (stripPolicy.shouldStripFile(file)) {
                    Files.deleteIfExists(file);
                    deletedFiles.incrementAndGet();
                }
                return FileVisitResult.CONTINUE;
            }
        });

        verifyNoGitDirectory(root);
        return new CleanupReport(deletedFiles.get(), deletedDirectories.get());
    }

    private void verifyNoGitDirectory(Path root) throws IOException {
        try (Stream<Path> stream = Files.walk(root)) {
            boolean gitFound = stream
                    .filter(Files::isDirectory)
                    .anyMatch(path -> ".git".equals(path.getFileName() == null ? "" : path.getFileName().toString()));
            if (gitFound) {
                throw new IllegalStateException("Cleanup failed to remove all .git directories below " + root);
            }
        }
    }
}

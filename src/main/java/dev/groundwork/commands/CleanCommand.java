package dev.groundwork.commands;

import dev.groundwork.support.FileTreeDeleter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "clean", mixinStandardHelpOptions = true, description = "Remove the _reference directory from an existing project.")
public final class CleanCommand implements Callable<Integer> {
    @Parameters(index = "0", description = "Project directory.")
    private Path projectDirectory;

    @Override
    public Integer call() throws Exception {
        Path target = projectDirectory.toAbsolutePath().normalize().resolve("_reference");
        if (!Files.exists(target)) {
            System.out.println("No _reference directory found at " + target);
            return 0;
        }

        FileTreeDeleter.deleteRecursively(target);
        System.out.println("Removed " + target);
        return 0;
    }
}

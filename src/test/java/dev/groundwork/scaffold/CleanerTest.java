package dev.groundwork.scaffold;

import dev.groundwork.template.GenerateSpec;
import dev.groundwork.template.Template;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CleanerTest {
    @TempDir
    Path tempDir;

    @Test
    void removesResidualArtifactsUsingGlobAndExactNameRules() throws Exception {
        Path copiedReference = Files.createDirectories(tempDir.resolve("copied"));
        Files.createDirectories(copiedReference.resolve(".git"));
        Files.createDirectories(copiedReference.resolve("__pycache__"));
        Files.writeString(copiedReference.resolve("module.pyc"), "bytecode");
        Files.writeString(copiedReference.resolve("keep.py"), "print('ok')");

        Template template = new Template(
                tempDir.resolve("template.yml"),
                "test",
                "Test template",
                1,
                List.of(),
                List.of("*.pyc"),
                null,
                GenerateSpec.defaults(),
                List.of(),
                null
        );

        Cleaner cleaner = new Cleaner();
        CleanupReport report = cleaner.clean(copiedReference, StripPolicy.from(template));

        assertTrue(report.deletedDirectories() >= 1);
        assertTrue(report.deletedFiles() >= 1);
        assertFalse(Files.exists(copiedReference.resolve(".git")));
        assertFalse(Files.exists(copiedReference.resolve("__pycache__")));
        assertFalse(Files.exists(copiedReference.resolve("module.pyc")));
        assertTrue(Files.exists(copiedReference.resolve("keep.py")));
    }
}

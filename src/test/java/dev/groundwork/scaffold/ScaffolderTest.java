package dev.groundwork.scaffold;

import dev.groundwork.config.GroundworkPaths;
import dev.groundwork.generate.BlueprintCopier;
import dev.groundwork.generate.ClaudeMdGenerator;
import dev.groundwork.generate.GitIgnoreGenerator;
import dev.groundwork.generate.ReadmeGenerator;
import dev.groundwork.generate.RenderContextFactory;
import dev.groundwork.generate.TemplateRenderer;
import dev.groundwork.support.ClasspathResources;
import dev.groundwork.template.ClaudeMdSpec;
import dev.groundwork.template.GenerateSpec;
import dev.groundwork.template.ReferenceSpec;
import dev.groundwork.template.Template;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScaffolderTest {
    @TempDir
    Path tempDir;

    @Test
    void scaffoldsProjectAndStripsArtifactsFromReferenceCopies() throws Exception {
        Path sourceRepo = Files.createDirectories(tempDir.resolve("source-app"));
        Files.createDirectories(sourceRepo.resolve(".git"));
        Files.createDirectories(sourceRepo.resolve("node_modules"));
        Files.createDirectories(sourceRepo.resolve("build"));
        Files.createDirectories(sourceRepo.resolve("ios/App"));
        Files.createDirectories(sourceRepo.resolve("android/app"));
        Files.createDirectories(sourceRepo.resolve("src"));
        Files.writeString(sourceRepo.resolve("src/main.py"), "print('hello')");
        Files.writeString(sourceRepo.resolve("ios/App/main.swift"), "print(\"hi\")");
        Files.writeString(sourceRepo.resolve("android/app/build.gradle"), "plugins {}");
        Files.writeString(sourceRepo.resolve(".env.local"), "SECRET=1");
        Files.writeString(sourceRepo.resolve("build/output.txt"), "artifact");

        Path blueprint = tempDir.resolve("PRODUCT_BLUEPRINT.md");
        Files.writeString(blueprint, "# Blueprint");

        Template template = new Template(
                tempDir.resolve("template.yml"),
                "camera-ai-app",
                "Camera to AI app",
                1,
                List.of(new ReferenceSpec(sourceRepo, "Core app patterns")),
                List.of(".env.*"),
                new ClaudeMdSpec("python-fastapi", Map.of("project_name", "{{name}}")),
                new GenerateSpec(true, true, "general"),
                List.of("backend/", "docs/"),
                blueprint
        );

        Scaffolder scaffolder = new Scaffolder(
                new ReferenceCopier(),
                new Cleaner(),
                new ClaudeMdGenerator(new GroundworkPaths(), new TemplateRenderer(), new RenderContextFactory(), new ClasspathResources()),
                new ReadmeGenerator(new TemplateRenderer(), new RenderContextFactory()),
                new GitIgnoreGenerator(new ClasspathResources()),
                new BlueprintCopier(),
                new GitInitializer()
        );

        Path destination = tempDir.resolve("PlantSnap");
        ScaffoldResult result = scaffolder.scaffold(
                template,
                new ProjectRequest("PlantSnap", "AI plant species identifier", destination, Map.of(), false)
        );

        Path copiedSource = destination.resolve("_reference/source-app");
        assertTrue(Files.exists(result.projectDirectory()));
        assertTrue(Files.exists(destination.resolve("CLAUDE.md")));
        assertTrue(Files.exists(destination.resolve("README.md")));
        assertTrue(Files.exists(destination.resolve(".gitignore")));
        assertTrue(Files.exists(destination.resolve("docs/PRODUCT_BLUEPRINT.md")));
        assertTrue(Files.exists(destination.resolve("backend")));
        assertTrue(Files.exists(copiedSource.resolve("src/main.py")));
        assertTrue(Files.exists(copiedSource.resolve("ios/App/main.swift")));
        assertTrue(Files.exists(copiedSource.resolve("android/app/build.gradle")));
        assertFalse(Files.exists(copiedSource.resolve(".git")));
        assertFalse(Files.exists(copiedSource.resolve("node_modules")));
        assertFalse(Files.exists(copiedSource.resolve("build")));
        assertFalse(Files.exists(copiedSource.resolve(".env.local")));

        String claude = Files.readString(destination.resolve("CLAUDE.md"));
        assertTrue(claude.contains("PlantSnap"));
        assertTrue(claude.contains("AI plant species identifier"));
    }
}

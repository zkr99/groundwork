package dev.groundwork.scaffold;

import dev.groundwork.generate.BlueprintCopier;
import dev.groundwork.generate.ClaudeMdGenerator;
import dev.groundwork.generate.GitIgnoreGenerator;
import dev.groundwork.generate.ReadmeGenerator;
import dev.groundwork.support.PhaseTimer;
import dev.groundwork.template.Template;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public final class Scaffolder {
    private final ReferenceCopier referenceCopier;
    private final Cleaner cleaner;
    private final ClaudeMdGenerator claudeMdGenerator;
    private final ReadmeGenerator readmeGenerator;
    private final GitIgnoreGenerator gitIgnoreGenerator;
    private final BlueprintCopier blueprintCopier;
    private final GitInitializer gitInitializer;

    public Scaffolder(
            ReferenceCopier referenceCopier,
            Cleaner cleaner,
            ClaudeMdGenerator claudeMdGenerator,
            ReadmeGenerator readmeGenerator,
            GitIgnoreGenerator gitIgnoreGenerator,
            BlueprintCopier blueprintCopier,
            GitInitializer gitInitializer
    ) {
        this.referenceCopier = referenceCopier;
        this.cleaner = cleaner;
        this.claudeMdGenerator = claudeMdGenerator;
        this.readmeGenerator = readmeGenerator;
        this.gitIgnoreGenerator = gitIgnoreGenerator;
        this.blueprintCopier = blueprintCopier;
        this.gitInitializer = gitInitializer;
    }

    public ScaffoldResult scaffold(Template template, ProjectRequest request) throws Exception {
        if (Files.exists(request.destination())) {
            throw new IllegalStateException("Destination already exists: " + request.destination());
        }

        PhaseTimer timer = new PhaseTimer();
        List<Path> generatedFiles = new ArrayList<>();
        StripPolicy stripPolicy = StripPolicy.from(template);

        timer.time("create-root", () -> Files.createDirectories(request.destination()));

        Path referenceRoot = request.destination().resolve("_reference");
        List<ReferenceCopy> copies = timer.time("copy-references", () -> {
            if (template.references().isEmpty()) {
                return List.of();
            }
            return referenceCopier.copyReferences(template.references(), referenceRoot, stripPolicy);
        });

        CleanupReport cleanupReport = timer.time("cleanup", () -> {
            if (template.references().isEmpty()) {
                return CleanupReport.empty();
            }
            return cleaner.clean(referenceRoot, stripPolicy);
        });

        timer.time("create-directories", () -> createDirectories(template, request.destination()));

        timer.time("generate-files", () -> {
            if (template.claudeMd() != null) {
                generatedFiles.add(writeFile(
                        request.destination().resolve("CLAUDE.md"),
                        claudeMdGenerator.generate(template, request)
                ));
            }
            if (template.generate().readme()) {
                generatedFiles.add(writeFile(
                        request.destination().resolve("README.md"),
                        readmeGenerator.generate(template, request)
                ));
            }
            if (template.generate().gitignore()) {
                generatedFiles.add(writeFile(
                        request.destination().resolve(".gitignore"),
                        gitIgnoreGenerator.generate(template.generate())
                ));
            }
            if (template.blueprint() != null) {
                generatedFiles.add(blueprintCopier.copy(template.blueprint(), request.destination().resolve("docs")));
            }
        });

        if (request.initializeGit()) {
            timer.time("git-init", () -> gitInitializer.initialize(request.destination()));
        }

        return new ScaffoldResult(
                request.destination(),
                copies,
                generatedFiles,
                cleanupReport,
                timer.durations()
        );
    }

    private void createDirectories(Template template, Path destination) throws IOException {
        Path normalizedDestination = destination.toAbsolutePath().normalize();
        for (String directory : template.directories()) {
            String sanitized = directory.endsWith("/") ? directory.substring(0, directory.length() - 1) : directory;
            if (!sanitized.isBlank()) {
                Path target = normalizedDestination.resolve(sanitized).normalize();
                if (!target.startsWith(normalizedDestination)) {
                    throw new IllegalStateException("Directory escapes project root: " + directory);
                }
                Files.createDirectories(target);
            }
        }
    }

    private Path writeFile(Path target, String contents) throws IOException {
        if (target.getParent() != null) {
            Files.createDirectories(target.getParent());
        }
        Files.writeString(target, contents, StandardOpenOption.CREATE_NEW);
        return target;
    }
}

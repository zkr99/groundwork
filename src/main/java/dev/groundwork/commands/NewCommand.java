package dev.groundwork.commands;

import dev.groundwork.config.GroundworkPaths;
import dev.groundwork.generate.BlueprintCopier;
import dev.groundwork.generate.ClaudeMdGenerator;
import dev.groundwork.generate.GitIgnoreGenerator;
import dev.groundwork.generate.ReadmeGenerator;
import dev.groundwork.generate.RenderContextFactory;
import dev.groundwork.generate.TemplateRenderer;
import dev.groundwork.scaffold.Cleaner;
import dev.groundwork.scaffold.GitInitializer;
import dev.groundwork.scaffold.ProjectRequest;
import dev.groundwork.scaffold.ReferenceCopier;
import dev.groundwork.scaffold.ScaffoldResult;
import dev.groundwork.scaffold.Scaffolder;
import dev.groundwork.support.CliVariables;
import dev.groundwork.support.ClasspathResources;
import dev.groundwork.template.Template;
import dev.groundwork.template.TemplateLoader;
import dev.groundwork.template.TemplateRegistry;
import dev.groundwork.template.TemplateValidationResult;
import dev.groundwork.template.TemplateValidator;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExecutionException;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Model.CommandSpec;

@Command(name = "new", mixinStandardHelpOptions = true, description = "Create a new project from a template.")
public final class NewCommand implements Callable<Integer> {
    @Parameters(index = "0", description = "Project name.")
    private String name;

    @Option(names = "--template", required = true, description = "Template name or YAML path.")
    private String templateName;

    @Option(names = "--description", description = "Project description override.")
    private String description;

    @Option(names = "--output-dir", defaultValue = ".", description = "Directory where the project should be created.")
    private Path outputDirectory;

    @Option(names = "--git", description = "Initialize a git repository after scaffolding.")
    private boolean initializeGit;

    @Option(names = "--var", description = "Extra template variable in key=value format.", arity = "1")
    private List<String> rawVariables = new ArrayList<>();

    @Spec
    private CommandSpec spec;

    @Override
    public Integer call() throws Exception {
        GroundworkPaths paths = new GroundworkPaths();
        TemplateRegistry registry = new TemplateRegistry(paths);
        TemplateLoader loader = new TemplateLoader(paths);
        TemplateValidator validator = new TemplateValidator(paths, new ClasspathResources());

        Optional<Path> templatePath = registry.findTemplateFile(templateName);
        if (templatePath.isEmpty()) {
            throw new ExecutionException(spec.commandLine(), "Template not found: " + templateName);
        }

        Template template = loader.load(templatePath.get());
        TemplateValidationResult validation = validator.validate(template);
        if (!validation.isValid()) {
            throw new ExecutionException(spec.commandLine(), validation.render("Template is invalid"));
        }

        Map<String, String> cliVariables = CliVariables.parse(rawVariables);
        String effectiveDescription = description == null || description.isBlank()
                ? template.description()
                : description;
        Path destination = resolveDestination(name);
        ProjectRequest request = new ProjectRequest(name, effectiveDescription, destination, cliVariables, initializeGit);

        Scaffolder scaffolder = new Scaffolder(
                new ReferenceCopier(),
                new Cleaner(),
                new ClaudeMdGenerator(paths, new TemplateRenderer(), new RenderContextFactory(), new ClasspathResources()),
                new ReadmeGenerator(new TemplateRenderer(), new RenderContextFactory()),
                new GitIgnoreGenerator(new ClasspathResources()),
                new BlueprintCopier(),
                new GitInitializer()
        );

        ScaffoldResult result = scaffolder.scaffold(template, request);
        printSummary(result);
        return 0;
    }

    private Path resolveDestination(String projectName) {
        if (projectName == null || projectName.isBlank()) {
            throw new ExecutionException(spec.commandLine(), "Project name must not be blank.");
        }
        if (projectName.contains("/") || projectName.contains("\\")) {
            throw new ExecutionException(spec.commandLine(),
                    "Project name must be a single directory name, not a path: " + projectName);
        }

        Path namePath = Path.of(projectName).normalize();
        if (namePath.isAbsolute()
                || namePath.getNameCount() != 1
                || ".".equals(namePath.toString())
                || "..".equals(namePath.toString())) {
            throw new ExecutionException(spec.commandLine(),
                    "Project name must be a single directory name, not a path: " + projectName);
        }

        Path normalizedOutput = outputDirectory.toAbsolutePath().normalize();
        Path destination = normalizedOutput.resolve(namePath).normalize();
        if (!destination.startsWith(normalizedOutput)) {
            throw new ExecutionException(spec.commandLine(),
                    "Project name escapes the output directory: " + projectName);
        }
        return destination;
    }

    private void printSummary(ScaffoldResult result) {
        System.out.println("Created project at " + result.projectDirectory());
        System.out.println("Copied references: " + result.references().size());
        System.out.println("Generated files: " + result.generatedFiles().size());
        if (result.cleanupReport().deletedFiles() > 0 || result.cleanupReport().deletedDirectories() > 0) {
            System.out.println("Cleanup removed "
                    + result.cleanupReport().deletedDirectories()
                    + " directories and "
                    + result.cleanupReport().deletedFiles()
                    + " files.");
        }
        if (!result.phaseDurations().isEmpty()) {
            System.out.println("Timings:");
            for (Map.Entry<String, Duration> entry : result.phaseDurations().entrySet()) {
                System.out.println("  - " + entry.getKey() + ": " + entry.getValue().toMillis() + "ms");
            }
        }
    }
}

package dev.groundwork.commands;

import dev.groundwork.config.GroundworkPaths;
import dev.groundwork.template.TemplateDraftReference;
import dev.groundwork.template.TemplateDraftRequest;
import dev.groundwork.template.TemplateDraftService;
import dev.groundwork.template.TemplateDraftWriter;
import dev.groundwork.template.TemplateValidationResult;
import dev.groundwork.support.ClasspathResources;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExecutionException;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Model.CommandSpec;

@Command(name = "create", mixinStandardHelpOptions = true, description = "Create a template file without hand-writing YAML.")
public final class TemplateCreateCommand implements Callable<Integer> {
    @Parameters(index = "0", description = "Template name.")
    private String name;

    @Option(names = "--description", required = true, description = "Template description.")
    private String description;

    @Option(
            names = {"--reference", "--repo"},
            description = "Reference repo in PATH[::PURPOSE] format. Repeat for multiple repos."
    )
    private List<String> rawReferences = new ArrayList<>();

    @Option(names = "--strip", description = "Extra strip pattern. Repeat for multiple values.")
    private List<String> stripPatterns = new ArrayList<>();

    @Option(names = {"--dir", "--directory"}, description = "Starter directory to create. Repeat for multiple values.")
    private List<String> directories = new ArrayList<>();

    @Option(names = "--claude-template", defaultValue = "python-fastapi", description = "Built-in or custom CLAUDE template.")
    private String claudeTemplate;

    @Option(names = "--gitignore-profile", defaultValue = "general", description = "Built-in .gitignore profile.")
    private String gitignoreProfile;

    @Option(names = "--blueprint", description = "Optional blueprint file path to copy into docs/.")
    private String blueprint;

    @Option(names = "--output", description = "Output template file. Defaults to ~/.groundwork/templates/<name>.yml.")
    private Path outputFile;

    @Option(names = "--no-readme", description = "Do not generate README.md for new projects.")
    private boolean noReadme;

    @Option(names = "--no-gitignore", description = "Do not generate .gitignore for new projects.")
    private boolean noGitignore;

    @Option(names = "--force", description = "Overwrite the target template file if it already exists.")
    private boolean overwrite;

    @Option(names = "--absolute-paths", description = "Write absolute repo and blueprint paths even when --output is used.")
    private boolean absolutePaths;

    @Spec
    private CommandSpec spec;

    @Override
    public Integer call() throws Exception {
        GroundworkPaths paths = new GroundworkPaths();
        Path target = outputFile == null
                ? paths.templatesDirectory().resolve(name + ".yml")
                : outputFile.toAbsolutePath().normalize();

        List<TemplateDraftReference> references = parseReferences(paths);
        Path blueprintPath = blueprint == null ? null : paths.expandPath(blueprint);

        TemplateDraftRequest request = new TemplateDraftRequest(
                name,
                description,
                references,
                stripPatterns,
                claudeTemplate,
                !noReadme,
                !noGitignore,
                gitignoreProfile,
                directories,
                blueprintPath
        );

        TemplateDraftService draftService = new TemplateDraftService(paths, new ClasspathResources(), new TemplateDraftWriter());
        TemplateValidationResult validation = draftService.writeValidated(
                target,
                request,
                overwrite,
                outputFile != null && !absolutePaths
        );

        if (!validation.isValid()) {
            System.out.println(validation.render("Template was not written because it is invalid"));
            return 1;
        }

        System.out.println("Created template at " + target);
        System.out.println("Template is valid and ready to use.");
        return 0;
    }

    private List<TemplateDraftReference> parseReferences(GroundworkPaths paths) {
        List<TemplateDraftReference> references = new ArrayList<>();
        for (String rawReference : rawReferences) {
            String[] parts = rawReference.split("::", 2);
            Path repo = paths.expandPath(parts[0].trim());
            if (!Files.exists(repo)) {
                throw new ExecutionException(spec.commandLine(), "Reference repo does not exist: " + repo);
            }
            String readFor = parts.length > 1 ? parts[1].trim() : "";
            references.add(new TemplateDraftReference(repo, readFor));
        }
        return List.copyOf(references);
    }
}

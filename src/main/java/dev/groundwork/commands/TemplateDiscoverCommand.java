package dev.groundwork.commands;

import dev.groundwork.config.GroundworkPaths;
import dev.groundwork.support.ClasspathResources;
import dev.groundwork.template.TemplateDraftRequest;
import dev.groundwork.template.TemplateDraftService;
import dev.groundwork.template.TemplateDraftWriter;
import dev.groundwork.template.TemplateValidationResult;
import dev.groundwork.template.WorkspaceReferenceDiscoverer;
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

@Command(name = "discover", mixinStandardHelpOptions = true, description = "Create a template by discovering local git repos in a workspace.")
public final class TemplateDiscoverCommand implements Callable<Integer> {
    @Parameters(index = "0", description = "Template name.")
    private String name;

    @Option(names = "--from", required = true, description = "Workspace directory containing one or more local git repos.")
    private Path workspaceRoot;

    @Option(names = "--description", required = true, description = "Template description.")
    private String description;

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

        TemplateDraftRequest request = new TemplateDraftRequest(
                name,
                description,
                new WorkspaceReferenceDiscoverer().discover(workspaceRoot),
                stripPatterns,
                claudeTemplate,
                !noReadme,
                !noGitignore,
                gitignoreProfile,
                directories,
                blueprint == null ? null : paths.expandPath(blueprint)
        );

        TemplateDraftService draftService = new TemplateDraftService(paths, new ClasspathResources(), new TemplateDraftWriter());
        TemplateValidationResult validation = draftService.writeValidated(
                target,
                request,
                overwrite,
                outputFile != null && !absolutePaths
        );

        if (!validation.isValid()) {
            throw new ExecutionException(spec.commandLine(), validation.render("Discovered template is invalid"));
        }

        System.out.println("Created template at " + target);
        System.out.println("Discovered " + request.references().size() + " local git reference"
                + (request.references().size() == 1 ? "." : "s."));
        return 0;
    }
}

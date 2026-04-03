package dev.groundwork.commands;

import dev.groundwork.config.GroundworkPaths;
import dev.groundwork.support.ClasspathResources;
import dev.groundwork.template.Template;
import dev.groundwork.template.TemplateLoader;
import dev.groundwork.template.TemplateRegistry;
import dev.groundwork.template.TemplateValidationResult;
import dev.groundwork.template.TemplateValidator;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExecutionException;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Model.CommandSpec;

@Command(name = "validate", description = "Validate a template file or template name.")
public final class ValidateCommand implements Callable<Integer> {
    @Parameters(index = "0", description = "Template name or YAML path.")
    private String templateName;

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
        TemplateValidationResult result = validator.validate(template);
        if (!result.isValid()) {
            System.out.println(result.render("Template is invalid"));
            return 1;
        }

        System.out.println("Template is valid: " + template.name());
        return 0;
    }
}

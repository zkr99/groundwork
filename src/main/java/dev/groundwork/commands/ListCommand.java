package dev.groundwork.commands;

import dev.groundwork.config.GroundworkPaths;
import dev.groundwork.support.ClasspathResources;
import dev.groundwork.template.Template;
import dev.groundwork.template.TemplateLoader;
import dev.groundwork.template.TemplateRegistry;
import dev.groundwork.template.TemplateValidationResult;
import dev.groundwork.template.TemplateValidator;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;

@Command(name = "list", mixinStandardHelpOptions = true, description = "List available templates.")
public final class ListCommand implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
        GroundworkPaths paths = new GroundworkPaths();
        TemplateRegistry registry = new TemplateRegistry(paths);
        TemplateLoader loader = new TemplateLoader(paths);
        TemplateValidator validator = new TemplateValidator(paths, new ClasspathResources());
        List<Path> templateFiles = registry.listTemplateFiles();

        if (templateFiles.isEmpty()) {
            System.out.println("No templates found in " + paths.templatesDirectory());
            System.out.println("Run `groundwork init` to create starter files.");
            return 0;
        }

        System.out.printf("%-24s %-8s %-10s %s%n", "Template", "Refs", "Status", "Description");
        for (Path templateFile : templateFiles) {
            try {
                Template template = loader.load(templateFile);
                TemplateValidationResult result = validator.validate(template);
                System.out.printf(
                        "%-24s %-8d %-10s %s%n",
                        template.name(),
                        template.references().size(),
                        result.isValid() ? "valid" : "invalid",
                        template.description()
                );
            } catch (Exception exception) {
                System.out.printf(
                        "%-24s %-8s %-10s %s%n",
                        fileStem(templateFile),
                        "-",
                        "error",
                        exception.getMessage()
                );
            }
        }
        return 0;
    }

    private String fileStem(Path path) {
        String fileName = path.getFileName().toString();
        int extensionStart = fileName.lastIndexOf('.');
        return extensionStart >= 0 ? fileName.substring(0, extensionStart) : fileName;
    }
}

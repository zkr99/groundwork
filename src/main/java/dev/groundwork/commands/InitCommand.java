package dev.groundwork.commands;

import dev.groundwork.config.GroundworkPaths;
import dev.groundwork.support.ClasspathResources;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;

@Command(name = "init", mixinStandardHelpOptions = true, description = "Create ~/.groundwork starter directories and example templates.")
public final class InitCommand implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
        GroundworkPaths paths = new GroundworkPaths();
        ClasspathResources resources = new ClasspathResources();

        Files.createDirectories(paths.groundworkHomeDirectory());
        Files.createDirectories(paths.templatesDirectory());
        Files.createDirectories(paths.claudeTemplatesDirectory());

        Map<String, Path> seededFiles = new LinkedHashMap<>();
        seededFiles.put("init/templates/camera-ai-app.yml", paths.templatesDirectory().resolve("camera-ai-app.yml"));
        seededFiles.put("init/claude-templates/project-guide.md", paths.claudeTemplatesDirectory().resolve("project-guide.md"));

        int created = 0;
        int skipped = 0;
        for (Map.Entry<String, Path> entry : seededFiles.entrySet()) {
            Path target = entry.getValue();
            if (Files.exists(target)) {
                skipped++;
                continue;
            }
            Files.createDirectories(target.getParent());
            Files.writeString(
                    target,
                    resources.readText(entry.getKey()),
                    StandardOpenOption.CREATE_NEW
            );
            created++;
        }

        System.out.println("Initialized Groundwork home at " + paths.groundworkHomeDirectory());
        System.out.println("Created " + created + " files, skipped " + skipped + " existing files.");
        return 0;
    }
}

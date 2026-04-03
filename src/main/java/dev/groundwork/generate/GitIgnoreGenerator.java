package dev.groundwork.generate;

import dev.groundwork.support.ClasspathResources;
import dev.groundwork.template.GenerateSpec;

public final class GitIgnoreGenerator {
    private final ClasspathResources resources;

    public GitIgnoreGenerator(ClasspathResources resources) {
        this.resources = resources;
    }

    public String generate(GenerateSpec generateSpec) {
        String contents = resources.readText("defaults/gitignore-" + generateSpec.gitignoreProfile() + ".txt");
        return contents.endsWith(System.lineSeparator()) ? contents : contents + System.lineSeparator();
    }
}

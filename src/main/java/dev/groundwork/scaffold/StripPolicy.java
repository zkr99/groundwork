package dev.groundwork.scaffold;

import dev.groundwork.template.Template;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class StripPolicy {
    private static final List<String> GLOBAL_DEFAULTS = List.of(
            ".git",
            "node_modules",
            "__pycache__",
            ".venv",
            "venv",
            "chroma_db",
            "chroma_data",
            ".next",
            ".expo",
            ".netlify",
            "build",
            "dist",
            ".env",
            ".env.*",
            "*.pyc",
            "*.egg-info"
    );

    private final Set<String> exactNames;
    private final List<PathMatcher> globMatchers;

    private StripPolicy(Set<String> exactNames, List<PathMatcher> globMatchers) {
        this.exactNames = exactNames;
        this.globMatchers = globMatchers;
    }

    public static StripPolicy from(Template template) {
        Set<String> exactNames = new LinkedHashSet<>();
        List<PathMatcher> globMatchers = new ArrayList<>();
        for (String pattern : mergedPatterns(template)) {
            if (looksLikeGlob(pattern)) {
                globMatchers.add(FileSystems.getDefault().getPathMatcher("glob:" + pattern));
            } else {
                exactNames.add(pattern);
            }
        }
        return new StripPolicy(Set.copyOf(exactNames), List.copyOf(globMatchers));
    }

    public boolean shouldStripDirectory(Path directory, boolean isRoot) {
        if (isRoot) {
            return false;
        }
        return matches(directory);
    }

    public boolean shouldStripFile(Path file) {
        return matches(file);
    }

    private boolean matches(Path path) {
        Path fileName = path.getFileName();
        if (fileName == null) {
            return false;
        }
        if (exactNames.contains(fileName.toString())) {
            return true;
        }
        for (PathMatcher matcher : globMatchers) {
            if (matcher.matches(fileName) || matcher.matches(path)) {
                return true;
            }
        }
        return false;
    }

    private static List<String> mergedPatterns(Template template) {
        LinkedHashSet<String> patterns = new LinkedHashSet<>(GLOBAL_DEFAULTS);
        patterns.addAll(template.stripPatterns());
        return List.copyOf(patterns);
    }

    private static boolean looksLikeGlob(String value) {
        return value.contains("*") || value.contains("?") || value.contains("[") || value.contains("{");
    }
}

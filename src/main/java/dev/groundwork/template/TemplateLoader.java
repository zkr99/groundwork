package dev.groundwork.template;

import dev.groundwork.config.GroundworkPaths;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

public final class TemplateLoader {
    private final GroundworkPaths paths;

    public TemplateLoader(GroundworkPaths paths) {
        this.paths = paths;
    }

    public Template load(Path templateFile) throws IOException {
        String yamlText = java.nio.file.Files.readString(templateFile);
        Yaml yaml = new Yaml(new SafeConstructor(new LoaderOptions()));
        Object loaded = yaml.load(yamlText);
        if (!(loaded instanceof Map<?, ?> rawMap)) {
            throw new IllegalArgumentException("Template root must be a YAML mapping: " + templateFile);
        }
        return toTemplate(templateFile, rawMap);
    }

    private Template toTemplate(Path templateFile, Map<?, ?> rawMap) {
        Path baseDirectory = templateFile.toAbsolutePath().normalize().getParent();
        String name = stringValue(rawMap, "name", fileStem(templateFile));
        String description = stringValue(rawMap, "description", "");
        int version = integerValue(rawMap, "version", 1);
        List<ReferenceSpec> references = parseReferences(rawMap.get("references"), baseDirectory);
        List<String> stripPatterns = stringList(rawMap.get("strip"));
        ClaudeMdSpec claudeMd = parseClaudeMd(rawMap.get("claude_md"));
        GenerateSpec generate = parseGenerate(rawMap.get("generate"));
        List<String> directories = stringList(rawMap.get("directories"));
        Path blueprint = optionalPath(rawMap.get("blueprint"), baseDirectory);
        return new Template(
                templateFile.toAbsolutePath().normalize(),
                name,
                description,
                version,
                references,
                stripPatterns,
                claudeMd,
                generate,
                directories,
                blueprint
        );
    }

    private List<ReferenceSpec> parseReferences(Object rawReferences, Path baseDirectory) {
        if (rawReferences == null) {
            return List.of();
        }
        if (!(rawReferences instanceof List<?> referencesList)) {
            throw new IllegalArgumentException("`references` must be a list.");
        }

        List<ReferenceSpec> references = new ArrayList<>();
        for (Object item : referencesList) {
            if (!(item instanceof Map<?, ?> map)) {
                throw new IllegalArgumentException("Each reference must be a mapping.");
            }
            String repo = requiredString(map, "repo");
            String readFor = stringValue(map, "read_for", "");
            references.add(new ReferenceSpec(paths.resolvePath(repo, baseDirectory), readFor));
        }
        return references;
    }

    private ClaudeMdSpec parseClaudeMd(Object rawClaudeMd) {
        if (rawClaudeMd == null) {
            return null;
        }
        if (!(rawClaudeMd instanceof Map<?, ?> map)) {
            throw new IllegalArgumentException("`claude_md` must be a mapping.");
        }
        String template = requiredString(map, "template");
        Map<String, String> variables = new LinkedHashMap<>();
        Object rawVariables = map.get("variables");
        if (rawVariables != null) {
            if (!(rawVariables instanceof Map<?, ?> variableMap)) {
                throw new IllegalArgumentException("`claude_md.variables` must be a mapping.");
            }
            for (Map.Entry<?, ?> entry : variableMap.entrySet()) {
                variables.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
            }
        }
        return new ClaudeMdSpec(template, variables);
    }

    private GenerateSpec parseGenerate(Object rawGenerate) {
        if (rawGenerate == null) {
            return GenerateSpec.defaults();
        }
        if (!(rawGenerate instanceof List<?> items)) {
            throw new IllegalArgumentException("`generate` must be a list.");
        }

        boolean readme = false;
        boolean gitignore = false;
        String gitignoreProfile = "general";
        for (Object item : items) {
            if (item instanceof String value) {
                if ("README.md".equals(value)) {
                    readme = true;
                } else if (".gitignore".equals(value)) {
                    gitignore = true;
                } else {
                    throw new IllegalArgumentException("Unsupported `generate` entry: " + value);
                }
            } else if (item instanceof Map<?, ?> map) {
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    String key = String.valueOf(entry.getKey());
                    if (".gitignore".equals(key)) {
                        gitignore = true;
                        gitignoreProfile = String.valueOf(entry.getValue());
                    } else {
                        throw new IllegalArgumentException("Unsupported `generate` mapping key: " + key);
                    }
                }
            } else {
                throw new IllegalArgumentException("`generate` entries must be strings or mappings.");
            }
        }
        return new GenerateSpec(readme, gitignore, gitignoreProfile);
    }

    private List<String> stringList(Object rawValue) {
        if (rawValue == null) {
            return List.of();
        }
        if (!(rawValue instanceof List<?> list)) {
            throw new IllegalArgumentException("Expected a YAML list.");
        }
        List<String> values = new ArrayList<>();
        for (Object item : list) {
            values.add(String.valueOf(item));
        }
        return values;
    }

    private Path optionalPath(Object rawValue, Path baseDirectory) {
        if (rawValue == null) {
            return null;
        }
        return paths.resolvePath(String.valueOf(rawValue), baseDirectory);
    }

    private String stringValue(Map<?, ?> map, String key, String defaultValue) {
        Object value = map.get(key);
        if (value == null) {
            return defaultValue;
        }
        return String.valueOf(value);
    }

    private String requiredString(Map<?, ?> map, String key) {
        Object value = map.get(key);
        if (value == null || String.valueOf(value).isBlank()) {
            throw new IllegalArgumentException("Missing required value `" + key + "`.");
        }
        return String.valueOf(value);
    }

    private int integerValue(Map<?, ?> map, String key, int defaultValue) {
        Object value = map.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }

    private String fileStem(Path path) {
        String name = path.getFileName().toString();
        int extensionIndex = name.lastIndexOf('.');
        return extensionIndex >= 0 ? name.substring(0, extensionIndex) : name;
    }
}

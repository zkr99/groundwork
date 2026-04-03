package dev.groundwork.scaffold;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record ScaffoldResult(
        Path projectDirectory,
        List<ReferenceCopy> references,
        List<Path> generatedFiles,
        CleanupReport cleanupReport,
        Map<String, Duration> phaseDurations
) {
    public ScaffoldResult {
        references = List.copyOf(references);
        generatedFiles = List.copyOf(generatedFiles);
        phaseDurations = Collections.unmodifiableMap(new LinkedHashMap<>(phaseDurations));
    }
}

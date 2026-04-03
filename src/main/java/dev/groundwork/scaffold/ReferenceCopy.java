package dev.groundwork.scaffold;

import java.nio.file.Path;

public record ReferenceCopy(Path source, Path destination, String readFor) {
}

package dev.groundwork.template;

import java.nio.file.Path;

public record TemplateDraftReference(Path repo, String readFor) {
}

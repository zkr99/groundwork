package dev.groundwork.template;

import java.nio.file.FileSystems;
import java.nio.file.Path;

public record ReferenceSpec(Path repo, String readFor) {
    public String targetDirectoryName() {
        Path fileName = repo.getFileName();
        if (fileName != null) {
            return fileName.toString();
        }
        return repo.toString().replace(FileSystems.getDefault().getSeparator(), "-");
    }
}

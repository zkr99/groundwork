package dev.groundwork.support;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class ClasspathResources {
    public String readText(String resourcePath) {
        try (InputStream stream = open(resourcePath)) {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read resource `" + resourcePath + "`.", exception);
        }
    }

    public boolean exists(String resourcePath) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return classLoader.getResource(resourcePath) != null;
    }

    private InputStream open(String resourcePath) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream stream = classLoader.getResourceAsStream(resourcePath);
        if (stream == null) {
            throw new IllegalArgumentException("Missing classpath resource: " + resourcePath);
        }
        return stream;
    }
}

package dev.groundwork.support;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;
import picocli.CommandLine.IVersionProvider;

public final class GroundworkVersionProvider implements IVersionProvider {
    private static final String RESOURCE_NAME = "groundwork-version.properties";
    private static final String VERSION_KEY = "version";
    private static final String FALLBACK_VERSION = "0.1.0-SNAPSHOT";

    @Override
    public String[] getVersion() {
        return new String[] {"Groundwork " + loadVersion()};
    }

    private String loadVersion() {
        Properties properties = new Properties();
        try (InputStream stream = GroundworkVersionProvider.class.getClassLoader().getResourceAsStream(RESOURCE_NAME)) {
            if (stream == null) {
                return FALLBACK_VERSION;
            }
            properties.load(stream);
            String resolved = Objects.requireNonNullElse(properties.getProperty(VERSION_KEY), FALLBACK_VERSION);
            if (resolved.isBlank() || resolved.contains("${")) {
                return FALLBACK_VERSION;
            }
            return resolved;
        } catch (IOException ignored) {
            return FALLBACK_VERSION;
        }
    }
}

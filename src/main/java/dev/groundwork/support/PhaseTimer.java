package dev.groundwork.support;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class PhaseTimer {
    private final Map<String, Duration> durations = new LinkedHashMap<>();

    public void time(String phase, CheckedRunnable runnable) throws Exception {
        long startedAt = System.nanoTime();
        try {
            runnable.run();
        } finally {
            durations.put(phase, Duration.ofNanos(System.nanoTime() - startedAt));
        }
    }

    public <T> T time(String phase, CheckedSupplier<T> supplier) throws Exception {
        long startedAt = System.nanoTime();
        try {
            return supplier.get();
        } finally {
            durations.put(phase, Duration.ofNanos(System.nanoTime() - startedAt));
        }
    }

    public Map<String, Duration> durations() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(durations));
    }

    @FunctionalInterface
    public interface CheckedRunnable {
        void run() throws Exception;
    }

    @FunctionalInterface
    public interface CheckedSupplier<T> {
        T get() throws Exception;
    }
}

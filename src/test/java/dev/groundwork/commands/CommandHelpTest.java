package dev.groundwork.commands;

import dev.groundwork.Groundwork;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import picocli.CommandLine;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommandHelpTest {
    @ParameterizedTest
    @MethodSource("helpCommandArgs")
    void leafCommandsSupportHelp(String[] args) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        CommandLine commandLine = new CommandLine(new Groundwork());
        commandLine.setOut(new PrintWriter(output, true));
        commandLine.setErr(new PrintWriter(output, true));

        int exitCode = commandLine.execute(args);

        assertEquals(0, exitCode, "Expected help command to succeed for: " + String.join(" ", args));
    }

    private static Stream<Arguments> helpCommandArgs() {
        return Stream.of(
                Arguments.of((Object) new String[] {"init", "--help"}),
                Arguments.of((Object) new String[] {"list", "--help"}),
                Arguments.of((Object) new String[] {"clean", "--help"}),
                Arguments.of((Object) new String[] {"validate", "--help"}),
                Arguments.of((Object) new String[] {"new", "--help"}),
                Arguments.of((Object) new String[] {"template", "create", "--help"}),
                Arguments.of((Object) new String[] {"template", "discover", "--help"})
        );
    }
}

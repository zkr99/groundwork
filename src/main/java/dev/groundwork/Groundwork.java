package dev.groundwork;

import dev.groundwork.commands.CleanCommand;
import dev.groundwork.commands.InitCommand;
import dev.groundwork.commands.ListCommand;
import dev.groundwork.commands.NewCommand;
import dev.groundwork.commands.TemplateCommand;
import dev.groundwork.commands.ValidateCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
        name = "groundwork",
        mixinStandardHelpOptions = true,
        version = "0.1.0",
        description = "Scaffold projects from live local repositories.",
        subcommands = {
                NewCommand.class,
                ListCommand.class,
                InitCommand.class,
                ValidateCommand.class,
                CleanCommand.class,
                TemplateCommand.class
        }
)
public final class Groundwork implements Runnable {
    public static void main(String[] args) {
        int exitCode = new CommandLine(new Groundwork()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        CommandLine.usage(this, System.out);
    }
}

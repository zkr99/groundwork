package dev.groundwork.commands;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
        name = "template",
        mixinStandardHelpOptions = true,
        description = "Create and manage Groundwork templates.",
        subcommands = {
                TemplateCreateCommand.class,
                TemplateDiscoverCommand.class
        }
)
public final class TemplateCommand implements Runnable {
    @Override
    public void run() {
        CommandLine.usage(this, System.out);
    }
}

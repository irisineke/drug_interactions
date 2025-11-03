package nl.bioinf;

import picocli.CommandLine;

public class Main {
    public static void main(String[] args) {
        CommandLine cmd = new CommandLine(new ArgumentParser())
                // Custom exception handler: nette foutmelding, geen stacktrace
                .setExecutionExceptionHandler((ex, commandLine, _) -> {
                    final String RED_BOLD = "\u001B[31;1m";
                    final String RESET = "\u001B[0m";

                    commandLine.getErr().println();
                    commandLine.getErr().println(RED_BOLD + "❌  Error: " + RESET + ex.getMessage());
                    commandLine.getErr().println();

                    // Gebruik exitcode uit commandSpec (standaard 1)
                    return commandLine.getCommandSpec().exitCodeOnExecutionException();
                });

        int exitCode = cmd.execute(args);
        System.exit(exitCode);
    }
}


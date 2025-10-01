package nl.bioinf;

import picocli.CommandLine.*;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.util.concurrent.Callable;


// name, help en version erin, description
@Command (name = "argumentparser",
        mixinStandardHelpOptions = true,
        version = "argumentparser 1.0",
        description = "handels the argument input")

// Callable: Integer of wat anders teruggeven ?
public class ArgumentParser implements Callable<Integer> {
//    vangt input file op
    @Option(names = { "-f", "--file" },
            paramLabel = "inputFile", // zo heet hij in help
            description = "the input file", // ook in help
            required = false)
    File inputFile;

    // geeft paden terug van files
    @Override
    public Integer call() {
        System.out.println("File: " + inputFile.getAbsolutePath());
        return 0; // return 0 als het goed gaat
    }

// maakt commandline object van de class argumentparser
    public static void main(String[] args) {
//        int exitCode = new picocli.CommandLine(new ArgumentParser()).execute(args);
//        System.exit(exitCode); // geef mee aan het systeem of het goed gaat (0 of 1)
          System.exit(new CommandLine(new MyApp()).execute(args));
    }
}


// runnen: ./gradlew run --args="--file data/raw/interactions.tsv"
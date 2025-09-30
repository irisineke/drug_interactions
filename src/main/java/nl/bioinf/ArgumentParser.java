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
            paramLabel = "inputFile",
            description = "the input file",
            required = true)
    File inputFile;

    @Override
    public Integer call() {
        System.out.println("File: " + inputFile.getAbsolutePath());
        return 0; // return 0 als het goed gaat
    }

    public static void main(String[] args) {
        int exitCode = new picocli.CommandLine(new ArgumentParser()).execute(args);
        System.exit(exitCode);
    }
}



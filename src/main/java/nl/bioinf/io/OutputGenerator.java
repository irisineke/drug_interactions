package nl.bioinf.io;

import java.nio.file.Path;

// dummy class voor uml
public class OutputGenerator {
    private final Path output;

    public OutputGenerator(Path output) {
        this.output = output;
    }

    public void run() {
        System.out.println("output path: " + output);
    }
}
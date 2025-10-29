package nl.bioinf.io;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.List;

// dummy class voor uml
public class OutputGenerator {
    private final Path output;

    public OutputGenerator(Path output) {
        this.output = output;
    }


    public void generateOutput(StringBuilder strBuilder) {
        try {
            List<String> lines = List.of(strBuilder.toString().split("\n"));
            Files.write(output, lines);
            System.out.println("Overlap genes written to: " + output);
        }
        catch (Exception e) {
            throw new RuntimeException("Error in writing output overlap genes to: " + output, e); // later e wegghalen
        }
    }
}
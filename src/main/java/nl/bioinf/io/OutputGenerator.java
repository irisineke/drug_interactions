package nl.bioinf.io;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

// dummy class voor uml
public class OutputGenerator {
    private final Path output;

    public OutputGenerator(Path output) {
        this.output = output;
    }

    public void generateOutput(Set<String> overlapGenes) {
        try {
            Files.write(output, overlapGenes);
            System.out.println("Overlap genes written to: " + output);
        } catch (Exception e) {
            throw new RuntimeException("Error in writing output overlap genes to: " + output, e); // later e wegghalen
        }
    }
}
package nl.bioinf.io;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;


/**
 * The {@code Validate} class provides static utility methods
 * for validating user-specified file paths before writing output.
 */
public class Validate {
    public static void validateOutputPath(Path output) {
    String fileName = output.getFileName().toString().toLowerCase(Locale.ROOT);
    if (!(fileName.endsWith(".txt") || fileName.endsWith(".pdf"))) {
        throw new IllegalArgumentException("ERROR: Output file must end with '.txt' → " + output);
    }

    Path parent = output.getParent();
    if (parent == null || !Files.exists(parent)) {
        throw new IllegalArgumentException("ERROR: Output directory does not exist → " + parent);
    }
}
}

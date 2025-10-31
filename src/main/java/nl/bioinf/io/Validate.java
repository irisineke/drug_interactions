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

    public static void validateDifferentDrugs(String drug1, String drug2) {
        if (drug1 == null || drug2 == null) {
            throw new IllegalArgumentException("ERROR: Drug names cannot be null.");
        }
        String trimmedDrug1 = drug1.trim();
        String trimmedDrug2 = drug2.trim();

        if (trimmedDrug1.equalsIgnoreCase(trimmedDrug2)) {
            throw new IllegalArgumentException("ERROR: You entered the same drug twice (" + drug1 + "). Please provide two different drugs.");
        }
    }
}
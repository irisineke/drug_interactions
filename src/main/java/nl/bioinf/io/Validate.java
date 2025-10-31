package nl.bioinf.io;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * The {@code Validate} class provides static utility methods
 * for validating user-specified file paths and drug inputs
 * before processing or writing output.
 */
public class Validate {

    /**
     * Validates that the provided output path points to a writable directory and a
     * file name ending in .txt or .pdf, and that the file has a non-empty base name.
     *
     * @param output path to the desired output file
     * @throws IllegalArgumentException if the file name or directory is invalid
     */
    public static void validateOutputPath(Path output) {
        if (output == null) {
            throw new IllegalArgumentException("ERROR: Output path is null.");
        }

        String fileName = output.getFileName().toString();
        String fileNameLower = fileName.toLowerCase(Locale.ROOT);

        // 1) Extension must be .txt or .pdf
        if (!(fileNameLower.endsWith(".txt") || fileNameLower.endsWith(".pdf"))) {
            throw new IllegalArgumentException("ERROR: Output file must end with '.txt' or '.pdf' → " + output);
        }

        // 2) Basename must be non-empty (reject ".txt" or ".pdf")
        int dotIdx = fileName.lastIndexOf('.');
        // dotIdx <= 0 means either no dot (-1) or the dot is the first char (0) → invalid basename
        if (dotIdx <= 0 || fileName.substring(0, dotIdx).trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "ERROR: Output filename must include a name before the extension (e.g., 'report.txt'), not just '" + fileName + "'."
            );
        }

        // 3) Directory checks
        Path parent = output.getParent();
        if (parent == null || !Files.exists(parent)) {
            throw new IllegalArgumentException("ERROR: Output directory does not exist → " + parent);
        }
        if (!Files.isDirectory(parent)) {
            throw new IllegalArgumentException("ERROR: Output parent is not a directory → " + parent);
        }
        if (!Files.isWritable(parent)) {
            throw new IllegalArgumentException("ERROR: Output directory is not writable → " + parent);
        }
    }


    /**
     * Validates that two drug names are not identical (case-sensitive).
     *
     * @param drug1 the first drug name
     * @param drug2 the second drug name
     * @throws IllegalArgumentException if the same drug name is provided twice
     */
    public static void validateDifferentDrugs(String drug1, String drug2) {
        if (drug1 == null || drug2 == null) {
            throw new IllegalArgumentException("ERROR: Drug names cannot be null.");
        }

        String trimmedDrug1 = drug1.trim();
        String trimmedDrug2 = drug2.trim();

        if (trimmedDrug1.equals(trimmedDrug2)) {
            throw new IllegalArgumentException("ERROR: You entered the same drug twice (" + drug1 + "). Please provide two different drugs.");
        }
    }
}

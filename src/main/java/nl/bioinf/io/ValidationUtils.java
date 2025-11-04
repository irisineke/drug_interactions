package nl.bioinf.io;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.io.File;
import java.io.IOException;


/**
 * The {@code Validate} utility class provides static methods for validating user input,
 * such as file paths, file formats, and drug names, before any processing or output is performed.
 * <p>
 * Its goal is to detect and report common input and I/O issues early with clear, consistent
 * error messages, ensuring that downstream components operate on valid and well-structured data.
 * </p>
 *
 * <p>Typical usage:</p>
 * <pre>{@code
 * Validate.validateOutputPath(Path.of("results/output.pdf"));
 * Validate.validateDifferentDrugs("Ibuprofen", "Paracetamol");
 * Validate.validateTsvFile(new File("interactions.tsv"), "Interactions file");
 * }</pre>
 */
public class ValidationUtils {

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

        if (trimmedDrug1.equalsIgnoreCase(trimmedDrug2)) {
            throw new IllegalArgumentException("ERROR: You entered the same drug twice (" + drug1 + "). Please provide two different drugs.");
        }
    }
    public static void validateTsvFile(File file, String label) {
        if (file == null) {
            throw new IllegalArgumentException(label + " is null.");
        }
        validateTsvPath(file.toPath(), label);
    }
    public static void validateTsvPath(Path path, String label) {
        performTsvValidation(path, label);
    }

    /**
     * Shared logic for validating both {@link File} and {@link Path} TSV inputs.
     * Prevents code duplication between validateTsvFile() and validateTsvPath().
     */
    private static void performTsvValidation(Path path, String label) {
        if (path == null) {
            throw new IllegalArgumentException(label + " is null.");
        }
        if (!Files.exists(path)) {
            throw new IllegalArgumentException(label + " not found: " + path.toAbsolutePath());
        }
        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException(label + " is not a file: " + path.toAbsolutePath());
        }
        if (!Files.isReadable(path)) {
            throw new IllegalArgumentException(label + " is not readable: " + path.toAbsolutePath());
        }
        try {
            if (Files.size(path) == 0) {
                throw new IllegalArgumentException(label + " is empty: " + path.toAbsolutePath());
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(label + " could not be inspected: " + path.toAbsolutePath());
        }

        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        if (!name.endsWith(".tsv")) {
            throw new IllegalArgumentException(label + " must be a .tsv file: " + path.toAbsolutePath());
        }
    }
    public static IllegalArgumentException ioWriteError(Path path, Exception cause) {
        return new IllegalArgumentException("ERROR: Failed to write output → " + path, cause);
    }
    public static IllegalArgumentException ioReadError(Path path, Exception cause) {
        return new IllegalArgumentException("ERROR: Failed to read input → " + path, cause);
    }
    public static IllegalArgumentException unsupportedOutputFormat(Path path) {
        return new IllegalArgumentException("ERROR: Output file must end with '.txt' or '.pdf' → " + path);
    }
}
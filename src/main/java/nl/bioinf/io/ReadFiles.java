package nl.bioinf.io;

import nl.bioinf.models.Combination;
import nl.bioinf.models.Drug;
import nl.bioinf.models.Interaction;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class responsible for reading and parsing all required input data files
 * for the drug–gene interaction analysis. This includes:
 * <ul>
 *     <li>A TSV file containing drug–gene interactions</li>
 *     <li>A TSV file containing basic drug information</li>
 *     <li>A resource file on the classpath (<code>drug_combinations.tsv</code>) defining
 *     possible outcomes for drug–type combinations</li>
 * </ul>
 *
 * <p>Each method validates the input file, trims headers, skips blank lines,
 * and performs basic structural validation to prevent malformed data from being used
 * downstream in the analysis pipeline.</p>
 *
 * <p><strong>Usage example:</strong></p>
 * <pre>{@code
 * File interactions = new File("interactions.tsv");
 * File drugs = new File("drugs.tsv");
 * ReadFiles reader = new ReadFiles(interactions, drugs);
 *
 * List<Interaction> interactionList = reader.processInteractions();
 * List<Drug> drugList = reader.processDrugs();
 * List<Combination> combinationList = reader.processCombinations();
 * }</pre>
 *
 */

public class ReadFiles {
    private final File interactionsFile;
    private final File drugsFile;
    private static final String COMBINATIONS_RESOURCE = "drug_combinations.tsv";

    public ReadFiles(File interactionsFile, File drugsFile) {
        this.interactionsFile = interactionsFile;
        this.drugsFile = drugsFile;
    }
    /**
     * Reads, validates, and parses the interactions file.
     *
     * @return a {@link List} of {@link Interaction} objects parsed from the file
     * @throws IllegalArgumentException if the file is missing, empty, malformed, or has invalid headers
     */
    public List<Interaction> processInteractions() {
        return readInteractions(interactionsFile);
    }

    public List<Drug> processDrugs() {
        return readDrugs(drugsFile);
    }

    public List<Combination> processCombinations() {
        return readCombinationsFromResource();
    }
    /**
     * Validates that the provided file exists, is a non-empty TSV file,
     * and is suitable for reading.
     *
     * @param file  the file to validate
     * @param label a descriptive label (used in error messages)
     * @throws IllegalArgumentException if the file is null, does not exist, is not a regular file,
     *                                  is empty, or does not have a .tsv extension
     */

    private void validateInputFile(File file, String label) {
        if (file == null) {
            throw new IllegalArgumentException(label + " is null.");
        }
        if (!file.exists()) {
            throw new IllegalArgumentException(label + " not found: " + file.getAbsolutePath());
        }
        if (!file.isFile()) {
            throw new IllegalArgumentException(label + " is not a file: " + file.getAbsolutePath());
        }
        if (file.length() == 0) {
            throw new IllegalArgumentException(label + " is empty: " + file.getAbsolutePath());
        }
        if (!file.getName().toLowerCase().endsWith(".tsv")) {
            throw new IllegalArgumentException(label + " must be a .tsv file: " + file.getAbsolutePath());
        }
    }

    /** Trim alle header-velden zodat stray spaces geen issues geven. */
    private List<String> readLines(File file) throws IOException {
        return Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)
                .stream()
                .filter(line -> !line.isBlank())
                .toList();
    }
    private static String[] normalizeHeaders(String headerLine) {
        String[] headers = headerLine.split("\t", -1);
        for (int i = 0; i < headers.length; i++) headers[i] = headers[i].trim();
        return headers;
    }

    private int indexOf(String[] headers, String name, File file) {
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].equals(name)) return i; // evt. equalsIgnoreCase
        }
        throw new IllegalArgumentException("Header not found: '" + name + "' in file: " + file.getAbsolutePath());
    }

    /**
     * Reads and parses a TSV file containing drug–gene interactions.
     * Expected header columns:
     * <ul>
     *     <li>gene_claim_name</li>
     *     <li>interaction_type</li>
     *     <li>interaction_score</li>
     *     <li>drug_concept_id</li>
     * </ul>
     *
     * @param file the interactions TSV file
     * @return a list of {@link Interaction} objects
     * @throws IllegalArgumentException if the file is invalid, malformed, or missing headers
     */
    private List<Interaction> readInteractions(File file) {
        validateInputFile(file, "Interactions file");
        try {
            List<String> lines = readLines(file);
            if (lines.isEmpty()) return List.of();

            String[] headers = normalizeHeaders(lines.getFirst());
            int idxGene  = indexOf(headers, "gene_claim_name", file);
            int idxType  = indexOf(headers, "interaction_type", file);
            int idxScore = indexOf(headers, "interaction_score", file);
            int idxDrug  = indexOf(headers, "drug_concept_id", file);


            if (lines.size() < 2) {
                throw new IllegalArgumentException("Interactions file has no data rows after header: " + file.getName());
            }

            List<Interaction> result = new ArrayList<>();


            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.isBlank()) continue; // sla lege regels over

                String[] parts = line.split("\t", -1);


                int maxIndex = Math.max(Math.max(idxGene, idxType), Math.max(idxScore, idxDrug));
                if (maxIndex >= parts.length) {
                    throw new IllegalArgumentException(
                            "Malformed row (too few columns) in " + file.getName() + " at line " + (i + 1)
                    );
                }
                result.add(new Interaction(parts[idxGene], parts[idxType], parts[idxScore], parts[idxDrug]));
            }

            return result;

        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + file.getAbsolutePath(), e);
        }
    }

    /**
     * Reads and parses a TSV file containing basic drug metadata.
     * Expected header columns:
     * <ul>
     *     <li>drug_claim_name</li>
     *     <li>concept_id</li>
     * </ul>
     *
     * @param file the drugs TSV file
     * @return a list of {@link Drug} objects
     * @throws IllegalArgumentException if the file is invalid, malformed, or missing headers
     */
    private List<Drug> readDrugs(File file) {
        validateInputFile(file, "Drugs file");
        try {
            List<String> lines = readLines(file);
            if (lines.isEmpty()) return List.of();

            String[] headers = normalizeHeaders(lines.getFirst());
            int idxName = indexOf(headers, "drug_claim_name", file);
            int idxId   = indexOf(headers, "concept_id", file);

            if (lines.size() < 2) {
                throw new IllegalArgumentException("Drugs file has no data rows after header: " + file.getName());
            }
            List<Drug> result = new ArrayList<>();

            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.isBlank()) continue; // sla lege regels over

                String[] parts = line.split("\t", -1);
                int maxIndex = Math.max(idxName, idxId);

                if (maxIndex >= parts.length) {
                    throw new IllegalArgumentException(
                            "Malformed row (too few columns) in " + file.getName() + " at line " + (i + 1)
                    );
                }
                result.add(new Drug(parts[idxName], parts[idxId]));
            }

            return result;

        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + file.getAbsolutePath(), e);
        }
    }
    /**
     * Loads and parses the <code>drug_combinations.tsv</code> file from the classpath.
     * Expected header columns:
     * <ul>
     *     <li>drugtype_1</li>
     *     <li>drugtype_2</li>
     *     <li>result</li>
     * </ul>
     *
     * @return a list of {@link Combination} records
     * @throws IllegalArgumentException if the resource is missing or malformed
     */
    private List<Combination> readCombinationsFromResource() {
        InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(COMBINATIONS_RESOURCE);
        if (in == null) {
            throw new IllegalArgumentException("Resource not found: " + COMBINATIONS_RESOURCE);
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            List<String> lines = br.lines().toList();
            if (lines.isEmpty()) return List.of();

            String[] headers = normalizeHeaders(lines.getFirst());
            int idxType1  = findHeader(headers, "drugtype_1");
            int idxType2  = findHeader(headers, "drugtype_2");
            int idxResult = findHeader(headers, "result");

            List<Combination> result = new ArrayList<>();
            for (int i = 1; i < lines.size(); i++) {
                String[] parts = lines.get(i).split("\t", -1);
                result.add(new Combination(parts[idxType1], parts[idxType2], parts[idxResult]));
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException("Error reading resource: " + COMBINATIONS_RESOURCE, e);
        }
    }

    private static int findHeader(String[] headers, String name) {
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].equals(name)) return i;
        }
        throw new IllegalArgumentException("Header not found: " + name);
    }
    }


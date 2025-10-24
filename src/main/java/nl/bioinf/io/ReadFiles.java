package nl.bioinf.io;

import nl.bioinf.methods.Combination;
import nl.bioinf.methods.Drug;
import nl.bioinf.methods.Interaction;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ReadFiles {
    private final File interactionsFile;
    private final File drugsFile;
    private final File combinationsFile = new File("data/drug_combinations.tsv");

    public ReadFiles(File interactionsFile, File drugsFile) {
        this.interactionsFile = interactionsFile;
        this.drugsFile = drugsFile;
    }

    public List<Interaction> processInteractions() {
        return readInteractions(interactionsFile);
    }

    public List<Drug> processDrugs() {
        return readDrugs(drugsFile);
    }

    public List<Combination> processCombinations() {
        return readCombinations(combinationsFile);
    }

    /** Basischecks per inputbestand: bestaat, is file, niet leeg, .tsv-extensie. */
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
    private static String[] normalizeHeaders(String headerLine) {
        String[] headers = headerLine.split("\t", -1);
        for (int i = 0; i < headers.length; i++) {
            headers[i] = headers[i].trim();
        }
        return headers;
    }

    /** Vind header-index; gooit duidelijke foutmelding met bestandsnaam. */
    private int indexOf(String[] headers, String name, File file) {
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].equals(name)) return i; // evt. equalsIgnoreCase
        }
        throw new IllegalArgumentException("Header not found: '" + name + "' in file: " + file.getAbsolutePath());
    }

    private List<Interaction> readInteractions(File file) {
        validateInputFile(file, "Interactions file");
        try {
            List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            if (lines.isEmpty()) return List.of();

            String[] headers = normalizeHeaders(lines.getFirst());
            int idxGene  = indexOf(headers, "gene_claim_name",   file);
            int idxType  = indexOf(headers, "interaction_type",  file);
            int idxScore = indexOf(headers, "interaction_score", file);
            int idxDrug  = indexOf(headers, "drug_concept_id",   file);

            int maxIdx = Math.max(Math.max(idxGene, idxType), Math.max(idxScore, idxDrug));

            List<Interaction> result = new ArrayList<>();
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.isBlank()) continue;
                String[] parts = line.split("\t", -1);
                if (parts.length <= maxIdx) {
                    throw new IllegalArgumentException(
                            "Malformed row (too few columns) in " + file.getName() + " at line " + (i + 1)
                    );
                }
                result.add(new Interaction(
                        parts[idxGene],
                        parts[idxType],
                        parts[idxScore],
                        parts[idxDrug]
                ));
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + file.getAbsolutePath(), e);
        }
    }

    private List<Drug> readDrugs(File file) {
        validateInputFile(file, "Drugs file");
        try {
            List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            if (lines.isEmpty()) return List.of();

            String[] headers = normalizeHeaders(lines.getFirst());
            int idxName = indexOf(headers, "drug_claim_name", file);
            int idxId   = indexOf(headers, "concept_id",      file);

            int maxIdx = Math.max(idxName, idxId);

            List<Drug> result = new ArrayList<>();
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.isBlank()) continue;
                String[] parts = line.split("\t", -1);
                if (parts.length <= maxIdx) {
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

    private List<Combination> readCombinations(File file) {
        validateInputFile(file, "Combinations file");
        try {
            List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            if (lines.isEmpty()) return List.of();

            String[] headers = normalizeHeaders(lines.getFirst());
            // Let op: gebruik hier dezelfde header-namen als in je TSV (je noemde eerder 'resultaat')
            int idxType1  = indexOf(headers, "drugtype_1", file);
            int idxType2  = indexOf(headers, "drugtype_2", file);
            int idxResult = indexOf(headers, "result",  file);

            int maxIdx = Math.max(idxResult, Math.max(idxType1, idxType2));

            List<Combination> result = new ArrayList<>();
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.isBlank()) continue;
                String[] parts = line.split("\t", -1);
                if (parts.length <= maxIdx) {
                    throw new IllegalArgumentException(
                            "Malformed row (too few columns) in " + file.getName() + " at line " + (i + 1)
                    );
                }
                result.add(new Combination(parts[idxType1], parts[idxType2], parts[idxResult]));
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + file.getAbsolutePath(), e);
        }
    }
}


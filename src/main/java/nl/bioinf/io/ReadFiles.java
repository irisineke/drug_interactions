package nl.bioinf.io;

import nl.bioinf.methods.Drug;
import nl.bioinf.methods.Interaction;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ReadFiles {

    private final File interactionsFile;
    private final File drugsFile;

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

    private List<Interaction> readInteractions(File file) {
        List<String> lines;
        try {
            lines = Files.readAllLines(file.toPath());
        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + file, e);
        }

        // Alleen jouw validaties (niet-leeg, .tsv, verplichte kolommen)
        String[] headers = validateAndGetHeaders(file, lines,
                "gene_claim_name", "interaction_type", "interaction_score", "drug_concept_id");

        int idxGene = indexOf(headers, "gene_claim_name");
        int idxType = indexOf(headers, "interaction_type");
        int idxScore = indexOf(headers, "interaction_score");
        int idxDrug  = indexOf(headers, "drug_concept_id");

        List<Interaction> result = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank()) continue;
            String[] parts = line.split("\t", -1);
            result.add(new Interaction(
                    parts[idxGene],
                    parts[idxType],
                    parts[idxScore],
                    parts[idxDrug]
            ));
        }
        return result;
    }

    private List<Drug> readDrugs(File file) {
        List<String> lines;
        try {
            lines = Files.readAllLines(file.toPath());
        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + file, e);
        }

        // Alleen jouw validaties (niet-leeg, .tsv, verplichte kolommen)
        String[] headers = validateAndGetHeaders(file, lines,
                "drug_claim_name", "concept_id");

        int idxName = indexOf(headers, "drug_claim_name");
        int idxId   = indexOf(headers, "concept_id");

        List<Drug> result = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank()) continue;
            String[] parts = line.split("\t", -1);
            result.add(new Drug(parts[idxName], parts[idxId]));
        }
        return result;
    }

    /** Doet exact: (1) niet-leeg, (2) .tsv, (3) verplichte kolommen aanwezig. */
    private String[] validateAndGetHeaders(File file, List<String> lines, String... requiredColumns) {
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("Input file " + file.getName() + " is empty!");
        }
        if (!file.getName().toLowerCase().endsWith(".tsv")) {
            throw new IllegalArgumentException("File " + file.getName() + " is not a .tsv file!");
        }
        String[] headers = lines.get(0).split("\t", -1);
        for (String col : requiredColumns) {
            assertColumnExists(headers, col, file);
        }
        return headers;
    }

    private int indexOf(String[] headers, String name) {
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].equals(name)) return i;
        }
        throw new IllegalArgumentException("Header not found: " + name);
    }

    private void assertColumnExists(String[] headers, String columnName, File file) {
        for (String header : headers) {
            if (header.equals(columnName)) return;
        }
        throw new IllegalArgumentException(
                "Required column '" + columnName + "' not found in file: " + file.getName()
        );
    }
}


package nl.bioinf.io;

import nl.bioinf.methods.Combination;
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

    private List<Interaction> readInteractions(File file) {
        List<String> lines;
        try {
            lines = Files.readAllLines(file.toPath());
        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + file, e);
        }

        if (lines.isEmpty()) {
            throw new IllegalArgumentException("Input file " + file.getName() + " is empty!");
        }

        if (!file.getName().toLowerCase().endsWith(".tsv")) {
            throw new IllegalArgumentException("File " + file.getName() + " is not a .tsv file!");
        }

        String[] headers = lines.getFirst().split("\t", -1);

        assertColumnExists(headers, "gene_claim_name", file);
        assertColumnExists(headers, "interaction_type", file);
        assertColumnExists(headers, "interaction_score", file);
        assertColumnExists(headers, "drug_concept_id", file);

        int idxGene  = indexOf(headers, "gene_claim_name");
        int idxType  = indexOf(headers, "interaction_type");
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

        if (lines.isEmpty()) {
            throw new IllegalArgumentException("Input file " + file.getName() + " is empty!");
        }

        if (!file.getName().toLowerCase().endsWith(".tsv")) {
            throw new IllegalArgumentException("File " + file.getName() + " is not a .tsv file!");
        }

        String[] headers = lines.getFirst().split("\t", -1);

        assertColumnExists(headers, "drug_claim_name", file);
        assertColumnExists(headers, "concept_id", file);

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

    private List<Combination> readCombinations(File file) {
        try {
            List<String> lines = Files.readAllLines(file.toPath());
            if (lines.isEmpty()) return List.of();

            String[] headers = lines.get(0).split("\t", -1);
            int idxType1 = indexOf(headers, "drugtype_1");
            int idxType2 = indexOf(headers, "drugtype_2");
            int idxResult = indexOf(headers, "result");

            List<Combination> result = new ArrayList<>();
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.isBlank()) continue;
                String[] parts = line.split("\t", -1);
                result.add(new Combination(parts[idxType1], parts[idxType2], parts[idxResult]));
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + file, e);
        }
    }

    private void assertColumnExists(String[] headers, String columnName, File file) {
        for (String header : headers) {
            if (header.equals(columnName)) return;
        }
        throw new IllegalArgumentException(
                "Required column '" + columnName + "' not found in file: " + file.getName()
        );
    }


    private int indexOf(String[] headers, String name) {
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].equals(name)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Header not found: " + name);
    }
}
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
        try {
            List<String> lines = Files.readAllLines(file.toPath());
            if (lines.isEmpty()) return List.of();

            String[] headers = lines.getFirst().split("\t", -1);
            int idxGene = indexOf(headers, "gene_claim_name", file);
            int idxType = indexOf(headers, "interaction_type", file);
            int idxScore = indexOf(headers, "interaction_score", file);
            int idxDrug = indexOf(headers, "drug_concept_id", file);


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
        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + file, e);
        }
    }

    private List<Drug> readDrugs(File file) {
        try {
            List<String> lines = Files.readAllLines(file.toPath());
            if (lines.isEmpty()) return List.of();

            String[] headers = lines.getFirst().split("\t", -1);
            int idxName = indexOf(headers, "drug_claim_name", file);
            int idxId = indexOf(headers, "concept_id", file);


            List<Drug> result = new ArrayList<>();
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.isBlank()) continue;
                String[] parts = line.split("\t", -1);
                result.add(new Drug(parts[idxName], parts[idxId]));
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + file, e);
        }
    }

    private List<Combination> readCombinations(File file) {
        try {
            List<String> lines = Files.readAllLines(file.toPath());
            if (lines.isEmpty()) return List.of();

            String[] headers = lines.getFirst().split("\t", -1);
            int idxType1 = indexOf(headers, "drugtype_1",file);
            int idxType2 = indexOf(headers, "drugtype_2", file);
            int idxResult = indexOf(headers, "resultaat", file);

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
    private int indexOf(String[] headers, String name, File file) {
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].equals(name)) return i;
        }
        throw new IllegalArgumentException("Header not found: '" + name + "' in file: " + file.getName());
    }
}

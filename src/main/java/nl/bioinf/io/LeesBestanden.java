package nl.bioinf.io;

import nl.bioinf.methods.Drug;
import nl.bioinf.methods.Interaction;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class LeesBestanden {

    private final File interactionsFile;
    private final File drugsFile;

    public LeesBestanden(File interactionsFile, File drugsFile) {
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
        try {
            List<String> lines = Files.readAllLines(file.toPath());
            if (lines.isEmpty()) return List.of();

            String[] headers = lines.get(0).split("\t", -1);
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
        } catch (IOException e) {
            throw new RuntimeException("Fout bij lezen: " + file, e);
        }
    }

    private List<Drug> readDrugs(File file) {
        try {
            List<String> lines = Files.readAllLines(file.toPath());
            if (lines.isEmpty()) return List.of();

            String[] headers = lines.get(0).split("\t", -1);
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
        } catch (IOException e) {
            throw new RuntimeException("Fout bij lezen: " + file, e);
        }
    }

    private int indexOf(String[] headers, String name) {
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].equals(name)) return i;
        }
        throw new IllegalArgumentException("Header niet gevonden: " + name);
    }
}

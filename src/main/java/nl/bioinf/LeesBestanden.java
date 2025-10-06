package nl.bioinf;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class LeesBestanden {
    private final File interactionsFile;
    private final File drugsFile;

    //Haalt de paden binnen van de ArgumentParser
    public LeesBestanden(File interactionsFile, File drugsFile) {
        this.interactionsFile = interactionsFile;
        this.drugsFile = drugsFile;
    }

    public Map<String, List<String>> process() {
        //filterd op benodigde collomen
        List<String> interactions = filterFile(interactionsFile, List.of(
                "gene_claim_name",
                "interaction_type",
                "interaction_score",
                "drug_concept_id"
        ));

        List<String> drugs = filterFile(drugsFile, List.of(
                "drug_claim_name",
                "concept_id"
        ));

        // Maakt een nieuwe Map aan in het geheugen om de resultaten op te slaan
        Map<String, List<String>> result = new HashMap<>();
        // Slaat de interactiegegevens op onder de sleutel "interactions"
        result.put("interactions", interactions);
        // Slaat de geneesmiddelgegevens op onder de sleutel "drugs"
        result.put("drugs", drugs);
        // Geeft de samengestelde Map terug
        return result;
    }


    // Filtert een bestand op opgegeven kolommen en leest alle rijen
    private List<String> filterFile(File file, List<String> keepCols) {
        try {
            // Leest alle regels uit het bestand
            List<String> lines = Files.readAllLines(file.toPath());
            if (lines.isEmpty()) return Collections.emptyList();

            // Leest de headerregel (eerste regel) en splitst deze op tabs
            String headerLine = lines.get(0);
            String[] headers = headerLine.split("\t");

            // Bepaalt welke kolommen behouden moeten blijven
            List<Integer> keepIndexes = new ArrayList<>();
            for (int i = 0; i < headers.length; i++) {
                if (keepCols.contains(headers[i])) keepIndexes.add(i);
            }

            // Maakt een nieuwe lijst om de gefilterde regels op te slaan
            List<String> result = new ArrayList<>();
            result.add(String.join("\t", keepCols)); // headerregel

            // Loopt door alle dataregels in het bestand
            for (int i = 1; i < lines.size(); i++) {
                String[] parts = lines.get(i).split("\t", -1);
                List<String> filtered = new ArrayList<>();
                for (int idx : keepIndexes) filtered.add(parts[idx]);
                result.add(String.join("\t", filtered));
            }

            // Geeft de gefilterde regels terug
            return result;

        } catch (IOException e) {
            // Geeft een foutmelding als het bestand niet gelezen kan worden
            throw new RuntimeException("Fout bij lezen van bestand: " + file, e);
        }
    }
}
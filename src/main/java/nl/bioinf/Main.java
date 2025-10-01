package nl.bioinf;

import java.nio.file.Path;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Path rawInteractions = Path.of("data/raw/interactions.tsv");
        Path rawDrugs = Path.of("data/raw/drugs.tsv");

        Path preparedInteractions = Path.of("data/prepared/interactions.tsv");
        Path preparedDrugs = Path.of("data/prepared/drugs.tsv");

        List<String> keepInteractions = List.of(
                "gene_claim_name",
                "interaction_type",
                "interaction_score",
                "drug_concept_id"
        );
        List<String> keepDrugs = List.of(
                "drug_claim_name",
                "concept_id"
        );

        LeesBestanden preparer = new LeesBestanden(
                rawInteractions,
                rawDrugs,
                preparedInteractions,
                preparedDrugs
        );

        try {
            preparer.ensurePreparedData(keepInteractions, keepDrugs);
            System.out.println("✅ Prepared bestanden OK:");
            System.out.println(" - " + preparedInteractions.toAbsolutePath());
            System.out.println(" - " + preparedDrugs.toAbsolutePath());

            System.out.println("\nVoorbeeld (eerste 2 regels interactions.tsv):");
            LeesBestanden.printFirstNLines(preparedInteractions, 2);

            System.out.println("\nVoorbeeld (eerste 2 regels drugs.tsv):");
            LeesBestanden.printFirstNLines(preparedDrugs, 2);

        } catch (Exception e) {
            System.err.println("❌ FOUT: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}

